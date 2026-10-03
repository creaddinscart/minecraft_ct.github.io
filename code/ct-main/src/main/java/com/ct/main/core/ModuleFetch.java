package com.ct.main.core;

import com.ct.main.api.Website;
import com.ct.main.moduleinstall.ModuleInfo;
import com.ct.main.moduleinstall.ModuleInstaller;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ModuleFetch {
    public record Result(String id, Path installed, String error) {
        public boolean succeeded() {
            return installed != null;
        }
    }

    private static final int ROUNDS = 3;

    private ModuleFetch() {
    }

    public static String url(String id, String version) {
        return Website.HOME + "/downloads/modules/ct-module-" + id + "-" + version + ".jar";
    }

    public static List<Result> fetchMissing(Path modulesDirectory, String version, List<String> lines) {
        List<Result> results = new ArrayList<>();
        List<String> versions = candidateVersions(modulesDirectory, version);
        for (int round = 0; round < ROUNDS; round++) {
            List<String> missing = missingModules(modulesDirectory);
            if (missing.isEmpty()) {
                return results;
            }
            for (String id : missing) {
                Result result = fetch(id, versions, modulesDirectory, lines);
                results.add(result);
                if (!result.succeeded()) {
                    return results;
                }
            }
        }
        return results;
    }

    public static List<String> missingModules(Path modulesDirectory) {
        List<ModuleInfo> installed = ModuleInfo.installed(modulesDirectory);
        LinkedHashSet<String> present = new LinkedHashSet<>();
        for (ModuleInfo module : installed) {
            present.add(module.id());
        }
        LinkedHashSet<String> missing = new LinkedHashSet<>();
        for (ModuleInfo module : installed) {
            for (String require : module.requires()) {
                if (!present.contains(require)) {
                    missing.add(require);
                }
            }
        }
        return List.copyOf(missing);
    }

    static List<String> candidateVersions(Path modulesDirectory, String version) {
        LinkedHashSet<String> versions = new LinkedHashSet<>();
        if (version != null && !version.isBlank()) {
            versions.add(version);
        }
        for (ModuleInfo module : ModuleInfo.installed(modulesDirectory)) {
            if (module.version() != null && !module.version().isBlank()) {
                versions.add(module.version());
            }
        }
        return List.copyOf(versions);
    }

    private static Result fetch(String id, List<String> versions, Path modulesDirectory,
            List<String> lines) {
        String reason = "no version to try";
        for (String version : versions) {
            lines.add("  fetching  ct-module-" + id + "-" + version + ".jar from " + Website.HOME);
            Result result = download(id, version, modulesDirectory, lines);
            if (result.succeeded()) {
                return result;
            }
            reason = result.error();
        }
        String published = publishedVersion();
        if (published != null && !versions.contains(published)) {
            lines.add("  fetching  ct-module-" + id + "-" + published + ".jar from " + Website.HOME);
            Result result = download(id, published, modulesDirectory, lines);
            if (result.succeeded()) {
                return result;
            }
            reason = result.error();
        }
        lines.add("  missing   " + id + " — could not download it (" + reason + ")");
        lines.add("            open " + Website.HOME + "/modules.html, download ct-module-" + id
                + "-<version>.jar and drop it on CT-Main as well");
        return new Result(id, null, reason);
    }

    static String publishedVersion() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(
                            URI.create(Website.HOME + "/assets/site.js"))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("version:\\s*\"([^\"]+)\"").matcher(response.body());
            return matcher.find() ? matcher.group(1) : null;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    private static Result download(String id, String version, Path modulesDirectory,
            List<String> lines) {
        Path temporary = null;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url(id, version)))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                return new Result(id, null, "the website answered HTTP " + response.statusCode()
                        + " for version " + version);
            }
            byte[] body = response.body();
            if (body == null || body.length == 0) {
                return new Result(id, null, "the download was empty");
            }
            temporary = Files.createTempFile("ct-module-", ".jar");
            Files.write(temporary, body);
            ModuleInfo info = ModuleInfo.read(temporary);
            if (!info.id().equals(id)) {
                return new Result(id, null, "the download holds module '" + info.id() + "'");
            }
            ModuleInstaller.Result result = ModuleInstaller.installModule(temporary, modulesDirectory);
            lines.add("  installed " + result.installed().getFileName() + " — " + info.label()
                    + " (" + body.length + " bytes)");
            return new Result(id, result.installed(), null);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new Result(id, null, "interrupted");
        } catch (IOException | RuntimeException exception) {
            return new Result(id, null, exception.getMessage() == null
                    ? exception.getClass().getSimpleName() : exception.getMessage());
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    temporary.toFile().deleteOnExit();
                }
            }
        }
    }
}
