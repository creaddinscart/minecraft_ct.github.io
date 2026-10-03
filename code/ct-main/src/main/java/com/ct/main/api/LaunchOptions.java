package com.ct.main.api;

public record LaunchOptions(int minimumMemoryMb, int maximumMemoryMb, boolean customResolution, int width,
        int height, boolean demoMode, boolean recommendedJvmFlags, String versionType) {

    public static final int MINIMUM_MEMORY_MB = 1024;
    public static final int MAXIMUM_MEMORY_MB = 65536;

    public static LaunchOptions defaults() {
        return new LaunchOptions(2048, 4096, false, 854, 480, false, true, "release");
    }

    public LaunchOptions {
        if (minimumMemoryMb < MINIMUM_MEMORY_MB || minimumMemoryMb > MAXIMUM_MEMORY_MB) {
            throw new IllegalArgumentException("Minimum memory must be between " + MINIMUM_MEMORY_MB
                    + " and " + MAXIMUM_MEMORY_MB + " MB.");
        }
        if (maximumMemoryMb < minimumMemoryMb || maximumMemoryMb > MAXIMUM_MEMORY_MB) {
            throw new IllegalArgumentException("Maximum memory must be at least the minimum memory and at most "
                    + MAXIMUM_MEMORY_MB + " MB.");
        }
        if (customResolution && (width < 320 || height < 240)) {
            throw new IllegalArgumentException("Custom resolution must be at least 320 by 240.");
        }
    }
}
