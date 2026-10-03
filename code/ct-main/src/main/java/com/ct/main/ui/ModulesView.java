package com.ct.main.ui;

import com.ct.main.api.ModuleContext;
import com.ct.main.api.Website;
import com.ct.main.core.DroppedModules;
import com.ct.main.core.LoadedModule;
import com.ct.main.core.Relaunch;
import com.ct.main.core.SkippedModule;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

final class ModulesView extends JPanel {
    private final ModuleContext context;

    ModulesView(List<LoadedModule> loaded, List<SkippedModule> skipped, ModuleContext context) {
        this.context = context;
        java.nio.file.Path modulesFolder = context.modulesDirectory();
        setLayout(new BorderLayout(0, 10));
        setBackground(BareTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel headings = new JPanel();
        headings.setOpaque(false);
        headings.setLayout(new BoxLayout(headings, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Loaded feature modules");
        title.setFont(BareTheme.uiBold(16));
        title.setForeground(BareTheme.TEXT);
        JLabel folder = new JLabel("Modules folder: " + modulesFolder
                + " — run any downloaded module jar once and it installs itself here.");
        folder.setFont(BareTheme.mono(11));
        folder.setForeground(BareTheme.TEXT_MUTED);
        headings.add(title);
        headings.add(Box.createVerticalStrut(4));
        headings.add(folder);
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton install = hostButton("Install module jar…");
        install.addActionListener(event -> chooseModules());
        JButton open = hostButton("Open modules folder");
        open.addActionListener(event -> openFolder(modulesFolder));
        JButton website = hostButton("Get modules on the official website");
        website.addActionListener(event -> Website.open("/modules.html"));
        actions.add(install);
        actions.add(open);
        actions.add(website);
        header.add(headings, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (LoadedModule module : loaded) {
            String version = module.manifest().version();
            list.add(moduleRow(module.manifest().id()
                            + (version.isBlank() ? "" : " " + version)
                            + " — " + module.manifest().name(),
                    module.manifest().description(), BareTheme.TEXT));
        }
        for (SkippedModule module : skipped) {
            list.add(moduleRow(module.id() + " — not loaded", module.reason(), BareTheme.TEXT_MUTED));
        }
        if (loaded.isEmpty() && skipped.isEmpty()) {
            list.add(moduleRow("No modules installed",
                    "Download a module jar from " + Website.HOME + " and run it once: it installs "
                            + "itself into the modules folder above.",
                    BareTheme.TEXT_MUTED));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(BareTheme.BORDER));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(BareTheme.BACKGROUND);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(24);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void chooseModules() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Choose ct-module-*.jar files to install");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CT module jars (*.jar)", "jar"));
        if (chooser.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File[] chosen = chooser.getSelectedFiles();
        if (chosen.length == 0 && chooser.getSelectedFile() != null) {
            chosen = new java.io.File[] {chooser.getSelectedFile()};
        }
        if (chosen.length == 0) {
            return;
        }
        List<java.nio.file.Path> sources = new java.util.ArrayList<>();
        for (java.io.File file : chosen) {
            sources.add(file.toPath());
        }
        DroppedModules.Report report = DroppedModules.install(sources, context.modulesDirectory(),
                context.mainVersion(), true);
        for (String line : report.lines()) {
            context.console().writeLine(line);
        }
        if (!report.changed()) {
            javax.swing.JOptionPane.showMessageDialog(this, String.join("\n", report.lines()),
                    "Nothing installed", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = javax.swing.JOptionPane.showOptionDialog(this,
                "Installed " + report.installed() + " module(s) into\n" + context.modulesDirectory()
                        + "\n\nCT-Main loads modules when it starts, so restart it to use them.",
                "Restart to load the new modules", javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.INFORMATION_MESSAGE, null,
                new Object[] {"Restart CT-Main now", "Later"}, "Later");
        if (choice != 0) {
            return;
        }
        if (Relaunch.relaunch(List.of())) {
            System.exit(0);
        }
        javax.swing.JOptionPane.showMessageDialog(this,
                "Close CT-Main and start it again to load the new modules.",
                "Restart CT-Main", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private static JButton hostButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BareTheme.ui(12));
        button.setBackground(BareTheme.SURFACE);
        button.setForeground(BareTheme.TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BareTheme.BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        return button;
    }

    private static void openFolder(java.nio.file.Path path) {
        try {
            java.awt.Desktop.getDesktop().open(path.toFile());
        } catch (Exception exception) {
            return;
        }
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
