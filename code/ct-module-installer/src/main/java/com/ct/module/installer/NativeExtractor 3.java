package com.ct.module.installer;

import com.ct.module.rules.RuleEvaluator;
import com.ct.module.rules.RuleFeatures;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class NativeExtractor {
    private static final String NATIVES_PLACEHOLDER = "${natives_directory}/";

    private NativeExtractor() {
    }

    static void extract(JsonObject metadata, Path librariesDirectory, Path nativesDirectory)
            throws IOException {
        Files.createDirectories(nativesDirectory);
        for (JsonElement element : metadata.getAsJsonArray("libraries")) {
            JsonObject library = element.getAsJsonObject();
            if (!RuleEvaluator.allows(library, RuleFeatures.none())) {
                continue;
            }
            String classifier = LibraryPlanner.nativeClassifier(library);
            if (classifier == null) {
                continue;
            }
            JsonObject downloads = library.getAsJsonObject("downloads");
            JsonObject classifiers = downloads == null ? null : downloads.getAsJsonObject("classifiers");
            String nativePath = classifiers != null && classifiers.has(classifier)
                    ? classifiers.getAsJsonObject(classifier).get("path").getAsString()
                    : LibraryPlanner.artifactPath(library.get("name").getAsString() + ":" + classifier);
            if (nativePath == null) {
                continue;
            }
            Path archivePath = SafePaths.resolve(librariesDirectory, nativePath);
            if (!Files.isRegularFile(archivePath)) {
                throw new IOException("A native library is missing: " + archivePath.getFileName());
            }
            extractArchive(archivePath, nativesDirectory, exclusions(library));
        }
    }

    private static void extractArchive(Path archivePath, Path nativesDirectory,
            List<String> exclusions) throws IOException {
        try (ZipFile archive = new ZipFile(archivePath.toFile())) {
            var entries = archive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || exclusions.stream()
                        .anyMatch(entry.getName()::startsWith)) {
                    continue;
                }
                Path destination = SafePaths.resolve(nativesDirectory, entry.getName());
                Files.createDirectories(destination.getParent());
                try (InputStream input = archive.getInputStream(entry)) {
                    Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static List<String> exclusions(JsonObject library) {
        List<String> exclusions = new ArrayList<>();
        JsonObject extract = library.getAsJsonObject("extract");
        if (extract != null && extract.has("exclude")) {
            for (JsonElement excludedPath : extract.getAsJsonArray("exclude")) {
                exclusions.add(excludedPath.getAsString());
            }
        }
        return exclusions;
    }

    static void prepareSubdirectories(JsonObject metadata, Path nativesDirectory) throws IOException {
        Files.createDirectories(nativesDirectory);
        JsonObject arguments = metadata.getAsJsonObject("arguments");
        if (arguments == null) {
            return;
        }
        for (String key : List.of("jvm", "default-user-jvm")) {
            if (!arguments.has(key)) {
                continue;
            }
            for (String value : flatten(arguments.getAsJsonArray(key))) {
                createNativesSubdirectory(nativesDirectory, value);
            }
        }
    }

    private static void createNativesSubdirectory(Path nativesDirectory, String argument)
            throws IOException {
        int index = argument.indexOf(NATIVES_PLACEHOLDER);
        if (index < 0) {
            return;
        }
        String remainder = argument.substring(index + NATIVES_PLACEHOLDER.length());
        StringBuilder name = new StringBuilder();
        for (int position = 0; position < remainder.length(); position++) {
            char character = remainder.charAt(position);
            boolean allowed = Character.isLetterOrDigit(character) || character == '-' || character == '_'
                    || character == '.';
            if (!allowed) {
                break;
            }
            name.append(character);
        }
        String directory = name.toString();
        if (!directory.isEmpty() && !directory.equals(".") && !directory.equals("..")) {
            Files.createDirectories(nativesDirectory.resolve(directory));
        }
    }

    static List<String> flatten(JsonArray entries) {
        List<String> values = new ArrayList<>();
        for (JsonElement entry : entries) {
            if (entry.isJsonPrimitive()) {
                values.add(entry.getAsString());
            } else if (entry.isJsonObject()) {
                JsonElement value = entry.getAsJsonObject().get("value");
                if (value != null && value.isJsonArray()) {
                    for (JsonElement element : value.getAsJsonArray()) {
                        values.add(element.getAsString());
                    }
                } else if (value != null && value.isJsonPrimitive()) {
                    values.add(value.getAsString());
                }
            }
        }
        return values;
    }
}
