package com.ct.module.launcher;

import com.ct.main.api.Account;
import com.ct.main.api.LaunchOptions;
import com.ct.main.api.LaunchOptionsReader;
import com.ct.main.api.SettingsAccess;
import com.ct.module.installer.GameInstaller;
import com.ct.module.installer.Installation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class LaunchService {
    private final GameInstaller installer;
    private final SettingsAccess settings;
    private final Optional<Path> loaderBootstrap;
    private final String launcherVersion;

    public LaunchService(GameInstaller installer, SettingsAccess settings,
            Optional<Path> loaderBootstrap, String launcherVersion) {
        this.installer = installer;
        this.settings = settings;
        this.loaderBootstrap = loaderBootstrap;
        this.launcherVersion = launcherVersion;
    }

    public Path gameDirectory() {
        return installer.gameDirectory();
    }

    public LaunchOptions launchOptions() {
        return LaunchOptionsReader.from(settings);
    }

    public List<String> buildCommand(String requestedProfile, Account account)
            throws IOException, InterruptedException {
        return buildCommand(requestedProfile, account, launchOptions());
    }

    public List<String> buildCommand(String requestedProfile, Account account, LaunchOptions options)
            throws IOException, InterruptedException {
        Installation installation = resolveInstallation(requestedProfile);
        Path override = javaExecutableOverride();
        if (override != null) {
            installation = installation.withRuntimeExecutable(override);
        }
        CommandBuilder builder = new CommandBuilder(gameDirectory(), launcherVersion);
        return builder.buildCommand(installation, account, options, loaderBootstrap);
    }

    public Process launch(String requestedProfile, Account account, com.ct.main.api.ConsoleWriter console)
            throws IOException, InterruptedException {
        return launch(requestedProfile, account, console, launchOptions());
    }

    public Process launch(String requestedProfile, Account account, com.ct.main.api.ConsoleWriter console,
            LaunchOptions options) throws IOException, InterruptedException {
        Installation installation = resolveInstallation(requestedProfile);
        Path override = javaExecutableOverride();
        if (override != null) {
            installation = installation.withRuntimeExecutable(override);
        }
        CommandBuilder builder = new CommandBuilder(gameDirectory(), launcherVersion);
        List<String> command = builder.buildCommand(installation, account, options, loaderBootstrap);
        Process process = ProcessStarter.start(command, gameDirectory());
        Path launchLog = ProcessStarter.launchLog(gameDirectory());
        Thread reader = new Thread(() -> ProcessStarter.forwardOutput(process, launchLog,
                console::writeLine), "ct-game-output");
        reader.setDaemon(true);
        reader.start();
        return process;
    }

    public Installation resolveInstallation(String requestedProfile)
            throws IOException, InterruptedException {
        List<String> loaderProfiles = installer.installedModLoaderProfiles();
        String profileId = ProfileRouter.resolveLaunchProfileId(installer, requestedProfile,
                loaderProfiles);
        if (!loaderProfiles.contains(profileId)) {
            Installation existing = installer.existing(profileId);
            if (existing != null) {
                return existing;
            }
            return installer.install(profileId, line -> { });
        }
        String parentVersion = installer.profileParent(profileId);
        if (installer.existing(parentVersion) == null) {
            installer.install(parentVersion, line -> { });
        }
        return installer.existingModLoaderProfile(profileId);
    }

    private Path javaExecutableOverride() throws IOException {
        String configured = settings.text(com.ct.main.api.SettingKeys.JAVA_EXECUTABLE, "");
        if (configured.isEmpty()) {
            return null;
        }
        Path override = Path.of(configured).toAbsolutePath().normalize();
        if (!java.nio.file.Files.isExecutable(override)) {
            throw new IOException("The configured Java executable is not runnable: " + override);
        }
        return override;
    }
}
