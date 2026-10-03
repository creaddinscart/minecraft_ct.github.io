package com.ct.main.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class WelcomeView extends JPanel {
    public WelcomeView(String version) {
        setLayout(new BorderLayout());
        setBackground(BareTheme.BACKGROUND);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel eyebrow = centered(new JLabel("CT " + version), BareTheme.monoBold(12), BareTheme.ACCENT);
        JLabel title = centered(new JLabel("WELCOME TO MINECRAFT-CT"), BareTheme.uiBold(30), BareTheme.TEXT);
        JLabel website = centered(new LinkLabel("https://ct.shit.pub — open the official website", null),
                BareTheme.uiBold(15), BareTheme.LINK);
        JLabel hint = centered(new JLabel("This is the CT main program. Every feature is a separate module."),
                BareTheme.ui(13), BareTheme.TEXT_MUTED);
        JLabel modulesHint = centered(new JLabel(
                        "Download feature modules from the website and place the jar files in the modules folder."),
                BareTheme.mono(11), BareTheme.TEXT_MUTED);

        content.add(eyebrow);
        content.add(Box.createVerticalStrut(18));
        content.add(title);
        content.add(Box.createVerticalStrut(26));
        content.add(website);
        content.add(Box.createVerticalStrut(34));
        content.add(hint);
        content.add(Box.createVerticalStrut(8));
        content.add(modulesHint);

        JPanel centered = new JPanel(new GridBagLayout());
        centered.setOpaque(false);
        centered.add(content);
        add(centered, BorderLayout.CENTER);
    }

    private static JLabel centered(JLabel label, Font font, java.awt.Color color) {
        label.setFont(font);
        label.setForeground(color);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}
