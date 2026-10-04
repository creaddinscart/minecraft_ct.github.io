package com.ct.main.api;

import java.io.IOException;
import java.nio.file.Path;

public interface SettingsAccess {
    String text(String key, String fallback);

    void setText(String key, String value) throws IOException;

    int number(String key, int fallback);

    void setNumber(String key, int value) throws IOException;

    boolean flag(String key, boolean fallback);

    void setFlag(String key, boolean value) throws IOException;

    Path gameDirectory();

    void setGameDirectory(Path directory) throws IOException;
}
