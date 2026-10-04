package com.ct.module.installer;

import com.google.gson.JsonObject;
import java.nio.file.Path;

public record Installation(Path gameDirectory, Path versionDirectory, Path librariesDirectory,
        Path assetsDirectory, Path nativesDirectory, Path clientJar, Path runtimeExecutable,
        Path logConfig, String classpath, String assetIndexId, String versionId,
        JsonObject metadata) {

    public Installation withRuntimeExecutable(Path executable) {
        return new Installation(gameDirectory, versionDirectory, librariesDirectory, assetsDirectory,
                nativesDirectory, clientJar, executable, logConfig, classpath, assetIndexId,
                versionId, metadata);
    }
}
