package com.ct.module.installer;

import com.ct.module.rules.PlatformInfo;
import com.ct.module.rules.RuleEvaluator;
import com.ct.module.rules.RuleFeatures;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class LibraryPlanner {
    private LibraryPlanner() {
    }

    static List<DownloadRequest> collect(JsonObject metadata, Path librariesDirectory)
            throws IOException {
        List<DownloadRequest> downloads = new ArrayList<>();
        for (JsonElement element : metadata.getAsJsonArray("libraries")) {
            JsonObject library = element.getAsJsonObject();
            if (!RuleEvaluator.allows(library, RuleFeatures.none())) {
                continue;
            }
            JsonObject artifacts = library.getAsJsonObject("downloads");
            if (artifacts != null && artifacts.has("artifact")) {
                JsonObject artifact = artifacts.getAsJsonObject("artifact");
                downloads.add(new DownloadRequest(artifact.get("url").getAsString(),
                        SafePaths.resolve(librariesDirectory, artifact.get("path").getAsString()),
                        artifact.has("sha1") ? artifact.get("sha1").getAsString() : null,
                        artifact.has("size") ? artifact.get("size").getAsLong() : -1));
            } else {
                String artifactPath = artifactPath(library);
                if (artifactPath != null) {
                    JsonArray checksums = library.getAsJsonArray("checksums");
                    String checksum = checksums == null || checksums.isEmpty()
                            ? null
                            : checksums.get(0).getAsString();
                    downloads.add(new DownloadRequest(repository(library) + artifactPath,
                            SafePaths.resolve(librariesDirectory, artifactPath), checksum, -1));
                }
            }

            String classifier = nativeClassifier(library);
            if (classifier == null) {
                continue;
            }
            JsonObject classifiers = artifacts == null ? null : artifacts.getAsJsonObject("classifiers");
            if (classifiers != null && classifiers.has(classifier)) {
                JsonObject artifact = classifiers.getAsJsonObject(classifier);
                downloads.add(new DownloadRequest(artifact.get("url").getAsString(),
                        SafePaths.resolve(librariesDirectory, artifact.get("path").getAsString()),
                        artifact.has("sha1") ? artifact.get("sha1").getAsString() : null,
                        artifact.has("size") ? artifact.get("size").getAsLong() : -1, false));
            } else {
                String artifactPath = artifactPath(library.get("name").getAsString() + ":" + classifier);
                if (artifactPath != null) {
                    downloads.add(new DownloadRequest(repository(library) + artifactPath,
                            SafePaths.resolve(librariesDirectory, artifactPath), null, -1, false));
                }
            }
        }
        return downloads;
    }

    static String artifactPath(JsonObject library) {
        JsonObject downloads = library.getAsJsonObject("downloads");
        if (downloads != null && downloads.has("artifact")) {
            return downloads.getAsJsonObject("artifact").get("path").getAsString();
        }
        JsonElement nameValue = library.get("name");
        if (nameValue == null || !nameValue.isJsonPrimitive()) {
            return null;
        }
        return artifactPath(nameValue.getAsString());
    }

    static String artifactPath(String libraryName) {
        String[] coordinate = libraryName.split("@", 2);
        String[] parts = coordinate[0].split(":");
        if (parts.length < 3) {
            return null;
        }
        String classifier = parts.length > 3 ? "-" + parts[3] : "";
        String extension = coordinate.length > 1 ? coordinate[1] : "jar";
        return parts[0].replace('.', '/') + "/" + parts[1] + "/" + parts[2] + "/"
                + parts[1] + "-" + parts[2] + classifier + "." + extension;
    }

    static String nativeClassifier(JsonObject library) {
        JsonObject natives = library.getAsJsonObject("natives");
        String operatingSystem = PlatformInfo.operatingSystem();
        if (natives == null || !natives.has(operatingSystem)) {
            return null;
        }
        String classifier = natives.get(operatingSystem).getAsString();
        return classifier.replace("${arch}", PlatformInfo.is64Bit() ? "64" : "32");
    }

    private static String repository(JsonObject library) {
        String repository = library.has("url")
                ? library.get("url").getAsString()
                : "https://libraries.minecraft.net/";
        return repository.endsWith("/") ? repository : repository + "/";
    }
}
