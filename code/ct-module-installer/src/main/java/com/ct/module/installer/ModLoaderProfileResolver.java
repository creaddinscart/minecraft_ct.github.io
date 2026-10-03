package com.ct.module.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

final class ModLoaderProfileResolver {
    private ModLoaderProfileResolver() {
    }

    static Installation resolve(Path gameDirectory, String profileId, Installation parent)
            throws IOException {
        JsonObject profile = LoaderProfileReader.read(gameDirectory, profileId);
        String parentId = LoaderProfileReader.parent(profile, profileId);
        if (!profile.has("mainClass") || profile.get("mainClass").getAsString().isBlank()) {
            throw new IOException("The selected mod loader profile has no main class.");
        }

        JsonObject metadata = parent.metadata().deepCopy();
        metadata.addProperty("id", profileId);
        metadata.addProperty("inheritsFrom", parentId);
        metadata.add("mainClass", profile.get("mainClass").deepCopy());
        JsonArray libraries = metadata.getAsJsonArray("libraries");
        JsonArray profileLibraries = profile.getAsJsonArray("libraries");
        LinkedHashSet<String> classpath = new LinkedHashSet<>(List.of(parent.classpath()
                .split(Pattern.quote(System.getProperty("path.separator")))));
        if (profileLibraries != null) {
            for (JsonElement element : profileLibraries) {
                JsonObject library = element.getAsJsonObject();
                libraries.add(library.deepCopy());
                if (!RuleGate.allows(library)) {
                    continue;
                }
                String artifactPath = LibraryPlanner.artifactPath(library);
                if (artifactPath == null) {
                    continue;
                }
                Path artifact = SafePaths.resolve(gameDirectory.resolve("libraries"), artifactPath);
                if (!Files.isRegularFile(artifact)) {
                    throw new IOException("A dependency for " + profileId
                            + " is missing. Install or repair this profile with its mod loader installer.");
                }
                classpath.add(artifact.toString());
            }
        }
        LoaderProfileReader.mergeArguments(metadata, profile);
        return new Installation(parent.gameDirectory(),
                gameDirectory.resolve("versions").resolve(profileId),
                parent.librariesDirectory(), parent.assetsDirectory(), parent.nativesDirectory(),
                parent.clientJar(), parent.runtimeExecutable(), parent.logConfig(),
                String.join(System.getProperty("path.separator"), classpath), parent.assetIndexId(),
                profileId, metadata);
    }
}
