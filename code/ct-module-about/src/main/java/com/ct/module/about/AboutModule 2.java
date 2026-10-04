package com.ct.module.about;

import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;

public final class AboutModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        context.registerView("About", new AboutPanel(context));
        context.console().writeLine("About view registered.");
    }
}
