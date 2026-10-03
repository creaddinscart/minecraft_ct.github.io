package com.ct.main.api;

import java.nio.file.Path;

public final class GameDirectoryResolver {
    private GameDirectoryResolver() {
    }

    public static Path resolve(SettingsAccess settings) {
        String configured = settings.text(SettingKeys.GAME_DIRECTORY, "");
        Path directory = configured.isEmpty()
                ? settings.gameDirectory()
                : Path.of(configured);
        return directory.toAbsolutePath().normalize();
    }
}
