package com.ct.module.modman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ModScannerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void classifiesCtAndFabricAndUnknownJars() throws IOException {
        Path mods = Files.createDirectories(ModScanner.modsDirectory(temporaryDirectory));
        writeJar(mods, "a-ct.jar", "ct.mod.json",
                "{\"schemaVersion\":1,\"id\":\"demo\",\"name\":\"Demo\",\"version\":\"1.0.0\","
                        + "\"entrypoints\":[\"demo.Main\"]}");
        writeJar(mods, "b-fabric.jar", "fabric.mod.json",
                "{\"schemaVersion\":1,\"id\":\"fabric-demo\",\"name\":\"Fabric Demo\","
                        + "\"version\":\"2.0.0\"}");
        writeJar(mods, "c-plain.jar", "META-INF/MANIFEST.MF", "Manifest-Version: 1.0");

        List<ModEntry> entries = ModScanner.scan(temporaryDirectory);

        assertEquals(3, entries.size());
        assertEquals(ModEntry.Kind.CT, entries.get(0).kind());
        assertEquals("demo", entries.get(0).modId());
        assertEquals(ModEntry.Kind.FABRIC, entries.get(1).kind());
        assertEquals("Fabric Demo", entries.get(1).name());
        assertEquals(ModEntry.Kind.UNKNOWN, entries.get(2).kind());
    }

    @Test
    void missingModsFolderIsAnEmptyList() throws IOException {
        assertTrue(ModScanner.scan(temporaryDirectory).isEmpty());
    }

    private static void writeJar(Path modsDirectory, String jarName, String entryName,
            String content) throws IOException {
        Path jarPath = modsDirectory.resolve(jarName);
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarPath))) {
            jar.putNextEntry(new JarEntry(entryName));
            jar.write(content.getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
        }
    }
}
