package com.ct.module.account.offline;

import com.ct.main.api.Account;
import com.ct.main.api.CtModule;
import com.ct.main.api.ModuleContext;
import com.ct.main.api.SettingsAccess;
import java.util.Optional;

public final class OfflineAccountModule implements CtModule {
    private static final String OFFLINE_NAME_KEY = "offline.playerName";

    @Override
    public void initialize(ModuleContext context) {
        Optional<SettingsAccess> settings = context.service(SettingsAccess.class);
        OfflineProfilePanel panel = new OfflineProfilePanel(context.accounts(), settings);
        settings.flatMap(access -> Optional.ofNullable(blankToNull(access.text(OFFLINE_NAME_KEY, ""))))
                .ifPresent(name -> {
                    try {
                        context.accounts().setCurrent(Account.offline(name));
                    } catch (IllegalArgumentException ignored) {
                        return;
                    }
                });
        context.registerView("Offline profile", panel);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
