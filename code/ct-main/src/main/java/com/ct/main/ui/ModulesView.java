package com.ct.main.ui;

import com.ct.main.api.Website;
import com.ct.main.core.LoadedModule;
import com.ct.main.core.SkippedModule;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

final class ModulesView extends JPanel {
    ModulesView(List<LoadedModule> loaded, List<SkippedModule> skipped) {
        setLayout(new BorderLayout(0, 10));
        setBackground(BareTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Loaded feature modules");
        title.setFont(BareTheme.uiBold(16));
        title.setForeground(BareTheme.TEXT);
        JButton website = new JButton("Get modules on the official website");
        website.setFont(BareTheme.ui(12));
        website.addActionListener(event -> Website.open("/modules.html"));
        header.add(title, BorderLayout.WEST);
        header.add(website, BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (LoadedModule module : loaded) {
            list.add(moduleRow(module.manifest().id() + " — " + module.manifest().name(),
                    module.manifest().description(), BareTheme.TEXT));
        }
        for (SkippedModule module : skipped) {
            list.add(moduleRow(module.id() + " — not loaded", module.reason(), BareTheme.TEXT_MUTED));
        }
        if (loaded.isEmpty() && skipped.isEmpty()) {
            list.add(moduleRow("No modules installed",
                    "Download module jars from " + Website.HOME + " and place them in the modules folder.",
                    BareTheme.TEXT_MUTED));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(BareTheme.BORDER));
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(24);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JComponent moduleRow(String title, String detail, java.awt.Color color) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        row.setOpaque(false);
        row.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        JLabel name = new JLabel(title);
        name.setFont(BareTheme.uiBold(13));
        name.setForeground(color);
        JLabel description = new JLabel(detail);
        description.setFont(BareTheme.ui(12));
        description.setForeground(BareTheme.TEXT_MUTED);
        row.add(name);
        row.add(description);
        return row;
    }
}
