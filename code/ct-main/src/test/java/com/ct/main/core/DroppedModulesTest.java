package com.ct.main.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ct.main.moduleinstall.ModuleInstaller;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class DroppedModulesTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void installsEveryDroppedJarInOrder() throws IOException {
        Path downloads = Files.createDirectories(temporaryDirectory.resolve("downloads"));
        Path modules = temporaryDirectory.resolve("modules");
        writeModule(downloads, "ct-module-theme-4.2.0.jar", "theme", "4.2.0");
        writeModule(downloads, "ct-module-rules-4.2.0.jar", "rules", "4.2.0");

        DroppedModules.Report report = DroppedModules.install(List.of(
                downloads.resolve("ct-module-theme-4.2.0.jar"),
                downloads.resolve("ct-module-rules-4.2.0.jar")), modules);

        assertEquals(2, report.installed());
        assertEquals(0, report.skipped());
        assertTrue(report.changed());
        assertTrue(Files.isRegularFile(modules.resolve("ct-module-theme-4.2.0.jar")));
        assertTrue(Files.isRegularFile(modules.resolve("ct-module-rules-4.2.0.jar")));
    }

    @Test
    void installsEveryModuleJarInADroppedFolder() throws IOException {
        Path downloads = Files.createDirectories(temporaryDirectory.resolve("downloads"));
        Path modules = temporaryDirectory.resolve("modules");
        writeModule(downloads, "ct-module-theme-4.2.0.jar", "theme", "4.2.0");
        writeModule(downloads, "ct-module-rules-4.2.0.jar", "rules", "4.2.0");
        Files.writeString(downloads.resolve("notes.txt"), "ignore me", StandardCharsets.UTF_8);

        DroppedModules.Report report = DroppedModules.install(List.of(downloads), modules);

        assertEquals(2, report.installed());
        assertEquals(0, report.skipped());
        assertTrue(Files.isRegularFile(modules.resolve("ct-module-theme-4.2.0.jar")));
    }

    @Test
    void replacesAnOlderBuildOfTheSameModule() throws IOException {
        Path modules = Files.createDirectories(temporaryDirectory.resolve("modules"));
        writeModule(modules, "ct-module-theme-4.1.0.jar", "theme", "4.1.0");
        Path downloads = Files.createDirectories(temporaryDirectory.resolve("downloads"));
        Path dropped = writeModule(downloads, "ct-module-theme-4.2.0.jar", "theme", "4.2.0");

        DroppedModules.Report report = DroppedModules.install(List.of(dropped), modules);

        assertEquals(1, report.installed());
        assertFalse(Files.exists(modules.resolve("ct-module-theme-4.1.0.jar")));
        assertTrue(Files.isRegularFile(modules.resolve("ct-module-theme-4.2.0.jar")));
        assertTrue(report.lines().stream().anyMatch(
                line -> line.contains("replaced") && line.contains("ct-module-theme-4.1.0.jar")));
    }

    @Test
    void reportsFilesThatAreNotModules() throws IOException {
        Path downloads = Files.createDirectories(temporaryDirectory.resolve("downloads"));
        Path modules = temporaryDirectory.resolve("modules");
        Path plain = downloads.resolve("plain.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(plain))) {
            out.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            out.write("Manifest-Version: 1.0\n".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }

        DroppedModules.Report report = DroppedModules.install(List.of(plain), modules);

        assertEquals(0, report.installed());
        assertEquals(1, report.skipped());
        assertTrue(report.failed());
        assertFalse(report.changed());
        assertTrue(report.lines().stream().anyMatch(line -> line.contains("not a CT module jar")));
    }

    @Test
    void reportsAMissingDrop() {
        Path modules = temporaryDirectory.resolve("modules");
        Path missing = temporaryDirectory.resolve("ct-module-gone-4.2.0.jar");

        DroppedModules.Report report = DroppedModules.install(List.of(missing), modules);

        assertEquals(0, report.installed());
        assertEquals(1, report.skipped());
        assertTrue(report.lines().stream().anyMatch(line -> line.contains("not found")));
    }

    @Test
    void anEmptyFolderIsReportedNotInstalled() throws IOException {
        Path empty = Files.createDirectories(temporaryDirectory.resolve("empty"));
        Path modules = temporaryDirectory.resolve("modules");

        DroppedModules.Report report = DroppedModules.install(List.of(empty), modules);

        assertEquals(0, report.installed());
        assertEquals(1, report.skipped());
        assertTrue(report.lines().stream().anyMatch(
                line -> line.contains("holds no CT module jar")));
        assertFalse(Files.exists(modules.resolve("ct-module-theme-4.2.0.jar")));
    }

    @Test
    void installingThroughTheDropPathMatchesTheModuleInstaller() throws IOException {
        Path downloads = Files.createDirectories(temporaryDirectory.resolve("downloads"));
        Path modules = temporaryDirectory.resolve("modules");
        Path dropped = writeModule(downloads, "ct-module-theme-4.2.0.jar", "theme", "4.2.0");

        DroppedModules.install(List.of(dropped), modules);
        ModuleInstaller.Result again = ModuleInstaller.installModule(dropped, modules);

        assertFalse(again.copied());
        assertEquals("theme", again.module().id());
    }

    private static Path writeModule(Path directory, String fileName, String id, String version)
            throws IOException {
        Files.createDirectories(directory);
        Path jar = directory.resolve(fileName);
        String manifest = "{\n  \"schemaVersion\": 1,\n  \"id\": \"" + id + "\",\n  \"version\": \""
                + version + "\",\n  \"name\": \"" + id + " module\",\n  \"requires\": [],\n"
                + "  \"entrypoint\": \"com.ct.demo.Module\"\n}\n";
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("ct.module.json"));
            out.write(manifest.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        return jar;
    }
}
