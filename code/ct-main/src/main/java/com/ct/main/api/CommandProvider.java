package com.ct.main.api;

public interface CommandProvider {
    String command();

    String description();

    boolean handle(String[] args, ModuleContext context) throws Exception;
}
