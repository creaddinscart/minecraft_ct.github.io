package com.ct.module.settings;

import com.ct.main.api.SettingKeys;
import com.ct.main.api.SettingsAccess;
import com.ct.module.theme.UiKit;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

final class JavaSection extends JPanel {
    private final BrowseField javaExecutable = new BrowseField(24, false);
    private final JCheckBox recommendedFlags = UiKit.checkBox(
            "Apply the recommended JVM flags from Mojang");
    private final SettingsAccess settings;

    JavaSection(SettingsAccess settings) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        this.settings = settings;
        javaExecutable.field().setText(settings.text(SettingKeys.JAVA_EXECUTABLE, ""));
        javaExecutable.field().addActionListener(event -> saveExecutable());
        javaExecutable.field().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                saveExecutable();
            }
        });
        recommendedFlags.setSelected(settings.flag(SettingKeys.RECOMMENDED_JVM_FLAGS, true));
        recommendedFlags.addActionListener(event -> saveFlags());

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(UiKit.caption("Java executable override"), BorderLayout.NORTH);
        top.add(javaExecutable.asComponent(), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(recommendedFlags, BorderLayout.CENTER);
    }

    private void saveExecutable() {
        try {
            settings.setText(SettingKeys.JAVA_EXECUTABLE, javaExecutable.field().getText().trim());
        } catch (java.io.IOException ignored) {
            return;
        }
    }

    private void saveFlags() {
        try {
            settings.setFlag(SettingKeys.RECOMMENDED_JVM_FLAGS, recommendedFlags.isSelected());
        } catch (java.io.IOException ignored) {
            return;
        }
    }
}
