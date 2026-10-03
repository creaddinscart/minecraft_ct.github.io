package com.ct.module.settings;

import com.ct.main.api.SettingKeys;
import com.ct.main.api.SettingsAccess;
import com.ct.module.theme.UiKit;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class ResolutionSection extends JPanel {
    private final JCheckBox custom = UiKit.checkBox("Custom resolution");
    private final JTextField width = UiKit.field(4);
    private final JTextField height = UiKit.field(4);
    private final SettingsAccess settings;

    ResolutionSection(SettingsAccess settings) {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        this.settings = settings;
        custom.setSelected(settings.flag(SettingKeys.CUSTOM_RESOLUTION, false));
        width.setText(Integer.toString(settings.number(SettingKeys.RESOLUTION_WIDTH, 854)));
        height.setText(Integer.toString(settings.number(SettingKeys.RESOLUTION_HEIGHT, 480)));
        custom.addActionListener(event -> {
            updateEnabled();
            save();
        });
        width.addActionListener(event -> save());
        height.addActionListener(event -> save());
        updateEnabled();

        JPanel row = UiKit.row(8);
        row.add(custom);
        row.add(width);
        row.add(UiKit.muted("x"));
        row.add(height);
        add(UiKit.caption("Window size"), BorderLayout.NORTH);
        add(row, BorderLayout.CENTER);
    }

    private void updateEnabled() {
        width.setEnabled(custom.isSelected());
        height.setEnabled(custom.isSelected());
    }

    private void save() {
        try {
            settings.setFlag(SettingKeys.CUSTOM_RESOLUTION, custom.isSelected());
            settings.setNumber(SettingKeys.RESOLUTION_WIDTH, parse(width));
            settings.setNumber(SettingKeys.RESOLUTION_HEIGHT, parse(height));
        } catch (java.io.IOException ignored) {
            return;
        }
    }

    private static int parse(JTextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
