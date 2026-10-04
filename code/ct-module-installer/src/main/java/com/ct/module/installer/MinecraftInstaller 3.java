package com.ct.module.installer;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class MinecraftInstaller implements GameInstaller {
    private final Path gameDirectory;

    public MinecraftInstaller(Path gameDirectory) {
        this.gameDirectory = gameDirectory.toAbsolutePath().normalize();
    }

    @Override
    public Path gameDirectory() {
        return gameDirectory;
    }

    @Override
    public VersionCatalog versionCatalog() throws IOException, InterruptedException {
        return CatalogParser.parse(HttpApi.fetchJson(OfficialEndpoints.VERSION_MANIFEST));
    }

    @Override
    public Installation install(String version, Consumer<String> status)
            throws IOException, InterruptedException {
        JsonObject manifest = HttpApi.fetchJson(OfficialEndpoints.VERSION_MANIFEST);
        JsonObject selectedVersion = VersionLocator.find(manifest, version);
        VerifiedDocument document = MetadataStore.resolve(gameDirectory, version, selectedVersion);
        JsonObject metadata = document.json();

        Path versionDirectory = gameDirectory.resolve("versions").resolve(version);
        MetadataStore.store(gameDirectory, version, selectedVersion, document);

        Path clientJar = versionDirectory.resolve(version + ".jar");
        JsonObject clientDownload = metadata.getAsJsonObject("downloads").getAsJsonObject("client");
        status.accept("Downloading the Minecraft " + version + " client...");
        DownloadExecutor.downloadVerified(clientDownload.get("url").getAsString(), clientJar,
                clientDownload.get("sha1").getAsString(), clientDownload.get("size").getAsLong());

        Path librariesDirectory = gameDirectory.resolve("libraries");
        Path nativesDirectory = versionDirectory.resolve("natives");
        List<DownloadRequest> libraryDownloads = LibraryPlanner.collect(metadata, librariesDirectory);
        DownloadExecutor.downloadAll(libraryDownloads, "game libraries", status);
        NativeExtractor.extract(metadata, librariesDirectory, nativesDirectory);

        Path assetsDirectory = gameDirectory.resolve("assets");
        JsonObject assetIndex = metadata.getAsJsonObject("assetIndex");
        if (assetIndex != null) {
            installAssets(assetIndex, assetsDirectory, status);
        }

        Path logConfig = LogConfigDownloader.download(gameDirectory, metadata, status);
        NativeExtractor.prepareSubdirectories(metadata, nativesDirectory);
        Path runtimeExecutable = RuntimeInstaller.install(gameDirectory, metadata, status);

        return InstallationBuilder.build(gameDirectory, metadata, versionDirectory,
                librariesDirectory, assetsDirectory, nativesDirectory, clientJar,
                runtimeExecutable, logConfig);
    }

    private static void installAssets(JsonObject assetIndex, Path assetsDirectory,
            Consumer<String> status) throws IOException, InterruptedException {
        Path indexesDirectory = assetsDirectory.resolve("indexes");
        Files.createDirectories(indexesDirectory);
        Path indexFile = indexesDirectory.resolve(assetIndex.get("id").getAsString() + ".json");
        DownloadExecutor.downloadVerified(assetIndex.get("url").getAsString(), indexFile,
                assetIndex.get("sha1").getAsString(), assetIndex.get("size").getAsLong());
        JsonObject index = com.google.gson.JsonParser.parseString(Files.readString(indexFile))
                .getAsJsonObject();
        List<DownloadRequest> assetDownloads = AssetPlanner.collect(index, assetsDirectory);
        DownloadExecutor.downloadAll(assetDownloads, "game assets", status);
    }

    @Override
    public Installation existing(String version) {
        if (!SafePaths.isSafeVersionId(version)) {
            return null;
        }
        Path versionDirectory = gameDirectory.resolve("versions").resolve(version);
        Path metadataFile = MetadataStore.metadataFile(gameDirectory, version);
        Path clientJar = versionDirectory.resolve(version + ".jar");
        if (!Files.isRegularFile(metadataFile) || !Files.isRegularFile(clientJar)) {
            return null;
        }
        JsonObject metadata;
        try {
            metadata = MetadataStore.read(metadataFile);
        } catch (IOException | RuntimeException exception) {
            return null;
        }
        String javaComponent = RuntimeInstaller.javaVersion(metadata).get("component").getAsString();
        Path runtimeExecutable = RuntimeInstaller.locate(gameDirectory, javaComponent);
        if (runtimeExecutable == null) {
            return null;
        }
        Path logConfig = LogConfigDownloader.locateExisting(gameDirectory, metadata);
        try {
            return InstallationBuilder.build(gameDirectory, metadata, versionDirectory,
                    gameDirectory.resolve("libraries"), gameDirectory.resolve("assets"),
                    versionDirectory.resolve("natives"), clientJar, runtimeExecutable, logConfig);
        } catch (IOException exception) {
            return null;
        }
    }

    @Override
    public boolean isInstalled(String version) {
        return existing(version) != null;
    }

    @Override
    public List<String> installedModLoaderProfiles() throws IOException {
        Path versionsDirectory = gameDirectory.resolve("versions");
        if (!Files.isDirectory(versionsDirectory)) {
            return List.of();
        }
        try (Stream<Path> directories = Files.list(versionsDirectory)) {
            return directories.filter(Files::isDirectory)
                    .map(directory -> directory.getFileName().toString())
                    .filter(profileId -> LoaderProfileReader.isSupported(gameDirectory, profileId))
                    .sorted()
                    .toList();
        }
    }

    @Override
    public boolean hasFabricMods() throws IOException {
        return FabricProfileDetector.hasFabricMods(gameDirectory);
    }

    @Override
    public List<String> installedFabricProfiles(String minecraftVersion) throws IOException {
        return FabricProfileDetector.installedFabricProfiles(gameDirectory,
                installedModLoaderProfiles(), minecraftVersion);
    }

    @Override
    public String profileParent(String profileId) throws IOException {
        return LoaderProfileReader.parent(LoaderProfileReader.read(gameDirectory, profileId), profileId);
    }

    @Override
    public Installation existingModLoaderProfile(String profileId)
            throws IOException, InterruptedException {
        JsonObject profile = LoaderProfileReader.read(gameDirectory, profileId);
        String parentId = LoaderProfileReader.parent(profile, profileId);
        Installation parent = existing(parentId);
        if (parent == null) {
            throw new IOException("Minecraft " + parentId
                    + " must be installed before launching " + profileId + ".");
        }
        return ModLoaderProfileResolver.resolve(gameDirectory, profileId, parent);
    }
}
