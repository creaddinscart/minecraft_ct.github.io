package com.ct.module.launcher;

import com.ct.main.api.Account;
import com.ct.main.api.AccountProvider;
import com.ct.module.versions.VersionSelection;
import com.ct.module.theme.UiKit;
import com.ct.module.theme.widget.CTButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

final class LaunchPanel extends JPanel {
    private final LaunchService service;
    private final AccountProvider accounts;
    private final VersionSelection selection;
    private final com.ct.main.api.ConsoleWriter console;
    private final javax.swing.JButton launch =
            UiKit.button("Launch selected version", CTButton.Kind.PRIMARY, 40);
    private final JLabel accountLabel = UiKit.label("Not signed in");
    private final JLabel status = UiKit.muted("Ready.");
    private final JProgressBar progress = UiKit.progressBar();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ct-launch-worker");
        thread.setDaemon(true);
        return thread;
    });
    private volatile Process gameProcess;
    private volatile boolean busy;

    LaunchPanel(LaunchService service, AccountProvider accounts, VersionSelection selection,
            com.ct.main.api.ConsoleWriter console) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        this.service = service;
        this.accounts = accounts;
        this.selection = selection;
        this.console = console;

        accounts.addListener(account -> SwingUtilities.invokeLater(() -> {
            accountLabel.setText(account == null ? "Not signed in" : account.label());
            updateControls();
        }));
        launch.addActionListener(event -> launchSelected());
        progress.setPreferredSize(new Dimension(240, 8));

        JPanel card = UiKit.card("Launch");
        JPanel actions = new JPanel(new BorderLayout(14, 0));
        actions.setOpaque(false);
        actions.add(launch, BorderLayout.WEST);
        actions.add(progress, BorderLayout.EAST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(accountLabel);
        info.add(Box.createVerticalStrut(8));
        info.add(status);

        card.add(actions, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);
        updateControls();
    }

    private void updateControls() {
        boolean gameRunning = gameProcess != null && gameProcess.isAlive();
        launch.setEnabled(!busy && accounts.current().isPresent() && !gameRunning);
        progress.setIndeterminate(busy);
    }

    private void launchSelected() {
        if (busy) {
            return;
        }
        Account account = accounts.current().orElse(null);
        if (account == null) {
            status.setText("Sign in with Microsoft or select the offline profile before launching.");
            return;
        }
        String version = selection.selected() == null ? "latest" : selection.selected();
        busy = true;
        status.setText("Preparing to launch Minecraft profile " + version + "...");
        updateControls();
        worker.submit(() -> {
            try {
                Process process = service.launch(version, account, console);
                gameProcess = process;
                SwingUtilities.invokeLater(() -> {
                    status.setText("Minecraft profile " + version + " started as "
                            + account.label() + ".");
                    updateControls();
                });
                int exitCode = process.waitFor();
                SwingUtilities.invokeLater(() -> {
                    gameProcess = null;
                    status.setText("Minecraft exited with code " + exitCode + ".");
                    console.writeLine("Minecraft exited with code " + exitCode + ".");
                    updateControls();
                });
            } catch (Exception exception) {
                String message = exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage();
                console.writeLine("Error: " + message);
                SwingUtilities.invokeLater(() -> {
                    status.setText("Error: " + message);
                    busy = false;
                    updateControls();
                });
                return;
            }
            SwingUtilities.invokeLater(() -> {
                busy = false;
                updateControls();
            });
        });
    }

}
