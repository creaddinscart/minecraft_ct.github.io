package com.ct.main.core;

import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public final class ModuleHost implements AutoCloseable {
    private final List<LoadedModule> loaded = new ArrayList<>();
    private final List<SkippedModule> skipped = new ArrayList<>();
    private final URLClassLoader classLoader;

    private ModuleHost(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static ModuleHost start(ModuleCatalog catalog, ModuleContext context) {
        List<URL> urls = new ArrayList<>();
        for (ModuleCandidate candidate : catalog.ordered()) {
            urls.add(toUrl(candidate.jar()));
        }
        URLClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new),
                ModuleHost.class.getClassLoader());
        ModuleHost host = new ModuleHost(loader);
        host.skipped.addAll(catalog.skipped());
        for (ModuleCandidate candidate : catalog.ordered()) {
            ModuleManifest manifest = candidate.manifest();
            try {
                Class<?> type = Class.forName(manifest.entrypoint(), true, loader);
                if (!CtModule.class.isAssignableFrom(type)) {
                    throw new IOException("Module entrypoint must implement " + CtModule.class.getName()
                            + ": " + manifest.id());
                }
                CtModule module = (CtModule) type.getConstructor().newInstance();
                module.initialize(context);
                host.loaded.add(new LoadedModule(manifest, candidate.jar(), module));
            } catch (Exception | LinkageError exception) {
                host.skipped.add(new SkippedModule(manifest.id(), candidate.jar(),
                        "initialization failed: " + exception.getMessage()));
            }
        }
        return host;
    }

    private static URL toUrl(java.nio.file.Path jar) {
        try {
            return jar.toUri().toURL();
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid module jar path: " + jar, exception);
        }
    }

    public List<LoadedModule> loaded() {
        return List.copyOf(loaded);
    }

    public List<SkippedModule> skipped() {
        return List.copyOf(skipped);
    }

    public <T> List<T> instancesOf(Class<T> type) {
        List<T> instances = new ArrayList<>();
        for (LoadedModule module : loaded) {
            if (type.isInstance(module.instance())) {
                instances.add(type.cast(module.instance()));
            }
        }
        return instances;
    }

    @Override
    public void close() throws IOException {
        classLoader.close();
    }
}
