package com.ct.module.settings;

import com.ct.main.api.SettingsAccess;
import com.ct.module.theme.UiKit;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;

final class SettingsPanel extends JPanel {
    SettingsPanel(SettingsAccess settings) {
        super(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel card = UiKit.card("Settings");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx = 1;

        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.insets = new Insets(0, 0, 14, 14);
        grid.add(new DirectorySection(settings), constraints);
        constraints.gridx = 1;
        constraints.insets = new Insets(0, 0, 14, 0);
        grid.add(new MemorySection(settings), constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.insets = new Insets(0, 0, 14, 14);
        grid.add(new ResolutionSection(settings), constraints);
        constraints.gridx = 1;
        constraints.insets = new Insets(0, 0, 14, 0);
        grid.add(new JavaSection(settings), constraints);

        card.add(grid, BorderLayout.CENTER);
        add(card, BorderLayout.NORTH);
    }
}
