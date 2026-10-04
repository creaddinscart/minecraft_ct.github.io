package com.ct.module.launcher;

import com.ct.main.api.Account;
import com.ct.main.api.LaunchOptions;
import com.ct.module.installer.Installation;
import com.ct.module.rules.RuleFeatures;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CommandBuilder {
    private final Path gameDirectory;
    private final String launcherVersion;

    public CommandBuilder(Path gameDirectory, String launcherVersion) {
        this.gameDirectory = gameDirectory.toAbsolutePath().normalize();
        this.launcherVersion = launcherVersion;
    }

    public List<String> buildCommand(Installation installation, Account account,
            LaunchOptions options, Optional<Path> loaderBootstrap) throws IOException {
        JsonObject metadata = installation.metadata();
        JsonObject arguments = metadata.getAsJsonObject("arguments");
        if (arguments == null) {
            arguments = new JsonObject();
            arguments.add("jvm", new JsonArray());
            arguments.add("game", LegacyArgumentParser.gameArguments(metadata));
        }

        RuleFeatures features = new RuleFeatures(options.demoMode(), options.customResolution(),
                false, false, false, false);
        String gameClasspath = gameClasspath(installation, loaderBootstrap);
        Map<String, String> placeholders = PlaceholderMap.build(installation, account, options,
                gameDirectory, launcherVersion, gameClasspath);

        List<String> command = new ArrayList<>();
        command.add(installation.runtimeExecutable().toString());
        command.addAll(JvmFlagBuilder.userJvmArguments(arguments, options));
        if (installation.logConfig() != null && metadata.has("logging")) {
            String argument = metadata.getAsJsonObject("logging").getAsJsonObject("client")
                    .get("argument").getAsString();
            command.add(ArgumentExpander.substitute(argument, placeholders));
        }
        command.addAll(ArgumentExpander.expand(arguments.getAsJsonArray("jvm"), features, placeholders));
        if (!command.contains(gameClasspath)) {
            command.add("-cp");
            command.add(gameClasspath);
        }
        String mainClass = metadata.has("mainClass")
                ? metadata.get("mainClass").getAsString()
                : "net.minecraft.client.main.Main";
        if (metadata.has("inheritsFrom")) {
            command.add(mainClass);
        } else if (loaderBootstrap.isPresent()) {
            command.add("com.ct.module.ctloader.CTLoaderBootstrap");
            command.add("--ct-main-class");
            command.add(mainClass);
            command.add("--ct-game-directory");
            command.add(gameDirectory.toString());
            command.add("--");
        } else {
            command.add(mainClass);
        }
        command.addAll(ArgumentExpander.expand(arguments.getAsJsonArray("game"), features, placeholders));

        if (!command.contains(gameClasspath)) {
            throw new IOException("The generated command is missing the game classpath.");
        }
        return command;
    }

    private static String gameClasspath(Installation installation, Optional<Path> loaderBootstrap)
            throws IOException {
        if (installation.metadata().has("inheritsFrom")) {
            return installation.classpath();
        }
        if (loaderBootstrap.isEmpty()) {
            return installation.classpath();
        }
        return installation.classpath() + System.getProperty("path.separator")
                + loaderBootstrap.get();
    }
}
