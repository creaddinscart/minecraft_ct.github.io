package com.ct.module.settings;

import com.ct.main.api.SettingsAccess;
import com.ct.module.theme.UiKit;
import java.awt.BorderLayout;
import java.nio.file.Path;
import javax.swing.JPanel;

final class DirectorySection extends JPanel {
    private final BrowseField gameDirectory = new BrowseField(24, true);

    DirectorySection(SettingsAccess settings) {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        gameDirectory.field().setText(settings.text(com.ct.main.api.SettingKeys.GAME_DIRECTORY, ""));
        gameDirectory.field().addActionListener(event -> save(settings));
        gameDirectory.field().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                save(settings);
            }
        });
        add(UiKit.caption("Game directory"), BorderLayout.NORTH);
        add(gameDirectory.asComponent(), BorderLayout.CENTER);
    }

    private void save(SettingsAccess settings) {
        try {
            String value = gameDirectory.field().getText().trim();
            settings.setText(com.ct.main.api.SettingKeys.GAME_DIRECTORY, value);
        } catch (java.io.IOException ignored) {
            return;
        }
    }

    Path currentDirectory() {
        return Path.of(gameDirectory.field().getText().trim());
    }
}
