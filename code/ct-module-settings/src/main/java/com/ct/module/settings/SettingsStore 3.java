package com.ct.module.settings;

import com.ct.main.api.SettingsAccess;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class SettingsStore implements SettingsAccess {
    private final Path settingsFile;
    private final Properties properties = new Properties();

    public SettingsStore(Path applicationDirectory) throws IOException {
        Files.createDirectories(applicationDirectory);
        settingsFile = applicationDirectory.resolve("settings.properties");
        if (Files.isRegularFile(settingsFile)) {
            try (InputStream input = Files.newInputStream(settingsFile)) {
                properties.load(input);
            }
        }
    }

    public static Path defaultApplicationDirectory() {
        return Path.of(System.getProperty("user.home"), ".ct-client").toAbsolutePath().normalize();
    }

    @Override
    public String text(String key, String fallback) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    @Override
    public void setText(String key, String value) throws IOException {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            properties.remove(key);
        } else {
            properties.setProperty(key, normalized);
        }
        store();
    }

    @Override
    public int number(String key, int fallback) {
        try {
            return Integer.parseInt(text(key, Integer.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    @Override
    public void setNumber(String key, int value) throws IOException {
        setText(key, Integer.toString(value));
    }

    @Override
    public boolean flag(String key, boolean fallback) {
        return Boolean.parseBoolean(text(key, Boolean.toString(fallback)));
    }

    @Override
    public void setFlag(String key, boolean value) throws IOException {
        setText(key, Boolean.toString(value));
    }

    @Override
    public Path gameDirectory() {
        return defaultApplicationDirectory().resolve("game");
    }

    @Override
    public void setGameDirectory(Path directory) throws IOException {
        setText(com.ct.main.api.SettingKeys.GAME_DIRECTORY, directory == null ? "" : directory.toString());
    }

    private void store() throws IOException {
        try (OutputStream output = Files.newOutputStream(settingsFile)) {
            properties.store(output,
                    "CT Client settings. Credentials and access tokens are never written here.");
        }
    }
}
