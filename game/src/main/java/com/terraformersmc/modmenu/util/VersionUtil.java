package com.terraformersmc.modmenu.util;

import java.util.List;

public final class StringUtil {
    private static final List<String> PREFIXES = List.of("version", "ver", "v");

    private StringUtil() {
    }

    public static String stripPrefix(String version) {
        version = version.trim();

        for (String prefix : PREFIXES) {
            if (version.startsWith(prefix)) {
                return version.substring(prefix.length());
            }
        }

        return version;
    }

    public static String getPrefixedString(String version) {
        return "v" + stripPrefix(version);
    }

    public static String removeBuildMetadata(String version) {
        return version.split("\\+")[0];
    }
}
