package com.ct.module.installer;

import com.ct.main.api.CtModule;
import com.ct.main.api.GameDirectoryResolver;
import com.ct.main.api.ModuleContext;
import com.ct.main.api.SettingsAccess;
import java.nio.file.Path;
import java.util.Optional;

public final class InstallerModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        Optional<SettingsAccess> settings = context.service(SettingsAccess.class);
        if (settings.isEmpty()) {
            throw new IllegalStateException("The installer module requires the settings module.");
        }
        Path gameDirectory = GameDirectoryResolver.resolve(settings.get());
        GameInstaller installer = new MinecraftInstaller(gameDirectory);
        context.publishService(GameInstaller.class, installer);
        context.console().writeLine("Minecraft installer ready for game directory " + gameDirectory + ".");
    }
}
