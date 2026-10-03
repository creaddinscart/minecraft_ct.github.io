package com.ct.module.rules;

import com.google.gson.JsonObject;
import java.util.Map;
import java.util.regex.Pattern;

public final class RuleEvaluator {
    private RuleEvaluator() {
    }

    public static boolean allows(JsonObject element, RuleFeatures features) {
        if (element == null || !element.has("rules")) {
            return true;
        }
        boolean allowed = false;
        for (var entry : element.getAsJsonArray("rules")) {
            JsonObject rule = entry.getAsJsonObject();
            if (matches(rule, features)) {
                allowed = "allow".equals(rule.get("action").getAsString());
            }
        }
        return allowed;
    }

    private static boolean matches(JsonObject rule, RuleFeatures features) {
        if (rule.has("os") && !matchesOperatingSystem(rule.getAsJsonObject("os"))) {
            return false;
        }
        if (rule.has("features")) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry
                    : rule.getAsJsonObject("features").entrySet()) {
                if (features.enabled(entry.getKey()) != entry.getValue().getAsBoolean()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean matchesOperatingSystem(JsonObject os) {
        if (os.has("name") && !os.get("name").getAsString().equals(PlatformInfo.operatingSystem())) {
            return false;
        }
        if (os.has("arch") && !Pattern.matches(os.get("arch").getAsString(), PlatformInfo.architecture())) {
            return false;
        }
        String version = System.getProperty("os.version", "");
        if (os.has("version") && !Pattern.compile(os.get("version").getAsString())
                .matcher(version).find()) {
            return false;
        }
        if (os.has("versionRange")) {
            JsonObject range = os.getAsJsonObject("versionRange");
            if (range.has("min") && VersionComparer.compare(version, range.get("min").getAsString()) < 0) {
                return false;
            }
            if (range.has("max") && VersionComparer.compare(version, range.get("max").getAsString()) >= 0) {
                return false;
            }
        }
        return true;
    }
}
