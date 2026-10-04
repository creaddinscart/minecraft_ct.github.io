package com.ct.module.installer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Stream;

final class FabricProfileDetector {
    private FabricProfileDetector() {
    }

    static boolean hasFabricMods(Path gameDirectory) throws IOException {
        Path modsDirectory = gameDirectory.resolve("mods");
        if (!Files.isDirectory(modsDirectory)) {
            return false;
        }
        try (Stream<Path> files = Files.list(modsDirectory)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar")).toList()) {
                try (JarFile jar = new JarFile(file.toFile())) {
                    if (jar.getJarEntry("fabric.mod.json") != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static List<String> installedFabricProfiles(Path gameDirectory, List<String> loaderProfiles,
            String minecraftVersion) throws IOException {
        List<String> profiles = new ArrayList<>();
        for (String profileId : loaderProfiles) {
            try {
                JsonObject profile = LoaderProfileReader.read(gameDirectory, profileId);
                JsonElement parent = profile.get("inheritsFrom");
                if (parent != null && parent.isJsonPrimitive()
                        && minecraftVersion.equals(parent.getAsString())
                        && LoaderProfileReader.isFabric(profile)) {
                    profiles.add(profileId);
                }
            } catch (IOException ignored) {
                continue;
            }
        }
        return List.copyOf(profiles);
    }
}
