package com.ct.module.installer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class CatalogParser {
    private CatalogParser() {
    }

    static VersionCatalog parse(JsonObject manifest) throws IOException {
        try {
            String latestRelease = manifest.getAsJsonObject("latest").get("release").getAsString();
            List<String> versions = new ArrayList<>();
            for (JsonElement element : manifest.getAsJsonArray("versions")) {
                JsonObject version = element.getAsJsonObject();
                String id = version.get("id").getAsString();
                if (SafePaths.isSafeVersionId(id)) {
                    versions.add(id);
                }
            }
            if (!versions.contains(latestRelease)) {
                throw new IOException("The latest Minecraft release is missing from the version manifest.");
            }
            return new VersionCatalog(latestRelease, versions);
        } catch (RuntimeException exception) {
            throw new IOException("The official Minecraft version manifest is invalid.", exception);
        }
    }
}
