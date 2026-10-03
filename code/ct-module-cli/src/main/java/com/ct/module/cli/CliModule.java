package com.ct.module.cli;

import com.ct.main.api.CommandProvider;
import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;

public final class CliModule implements CtModule, CommandProvider {
    @Override
    public void initialize(ModuleContext context) {
        context.console().writeLine("Command line module ready. Try: java -jar CT-Main.jar ct --help");
    }

    @Override
    public String command() {
        return "ct";
    }

    @Override
    public String description() {
        return "Install and launch Minecraft from the terminal (ct --help).";
    }

    @Override
    public boolean handle(String[] args, ModuleContext context) throws Exception {
        return CliRunner.run(args, context);
    }
}
