package com.ct.module.accountmicrosoft;

import com.ct.main.api.Account;
import com.ct.main.api.AccountProvider;
import com.ct.main.api.ConsoleWriter;
import com.ct.main.api.SettingKeys;
import com.ct.main.api.SettingsAccess;
import com.ct.module.theme.UiKit;
import com.ct.module.theme.widget.CTButton;
import com.ct.module.theme.widget.CTComboBox;
import com.ct.module.theme.widget.CTField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

final class MicrosoftAccountPanel extends JPanel {
    private static final String BUILT_IN_SIGN_IN = "Microsoft account (built-in)";
    private static final String CUSTOM_SIGN_IN = "Custom Azure client ID";
    private static final String MODE_BUILT_IN = "builtIn";
    private static final String MODE_CUSTOM = "custom";

    private final SettingsAccess settings;
    private final AccountProvider accounts;
    private final ConsoleWriter console;
    private final CTComboBox signInMethod = UiKit.comboBox();
    private final javax.swing.JTextField clientIdField = UiKit.field(20);
    private final javax.swing.JButton signInButton = UiKit.button("Sign in with Microsoft",
            CTButton.Kind.SECONDARY, 32);
    private final JLabel status = UiKit.muted("Sign in with a Microsoft account to launch the game.");
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ct-microsoft-sign-in");
        thread.setDaemon(true);
        return thread;
    });
    private boolean applyingSettings;
    private boolean busy;

    MicrosoftAccountPanel(SettingsAccess settings, AccountProvider accounts, ConsoleWriter console) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        this.settings = settings;
        this.accounts = accounts;
        this.console = console;

        signInMethod.addActionListener(event -> selectSignInMethod());
        signInButton.addActionListener(event -> signIn());
        clientIdField.setToolTipText("Azure public client ID with the device code flow enabled.");

        JPanel card = UiKit.card("Microsoft account");
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(UiKit.labeled("Microsoft sign-in", signInMethod));
        body.add(Box.createVerticalStrut(10));
        body.add(UiKit.labeled("Azure client ID (custom mode)", clientIdField));
        body.add(Box.createVerticalStrut(12));
        body.add(signInButton);
        body.add(Box.createVerticalStrut(10));
        status.setPreferredSize(new Dimension(360, 40));
        body.add(status);
        card.add(body, BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);

        applyStoredSettings();
        updateControls();
    }

    private void applyStoredSettings() {
        applyingSettings = true;
        boolean custom = MODE_CUSTOM.equals(
                settings.text(SettingKeys.MICROSOFT_LOGIN_MODE, MODE_BUILT_IN));
        signInMethod.setSelectedItem(custom ? CUSTOM_SIGN_IN : BUILT_IN_SIGN_IN);
        clientIdField.setText(settings.text(SettingKeys.MICROSOFT_CLIENT_ID, ""));
        applyingSettings = false;
    }

    private void selectSignInMethod() {
        if (applyingSettings) {
            return;
        }
        updateControls();
        try {
            settings.setText(SettingKeys.MICROSOFT_LOGIN_MODE, isCustomMode() ? MODE_CUSTOM : MODE_BUILT_IN);
        } catch (IOException exception) {
            reportFailure("The sign-in method could not be saved.", exception);
        }
    }

    private boolean isCustomMode() {
        return CUSTOM_SIGN_IN.equals(signInMethod.getSelectedItem());
    }

    private void updateControls() {
        boolean custom = isCustomMode();
        clientIdField.setEnabled(custom && !busy);
        signInButton.setEnabled(!busy);
        signInButton.setToolTipText(custom
                ? "Signs in with the Azure application client ID below."
                : "Signs in with the built-in Microsoft sign-in. No client ID is needed.");
    }

    private void signIn() {
        String clientId = clientIdField.getText().trim();
        if (isCustomMode() && clientId.isBlank()) {
            status.setText("Enter an Azure application client ID, or switch to the "
                    + "built-in Microsoft sign-in.");
            return;
        }
        busy = true;
        updateControls();
        status.setText(isCustomMode()
                ? "Signing in with the custom Azure client ID..."
                : "Signing in with Microsoft...");
        worker.submit(() -> {
            try {
                MicrosoftSignIn authenticator = new MicrosoftSignIn();
                Account signedIn = isCustomMode()
                        ? authenticator.signIn(clientId, this::reportProgress)
                        : authenticator.signInDirect(this::reportProgress);
                settings.setText(SettingKeys.MICROSOFT_CLIENT_ID, clientId);
                SwingUtilities.invokeLater(() -> {
                    accounts.setCurrent(signedIn);
                    console.writeLine("Signed in as " + signedIn.label() + ".");
                    status.setText("Signed in as " + signedIn.name() + ".");
                    busy = false;
                    updateControls();
                });
            } catch (Exception exception) {
                reportFailure("Microsoft sign-in failed.", exception);
                SwingUtilities.invokeLater(() -> {
                    busy = false;
                    updateControls();
                });
            }
        });
    }

    private void reportProgress(String line) {
        console.writeLine(line);
        SwingUtilities.invokeLater(() -> status.setText(line));
    }

    private void reportFailure(String prefix, Exception exception) {
        String message = exception.getMessage() == null
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
        console.writeLine("Error: " + message);
        SwingUtilities.invokeLater(() -> status.setText(prefix + " " + message));
    }
}
