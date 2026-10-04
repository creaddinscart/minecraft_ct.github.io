package com.ct.module.installer;

import com.ct.module.rules.PlatformInfo;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class RuntimeInstaller {
    static final String RUNTIME_MARKER = ".ct-runtime-complete";

    private RuntimeInstaller() {
    }

    static Path install(Path gameDirectory, JsonObject metadata, Consumer<String> status)
            throws IOException, InterruptedException {
        JsonObject javaVersion = javaVersion(metadata);
        String component = javaVersion.get("component").getAsString();
        int majorVersion = javaVersion.get("majorVersion").getAsInt();
        Path installed = locate(gameDirectory, component);
        if (installed != null && Files.isRegularFile(installed.getParent().resolve(RUNTIME_MARKER))) {
            status.accept("The official Java " + majorVersion + " runtime is already installed.");
            return installed;
        }

        String platform = platform();
        JsonObject runtimeManifest = HttpApi.fetchJson(OfficialEndpoints.JAVA_RUNTIME_MANIFEST);
        if (!runtimeManifest.has(platform) || !runtimeManifest.getAsJsonObject(platform).has(component)) {
            throw new IOException("The official runtime manifest has no " + component
                    + " build for the " + platform + " platform.");
        }

        JsonObject runtime = runtimeManifest.getAsJsonObject(platform).getAsJsonArray(component)
                .get(0).getAsJsonObject();
        JsonObject manifestReference = runtime.getAsJsonObject("manifest");
        JsonObject files = HttpApi.fetchVerifiedJson(manifestReference.get("url").getAsString(),
                manifestReference.get("sha1").getAsString()).json();
        String runtimeVersion = runtime.getAsJsonObject("version").get("name").getAsString();
        Path target = gameDirectory.resolve("runtimes").resolve(component).resolve(runtimeVersion);

        List<DownloadRequest> downloads = new ArrayList<>();
        List<JsonObject> links = new ArrayList<>();
        List<Path> executables = new ArrayList<>();
        collectRuntimeFiles(files, target, downloads, links, executables);

        status.accept("Installing the official Java " + majorVersion + " runtime ("
                + downloads.size() + " files)...");
        DownloadExecutor.downloadAll(downloads, "Java runtime", status);

        for (Path executable : executables) {
            if (!executable.toFile().setExecutable(true, true) && !Files.isExecutable(executable)) {
                throw new IOException("The official Java runtime could not be marked executable.");
            }
        }
        for (JsonObject link : links) {
            createLink(target, link.get("path").getAsString(), link.get("target").getAsString());
        }

        Path executable = locate(gameDirectory, component);
        if (executable == null) {
            throw new IOException("The official Java runtime is missing its executable.");
        }
        Files.writeString(executable.getParent().resolve(RUNTIME_MARKER), runtimeVersion,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return executable;
    }

    private static void collectRuntimeFiles(JsonObject files, Path target,
            List<DownloadRequest> downloads, List<JsonObject> links, List<Path> executables)
            throws IOException {
        for (var entry : files.getAsJsonObject("files").entrySet()) {
            Path destination = SafePaths.resolve(target, entry.getKey());
            JsonObject file = entry.getValue().getAsJsonObject();
            switch (file.get("type").getAsString()) {
                case "directory" -> Files.createDirectories(destination);
                case "file" -> {
                    JsonObject raw = file.getAsJsonObject("downloads").getAsJsonObject("raw");
                    downloads.add(new DownloadRequest(raw.get("url").getAsString(), destination,
                            raw.get("sha1").getAsString(), raw.get("size").getAsLong()));
                    if (file.has("executable") && file.get("executable").getAsBoolean()) {
                        executables.add(destination);
                    }
                }
                case "link" -> {
                    JsonObject link = new JsonObject();
                    link.addProperty("path", entry.getKey());
                    link.addProperty("target", file.get("target").getAsString());
                    links.add(link);
                }
                default -> {
                }
            }
        }
    }

    static void createLink(Path runtimeDirectory, String relativePath, String target)
            throws IOException {
        Path linkPath = SafePaths.resolve(runtimeDirectory, relativePath);
        Path targetPath = linkPath.getParent().resolve(target).normalize();
        if (!targetPath.startsWith(runtimeDirectory.normalize())) {
            throw new IOException("Refusing an unsafe link target in the official Java runtime manifest.");
        }
        Files.createDirectories(linkPath.getParent());
        if (Files.exists(linkPath, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        try {
            Files.createSymbolicLink(linkPath, Path.of(target));
        } catch (UnsupportedOperationException | IOException exception) {
            if (!Files.isRegularFile(targetPath)) {
                throw new IOException("An official Java runtime link could not be created.", exception);
            }
            Files.copy(targetPath, linkPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static Path locate(Path gameDirectory, String component) {
        Path root = gameDirectory.resolve("runtimes").resolve(component);
        if (!Files.isDirectory(root)) {
            return null;
        }
        String executableName = PlatformInfo.operatingSystem().equals("windows")
                ? "java.exe" : "java";
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(executableName))
                    .filter(path -> path.getParent() != null
                            && path.getParent().getFileName().toString().equals("bin"))
                    .findFirst()
                    .orElse(null);
        } catch (IOException exception) {
            return null;
        }
    }

    private static String platform() throws IOException {
        boolean arm = PlatformInfo.isArm();
        return switch (PlatformInfo.operatingSystem()) {
            case "osx" -> arm ? "mac-os-arm64" : "mac-os";
            case "linux" -> PlatformInfo.architecture().matches("i[3-6]86|x86")
                    ? "linux-i386" : "linux";
            case "windows" -> arm ? "windows-arm64"
                    : PlatformInfo.is64Bit() ? "windows-x64" : "windows-x86";
            default -> throw new IOException("This platform has no official Java runtime.");
        };
    }

    static JsonObject javaVersion(JsonObject metadata) {
        JsonObject javaVersion = metadata.getAsJsonObject("javaVersion");
        if (javaVersion != null) {
            return javaVersion;
        }
        JsonObject legacy = new JsonObject();
        legacy.addProperty("component", "jre-legacy");
        legacy.addProperty("majorVersion", 8);
        return legacy;
    }
}
