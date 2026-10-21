package com.terraformersmc.modmenu.util.mod.fabric;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Browser-safe placeholder for Fabric's synthetic parent entries. */
public final class FabricDummyParentMod implements Mod {
    private final String id;
    private boolean childHasUpdate;

    public FabricDummyParentMod(FabricMod host, String id) {
        this.id = id;
    }

    @Override public @NotNull String getId() { return id; }
    @Override public @NotNull String getName() { return id.equals("fabric-api") ? "Fabric API" : id; }
    @Override public @NotNull DynamicTexture getIcon(FabricIconHandler iconHandler, int i) { return null; }
    @Override public @NotNull String getDescription() { return ""; }
    @Override public @NotNull String getVersion() { return ""; }
    @Override public @NotNull String getPrefixedVersion() { return ""; }
    @Override public @NotNull List<String> getAuthors() { return List.of(); }
    @Override public ContactInformation getContact(String author) { return null; }
    @Override public @NotNull Map<String, Collection<String>> getContributors() { return Map.of(); }
    @Override public @NotNull SortedMap<String, Set<String>> getCredits() { return new TreeMap<>(); }
    @Override public @NotNull Set<Badge> getBadges() { return id.equals("fabric-api") ? Set.of(Badge.LIBRARY) : Set.of(); }
    @Override public @Nullable String getWebsite() { return null; }
    @Override public @Nullable String getIssueTracker() { return null; }
    @Override public @Nullable String getSource() { return null; }
    @Override public @Nullable String getParent() { return null; }
    @Override public @NotNull Set<String> getLicense() { return Set.of(); }
    @Override public @NotNull Map<String, String> getLinks() { return Map.of(); }
    @Override public boolean isReal() { return false; }
    @Override public boolean allowsUpdateChecks() { return false; }
    @Override public @Nullable com.terraformersmc.modmenu.api.UpdateChecker getUpdateChecker() { return null; }
    @Override public void setUpdateChecker(@Nullable com.terraformersmc.modmenu.api.UpdateChecker updateChecker) { }
    @Override public @Nullable com.terraformersmc.modmenu.api.UpdateInfo getUpdateInfo() { return null; }
    @Override public void setUpdateInfo(@Nullable com.terraformersmc.modmenu.api.UpdateInfo updateInfo) { }
    @Override public boolean getChildHasUpdate() { return childHasUpdate; }
    @Override public void setChildHasUpdate() { childHasUpdate = true; }
    @Override public boolean isHidden() { return ModMenuConfig.HIDDEN_MODS.getValue().contains(id); }
}
