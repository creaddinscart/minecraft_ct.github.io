package com.ct.module.ctloader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class CTModLoaderTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void initializesModsAfterTheirDependencies() throws Exception {
        Path modsDirectory = Files.createDirectories(temporaryDirectory.resolve("mods"));
        writeMod(modsDirectory, "z-addon.jar", "addon", "[\"base\"]");
        writeMod(modsDirectory, "a-base.jar", "base", "[]");
        List<String> messages = new ArrayList<>();

        try (var ignored = CTModLoader.load(temporaryDirectory, messages::add)) {
            assertEquals(List.of("[CT] [base] initialized", "[CT] Loaded base 1.0.0.",
                    "[CT] [addon] initialized", "[CT] Loaded addon 1.0.0."), messages.subList(1, 5));
        }
    }

    @Test
    void rejectsModsWithMissingDependencies() throws Exception {
        Path modsDirectory = Files.createDirectories(temporaryDirectory.resolve("mods"));
        writeMod(modsDirectory, "addon.jar", "addon", "[\"missing\"]");

        IOException exception = assertThrows(IOException.class,
                () -> CTModLoader.load(temporaryDirectory, ignored -> { }));

        assertEquals(true, exception.getMessage().contains("requires missing mod 'missing'"));
    }

    private static void writeMod(Path modsDirectory, String jarName, String id, String depends)
            throws IOException {
        Path jarPath = modsDirectory.resolve(jarName);
        String metadata = "{\"schemaVersion\":1,\"id\":\"" + id + "\",\"name\":\""
                + id + "\",\"version\":\"1.0.0\",\"entrypoints\":[\""
                + CTModLoaderTest.SampleMod.class.getName() + "\"],\"depends\":" + depends + "}";
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarPath))) {
            jar.putNextEntry(new JarEntry("ct.mod.json"));
            jar.write(metadata.getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
            String classResource = CTModLoaderTest.SampleMod.class.getName().replace('.', '/') + ".class";
            jar.putNextEntry(new JarEntry(classResource));
            try (InputStream classBytes = CTModLoaderTest.class.getClassLoader()
                    .getResourceAsStream(classResource)) {
                if (classBytes == null) {
                    throw new IOException("Test mod class bytes are unavailable.");
                }
                classBytes.transferTo(jar);
            }
            jar.closeEntry();
        }
    }

    public static final class SampleMod implements CTMod {
        @Override
        public void onInitialize(CTModContext context) {
            context.log("initialized");
        }
    }
}
