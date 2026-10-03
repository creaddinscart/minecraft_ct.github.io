package com.ct.module.ctloader;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class CTModContext {
    private final CTModMetadata metadata;
    private final Path gameDirectory;
    private final Path modsDirectory;
    private final Consumer<String> logger;

    CTModContext(CTModMetadata metadata, Path gameDirectory, Path modsDirectory,
            Consumer<String> logger) {
        this.metadata = metadata;
        this.gameDirectory = gameDirectory;
        this.modsDirectory = modsDirectory;
        this.logger = logger;
    }

    public CTModMetadata metadata() {
        return metadata;
    }

    public Path gameDirectory() {
        return gameDirectory;
    }

    public Path modsDirectory() {
        return modsDirectory;
    }

    public void log(String message) {
        logger.accept("[CT] [" + metadata.id() + "] " + message);
    }
}
