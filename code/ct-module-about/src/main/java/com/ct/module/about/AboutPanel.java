package com.ct.module.about;

import com.ct.main.api.ModuleContext;
import com.ct.main.ui.LinkLabel;
import com.ct.module.theme.Theme;
import com.ct.module.theme.UiKit;
import com.ct.module.theme.widget.CTButton;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class AboutPanel extends JPanel {
    private final ModuleContext context;
    private final JLabel status = UiKit.muted("");

    AboutPanel(ModuleContext context) {
        this.context = context;
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel card = UiKit.card("About CT-Main");
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel version = new JLabel("CT-Main " + context.mainVersion());
        version.setFont(Theme.uiBold(20));
        version.setForeground(Theme.TEXT);
        JLabel role = UiKit.muted(
                "The CT main program is only a module host. Every feature is a module jar "
                        + "in the modules folder.");
        LinkLabel website = new LinkLabel("https://ct.shit.pub — official website", null);
        website.setFont(Theme.uiBold(14));
        LinkLabel license = new LinkLabel("License", "/license.html");
        LinkLabel modulesPage = new LinkLabel("Module downloads", "/modules.html");

        JLabel applicationPath = mono(context.applicationDirectory().toString());
        JLabel modulesPath = mono(context.modulesDirectory().toString());

        JButton openModules = UiKit.button("Open modules folder", CTButton.Kind.GHOST);
        openModules.addActionListener(event -> open(context.modulesDirectory()));
        JButton openApplication = UiKit.button("Open application folder", CTButton.Kind.GHOST);
        openApplication.addActionListener(event -> open(context.applicationDirectory()));

        version.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        role.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        website.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        license.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        modulesPage.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        applicationPath.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        modulesPath.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        body.add(version);
        body.add(Box.createVerticalStrut(8));
        body.add(role);
        body.add(Box.createVerticalStrut(16));
        body.add(website);
        body.add(Box.createVerticalStrut(6));
        body.add(modulesPage);
        body.add(Box.createVerticalStrut(6));
        body.add(license);
        body.add(Box.createVerticalStrut(16));
        body.add(captioned("Application folder", applicationPath));
        body.add(Box.createVerticalStrut(8));
        body.add(captioned("Modules folder", modulesPath));
        body.add(Box.createVerticalStrut(16));

        JPanel actions = UiKit.row(10);
        actions.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        actions.add(openModules);
        actions.add(openApplication);
        body.add(actions);
        body.add(Box.createVerticalStrut(8));
        status.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        body.add(status);

        card.add(body, BorderLayout.CENTER);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);
    }

    private void open(Path directory) {
        try {
            if (!Files.isDirectory(directory)) {
                Files.createDirectories(directory);
            }
            Desktop.getDesktop().open(directory.toFile());
            status.setText("Opened " + directory + ".");
        } catch (IOException exception) {
            status.setText("The folder could not be opened: " + exception.getMessage());
        }
    }

    private static JPanel captioned(String caption, JLabel value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel title = UiKit.caption(caption);
        title.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        value.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(value);
        return panel;
    }

    private static JLabel mono(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.mono(11));
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }
}
