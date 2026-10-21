package com.terraformersmc.modmenu;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.util.EnumToLowerCaseJsonConverter;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.NullScreenFactory;
import com.terraformersmc.modmenu.util.UpdateCheckerUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricDummyParentMod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import net.lax1dude.eaglercraft.v1_8.mods.resources.EaglerModResourcePackSource;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.Screen;
import net.lax1dude.eaglercraft.v1_8.EaglercraftVersion;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ModMenu {
    public static final String MOD_ID = "modmenu";
    public static final String GITHUB_REF = "TerraformersMC/ModMenu";
    public static final String VERSION = "12.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
    public static final Gson GSON;
    public static final Gson GSON_MINIFIED;

    static {
        GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Enum.class, new EnumToLowerCaseJsonConverter()).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        GSON = builder.setPrettyPrinting().create();
        GSON_MINIFIED = builder.create();
    }

    public static final Map<String, Mod> MODS = new ConcurrentHashMap<>();
    public static final Map<String, Mod> ROOT_MODS = new ConcurrentHashMap<>();
    public static final ListMultimap<Mod, Mod> PARENT_MAP = Multimaps.synchronizedListMultimap(LinkedListMultimap.create());

    private static final Map<String, ConfigScreenFactory<?>> configScreenFactories = new ConcurrentHashMap<>();
    private static final List<ModMenuApi> apiImplementations = new ArrayList<>();

    private static int cachedDisplayedModCount = -1;
    private static boolean initialized;
    public static final boolean RUNNING_QUILT = false;
    public static final boolean DEV_ENVIRONMENT = false;
    public static final boolean TEXT_PLACEHOLDER_COMPAT = false;

    public static boolean hasConfigScreen(String modId) {
        return getConfigScreenFactory(modId) != null;
    }

    public static @Nullable Screen getConfigScreen(String modId, Screen parent) {
        ConfigScreenFactory<?> factory = getConfigScreenFactory(modId);
        if (factory != null) {
            return factory.create(parent);
        } else {
            return null;
        }
    }

    private static @Nullable ConfigScreenFactory<?> getConfigScreenFactory(String modId) {
        if (ModMenuConfig.HIDDEN_CONFIGS.getValue().contains(modId)) {
            return null;
        }

        for (ModMenuApi api : apiImplementations) {
            var factoryProviders = api.getProvidedConfigScreenFactories();
            if (!factoryProviders.isEmpty()) {
                factoryProviders.forEach(configScreenFactories::putIfAbsent);
            }
        }

        return configScreenFactories.get(modId);
    }

    public static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            new ModMenu().init();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize the Mod Menu", t);
        }
    }

    public void init() {
        ModMenuConfigManager.initializeConfig();
        Set<String> modpackMods = new HashSet<>();
        Map<String, UpdateChecker> updateCheckers = new HashMap<>();
        Map<String, UpdateChecker> providedUpdateCheckers = new HashMap<>();

        MODS.clear();
        ROOT_MODS.clear();
        PARENT_MAP.clear();
        configScreenFactories.clear();
        apiImplementations.clear();

        try {
            ModMenuApi api = new ModMenuModMenuCompat();
            apiImplementations.add(api);
            providedUpdateCheckers.putAll(api.getProvidedUpdateCheckers());
        } catch (Throwable t) {
            LOGGER.error("Failed to register the built-in ModMenuApi implementation", t);
        }

        for (Mod mod : collectMods()) {
            var updateChecker = updateCheckers.get(mod.getId());
            if (updateChecker == null) {
                updateChecker = providedUpdateCheckers.get(mod.getId());
            }

            MODS.put(mod.getId(), mod);
            mod.setUpdateChecker(updateChecker);
        }

        checkForUpdates();

        Map<String, Mod> dummyParents = new HashMap<>();

        HashSet<String> modParentSet = new HashSet<>();
        for (Mod mod : MODS.values()) {
            String parentId = mod.getParent();
            if (parentId == null) {
                ROOT_MODS.put(mod.getId(), mod);
                continue;
            }

            Mod parent;
            modParentSet.clear();
            while (true) {
                parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
                if (parent == null) {
                    if (mod instanceof FabricMod) {
                        parent = new FabricDummyParentMod((FabricMod) mod, parentId);
                        dummyParents.put(parentId, parent);
                    }
                }

                parentId = parent != null ? parent.getParent() : null;
                if (parentId == null) {
                    break;
                }

                if (modParentSet.contains(parentId)) {
                    LOGGER.warn("Mods contain each other as parents: {}", modParentSet);
                    parent = null;
                    break;
                }

                modParentSet.add(parentId);
            }

            if (parent == null) {
                ROOT_MODS.put(mod.getId(), mod);
                continue;
            }

            PARENT_MAP.put(parent, mod);
        }

        MODS.putAll(dummyParents);
        ModMenuEventHandler.register();
    }

    private static List<Mod> collectMods() {
        List<Mod> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        Set<Mod.Badge> minecraftBadges = EnumSet.of(Mod.Badge.MINECRAFT);
        result.add(new FabricMod("minecraft", "Minecraft", "The base game", SharedConstants.getCurrentVersion().name(), List.of("Mojang Studios"), minecraftBadges));
        seen.add("minecraft");

        Set<Mod.Badge> javaBadges = EnumSet.of(Mod.Badge.LIBRARY);
        result.add(new FabricMod("java", "Java", "Java Runtime Environment", System.getProperty("java.version", "unknown"), List.of(), javaBadges));
        seen.add("java");

        Set<Mod.Badge> eaglerBadges = EnumSet.of(Mod.Badge.CLIENT);
        result.add(new FabricMod("eaglercraft", "Eaglercraft", "A Minecraft 1.8.8 client that runs in the browser", EaglercraftVersion.projectForkVersion, List.of("Eaglercraft contributors"), eaglerBadges));
        seen.add("eaglercraft");

        Set<Mod.Badge> modMenuBadges = EnumSet.of(Mod.Badge.CLIENT);
        result.add(new FabricMod(MOD_ID, "Mod Menu", "Adds a mod list to the game", VERSION, List.of("TerraformersMC"), modMenuBadges));
        seen.add(MOD_ID);

        for (String moduleId : EaglerModResourcePackSource.mountedModuleIds()) {
            if (!seen.add(moduleId)) {
                continue;
            }

            Set<Mod.Badge> badges = EnumSet.of(Mod.Badge.CLIENT);
            result.add(new FabricMod(moduleId, moduleId, "Eaglercraft module", "unknown", List.of(), badges));
        }

        return result;
    }

    public static void clearModCountCache() {
        cachedDisplayedModCount = -1;
    }

    public static void checkForUpdates() {
        UpdateCheckerUtil.checkForUpdates();
    }

    public static boolean areModUpdatesAvailable() {
        if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
            return false;
        }

        for (Mod mod : MODS.values()) {
            if (mod.isHidden()) {
                continue;
            }

            if (ModMenuConfig.SHOW_LIBRARIES.getValue().hideMod(mod, getConfigScreenFactory(mod.getId()) != null)) {
                continue;
            }

            if (mod.hasUpdate() || mod.getChildHasUpdate()) {
                return true;
            }
        }

        return false;
    }

    public static String getDisplayedModCount() {
        if (cachedDisplayedModCount == -1) {
            boolean includeChildren = ModMenuConfig.COUNT_CHILDREN.getValue();
            boolean includeLibraries = ModMenuConfig.COUNT_LIBRARIES.getValue();
            boolean includeHidden = ModMenuConfig.COUNT_HIDDEN_MODS.getValue();

            cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter(mod -> {
                boolean isChild = mod.getParent() != null;
                if (!includeChildren && isChild) {
                    return false;
                }

                boolean isLibrary = mod.getBadges().contains(Mod.Badge.LIBRARY);
                if (!includeLibraries && isLibrary) {
                    return false;
                }

                return includeHidden || !mod.isHidden();
            }).count());
        }

        return NumberFormat.getInstance().format(cachedDisplayedModCount);
    }

    public static Component createModsButtonText(boolean title) {
        var titleStyle = ModMenuConfig.MODS_BUTTON_STYLE.getValue();
        var gameMenuStyle = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
        var isIcon = title ?
                titleStyle == ModMenuConfig.TitleMenuButtonStyle.ICON :
                gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.ICON;
        var isShort = title ?
                titleStyle == ModMenuConfig.TitleMenuButtonStyle.SHRINK :
                gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.ICON;
        MutableComponent modsText = ModMenuScreenTexts.TITLE.copy();
        if (ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnModsButton() && !isIcon) {
            String count = ModMenu.getDisplayedModCount();
            if (isShort) {
                modsText.append(Component.literal(" ")).append(Component.translatable("modmenu.loaded.short", count));
            } else {
                String specificKey = "modmenu.loaded." + count;
                String key = Language.getInstance().has(specificKey) ? specificKey : "modmenu.loaded";
                if (ModMenuConfig.EASTER_EGGS.getValue() && Language.getInstance().has(specificKey + ".secret")) {
                    key = specificKey + ".secret";
                }
                modsText.append(Component.literal(" ")).append(Component.translatable(key, count));
            }
        }
        return modsText;
    }
}
