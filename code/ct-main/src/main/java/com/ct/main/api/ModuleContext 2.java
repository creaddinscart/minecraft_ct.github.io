package com.ct.main.api;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JComponent;

public final class ModuleContext {
    private final String mainVersion;
    private final PathAccess paths;
    private final ConsoleWriter console;
    private final AccountProvider accounts;
    private final List<ViewEntry> views = new ArrayList<>();
    private final ServiceRegistry services;

    public record ViewEntry(String title, JComponent component) {
    }

    public ModuleContext(String mainVersion, PathAccess paths, ConsoleWriter console,
            AccountProvider accounts, ServiceRegistry services) {
        this.mainVersion = mainVersion;
        this.paths = paths;
        this.console = console;
        this.accounts = accounts;
        this.services = services;
    }

    public String mainVersion() {
        return mainVersion;
    }

    public Path applicationDirectory() {
        return paths.applicationDirectory();
    }

    public Path modulesDirectory() {
        return paths.modulesDirectory();
    }

    public ConsoleWriter console() {
        return console;
    }

    public AccountProvider accounts() {
        return accounts;
    }

    public void registerView(String title, JComponent component) {
        views.add(new ViewEntry(title, component));
    }

    public List<ViewEntry> views() {
        return List.copyOf(views);
    }

    public <T> void publishService(Class<T> type, T instance) {
        services.publish(type, instance);
    }

    public <T> Optional<T> service(Class<T> type) {
        return services.lookup(type);
    }

    public void openWebsite() {
        Website.open(null);
    }

    public void openWebsite(String path) {
        Website.open(path);
    }

    public interface PathAccess {
        Path applicationDirectory();

        Path modulesDirectory();
    }

    public interface ServiceRegistry {
        <T> void publish(Class<T> type, T instance);

        <T> Optional<T> lookup(Class<T> type);
    }
}
