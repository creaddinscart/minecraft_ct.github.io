package com.ct.main.moduleinstall;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public record ModuleInfo(String id, String name, String version, List<String> requires) {
    public ModuleInfo {
        requires = List.copyOf(requires);
    }

    public String fileName() {
        String suffix = version == null || version.isBlank() ? "" : "-" + version;
        return "ct-module-" + id + suffix + ".jar";
    }

    public String label() {
        if (version == null || version.isBlank()) {
            return name + " (" + id + ")";
        }
        return name + " (" + id + " " + version + ")";
    }

    public static ModuleInfo read(Path jar) throws IOException {
        try (JarFile file = new JarFile(jar.toFile())) {
            JarEntry entry = file.getJarEntry(ModuleInstaller.MANIFEST_FILE);
            if (entry == null) {
                throw new IOException("Not a CT module jar: " + jar.getFileName()
                        + " has no " + ModuleInstaller.MANIFEST_FILE + ".");
            }
            try (InputStream stream = file.getInputStream(entry);
                    Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return parse(Json.parse(reader), jar);
            }
        }
    }

    public static ModuleInfo readQuietly(Path jar) {
        try {
            return read(jar);
        } catch (Exception exception) {
            return null;
        }
    }

    private static ModuleInfo parse(Object parsed, Path jar) throws IOException {
        if (!(parsed instanceof Map<?, ?> manifest)) {
            throw new IOException("Module manifest is not a JSON object: " + jar.getFileName());
        }
        String id = string(manifest.get("id"));
        if (id == null || id.isBlank()) {
            throw new IOException("Module manifest has no id: " + jar.getFileName());
        }
        String name = string(manifest.get("name"));
        if (name == null || name.isBlank()) {
            name = id;
        }
        String version = string(manifest.get("version"));
        if (version == null || version.isBlank()) {
            version = versionFromFileName(jar.getFileName().toString(), id);
        }
        List<String> requires = new ArrayList<>();
        if (manifest.get("requires") instanceof List<?> list) {
            for (Object element : list) {
                String required = string(element);
                if (required != null && !required.isBlank()) {
                    requires.add(required);
                }
            }
        }
        return new ModuleInfo(id, name, version, requires);
    }

    static String versionFromFileName(String fileName, String id) {
        String prefix = "ct-module-" + id + "-";
        if (fileName.startsWith(prefix) && fileName.endsWith(".jar")) {
            return fileName.substring(prefix.length(), fileName.length() - ".jar".length());
        }
        return "";
    }

    private static String string(Object value) {
        return value instanceof String text ? text : null;
    }

    public static List<ModuleInfo> installed(Path modulesDirectory) {
        List<ModuleInfo> modules = new ArrayList<>();
        if (!Files.isDirectory(modulesDirectory)) {
            return modules;
        }
        try (var files = Files.list(modulesDirectory)) {
            for (Path jar : files.filter(ModuleInstaller::isModuleJar)
                    .sorted(java.util.Comparator.comparing(path -> path.getFileName().toString()))
                    .toList()) {
                ModuleInfo info = readQuietly(jar);
                if (info != null) {
                    modules.add(info);
                }
            }
        } catch (IOException ignored) {
            return modules;
        }
        return modules;
    }
}
