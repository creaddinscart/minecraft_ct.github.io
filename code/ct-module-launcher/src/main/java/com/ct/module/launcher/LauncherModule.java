package com.ct.module.launcher;

import com.ct.main.api.BootstrapAccess;
import com.ct.main.api.ConsoleWriter;
import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;
import com.ct.main.api.SettingsAccess;
import com.ct.module.installer.GameInstaller;
import java.nio.file.Path;
import java.util.Optional;

public final class LauncherModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) throws Exception {
        ConsoleWriter console = context.console();
        GameInstaller installer = context.service(GameInstaller.class)
                .orElseThrow(() -> new IllegalStateException(
                        "The launcher module requires the installer module."));
        SettingsAccess settings = context.service(SettingsAccess.class)
                .orElseThrow(() -> new IllegalStateException(
                        "The launcher module requires the settings module."));
        Optional<Path> loaderBootstrap = context.service(BootstrapAccess.class)
                .flatMap(BootstrapAccess::bootstrapJar);

        LaunchService service = new LaunchService(installer, settings, loaderBootstrap,
                context.mainVersion());
        context.publishService(LaunchService.class, service);
        context.registerView("Launch", new LaunchPanel(service, context.accounts(),
                context.service(com.ct.module.versions.VersionSelection.class).orElse(null),
                console));
        console.writeLine("Launcher module ready. Fabric bootstrap "
                + loaderBootstrap.map(Path::toString).orElse("not present") + ".");
    }
}
