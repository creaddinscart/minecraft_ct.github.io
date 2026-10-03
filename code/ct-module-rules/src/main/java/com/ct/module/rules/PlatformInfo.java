package com.ct.module.rules;

import java.util.Locale;

public final class PlatformInfo {
    private PlatformInfo() {
    }

    public static String operatingSystem() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "windows";
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return "osx";
        }
        return "linux";
    }

    public static String architecture() {
        return System.getProperty("os.arch", "");
    }

    public static boolean isArm() {
        String arch = architecture().toLowerCase(Locale.ROOT);
        return arch.contains("aarch64") || arch.contains("arm64");
    }

    public static boolean is64Bit() {
        String arch = architecture().toLowerCase(Locale.ROOT);
        return arch.contains("64") || isArm();
    }
}
