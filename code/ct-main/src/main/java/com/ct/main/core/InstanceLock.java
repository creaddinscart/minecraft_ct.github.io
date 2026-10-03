package com.ct.main.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class InstanceLock {
    public static final String LOCK_FILE = "instance.lock";

    private InstanceLock() {
    }

    public static void acquire(Path applicationDirectory, Path modulesDirectory) {
        Path lock = applicationDirectory.resolve(LOCK_FILE);
        long pid = ProcessHandle.current().pid();
        try {
            Files.writeString(lock, "pid=" + pid + "\nmodules=" + modulesDirectory + "\n",
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> release(lock, pid)));
    }

    public static boolean isRunning(Path applicationDirectory, Path modulesDirectory) {
        Path lock = applicationDirectory.resolve(LOCK_FILE);
        if (!Files.isRegularFile(lock)) {
            return false;
        }
        long pid = 0;
        String modules = "";
        try {
            List<String> lines = Files.readAllLines(lock, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.startsWith("pid=")) {
                    pid = Long.parseLong(line.substring("pid=".length()).trim());
                } else if (line.startsWith("modules=")) {
                    modules = line.substring("modules=".length()).trim();
                }
            }
        } catch (Exception exception) {
            return false;
        }
        if (pid <= 0 || pid == ProcessHandle.current().pid()) {
            return false;
        }
        if (modules.isEmpty() || !Path.of(modules).toAbsolutePath().normalize()
                .equals(modulesDirectory.toAbsolutePath().normalize())) {
            return false;
        }
        return ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
    }

    private static void release(Path lock, long pid) {
        try {
            if (!Files.isRegularFile(lock)) {
                return;
            }
            for (String line : Files.readAllLines(lock, StandardCharsets.UTF_8)) {
                if (line.startsWith("pid=")
                        && line.substring("pid=".length()).trim().equals(Long.toString(pid))) {
                    Files.delete(lock);
                    return;
                }
            }
        } catch (Exception ignored) {
            return;
        }
    }
}
