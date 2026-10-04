package com.ct.module.accountmicrosoft;

import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;
import com.ct.main.api.SettingsAccess;

public final class MicrosoftAccountModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        SettingsAccess settings = context.service(SettingsAccess.class)
                .orElseThrow(() -> new IllegalStateException(
                        "The Microsoft account module requires the settings module."));
        context.registerView("Microsoft account", new MicrosoftAccountPanel(settings,
                context.accounts(), context.console()));
        context.console().writeLine("Microsoft account module ready.");
    }
}
