package com.ct.main;

import com.ct.main.api.CommandProvider;
import com.ct.main.api.ModuleContext;
import com.ct.main.core.AccountHub;
import com.ct.main.core.ApplicationPaths;
import com.ct.main.core.ConsoleHub;
import com.ct.main.core.ModuleCatalog;
import com.ct.main.core.ModuleHost;
import com.ct.main.core.ModuleLoader;
import com.ct.main.core.ServiceHub;
import com.ct.main.ui.HostWindow;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.List;
import javax.swing.SwingUtilities;

public final class Main {
    public static final String VERSION = "4.0.0";

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        Path modulesOverride = parseModulesDirectory(args);
        ApplicationPaths paths = ApplicationPaths.resolve(modulesOverride);
        ConsoleHub console = new ConsoleHub();
        AccountHub accounts = new AccountHub();
        ServiceHub services = new ServiceHub();
        ModuleContext context = new ModuleContext(VERSION, paths, console, accounts, services);

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

        String[] commandArgs = remainingArgs(args);
        if (commandArgs.length > 0) {
            dispatchCommand(commandArgs, context, host);
            host.close();
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            console.writeLine("No display is available. Start CT-Main from a desktop session, "
                    + "or run a command: java -jar CT-Main.jar ct --help");
            host.close();
            return;
        }
        SwingUtilities.invokeLater(() -> new HostWindow(context, host).show());
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
                "  module " + module.manifest().id() + " (" + module.manifest().name() + ")"));
        host.skipped().forEach(module -> console.writeLine(
                "  skipped " + module.id() + ": " + module.reason()));
        if (host.loaded().isEmpty()) {
            console.writeLine("This is the bare CT-Main. Every feature is a module you download "
                    + "from " + com.ct.main.api.Website.HOME + " into the modules folder.");
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
