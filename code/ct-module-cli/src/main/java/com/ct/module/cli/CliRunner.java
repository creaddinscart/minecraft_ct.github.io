package com.ct.module.cli;

import com.ct.main.api.Account;
import com.ct.main.api.BootstrapAccess;
import com.ct.main.api.LaunchOptions;
import com.ct.main.api.LaunchOptionsReader;
import com.ct.main.api.ModuleContext;
import com.ct.main.api.SettingsAccess;
import com.ct.module.installer.GameInstaller;
import com.ct.module.installer.Installation;
import com.ct.module.installer.MinecraftInstaller;
import com.ct.module.installer.VersionCatalog;
import com.ct.module.launcher.CommandBuilder;
import com.ct.module.launcher.ProfileRouter;
import com.ct.module.launcher.ProcessStarter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CliRunner {
    private static final String VERSION_FLAG = "--version";
    private static final String GAME_DIRECTORY_FLAG = "--game-dir";
    private static final String PLAYER_FLAG = "--player";
    private static final String MAXIMUM_MEMORY_FLAG = "--memory-max";
    private static final String INSTALL_ONLY_FLAG = "--install-only";
    private static final String VERIFY_FLAG = "--verify";
    private static final String DRY_RUN_FLAG = "--dry-run";
    private static final String HELP_FLAG = "--help";

    private static final List<String> VALUE_FLAGS = List.of(VERSION_FLAG, GAME_DIRECTORY_FLAG,
            PLAYER_FLAG, MAXIMUM_MEMORY_FLAG);

    private CliRunner() {
    }

    public static boolean run(String[] args, ModuleContext context) throws Exception {
        Map<String, String> options = parse(args);
        if (options.containsKey(HELP_FLAG)) {
            printUsage(context);
            return true;
        }

        GameInstaller installer = pickInstaller(options, context);
        SettingsAccess settings = context.service(SettingsAccess.class)
                .orElseThrow(() -> new IOException("The settings module is not installed."));
        Optional<Path> bootstrap = context.service(BootstrapAccess.class)
                .flatMap(BootstrapAccess::bootstrapJar);

        String requested = options.get(VERSION_FLAG);
        String version = requested == null
                ? installer.versionCatalog().latestRelease()
                : requested;
        context.console().writeLine("Game directory: " + installer.gameDirectory());

        Installation installation = resolveInstallation(installer, version, options, context);
        context.console().writeLine("Client jar: " + installation.clientJar());
        context.console().writeLine("Java runtime: " + installation.runtimeExecutable());
        context.console().writeLine("Classpath entries: " + installation.classpath().split(
                java.util.regex.Pattern.quote(System.getProperty("path.separator"))).length);

        if (options.containsKey(INSTALL_ONLY_FLAG)) {
            context.console().writeLine("Installation verified.");
            return true;
        }

        Account account = Account.offline(options.getOrDefault(PLAYER_FLAG, "CTPlayer"));
        LaunchOptions launchOptions = launchOptions(settings, options);
        CommandBuilder builder = new CommandBuilder(installer.gameDirectory(), context.mainVersion());
        List<String> command = builder.buildCommand(installation, account, launchOptions, bootstrap);

        if (options.containsKey(DRY_RUN_FLAG)) {
            context.console().writeLine(String.join(" ", command));
            return true;
        }

        context.console().writeLine("Launching Minecraft " + version + " as " + account.label() + ".");
        Process process = ProcessStarter.start(command, installer.gameDirectory());
        Path launchLog = ProcessStarter.launchLog(installer.gameDirectory());
        Thread reader = new Thread(() -> ProcessStarter.forwardOutput(process, launchLog,
                context.console()::writeLine), "ct-game-output");
        reader.setDaemon(true);
        reader.start();
        int exitCode = process.waitFor();
        context.console().writeLine("Minecraft exited with code " + exitCode + ".");
        System.exit(exitCode);
        return true;
    }

    private static Installation resolveInstallation(GameInstaller installer, String requested,
            Map<String, String> options, ModuleContext context)
            throws IOException, InterruptedException {
        List<String> loaderProfiles = installer.installedModLoaderProfiles();
        String profileId = ProfileRouter.resolveLaunchProfileId(installer, requested, loaderProfiles);
        boolean verify = options.containsKey(VERIFY_FLAG);
        Installation installation;
        if (loaderProfiles.contains(profileId)) {
            String parent = installer.profileParent(profileId);
            if (verify || installer.existing(parent) == null) {
                installer.install(parent, line -> context.console().writeLine("[ct] " + line));
            }
            installation = installer.existingModLoaderProfile(profileId);
        } else {
            if (verify || installer.existing(profileId) == null) {
                installation = installer.install(profileId,
                        line -> context.console().writeLine("[ct] " + line));
            } else {
                installation = installer.existing(profileId);
                context.console().writeLine("Minecraft " + profileId
                        + " is already installed. Use --verify to re-check it.");
            }
        }
        return installation;
    }

    private static GameInstaller pickInstaller(Map<String, String> options, ModuleContext context)
            throws IOException {
        if (options.containsKey(GAME_DIRECTORY_FLAG)) {
            Path directory = Path.of(options.get(GAME_DIRECTORY_FLAG)).toAbsolutePath().normalize();
            return new MinecraftInstaller(directory);
        }
        return context.service(GameInstaller.class)
                .orElseThrow(() -> new IOException("The installer module is not installed. "
                        + "Place the ct-module-installer jar in the modules folder."));
    }

    private static LaunchOptions launchOptions(SettingsAccess settings, Map<String, String> options)
            throws IOException {
        LaunchOptions base = LaunchOptionsReader.from(settings);
        if (!options.containsKey(MAXIMUM_MEMORY_FLAG)) {
            return base;
        }
        int maximum;
        try {
            maximum = Integer.parseInt(options.get(MAXIMUM_MEMORY_FLAG));
        } catch (NumberFormatException exception) {
            throw new IOException("--memory-max needs a number of megabytes, for example --memory-max 8192.");
        }
        try {
            return new LaunchOptions(Math.min(base.minimumMemoryMb(), maximum), maximum,
                    base.customResolution(), base.width(), base.height(), false,
                    base.recommendedJvmFlags(), base.versionType());
        } catch (IllegalArgumentException exception) {
            throw new IOException(exception.getMessage());
        }
    }

    static Map<String, String> parse(String[] args) throws IOException {
        Map<String, String> options = new LinkedHashMap<>();
        List<String> remaining = new ArrayList<>(List.of(args));
        while (!remaining.isEmpty()) {
            String flag = remaining.remove(0);
            if (!flag.startsWith("--")) {
                throw new IOException("Unexpected argument: " + flag);
            }
            if (VALUE_FLAGS.contains(flag)) {
                if (remaining.isEmpty()) {
                    throw new IOException("Missing value for " + flag + ".");
                }
                options.put(flag, remaining.remove(0));
            } else {
                options.put(flag, "true");
            }
        }
        return options;
    }

    private static void printUsage(ModuleContext context) {
        context.console().writeLine("""
                CT command line mode

                  --version <id>        Any official Minecraft version ID; defaults to the latest release
                  --game-dir <path>     Game directory override; default is the one configured in CT settings
                  --player <name>       Offline profile name, default CTPlayer
                  --memory-max <mb>     Maximum heap size in megabytes
                  --verify              Re-check every installed file against the official SHA-1
                  --install-only        Install and verify the game, then stop
                  --dry-run             Print the launch command without starting the game
                  --help                Show this message
                """);
    }
}
