package com.ct.main.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

final class InstanceLockTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void noLockMeansNoRunningInstance() {
        assertFalse(InstanceLock.isRunning(temporaryDirectory, temporaryDirectory.resolve("modules")));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void anotherProcessOnTheSameModulesFolderIsRunning() throws IOException, InterruptedException {
        Process other = new ProcessBuilder("sleep", "30").start();
        try {
            Path modules = temporaryDirectory.resolve("modules");
            writeLock(other.pid(), modules);

            assertTrue(InstanceLock.isRunning(temporaryDirectory, modules));
            assertFalse(InstanceLock.isRunning(temporaryDirectory, temporaryDirectory.resolve("other")));
        } finally {
            other.destroyForcibly();
        }
    }

    @Test
    void aDeadProcessIsNotRunning() throws IOException {
        Path modules = temporaryDirectory.resolve("modules");
        writeLock(999_999_999L, modules);

        assertFalse(InstanceLock.isRunning(temporaryDirectory, modules));
    }

    @Test
    void aBrokenLockFileIsIgnored() throws IOException {
        Files.writeString(temporaryDirectory.resolve(InstanceLock.LOCK_FILE), "not a lock",
                StandardCharsets.UTF_8);

        assertFalse(InstanceLock.isRunning(temporaryDirectory, temporaryDirectory.resolve("modules")));
    }

    @Test
    void acquireWritesTheRunningInstance() throws IOException {
        Path modules = temporaryDirectory.resolve("modules");

        InstanceLock.acquire(temporaryDirectory, modules);

        String lock = Files.readString(temporaryDirectory.resolve(InstanceLock.LOCK_FILE),
                StandardCharsets.UTF_8);
        assertTrue(lock.contains("modules=" + modules));
        assertTrue(lock.contains("pid=" + ProcessHandle.current().pid()));
        assertFalse(InstanceLock.isRunning(temporaryDirectory, modules));
    }

    private void writeLock(long pid, Path modules) throws IOException {
        Files.writeString(temporaryDirectory.resolve(InstanceLock.LOCK_FILE),
                "pid=" + pid + "\nmodules=" + modules + "\n", StandardCharsets.UTF_8);
    }
}
