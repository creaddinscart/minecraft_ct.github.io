package com.ct.main.api;

public final class LaunchOptionsReader {
    private LaunchOptionsReader() {
    }

    public static LaunchOptions from(SettingsAccess settings) {
        LaunchOptions defaults = LaunchOptions.defaults();
        int minimum = settings.number(SettingKeys.MEMORY_MINIMUM, defaults.minimumMemoryMb());
        int maximum = settings.number(SettingKeys.MEMORY_MAXIMUM, defaults.maximumMemoryMb());
        int width = settings.number(SettingKeys.RESOLUTION_WIDTH, defaults.width());
        int height = settings.number(SettingKeys.RESOLUTION_HEIGHT, defaults.height());
        boolean custom = settings.flag(SettingKeys.CUSTOM_RESOLUTION, false);
        boolean flags = settings.flag(SettingKeys.RECOMMENDED_JVM_FLAGS, true);
        try {
            return new LaunchOptions(minimum, maximum, custom, width, height, false, flags, "release");
        } catch (IllegalArgumentException exception) {
            return defaults;
        }
    }
}
