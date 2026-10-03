package com.ct.main.core;

import com.ct.main.Main;
import com.ct.main.api.ModuleContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ApplicationPaths implements ModuleContext.PathAccess {
    private final Path applicationDirectory;
    private final Path modulesDirectory;

    private ApplicationPaths(Path applicationDirectory, Path modulesDirectory) {
        this.applicationDirectory = applicationDirectory;
        this.modulesDirectory = modulesDirectory;
    }

    public static ApplicationPaths resolve(Path modulesDirectoryOverride) throws IOException {
        Path applicationDirectory = Path.of(System.getProperty("user.home"), ".ct-client")
                .toAbsolutePath().normalize();
        Files.createDirectories(applicationDirectory);
        if (modulesDirectoryOverride != null) {
            return new ApplicationPaths(applicationDirectory,
                    modulesDirectoryOverride.toAbsolutePath().normalize());
        }
        Path local = localModulesDirectory();
        return new ApplicationPaths(applicationDirectory,
                local == null ? applicationDirectory.resolve("modules") : local);
    }

    private static Path localModulesDirectory() {
        try {
            java.nio.file.Path jar = Path.of(Main.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI());
            if (Files.isRegularFile(jar)) {
                return jar.getParent().resolve("modules");
            }
        } catch (Exception | LinkageError ignored) {
            return null;
        }
        return null;
    }

    public Path applicationDirectory() {
        return applicationDirectory;
    }

    public Path modulesDirectory() {
        return modulesDirectory;
    }

    public Path gameDirectory() {
        return applicationDirectory.resolve("game");
    }
}
