package com.ct.module.installer;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public final class SafePaths {
    private SafePaths() {
    }

    public static Path resolve(Path base, String relativePath) throws IOException {
        Path normalizedBase = base.toAbsolutePath().normalize();
        Path resolved = normalizedBase.resolve(relativePath).normalize();
        if (!resolved.startsWith(normalizedBase)) {
            throw new IOException("Refusing an unsafe path in official metadata.");
        }
        return resolved;
    }

    public static boolean isSafeVersionId(String version) {
        if (version == null || version.isBlank() || version.length() > 128
                || version.equals(".") || version.equals("..") || version.contains("/")
                || version.contains("\\")) {
            return false;
        }
        for (int index = 0; index < version.length(); index++) {
            if (Character.isISOControl(version.charAt(index))) {
                return false;
            }
        }
        try {
            return !Path.of(version).isAbsolute() && Path.of(version).getNameCount() == 1;
        } catch (InvalidPathException exception) {
            return false;
        }
    }
}
