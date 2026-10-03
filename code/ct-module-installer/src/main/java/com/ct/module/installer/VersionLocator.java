package com.ct.module.installer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;

final class VersionLocator {
    private VersionLocator() {
    }

    static JsonObject find(JsonObject manifest, String version) throws IOException {
        for (JsonElement element : manifest.getAsJsonArray("versions")) {
            JsonObject candidate = element.getAsJsonObject();
            if (candidate.get("id").getAsString().equals(version)) {
                return candidate;
            }
        }
        throw new IOException("Minecraft " + version + " was not found in the official version manifest.");
    }
}
