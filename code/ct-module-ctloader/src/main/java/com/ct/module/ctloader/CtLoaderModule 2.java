package com.ct.module.ctloader;

import com.ct.main.api.BootstrapAccess;
import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

public final class CtLoaderModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        context.publishService(BootstrapAccess.class, () -> Optional.ofNullable(ownJar()));
        context.console().writeLine("CT mod loader ready. CT-native launches boot "
                + CTLoaderBootstrap.class.getName() + " from " + ownJarPathLabel() + ".");
    }

    private static String ownJarPathLabel() {
        Path jar = ownJar();
        return jar == null ? "the ctloader module jar" : jar.toString();
    }

    private static Path ownJar() {
        try {
            URL location = CtLoaderModule.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null || !"file".equals(location.getProtocol())) {
                return null;
            }
            Path jar = Path.of(location.toURI());
            return jar.getFileName().toString().endsWith(".jar") ? jar : null;
        } catch (URISyntaxException | RuntimeException exception) {
            return null;
        }
    }
}
