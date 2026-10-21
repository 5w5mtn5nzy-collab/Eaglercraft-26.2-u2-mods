package com.terraformersmc.modmenu.util.mod.quilt;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.terraformersmc.modmenu.util.UpdateCheckerUtil;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class QuiltMod extends FabricMod {
    protected final Object container;
    protected final Object metadata;

    public QuiltMod(Object fabricObject, Set<String> modpackMods) {
        super(fabricObject, modpackMods);
        this.container = QuiltLoader.getObject(fabricObject.getMetadata().getId()).get();
        this.metadata = container.metadata();
        if ("quilt_loader".equals(metadata.id())) {
            badges.add(Badge.LIBRARY);
        }
    }

    @Override
    public @NotNull List<String> getAuthors() {
        List<String> authors = metadata.contributors()
                .stream()
                .filter(contributor -> contributor.role().equals("Author") || contributor.role().equals("Owner"))
                .map(Object::name)
                .collect(Collectors.toList());
        if (authors.isEmpty()) {
            metadata.contributors()
                    .stream()
                    .findFirst()
                    .ifPresent(modContributor -> authors.add(modContributor.name()));
        }

        if (authors.isEmpty()) {
            if ("minecraft".equals(getId())) {
                return Lists.newArrayList("Mojang Studios");
            } else if ("java".equals(getId())) {
                return Lists.newArrayList(System.getProperty("java.vendor"));
            }
        }

        return authors;
    }

    @Override
    public @NotNull Map<String, Collection<String>> getContributors() {
        Map<String, Collection<String>> contributors = new LinkedHashMap<>();

        for (var contributor : this.metadata.contributors()) {
            contributors.put(contributor.name(), contributor.roles());
        }

        return contributors;
    }

    @Override
    public @NotNull SortedMap<String, Set<String>> getCredits() {
        SortedMap<String, Set<String>> credits = new TreeMap<>();

        var contributors = this.getContributors();

        for (var contributor : contributors.entrySet()) {
            for (var role : contributor.getValue()) {
                credits.computeIfAbsent(role, key -> new LinkedHashSet<>());
                credits.get(role).add(contributor.getKey());
            }
        }

        return credits;
    }


    public @Nullable String getSha512Hash() throws IOException {
        var fabricResult = super.getSha512Hash();
        if (fabricResult == null) {
            UpdateCheckerUtil.LOGGER.debug("Checking {}", getId());
            if (container.getSourceType().equals(Object.BasicSourceType.NORMAL_QUILT) || container.getSourceType()
                    .equals(Object.BasicSourceType.NORMAL_FABRIC)) {
                for (var paths : container.getSourcePaths()) {
                    List<Path> jars = paths.stream()
                            .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                            .toList();

                    if (jars.size() == 1 && jars.get(0).getFileSystem() == FileSystems.getDefault()) {
                        var path = jars.get(0);

                        if (Files.exists(path)) {
                            UpdateCheckerUtil.LOGGER.debug("Found {} hash", getId());
                            return com.google.common.io.Files.asByteSource(path.toFile())
                                    .hash(Hashing.sha512())
                                    .toString();
                        }
                    }
                }
            }
        }
        return fabricResult;
    }
}
