package com.ct.module.modman;

import com.ct.main.api.CtModule;
import com.ct.main.api.GameDirectoryResolver;
import com.ct.main.api.ModuleContext;
import com.ct.main.api.SettingsAccess;
import java.nio.file.Path;

public final class ModManagerModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        SettingsAccess settings = context.service(SettingsAccess.class)
                .orElseThrow(() -> new IllegalStateException(
                        "The mod manager module requires the settings module."));
        Path gameDirectory = GameDirectoryResolver.resolve(settings);
        context.registerView("Mods", new ModManagerPanel(gameDirectory));
        context.console().writeLine("Mod manager ready for game directory " + gameDirectory + ".");
    }
}
