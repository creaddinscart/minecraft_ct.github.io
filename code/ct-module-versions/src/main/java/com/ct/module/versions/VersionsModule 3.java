package com.ct.module.versions;

import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;
import com.ct.module.installer.GameInstaller;

public final class VersionsModule implements CtModule {
    @Override
    public void initialize(ModuleContext context) {
        GameInstaller installer = context.service(GameInstaller.class)
                .orElseThrow(() -> new IllegalStateException(
                        "The versions module requires the installer module."));
        VersionSelection selection = new VersionSelection();
        BusyState busy = new BusyState();
        context.publishService(VersionSelection.class, selection);
        context.registerView("Versions", new VersionsPanel(installer, selection, busy));
    }
}
