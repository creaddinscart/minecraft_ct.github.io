package com.ct.module.modman;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class ModScanner {
    private ModScanner() {
    }

    public static List<ModEntry> scan(Path gameDirectory) throws IOException {
        Path modsDirectory = gameDirectory.toAbsolutePath().normalize().resolve("mods");
        List<ModEntry> entries = new ArrayList<>();
        if (!Files.isDirectory(modsDirectory)) {
            return entries;
        }
        try (Stream<Path> files = Files.list(modsDirectory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .filter(path -> !path.getFileName().toString().startsWith("."))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> entries.add(read(path)));
        }
        return entries;
    }

    public static Path modsDirectory(Path gameDirectory) {
        return gameDirectory.toAbsolutePath().normalize().resolve("mods");
    }

    private static ModEntry read(Path jar) {
        long size = size(jar);
        String fileName = jar.getFileName().toString();
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            JsonObject metadata = readJson(jarFile, "ct.mod.json");
            if (metadata != null) {
                return new ModEntry(jar, fileName, ModEntry.Kind.CT,
                        text(metadata, "id"), text(metadata, "name"), text(metadata, "version"),
                        size, null);
            }
            metadata = readJson(jarFile, "fabric.mod.json");
            if (metadata != null) {
                return new ModEntry(jar, fileName, ModEntry.Kind.FABRIC,
                        text(metadata, "id"), text(metadata, "name"), text(metadata, "version"),
                        size, null);
            }
            return new ModEntry(jar, fileName, ModEntry.Kind.UNKNOWN, null, null, null, size,
                    "The jar has no ct.mod.json or fabric.mod.json. It is not loaded by CT or Fabric.");
        } catch (IOException | RuntimeException exception) {
            return new ModEntry(jar, fileName, ModEntry.Kind.BROKEN, null, null, null, size,
                    exception.getMessage());
        }
    }

    private static JsonObject readJson(JarFile jarFile, String entryName) throws IOException {
        JarEntry entry = jarFile.getJarEntry(entryName);
        if (entry == null) {
            return null;
        }
        try (Reader reader = new InputStreamReader(jarFile.getInputStream(entry),
                StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
        }
    }

    private static String text(JsonObject metadata, String field) {
        JsonElement value = metadata.get(field);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }

    private static long size(Path jar) {
        try {
            return Files.size(jar);
        } catch (IOException exception) {
            return 0;
        }
    }
}
