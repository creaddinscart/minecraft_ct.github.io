package com.ct.main.core;

import com.ct.main.Main;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Relaunch {
    private Relaunch() {
    }

    public static Path ownJar() {
        try {
            Path location = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
            if (Files.isRegularFile(location) && location.toString().endsWith(".jar")) {
                return location;
            }
        } catch (Exception | LinkageError ignored) {
            return null;
        }
        return null;
    }

    public static boolean relaunch(List<String> arguments) {
        Path jar = ownJar();
        if (jar == null) {
            return false;
        }
        List<String> command = new ArrayList<>();
        command.add(javaCommand());
        command.add("-jar");
        command.add(jar.toString());
        command.addAll(arguments);
        try {
            new ProcessBuilder(command).inheritIO().start();
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static String javaCommand() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "java.exe" : "java";
        Path binary = Path.of(System.getProperty("java.home"), "bin", executable);
        return Files.isRegularFile(binary) ? binary.toString() : "java";
    }
}
