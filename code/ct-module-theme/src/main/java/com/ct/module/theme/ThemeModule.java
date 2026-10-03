package com.ct.module.theme;

import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;

public final class ThemeModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        context.console().writeLine("CT dark interface loaded.");
    }
}
