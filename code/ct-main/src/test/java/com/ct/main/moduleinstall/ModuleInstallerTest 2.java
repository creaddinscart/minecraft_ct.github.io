package com.ct.main.moduleinstall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ModuleInstallerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void installsTheJarUnderItsModuleName() throws IOException {
        Path source = writeModule(temporaryDirectory.resolve("downloads"),
                "ct-module-demo-2.0.0.jar", "demo", "2.0.0", List.of("theme"));
        Path modules = temporaryDirectory.resolve("modules");

        List<String> report = install(source, modules);

        Path installed = modules.resolve("ct-module-demo-2.0.0.jar");
        assertTrue(Files.isRegularFile(installed));
        assertEquals(Files.size(source), Files.size(installed));
        assertTrue(report.stream().anyMatch(line -> line.contains("installed ct-module-demo-2.0.0.jar")));
        assertTrue(report.stream().anyMatch(line -> line.contains("still requires: theme")));
    }

    @Test
    void replacesAnOlderCopyOfTheSameModule() throws IOException {
        Path modules = Files.createDirectories(temporaryDirectory.resolve("modules"));
        writeModule(modules, "ct-module-demo-1.0.0.jar", "demo", "1.0.0", List.of());
        Path source = writeModule(temporaryDirectory.resolve("downloads"),
                "ct-module-demo-2.0.0.jar", "demo", "2.0.0", List.of());

        List<String> report = install(source, modules);

        assertFalse(Files.exists(modules.resolve("ct-module-demo-1.0.0.jar")));
        assertTrue(Files.exists(modules.resolve("ct-module-demo-2.0.0.jar")));
        assertTrue(report.stream().anyMatch(
                line -> line.contains("replaced") && line.contains("ct-module-demo-1.0.0.jar")));
    }

    @Test
    void keepsOtherModulesInTheFolder() throws IOException {
        Path modules = Files.createDirectories(temporaryDirectory.resolve("modules"));
        writeModule(modules, "ct-module-other-2.0.0.jar", "other", "2.0.0", List.of());
        Path source = writeModule(temporaryDirectory.resolve("downloads"),
                "ct-module-demo-2.0.0.jar", "demo", "2.0.0", List.of());

        List<String> report = install(source, modules);

        assertTrue(Files.exists(modules.resolve("ct-module-other-2.0.0.jar")));
        assertTrue(report.stream().anyMatch(line -> line.contains("Modules in the folder (2): demo, other")));
    }

    @Test
    void printWritesNothing() throws IOException {
        Path modules = temporaryDirectory.resolve("modules");
        Path source = writeModule(temporaryDirectory.resolve("downloads"),
                "ct-module-demo-2.0.0.jar", "demo", "2.0.0", List.of());

        List<String> report = new ArrayList<>();
        int status = ModuleInstaller.run(
                ModuleInstaller.Options.parse(new String[] {"--print", "--source", source.toString(),
                        "--modules-dir", modules.toString()}), report);

        assertEquals(0, status);
        assertFalse(Files.exists(modules));
        assertTrue(report.stream().anyMatch(line -> line.contains("Dry run")));
    }

    @Test
    void removeDeletesEveryCopyOfTheModule() throws IOException {
        Path modules = Files.createDirectories(temporaryDirectory.resolve("modules"));
        writeModule(modules, "ct-module-demo-1.0.0.jar", "demo", "1.0.0", List.of());
        writeModule(modules, "ct-module-demo-2.0.0.jar", "demo", "2.0.0", List.of());
        writeModule(modules, "ct-module-other-2.0.0.jar", "other", "2.0.0", List.of());
        Path source = writeModule(temporaryDirectory.resolve("downloads"),
                "ct-module-demo-2.0.0.jar", "demo", "2.0.0", List.of());

        List<String> report = new ArrayList<>();
        int status = ModuleInstaller.run(
                ModuleInstaller.Options.parse(new String[] {"--remove", "--source", source.toString(),
                        "--modules-dir", modules.toString()}), report);

        assertEquals(0, status);
        assertFalse(Files.exists(modules.resolve("ct-module-demo-1.0.0.jar")));
        assertFalse(Files.exists(modules.resolve("ct-module-demo-2.0.0.jar")));
        assertTrue(Files.exists(modules.resolve("ct-module-other-2.0.0.jar")));
    }

    @Test
    void usesTheModulesFolderNextToTheMainJar() throws IOException {
        Path home = Files.createDirectories(temporaryDirectory.resolve("home"));
        Files.createFile(home.resolve("CT-Main-4.1.0.jar"));
        Path source = writeModule(home, "ct-module-demo-2.0.0.jar", "demo", "2.0.0", List.of());

        ModuleInstaller.Resolution resolution = ModuleInstaller.resolveModulesDirectory(
                ModuleInstaller.Options.parse(new String[] {"--source", source.toString()}), source);

        assertEquals(home.resolve("modules"), resolution.directory());
    }

    @Test
    void readsTheModuleIdFromTheManifestNotTheFileName() throws IOException {
        Path modules = Files.createDirectories(temporaryDirectory.resolve("modules"));
        Path source = writeModule(temporaryDirectory.resolve("downloads"), "custom-name.jar",
                "demo", "2.0.0", List.of());

        install(source, modules);

        assertTrue(Files.exists(modules.resolve("ct-module-demo-2.0.0.jar")));
    }

    private List<String> install(Path source, Path modules) throws IOException {
        List<String> report = new ArrayList<>();
        int status = ModuleInstaller.run(
                ModuleInstaller.Options.parse(new String[] {"--source", source.toString(),
                        "--modules-dir", modules.toString()}), report);
        assertEquals(0, status);
        return report;
    }

    private static Path writeModule(Path directory, String fileName, String id, String version,
            List<String> requires) throws IOException {
        Files.createDirectories(directory);
        Path jar = directory.resolve(fileName);
        String manifest = "{\n"
                + "  \"schemaVersion\": 1,\n"
                + "  \"id\": \"" + id + "\",\n"
                + "  \"version\": \"" + version + "\",\n"
                + "  \"name\": \"" + id + " module\",\n"
                + "  \"requires\": [" + requires.stream().map(require -> "\"" + require + "\"")
                        .reduce((left, right) -> left + ", " + right).orElse("") + "],\n"
                + "  \"entrypoint\": \"com.ct.demo.Module\"\n"
                + "}\n";
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("ct.module.json"));
            out.write(manifest.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
            out.putNextEntry(new JarEntry("com/ct/demo/Module.class"));
            out.write(new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});
            out.closeEntry();
        }
        return jar;
    }
}
