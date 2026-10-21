package com.terraformersmc.modmenu.util.mod.fabric;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Browser-target replacement for Fabric-backed mod metadata.
 *
 * The web client uses synthetic entries assembled by ModMenu.collectMods();
 * it does not have a running Fabric Loader instance to query.
 */
public class FabricMod implements Mod {
    private final String id;
    private final String name;
    private final String description;
    private final String version;
    private final List<String> authors;
    private final Set<Badge> badges;
    private @Nullable UpdateChecker updateChecker;
    private @Nullable UpdateInfo updateInfo;
    private boolean childHasUpdate;

    public FabricMod(String id, String name, String description, String version,
                     List<String> authors, Set<Badge> badges) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.authors = List.copyOf(authors);
        this.badges = new HashSet<>(badges);
    }

    /**
     * Retains the desktop constructor descriptor for shared Mod Menu bytecode.
     * Fabric-backed metadata is not loaded by this web implementation.
     */
    public FabricMod(ModContainer modContainer, Set<String> modpackMods) {
        this("unknown", "Unknown mod", "", "unknown", List.of(), Set.of(Badge.CLIENT));
    }

    @Nullable
    public ModContainer getContainer() {
        return null;
    }

    @Override public @NotNull String getId() { return id; }
    @Override public @NotNull String getName() { return name; }
    @Override public @NotNull DynamicTexture getIcon(FabricIconHandler iconHandler, int i) { return null; }
    @Override public @NotNull String getDescription() { return description; }
    @Override public @NotNull String getVersion() { return version; }
    @Override public @NotNull String getPrefixedVersion() { return version; }
    @Override public @NotNull List<String> getAuthors() { return authors; }
    @Override public ContactInformation getContact(String author) { return null; }
    @Override public @NotNull Map<String, Collection<String>> getContributors() { return Map.of(); }
    @Override public @NotNull SortedMap<String, Set<String>> getCredits() { return new TreeMap<>(); }
    @Override public @NotNull Set<Badge> getBadges() { return badges; }
    @Override public @Nullable String getWebsite() { return null; }
    @Override public @Nullable String getIssueTracker() { return null; }
    @Override public @Nullable String getSource() { return null; }
    @Override public @Nullable String getParent() { return null; }
    @Override public @NotNull Set<String> getLicense() { return Set.of(); }
    @Override public @NotNull Map<String, String> getLinks() { return Map.of(); }
    @Override public boolean isReal() { return false; }
    @Override public boolean allowsUpdateChecks() { return false; }
    @Override public @Nullable UpdateChecker getUpdateChecker() { return updateChecker; }
    @Override public void setUpdateChecker(@Nullable UpdateChecker updateChecker) { this.updateChecker = updateChecker; }
    @Override public @Nullable UpdateInfo getUpdateInfo() { return updateInfo; }
    @Override public void setUpdateInfo(@Nullable UpdateInfo updateInfo) { this.updateInfo = updateInfo; }
    @Override public boolean getChildHasUpdate() { return childHasUpdate; }
    @Override public void setChildHasUpdate() { childHasUpdate = true; }
    @Override public boolean isHidden() { return ModMenuConfig.HIDDEN_MODS.getValue().contains(id); }
}
