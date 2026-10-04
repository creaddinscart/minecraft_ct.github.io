package com.ct.module.rules;

import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;

public final class RulesModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        context.console().writeLine("Rule engine ready for "
                + PlatformInfo.operatingSystem() + " / " + PlatformInfo.architecture() + ".");
    }
}
