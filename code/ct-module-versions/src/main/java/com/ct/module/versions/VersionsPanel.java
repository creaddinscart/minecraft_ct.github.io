package com.ct.module.versions;

import com.ct.module.installer.GameInstaller;
import com.ct.module.installer.Installation;
import com.ct.module.theme.UiKit;
import com.ct.module.theme.widget.CTButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

final class VersionsPanel extends JPanel {
    private final GameInstaller installer;
    private final VersionSelection selection;
    private final BusyState busy;
    private final JComboBox<String> versionBox = UiKit.comboBox();
    private final JLabel status = UiKit.muted("Loading the official version list...");
    private final JProgressBar progress = UiKit.progressBar();
    private final CTButton refresh = new CTButton("Refresh versions", CTButton.Kind.SECONDARY);
    private final CTButton install = new CTButton("Install / Verify", CTButton.Kind.SECONDARY, 40);
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ct-versions-worker");
        thread.setDaemon(true);
        return thread;
    });

    VersionsPanel(GameInstaller installer, VersionSelection selection, BusyState busy) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        this.installer = installer;
        this.selection = selection;
        this.busy = busy;

        versionBox.setEditable(true);
        refresh.addActionListener(event -> refreshVersions(true));
        install.addActionListener(event -> installSelected());
        versionBox.addActionListener(event -> {
            if (busy.isBusy()) {
                return;
            }
            Object item = versionBox.getSelectedItem();
            if (item != null) {
                selection.setSelection(item.toString().trim());
            }
        });
        selection.addListener(this::reflectSelection);
        busy.addListener(value -> {
            versionBox.setEnabled(!value);
            refresh.setEnabled(!value);
            install.setEnabled(!value);
            progress.setIndeterminate(value);
        });
        progress.setPreferredSize(new Dimension(240, 8));

        JPanel card = UiKit.card("Minecraft versions");
        JPanel picker = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        picker.setOpaque(false);
        picker.add(versionBox);
        picker.add(refresh);
        card.add(picker, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(14, 0));
        actions.setOpaque(false);
        actions.add(install, BorderLayout.WEST);
        actions.add(progress, BorderLayout.EAST);
        actions.add(status, BorderLayout.SOUTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(card);
        body.add(Box.createVerticalStrut(10));
        body.add(actions);
        add(body, BorderLayout.NORTH);

        refreshVersions(false);
    }

    private void reflectSelection(String version) {
        if (version != null && !version.equals(versionBox.getSelectedItem())) {
            versionBox.setSelectedItem(version);
        }
    }

    private void refreshVersions(boolean announce) {
        run("Loading official Minecraft versions...", () -> {
            VersionCatalogSnapshot snapshot = loadCatalog();
            SwingUtilities.invokeLater(() -> {
                versionBox.removeAllItems();
                snapshot.versionIds().forEach(versionBox::addItem);
                snapshot.loaderProfiles().stream()
                        .filter(profile -> !snapshot.versionIds().contains(profile))
                        .forEach(versionBox::addItem);
                String target = selection.selected() != null
                        && (snapshot.versionIds().contains(selection.selected())
                        || snapshot.loaderProfiles().contains(selection.selected()))
                        ? selection.selected()
                        : snapshot.latestRelease();
                versionBox.setSelectedItem(target);
                selection.setCatalog(snapshot.versionIds(), snapshot.latestRelease());
                status.setText("Loaded " + snapshot.versionIds().size()
                        + " official versions and " + snapshot.loaderProfiles().size()
                        + " installed loader profile(s).");
            });
        });
    }

    private record VersionCatalogSnapshot(String latestRelease, List<String> versionIds,
            List<String> loaderProfiles) {
    }

    private VersionCatalogSnapshot loadCatalog() throws Exception {
        var catalog = installer.versionCatalog();
        List<String> loaderProfiles = installer.installedModLoaderProfiles();
        return new VersionCatalogSnapshot(catalog.latestRelease(), catalog.versionIds(), loaderProfiles);
    }

    private void installSelected() {
        String version = selection.selected() == null ? "latest" : selection.selected();
        run("Installing Minecraft " + version + "...", () -> {
            Installation installed = installer.install(version, line ->
                    SwingUtilities.invokeLater(() -> status.setText(line)));
            SwingUtilities.invokeLater(() -> status.setText("Minecraft "
                    + installed.versionId() + " is installed in " + installed.gameDirectory() + "."));
        });
    }

    private void run(String description, Task task) {
        if (busy.isBusy()) {
            return;
        }
        busy.setBusy(true);
        status.setText(description);
        worker.submit(() -> {
            try {
                task.run();
            } catch (Exception exception) {
                String message = exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage();
                SwingUtilities.invokeLater(() -> status.setText("Error: " + message));
            } finally {
                SwingUtilities.invokeLater(() -> busy.setBusy(false));
            }
        });
    }

    private interface Task {
        void run() throws Exception;
    }
}
