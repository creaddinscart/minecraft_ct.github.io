package com.ct.module.settings;

import com.ct.main.api.LaunchOptions;
import com.ct.main.api.SettingKeys;
import com.ct.main.api.SettingsAccess;
import com.ct.module.theme.UiKit;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

final class MemorySection extends JPanel {
    private final JSpinner minimum = UiKit.spinner(new SpinnerNumberModel(
            2048, LaunchOptions.MINIMUM_MEMORY_MB, LaunchOptions.MAXIMUM_MEMORY_MB, 512));
    private final JSpinner maximum = UiKit.spinner(new SpinnerNumberModel(
            4096, LaunchOptions.MINIMUM_MEMORY_MB, LaunchOptions.MAXIMUM_MEMORY_MB, 512));
    private final SettingsAccess settings;

    MemorySection(SettingsAccess settings) {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        this.settings = settings;
        minimum.setValue(settings.number(SettingKeys.MEMORY_MINIMUM, 2048));
        maximum.setValue(settings.number(SettingKeys.MEMORY_MAXIMUM, 4096));
        minimum.addChangeListener(event -> save());
        maximum.addChangeListener(event -> save());

        JPanel row = UiKit.row(8);
        row.add(minimum);
        row.add(UiKit.muted("MB min"));
        row.add(Box.createHorizontalStrut(6));
        row.add(maximum);
        row.add(UiKit.muted("MB max"));
        row.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        add(UiKit.caption("Memory"), BorderLayout.NORTH);
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(row);
        add(list, BorderLayout.CENTER);
    }

    private void save() {
        try {
            settings.setNumber(SettingKeys.MEMORY_MINIMUM, (Integer) minimum.getValue());
            settings.setNumber(SettingKeys.MEMORY_MAXIMUM, (Integer) maximum.getValue());
        } catch (java.io.IOException ignored) {
            return;
        }
    }
}
