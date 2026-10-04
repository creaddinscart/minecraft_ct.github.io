package com.ct.module.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

final class LoaderProfileReader {
    private LoaderProfileReader() {
    }

    static JsonObject read(Path gameDirectory, String profileId) throws IOException {
        if (profileId == null || !profileId.matches("[A-Za-z0-9._-]{1,128}")) {
            throw new IOException("Invalid mod loader profile ID.");
        }
        Path versionsDirectory = gameDirectory.resolve("versions").normalize();
        Path profileFile = versionsDirectory.resolve(profileId).resolve(profileId + ".json").normalize();
        if (!profileFile.startsWith(versionsDirectory) || !Files.isRegularFile(profileFile)) {
            throw new IOException("Mod loader profile was not found: " + profileId);
        }
        try {
            return JsonParser.parseString(Files.readString(profileFile)).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new IOException("Mod loader profile metadata is invalid: " + profileId, exception);
        }
    }

    static boolean isSupported(Path gameDirectory, String profileId) {
        try {
            JsonObject profile = read(gameDirectory, profileId);
            if (!profile.has("inheritsFrom") || !profile.has("mainClass")) {
                return false;
            }
            String id = profileId.toLowerCase(Locale.ROOT);
            if (id.startsWith("fabric-loader-") || id.contains("-forge-") || id.contains("-neoforge")) {
                return true;
            }
            JsonArray libraries = profile.getAsJsonArray("libraries");
            if (libraries == null) {
                return false;
            }
            for (JsonElement element : libraries) {
                JsonElement name = element.getAsJsonObject().get("name");
                if (name == null) {
                    continue;
                }
                String coordinate = name.getAsString().toLowerCase(Locale.ROOT);
                if (coordinate.startsWith("net.minecraftforge:forge:")
                        || coordinate.startsWith("net.neoforged:neoforge:")
                        || coordinate.startsWith("net.fabricmc:fabric-loader:")) {
                    return true;
                }
            }
        } catch (IOException | RuntimeException ignored) {
            return false;
        }
        return false;
    }

    static boolean isFabric(JsonObject profile) {
        JsonArray libraries = profile.getAsJsonArray("libraries");
        if (libraries == null) {
            return false;
        }
        for (JsonElement element : libraries) {
            JsonElement name = element.getAsJsonObject().get("name");
            if (name != null && name.isJsonPrimitive()
                    && name.getAsString().startsWith("net.fabricmc:fabric-loader:")) {
                return true;
            }
        }
        return false;
    }

    static String parent(JsonObject profile, String profileId) throws IOException {
        JsonElement parent = profile.get("inheritsFrom");
        if (parent == null || !parent.isJsonPrimitive() || !parent.getAsJsonPrimitive().isString()
                || parent.getAsString().isBlank()) {
            throw new IOException("The selected mod loader profile has no Minecraft parent version.");
        }
        return parent.getAsString();
    }

    static void mergeArguments(JsonObject metadata, JsonObject profile) {
        JsonObject inherited = metadata.has("arguments")
                ? metadata.getAsJsonObject("arguments")
                : new JsonObject();
        JsonObject overrides = profile.getAsJsonObject("arguments");
        if (overrides != null) {
            for (String key : List.of("jvm", "game")) {
                if (!overrides.has(key)) {
                    continue;
                }
                JsonArray merged = inherited.has(key) ? inherited.getAsJsonArray(key) : new JsonArray();
                for (JsonElement element : overrides.getAsJsonArray(key)) {
                    merged.add(element.deepCopy());
                }
                inherited.add(key, merged);
            }
        }
        metadata.add("arguments", inherited);
    }
}
