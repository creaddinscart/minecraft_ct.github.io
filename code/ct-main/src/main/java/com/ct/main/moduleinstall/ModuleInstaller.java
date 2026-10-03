package com.ct.main.moduleinstall;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class ModuleInstaller {
    public static final String MANIFEST_FILE = "ct.module.json";
    public static final String MODULES_DIR_ENV = "CT_MODULES_DIR";
    public static final String NO_GUI_ENV = "CT_MODULE_INSTALL_NO_GUI";
    public static final String APPLICATION_DIRECTORY = ".ct-client";
    public static final String POINTER_FILE = "last-modules-dir";
    private static final String MAIN_JAR_PREFIX = "CT-Main";

    private ModuleInstaller() {
    }

    public static void main(String[] args) {
        List<String> lines = new ArrayList<>();
        boolean allowGui = true;
        int status;
        try {
            Options options = Options.parse(args);
            allowGui = !options.noGui();
            if (options.help()) {
                lines.addAll(usage());
                status = 0;
            } else {
                status = run(options, lines);
            }
        } catch (InstallException exception) {
            lines.add(exception.getMessage());
            status = 1;
        } catch (Exception exception) {
            lines.add("Installation failed: " + exception);
            status = 1;
        }
        if (status != 0) {
            lines.add("");
            lines.add("Run this file again with --help to see every option.");
        }
        deliver(lines, status, allowGui);
        System.exit(status);
    }

    static int run(Options options, List<String> lines) throws IOException {
        Path source = options.source() != null ? options.source() : ownJar();
        if (source == null) {
            throw new InstallException("This installer has to run from a module jar: "
                    + "java -jar ct-module-<id>-<version>.jar");
        }
        Path jar = source.toAbsolutePath().normalize();
        if (!Files.isRegularFile(jar)) {
            throw new InstallException("The module jar could not be read: " + jar);
        }
        ModuleInfo info = ModuleInfo.read(jar);
        Resolution resolution = resolveModulesDirectory(options, jar);
        Path target = resolution.directory();
        lines.add("CT module installer");
        lines.add("  module   " + info.label());
        lines.add("  source   " + jar);
        lines.add("  modules  " + target + " (" + resolution.origin() + ")");

        if (options.remove()) {
            return remove(target, info, lines);
        }
        if (options.print()) {
            lines.add("");
            lines.add("Dry run. Nothing was written.");
            lines.add("Run again without --print to install into " + target + ".");
            return 0;
        }
        int status = install(jar, target, info, lines);
        if (status == 0 && options.launch()) {
            launch(target.getParent(), jar.getParent());
        }
        return status;
    }

    private static int install(Path jar, Path target, ModuleInfo info, List<String> lines)
            throws IOException {
        Files.createDirectories(target);
        Path destination = target.resolve(info.fileName());
        long sourceSize = Files.size(jar);
        for (Path existing : jarsOf(target, info.id())) {
            if (existing.equals(jar)) {
                continue;
            }
            if (existing.equals(destination) && Files.size(existing) == sourceSize) {
                continue;
            }
            Files.delete(existing);
            lines.add("  replaced  " + existing.getFileName());
        }

        if (Files.isRegularFile(destination) && Files.size(destination) == sourceSize) {
            lines.add("  current   " + destination.getFileName() + " (" + sourceSize + " bytes)");
        } else {
            Files.copy(jar, destination, StandardCopyOption.REPLACE_EXISTING);
            long copiedSize = Files.size(destination);
            if (copiedSize != sourceSize) {
                throw new InstallException("The copied jar is incomplete ("
                        + copiedSize + " of " + sourceSize + " bytes): " + destination);
            }
            lines.add("  installed " + destination.getFileName() + " (" + copiedSize + " bytes)");
        }

        List<String> present = new ArrayList<>(new LinkedHashSet<>(ModuleInfo.installed(target).stream()
                .map(ModuleInfo::id).toList()));
        present.sort(Comparator.naturalOrder());
        lines.add("");
        lines.add("Modules in the folder (" + present.size() + "): " + String.join(", ", present));
        List<String> missing = info.requires().stream().filter(require -> !present.contains(require))
                .toList();
        if (!missing.isEmpty()) {
            lines.add("This module still requires: " + String.join(", ", missing));
            lines.add("Download those module jars from the website and run each of them once.");
        } else if (!info.requires().isEmpty()) {
            lines.add("Every module this one requires is installed.");
        }

        Path main = findMainJar(target.getParent(), jar.getParent());
        lines.add("");
        if (main == null) {
            lines.add("Start CT-Main and it loads this module automatically.");
        } else {
            lines.add("Start CT-Main and it loads this module automatically:");
            lines.add("  java -jar " + main.getFileName());
        }
        return 0;
    }

    private static void launch(Path... directories) throws IOException {
        Path main = findMainJar(directories);
        if (main == null) {
            return;
        }
        new ProcessBuilder(javaCommand(), "-jar", main.toString()).inheritIO().start();
    }

    private static String javaCommand() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "java.exe" : "java";
        Path binary = Path.of(System.getProperty("java.home"), "bin", executable);
        return Files.isRegularFile(binary) ? binary.toString() : "java";
    }

    private static int remove(Path target, ModuleInfo info, List<String> lines) throws IOException {
        List<Path> removed = jarsOf(target, info.id());
        lines.add("");
        if (removed.isEmpty()) {
            lines.add("Nothing to remove: module '" + info.id() + "' is not installed in " + target + ".");
            return 0;
        }
        for (Path jar : removed) {
            Files.delete(jar);
            lines.add("  removed " + jar.getFileName());
        }
        lines.add("Module '" + info.id() + "' is uninstalled. CT-Main stops loading it on the next start.");
        return 0;
    }

    private static List<Path> jarsOf(Path modulesDirectory, String id) throws IOException {
        if (!Files.isDirectory(modulesDirectory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(modulesDirectory)) {
            return files.filter(ModuleInstaller::isModuleJar)
                    .filter(path -> belongsTo(path, id))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    private static boolean belongsTo(Path jar, String id) {
        ModuleInfo info = ModuleInfo.readQuietly(jar);
        if (info != null) {
            return info.id().equals(id);
        }
        return jar.getFileName().toString().startsWith("ct-module-" + id + "-");
    }

    static Resolution resolveModulesDirectory(Options options, Path sourceJar) {
        if (options.modulesDirectory() != null) {
            return new Resolution(options.modulesDirectory().toAbsolutePath().normalize(),
                    "from --modules-dir");
        }
        String environment = System.getenv(MODULES_DIR_ENV);
        if (environment != null && !environment.isBlank()) {
            return new Resolution(Path.of(environment).toAbsolutePath().normalize(),
                    "from " + MODULES_DIR_ENV);
        }
        for (Path anchor : anchors(sourceJar)) {
            Path candidate = adjacentModulesDirectory(anchor);
            if (candidate != null) {
                return new Resolution(candidate, "next to " + anchor.getFileName());
            }
        }
        Path recorded = recordedModulesDirectory();
        if (recorded != null) {
            return new Resolution(recorded, "recorded by CT-Main");
        }
        return new Resolution(applicationDirectory().resolve("modules"),
                "the default CT folder");
    }

    record Resolution(Path directory, String origin) {
    }

    private static List<Path> anchors(Path sourceJar) {
        Set<Path> anchors = new LinkedHashSet<>();
        Path parent = sourceJar.getParent();
        if (parent != null) {
            anchors.add(parent);
        }
        anchors.add(Path.of("").toAbsolutePath().normalize());
        return List.copyOf(anchors);
    }

    private static Path adjacentModulesDirectory(Path directory) {
        if (hasMainJar(directory)) {
            return directory.resolve("modules").toAbsolutePath().normalize();
        }
        Path modules = directory.resolve("modules");
        if (Files.isDirectory(modules)) {
            return modules.toAbsolutePath().normalize();
        }
        return null;
    }

    private static boolean hasMainJar(Path directory) {
        return findMainJar(directory) != null;
    }

    static Path findMainJar(Path... directories) {
        for (Path directory : directories) {
            if (directory == null || !Files.isDirectory(directory)) {
                continue;
            }
            try (Stream<Path> files = Files.list(directory)) {
                Path main = files.filter(path -> {
                    String name = path.getFileName().toString();
                    return Files.isRegularFile(path) && name.startsWith(MAIN_JAR_PREFIX)
                            && name.endsWith(".jar");
                }).sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .findFirst().orElse(null);
                if (main != null) {
                    return main;
                }
            } catch (IOException ignored) {
                continue;
            }
        }
        return null;
    }

    static Path recordedModulesDirectory() {
        Path pointer = applicationDirectory().resolve(POINTER_FILE);
        if (!Files.isRegularFile(pointer)) {
            return null;
        }
        try {
            String text = Files.readString(pointer, StandardCharsets.UTF_8).trim();
            if (text.isEmpty()) {
                return null;
            }
            return Path.of(text).toAbsolutePath().normalize();
        } catch (Exception exception) {
            return null;
        }
    }

    static Path applicationDirectory() {
        return Path.of(System.getProperty("user.home"), APPLICATION_DIRECTORY)
                .toAbsolutePath().normalize();
    }

    static boolean isModuleJar(Path file) {
        return Files.isRegularFile(file) && file.getFileName().toString().endsWith(".jar")
                && !file.getFileName().toString().startsWith(".");
    }

    private static Path ownJar() {
        try {
            Path location = Path.of(ModuleInstaller.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI());
            if (Files.isRegularFile(location) && isJar(location)) {
                return location;
            }
        } catch (Exception | LinkageError ignored) {
            return null;
        }
        return null;
    }

    private static boolean isJar(Path file) {
        try (JarFile ignored = new JarFile(file.toFile())) {
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static List<String> usage() {
        List<String> lines = new ArrayList<>();
        lines.add("CT module installer");
        lines.add("");
        lines.add("Every CT feature module is a runnable jar. Run it once and it copies itself into");
        lines.add("the modules folder that CT-Main scans, so CT-Main loads this feature next start.");
        lines.add("");
        lines.add("  java -jar ct-module-<id>-<version>.jar              install this module");
        lines.add("  java -jar ct-module-<id>-<version>.jar --print      report without writing");
        lines.add("  java -jar ct-module-<id>-<version>.jar --remove     uninstall this module");
        lines.add("  java -jar ct-module-<id>-<version>.jar --launch     install, then start CT-Main");
        lines.add("");
        lines.add("Options");
        lines.add("  --modules-dir <path>  Modules folder to install into.");
        lines.add("  --print               Report the target folder and exit without writing.");
        lines.add("  --remove              Delete every installed jar of this module.");
        lines.add("  --launch              Start the CT-Main jar found next to this file.");
        lines.add("  --no-gui              Never open the result window; print to the console.");
        lines.add("  --help                Show this message.");
        lines.add("");
        lines.add("Folder resolution order");
        lines.add("  1. --modules-dir <path>");
        lines.add("  2. the " + MODULES_DIR_ENV + " environment variable");
        lines.add("  3. a modules folder next to this jar or next to a CT-Main jar");
        lines.add("  4. the folder CT-Main recorded in ~/" + APPLICATION_DIRECTORY + "/" + POINTER_FILE);
        lines.add("  5. ~/" + APPLICATION_DIRECTORY + "/modules");
        return lines;
    }

    private static void deliver(List<String> lines, int status, boolean allowGui) {
        boolean canGui = allowGui && System.console() == null && System.getenv(NO_GUI_ENV) == null
                && System.getenv("TERM") == null && !GraphicsEnvironment.isHeadless();
        for (String line : lines) {
            System.out.println(line);
        }
        if (!canGui) {
            return;
        }
        try {
            javax.swing.JTextArea area = new javax.swing.JTextArea(String.join("\n", lines), 18, 74);
            area.setEditable(false);
            area.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
            area.setCaretPosition(0);
            javax.swing.JOptionPane.showMessageDialog(null, new javax.swing.JScrollPane(area),
                    status == 0 ? "CT module installer" : "CT module installer — failed",
                    status == 0 ? javax.swing.JOptionPane.INFORMATION_MESSAGE
                            : javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException | Error ignored) {
            return;
        }
    }

    record Options(boolean help, boolean print, boolean remove, boolean launch, boolean noGui,
            Path modulesDirectory, Path source) {
        static Options parse(String[] args) {
            boolean help = false;
            boolean print = false;
            boolean remove = false;
            boolean launch = false;
            boolean noGui = false;
            Path modulesDirectory = null;
            Path source = null;
            for (int index = 0; index < args.length; index++) {
                String argument = args[index];
                switch (argument) {
                    case "--help", "-h" -> help = true;
                    case "--print" -> print = true;
                    case "--remove" -> remove = true;
                    case "--launch" -> launch = true;
                    case "--no-gui" -> noGui = true;
                    case "--modules-dir" -> {
                        if (index + 1 >= args.length) {
                            throw new InstallException("--modules-dir needs a path.");
                        }
                        modulesDirectory = Path.of(args[++index]);
                    }
                    case "--source" -> {
                        if (index + 1 >= args.length) {
                            throw new InstallException("--source needs a jar path.");
                        }
                        source = Path.of(args[++index]);
                    }
                    default -> {
                        if (argument.startsWith("--modules-dir=")) {
                            modulesDirectory = Path.of(argument.substring("--modules-dir=".length()));
                        } else if (argument.startsWith("--source=")) {
                            source = Path.of(argument.substring("--source=".length()));
                        } else if (!argument.isBlank()) {
                            throw new InstallException("Unknown option: " + argument);
                        }
                    }
                }
            }
            return new Options(help, print, remove, launch, noGui, modulesDirectory, source);
        }
    }

    static final class InstallException extends RuntimeException {
        InstallException(String message) {
            super(message);
        }
    }
}
