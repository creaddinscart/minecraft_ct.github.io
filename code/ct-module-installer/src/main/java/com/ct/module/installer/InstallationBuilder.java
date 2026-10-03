package com.ct.module.installer;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class InstallationBuilder {
    private InstallationBuilder() {
    }

    static Installation build(Path gameDirectory, JsonObject metadata, Path versionDirectory,
            Path librariesDirectory, Path assetsDirectory, Path nativesDirectory, Path clientJar,
            Path runtimeExecutable, Path logConfig) throws IOException {
        List<String> entries = new ArrayList<>();
        for (DownloadRequest download : LibraryPlanner.collect(metadata, librariesDirectory)) {
            if (download.classpathEntry() && Files.isRegularFile(download.destination())) {
                entries.add(download.destination().toString());
            }
        }
        entries.add(clientJar.toString());
        String assetsId = metadata.has("assets") ? metadata.get("assets").getAsString() : "legacy";
        return new Installation(gameDirectory, versionDirectory, librariesDirectory, assetsDirectory,
                nativesDirectory, clientJar, runtimeExecutable, logConfig,
                String.join(System.getProperty("path.separator"), entries), assetsId,
                metadata.get("id").getAsString(), metadata);
    }
}
