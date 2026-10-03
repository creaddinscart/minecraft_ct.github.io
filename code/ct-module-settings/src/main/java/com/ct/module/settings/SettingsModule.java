package com.ct.module.settings;

import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;
import com.ct.main.api.SettingsAccess;
import java.io.IOException;

public final class SettingsModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) throws IOException {
        SettingsStore store = new SettingsStore(context.applicationDirectory());
        context.publishService(SettingsAccess.class, store);
        context.registerView("Settings", new SettingsPanel(store));
        context.console().writeLine("Settings loaded from "
                + context.applicationDirectory().resolve("settings.properties") + ".");
    }
}
