package com.ct.main;

import com.ct.main.api.CommandProvider;
import com.ct.main.api.ModuleContext;
import com.ct.main.core.AccountHub;
import com.ct.main.core.ApplicationPaths;
import com.ct.main.core.ConsoleHub;
import com.ct.main.core.DroppedModules;
import com.ct.main.core.InstanceLock;
import com.ct.main.core.ModuleCatalog;
import com.ct.main.core.ModuleHost;
import com.ct.main.core.ModuleLoader;
import com.ct.main.core.ServiceHub;
import com.ct.main.moduleinstall.ModuleInstaller;
import com.ct.main.ui.HostWindow;
import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.SwingUtilities;

public final class Main {
    public static final String VERSION = "4.2.0";

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        Path modulesOverride = parseModulesDirectory(args);
        ApplicationPaths paths = ApplicationPaths.resolve(modulesOverride);
        ConsoleHub console = new ConsoleHub();
        AccountHub accounts = new AccountHub();
        ServiceHub services = new ServiceHub();
        ModuleContext context = new ModuleContext(VERSION, paths, console, accounts, services);

        List<Path> dropped = new ArrayList<>();
        List<String> commandArgs = new ArrayList<>();
        boolean commandSeen = false;
        for (String argument : remainingArgs(args)) {
            Path source = commandSeen ? null : moduleSource(argument);
            if (source == null) {
                commandArgs.add(argument);
                commandSeen = true;
            } else {
                dropped.add(source);
            }
        }
        boolean installed = false;
        if (!dropped.isEmpty()) {
            DroppedModules.Report report = DroppedModules.install(dropped, paths.modulesDirectory());
            for (String line : report.lines()) {
                console.writeLine(line);
            }
            if (!ModuleInstaller.hasConsole()) {
                ModuleInstaller.report(report.lines(), report.failed());
            }
            installed = report.changed();
        }

        ModuleCatalog catalog = ModuleLoader.scan(paths.modulesDirectory());
        ModuleHost host = ModuleHost.start(catalog, context);
        printReport(host, console, paths);

        String smoke = System.getProperty("ct.smoke");
        if ("modules".equals(smoke)) {
            host.close();
            return;
        }
        String render = System.getProperty("ct.smoke.render");
        if (render != null) {
            OffscreenRender.renderMainViews(context, host, Path.of(render));
            host.close();
            return;
        }

        if (!commandArgs.isEmpty()) {
            dispatchCommand(commandArgs.toArray(new String[0]), context, host);
            host.close();
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            console.writeLine("No display is available. Start CT-Main from a desktop session, "
                    + "or run a command: java -jar CT-Main.jar ct --help");
            host.close();
            return;
        }
        if (installed && InstanceLock.isRunning(paths.applicationDirectory(), paths.modulesDirectory())) {
            List<String> notice = List.of(
                    "The modules are installed in " + paths.modulesDirectory() + ".",
                    "CT-Main is already running with this modules folder, so this copy stops here.",
                    "Close that window and start CT-Main again to use the new modules.");
            for (String line : notice) {
                console.writeLine(line);
            }
            if (!ModuleInstaller.hasConsole()) {
                ModuleInstaller.report(notice, false);
            }
            host.close();
            return;
        }
        InstanceLock.acquire(paths.applicationDirectory(), paths.modulesDirectory());
        SwingUtilities.invokeLater(() -> new HostWindow(context, host).show());
    }

    private static Path moduleSource(String argument) {
        if (argument.startsWith("-")) {
            return null;
        }
        boolean pathLike = argument.toLowerCase(Locale.ROOT).endsWith(".jar")
                || argument.contains("/") || argument.contains("\\");
        if (!pathLike) {
            return null;
        }
        try {
            return Path.of(argument);
        } catch (InvalidPathException exception) {
            return null;
        }
    }

    private static void dispatchCommand(String[] args, ModuleContext context, ModuleHost host)
            throws Exception {
        for (CommandProvider provider : host.instancesOf(CommandProvider.class)) {
            if (provider.command().equals(args[0])) {
                String[] rest = new String[args.length - 1];
                System.arraycopy(args, 1, rest, 0, rest.length);
                boolean handled = provider.handle(rest, context);
                if (!handled) {
                    System.out.println("The '" + provider.command() + "' module did not accept these arguments.");
                }
                return;
            }
        }
        System.out.println("No module handled the command '" + args[0] + "'.");
        System.out.println("Available commands:");
        for (CommandProvider provider : host.instancesOf(CommandProvider.class)) {
            System.out.println("  " + provider.command() + " — " + provider.description());
        }
        if (host.instancesOf(CommandProvider.class).isEmpty()) {
            System.out.println("  Install the CLI module into the modules folder to use command line mode.");
        }
    }

    private static void printReport(ModuleHost host, ConsoleHub console, ApplicationPaths paths) {
        console.writeLine("CT-Main " + VERSION + " — modules folder: " + paths.modulesDirectory());
        console.writeLine("Loaded " + host.loaded().size() + " module(s), skipped "
                + host.skipped().size() + ".");
        host.loaded().forEach(module -> console.writeLine(
                "  module " + module.manifest().id() + " " + module.manifest().version()
                        + " (" + module.manifest().name() + ")"));
        host.skipped().forEach(module -> console.writeLine(
                "  skipped " + module.id() + ": " + module.reason()));
        if (host.loaded().isEmpty()) {
            console.writeLine("This is the bare CT-Main. Every feature is a module you download "
                    + "from " + com.ct.main.api.Website.HOME + " and run once; the module "
                    + "installs itself into this folder.");
        }
    }

    private static Path parseModulesDirectory(String[] args) {
        for (int index = 0; index < args.length - 1; index++) {
            if ("--modules-dir".equals(args[index])) {
                return Path.of(args[index + 1]);
            }
        }
        return null;
    }

    private static String[] remainingArgs(String[] args) {
        List<String> remaining = new java.util.ArrayList<>();
        for (int index = 0; index < args.length; index++) {
            if ("--modules-dir".equals(args[index])) {
                index++;
                continue;
            }
            remaining.add(args[index]);
        }
        return remaining.toArray(new String[0]);
    }
}
