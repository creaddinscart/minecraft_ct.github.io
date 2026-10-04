package com.ct.main.core;

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
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public record ModuleManifest(String id, String name, String version, String description,
        String entrypoint, List<String> requires) {
    public static final String MANIFEST_FILE = "ct.module.json";

    public ModuleManifest {
        requires = List.copyOf(requires);
    }

    public static ModuleManifest read(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry entry = jar.getJarEntry(MANIFEST_FILE);
            if (entry == null) {
                throw new IOException("Module jar is missing " + MANIFEST_FILE + ": "
                        + jarPath.getFileName());
            }
            try (Reader reader = new InputStreamReader(jar.getInputStream(entry),
                    StandardCharsets.UTF_8)) {
                return parse(JsonParser.parseReader(reader).getAsJsonObject(), jarPath);
            }
        } catch (IOException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new IOException("Module manifest is invalid: " + jarPath.getFileName(), exception);
        }
    }

    private static ModuleManifest parse(JsonObject manifest, Path jarPath) throws IOException {
        String id = requireString(manifest, "id", jarPath);
        if (!id.matches("[a-z][a-z0-9_-]{0,63}")) {
            throw new IOException("Invalid module id '" + id + "' in " + jarPath.getFileName() + ".");
        }
        String entrypoint = requireString(manifest, "entrypoint", jarPath);
        List<String> requires = new ArrayList<>();
        JsonElement requiresElement = manifest.get("requires");
        if (requiresElement != null) {
            if (!requiresElement.isJsonArray()) {
                throw new IOException("Module 'requires' must be an array: " + jarPath.getFileName());
            }
            for (JsonElement element : requiresElement.getAsJsonArray()) {
                if (!element.isJsonPrimitive()) {
                    throw new IOException("Module 'requires' contains a non-string entry in "
                            + jarPath.getFileName() + ".");
                }
                requires.add(element.getAsString());
            }
        }
        String name = manifest.has("name") && manifest.get("name").isJsonPrimitive()
                ? manifest.get("name").getAsString()
                : id;
        String description = manifest.has("description") && manifest.get("description").isJsonPrimitive()
                ? manifest.get("description").getAsString()
                : "";
        String version = manifest.has("version") && manifest.get("version").isJsonPrimitive()
                ? manifest.get("version").getAsString()
                : "";
        return new ModuleManifest(id, name, version, description, entrypoint, requires);
    }

    private static String requireString(JsonObject manifest, String field, Path jarPath)
            throws IOException {
        JsonElement value = manifest.get(field);
        if (value == null || !value.isJsonPrimitive() || value.getAsString().isBlank()) {
            throw new IOException("Module manifest requires a non-empty '" + field + "' in "
                    + jarPath.getFileName() + ".");
        }
        return value.getAsString();
    }

    public static boolean isModuleJar(Path file) {
        return Files.isRegularFile(file) && file.getFileName().toString().endsWith(".jar")
                && !file.getFileName().toString().startsWith(".");
    }
}
