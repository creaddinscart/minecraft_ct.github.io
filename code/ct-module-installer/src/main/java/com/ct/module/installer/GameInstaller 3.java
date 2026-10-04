package com.ct.module.installer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface GameInstaller {
    Path gameDirectory();

    VersionCatalog versionCatalog() throws IOException, InterruptedException;

    Installation install(String version, Consumer<String> status)
            throws IOException, InterruptedException;

    Installation existing(String version);

    boolean isInstalled(String version);

    List<String> installedModLoaderProfiles() throws IOException;

    Installation existingModLoaderProfile(String profileId) throws IOException, InterruptedException;

    String profileParent(String profileId) throws IOException;

    List<String> installedFabricProfiles(String minecraftVersion) throws IOException;

    boolean hasFabricMods() throws IOException;
}
