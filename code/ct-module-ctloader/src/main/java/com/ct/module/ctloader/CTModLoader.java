package com.ct.module.ctloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class CTModLoader {
    private static final String METADATA_FILE = "ct.mod.json";

    private CTModLoader() {
    }

    public static URLClassLoader load(Path gameDirectory, Consumer<String> logger) throws IOException {
        Path normalizedGameDirectory = gameDirectory.toAbsolutePath().normalize();
        Path modsDirectory = normalizedGameDirectory.resolve("mods");
        Files.createDirectories(modsDirectory);

        List<Path> jars;
        try (var files = Files.list(modsDirectory)) {
            jars = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        Map<String, ModJar> mods = new HashMap<>();
        for (Path jar : jars) {
            CTModMetadata metadata = readMetadata(jar);
            if (mods.putIfAbsent(metadata.id(), new ModJar(jar, metadata)) != null) {
                throw new IOException("Duplicate CT mod id '" + metadata.id() + "'.");
            }
        }

        List<ModJar> loadOrder = resolveLoadOrder(mods);
        URL[] modUrls = loadOrder.stream().map(mod -> toUrl(mod.path())).toArray(URL[]::new);
        URLClassLoader modClassLoader = new URLClassLoader(modUrls, CTMod.class.getClassLoader());
        try {
            logger.accept("[CT] Found " + loadOrder.size() + " CT mod(s) in " + modsDirectory + ".");
            for (ModJar mod : loadOrder) {
                for (String entrypoint : mod.metadata().entrypoints()) {
                    initialize(modClassLoader, mod.metadata(), entrypoint, normalizedGameDirectory,
                            modsDirectory, logger);
                }
            }
            return modClassLoader;
        } catch (IOException exception) {
            modClassLoader.close();
            throw exception;
        }
    }

    private static CTModMetadata readMetadata(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry metadataEntry = jar.getJarEntry(METADATA_FILE);
            if (metadataEntry == null) {
                throw new IOException("CT mod jar is missing " + METADATA_FILE + ": " + jarPath);
            }
            try (Reader reader = new InputStreamReader(jar.getInputStream(metadataEntry),
                    StandardCharsets.UTF_8)) {
                JsonObject metadata = JsonParser.parseReader(reader).getAsJsonObject();
                if (metadata.get("schemaVersion").getAsInt() != 1) {
                    throw new IOException("Unsupported CT mod metadata schema in " + jarPath + ".");
                }
                String id = requiredString(metadata, "id", jarPath);
                if (!id.matches("[a-z][a-z0-9_-]{0,63}")) {
                    throw new IOException("Invalid CT mod id '" + id + "' in " + jarPath + ".");
                }
                return new CTModMetadata(id, requiredString(metadata, "name", jarPath),
                        requiredString(metadata, "version", jarPath),
                        stringArray(metadata, "entrypoints", jarPath),
                        stringArray(metadata, "depends", jarPath));
            }
        } catch (JsonParseException | IllegalStateException | NullPointerException exception) {
            throw new IOException("Invalid CT mod metadata in " + jarPath + ".", exception);
        }
    }

    private static String requiredString(JsonObject metadata, String key, Path jarPath)
            throws IOException {
        JsonElement value = metadata.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                || value.getAsString().isBlank()) {
            throw new IOException("CT mod metadata requires a non-empty '" + key + "' in " + jarPath + ".");
        }
        return value.getAsString();
    }

    private static List<String> stringArray(JsonObject metadata, String key, Path jarPath)
            throws IOException {
        JsonElement value = metadata.get(key);
        if (value == null && key.equals("depends")) {
            return List.of();
        }
        if (value == null || !value.isJsonArray()) {
            throw new IOException("CT mod metadata requires '" + key + "' to be an array in " + jarPath + ".");
        }
        JsonArray array = value.getAsJsonArray();
        List<String> result = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()
                    || element.getAsString().isBlank()) {
                throw new IOException("CT mod metadata contains an invalid '" + key + "' entry in "
                        + jarPath + ".");
            }
            result.add(element.getAsString());
        }
        if (key.equals("entrypoints") && result.isEmpty()) {
            throw new IOException("CT mod metadata must declare at least one entrypoint in " + jarPath + ".");
        }
        return result;
    }

    private static List<ModJar> resolveLoadOrder(Map<String, ModJar> mods) throws IOException {
        List<String> ids = mods.keySet().stream().sorted().toList();
        Map<String, Integer> states = new HashMap<>();
        List<ModJar> result = new ArrayList<>();
        for (String id : ids) {
            visit(id, mods, states, result, new HashSet<>());
        }
        return result;
    }

    private static void visit(String id, Map<String, ModJar> mods, Map<String, Integer> states,
            List<ModJar> result, Set<String> path) throws IOException {
        int state = states.getOrDefault(id, 0);
        if (state == 2) {
            return;
        }
        if (state == 1) {
            path.add(id);
            throw new IOException("CT mod dependency cycle: " + String.join(" -> ", path) + ".");
        }
        states.put(id, 1);
        path.add(id);
        ModJar mod = mods.get(id);
        for (String dependency : mod.metadata().depends().stream().sorted().toList()) {
            if (!mods.containsKey(dependency)) {
                throw new IOException("CT mod '" + id + "' requires missing mod '" + dependency + "'.");
            }
            visit(dependency, mods, states, result, path);
        }
        path.remove(id);
        states.put(id, 2);
        result.add(mod);
    }

    private static void initialize(URLClassLoader classLoader, CTModMetadata metadata,
            String entrypoint, Path gameDirectory, Path modsDirectory, Consumer<String> logger)
            throws IOException {
        try {
            Class<?> entrypointClass = Class.forName(entrypoint, true, classLoader);
            if (!CTMod.class.isAssignableFrom(entrypointClass)) {
                throw new IOException("CT mod entrypoint must implement CTMod: " + entrypoint);
            }
            CTMod mod = (CTMod) entrypointClass.getConstructor().newInstance();
            mod.onInitialize(new CTModContext(metadata, gameDirectory, modsDirectory, logger));
            logger.accept("[CT] Loaded " + metadata.name() + " " + metadata.version() + ".");
        } catch (Exception | LinkageError exception) {
            throw new IOException("Could not initialize CT mod '" + metadata.id()
                    + "' at entrypoint '" + entrypoint + "'.", exception);
        }
    }

    private static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid CT mod path: " + path, exception);
        }
    }

    private record ModJar(Path path, CTModMetadata metadata) {
    }
}
