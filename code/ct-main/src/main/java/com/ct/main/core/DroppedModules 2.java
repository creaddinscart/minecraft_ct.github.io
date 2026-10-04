package com.ct.main.core;

import com.ct.main.moduleinstall.ModuleInstaller;
import com.ct.main.moduleinstall.ModuleInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DroppedModules {
    public record Report(List<String> lines, int installed, int skipped) {
        public Report {
            lines = List.copyOf(lines);
        }

        public boolean changed() {
            return installed > 0;
        }

        public boolean failed() {
            return installed == 0 && skipped > 0;
        }
    }

    private DroppedModules() {
    }

    public static Report install(List<Path> sources, Path modulesDirectory) {
        return install(sources, modulesDirectory, null, false);
    }

    public static Report install(List<Path> sources, Path modulesDirectory, String version,
            boolean fetchMissing) {
        List<String> lines = new ArrayList<>();
        int installed = 0;
        int skipped = 0;
        lines.add("Installing dropped modules into " + modulesDirectory);
        for (Path source : sources) {
            if (!Files.exists(source)) {
                lines.add("  not found  " + source);
                skipped++;
                continue;
            }
            List<Path> jars;
            try {
                jars = ModuleInstaller.moduleJars(source);
            } catch (IOException exception) {
                lines.add("  unreadable " + source.getFileName() + " — " + exception.getMessage());
                skipped++;
                continue;
            }
            if (jars.isEmpty()) {
                lines.add("  skipped    " + source.getFileName()
                        + " — the folder holds no CT module jar");
                skipped++;
                continue;
            }
            for (Path jar : jars) {
                if (!ModuleInstaller.holdsManifest(jar)) {
                    lines.add("  skipped    " + jar.getFileName() + " — not a CT module jar");
                    skipped++;
                    continue;
                }
                try {
                    ModuleInstaller.Result result = ModuleInstaller.installModule(jar, modulesDirectory);
                    for (Path replaced : result.replaced()) {
                        lines.add("  replaced   " + replaced.getFileName());
                    }
                    ModuleInfo module = result.module();
                    lines.add("  " + (result.copied() ? "installed " : "current   ")
                            + result.installed().getFileName() + " — " + module.label() + " ("
                            + Files.size(result.installed()) + " bytes)");
                    installed++;
                } catch (Exception exception) {
                    lines.add("  failed     " + jar.getFileName() + " — " + exception.getMessage());
                    skipped++;
                }
            }
        }
        if (fetchMissing && version != null && !version.isBlank()) {
            List<String> missing = ModuleFetch.missingModules(modulesDirectory);
            if (!missing.isEmpty()) {
                lines.add("Required by the modules above: " + String.join(", ", missing));
                for (ModuleFetch.Result result : ModuleFetch.fetchMissing(modulesDirectory, version,
                        lines)) {
                    if (result.succeeded()) {
                        installed++;
                    }
                }
            }
        }
        lines.add("Installed " + installed + " module(s)"
                + (skipped == 0 ? "." : ", skipped " + skipped + "."));
        return new Report(lines, installed, skipped);
    }
}
