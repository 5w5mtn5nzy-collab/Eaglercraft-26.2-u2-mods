package com.terraformersmc.modmenu.util.mod.fabric;

import java.util.*;

public class CustomValueUtil {
    public static Optional<Boolean> getBoolean(String key, Object metadata) {
        if (metadata.containsCustomValue(key)) {
            return Optional.of(metadata.getCustomValue(key).getAsBoolean());
        }
        return Optional.empty();
    }

    public static Optional<String> getString(String key, Object metadata) {
        if (metadata.containsCustomValue(key)) {
            return Optional.of(metadata.getCustomValue(key).getAsString());
        }
        return Optional.empty();
    }

    public static Optional<Set<String>> getStringSet(String key, Object metadata) {
        if (metadata.containsCustomValue(key)) {
            return getStringSet(key, metadata.getCustomValue(key).getAsObject());
        }
        return Optional.empty();
    }

    public static Optional<Boolean> getBoolean(String key, Object object) {
        if (object.containsKey(key)) {
            return Optional.of(object.get(key).getAsBoolean());
        }
        return Optional.empty();
    }

    public static Optional<String> getString(String key, Object object) {
        if (object.containsKey(key)) {
            return Optional.of(object.get(key).getAsString());
        }
        return Optional.empty();
    }

    public static Optional<String[]> getStringArray(String key, Object object) {
        if (object.containsKey(key)) {
            Object cvArray = object.get(key).getAsArray();
            String[] strings = new String[cvArray.size()];
            for (int i = 0; i < cvArray.size(); i++) {
                strings[i] = cvArray.get(i).getAsString();
            }
            return Optional.of(strings);
        }
        return Optional.empty();
    }

    public static Optional<Set<String>> getStringSet(String key, Object object) {
        if (object.containsKey(key)) {
            Set<String> strings = new HashSet<>();
            for (CustomValue value : object.get(key).getAsArray()) {
                strings.add(value.getAsString());
            }
            return Optional.of(strings);
        }
        return Optional.empty();
    }

    public static Optional<Map<String, String>> getStringMap(String key, Object object) {
        if (object.containsKey(key)) {
            Map<String, String> strings = new HashMap<>();
            for (Map.Entry<String, CustomValue> entry : object.get(key).getAsObject()) {
                strings.put(entry.getKey(), entry.getValue().getAsString());
            }
            return Optional.of(strings);
        }
        return Optional.empty();
    }
}
