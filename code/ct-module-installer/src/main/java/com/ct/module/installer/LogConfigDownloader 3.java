package com.ct.module.installer;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

final class LogConfigDownloader {
    private LogConfigDownloader() {
    }

    static Path download(Path gameDirectory, JsonObject metadata, Consumer<String> status)
            throws IOException, InterruptedException {
        if (!metadata.has("logging")) {
            return null;
        }
        JsonObject file = metadata.getAsJsonObject("logging").getAsJsonObject("client")
                .getAsJsonObject("file");
        Path destination = gameDirectory.resolve("assets").resolve("log_configs")
                .resolve(file.get("id").getAsString());
        status.accept("Downloading the official log configuration...");
        DownloadExecutor.downloadVerified(file.get("url").getAsString(), destination,
                file.get("sha1").getAsString(), file.get("size").getAsLong());
        return destination;
    }

    static Path locateExisting(Path gameDirectory, JsonObject metadata) {
        if (!metadata.has("logging")) {
            return null;
        }
        Path candidate = gameDirectory.resolve("assets").resolve("log_configs").resolve(
                metadata.getAsJsonObject("logging").getAsJsonObject("client")
                        .getAsJsonObject("file").get("id").getAsString());
        return Files.isRegularFile(candidate) ? candidate : null;
    }
}
