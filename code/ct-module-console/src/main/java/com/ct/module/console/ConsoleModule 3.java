package com.ct.module.console;

import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;
import com.ct.main.core.ConsoleHub;

public final class ConsoleModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        ConsolePanel panel = new ConsolePanel();
        if (context.console() instanceof ConsoleHub hub) {
            hub.addSink(panel::appendLine);
        }
        context.registerView("Console", panel);
    }
}
