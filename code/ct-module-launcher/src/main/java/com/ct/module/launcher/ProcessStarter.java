package com.ct.module.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public final class ProcessStarter {
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ProcessStarter() {
    }

    public static Process start(List<String> command, Path gameDirectory) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(gameDirectory.toFile());
        builder.redirectErrorStream(true);
        try {
            return builder.start();
        } catch (IOException exception) {
            throw new IOException("The Minecraft process could not be started.", exception);
        }
    }

    public static void forwardOutput(Process process, Path launchLog, Consumer<String> console) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = Files.newBufferedWriter(launchLog, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write("---- launch " + LocalDateTime.now().format(TIMESTAMP) + " ----");
            writer.newLine();
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();
                console.accept(line);
            }
        } catch (IOException exception) {
            console.accept("Game output could not be read: " + exception.getMessage());
        }
    }

    public static Path launchLog(Path gameDirectory) throws IOException {
        Path logsDirectory = gameDirectory.resolve("logs");
        Files.createDirectories(logsDirectory);
        return logsDirectory.resolve("ct-client-launch.log");
    }
}
