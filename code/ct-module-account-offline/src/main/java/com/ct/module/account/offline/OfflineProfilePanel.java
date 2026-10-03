package com.ct.module.account.offline;

import com.ct.main.api.Account;
import com.ct.main.api.AccountProvider;
import com.ct.main.api.SettingsAccess;
import com.ct.module.theme.UiKit;
import com.ct.module.theme.widget.CTButton;
import java.awt.BorderLayout;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class OfflineProfilePanel extends JPanel {
    private final JTextField nameField = UiKit.field(18);
    private final JLabel status = UiKit.muted("No offline profile selected.");

    OfflineProfilePanel(AccountProvider accounts, Optional<SettingsAccess> settings) {
        super(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel card = UiKit.card("Offline profile");
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JPanel row = UiKit.row(10);
        row.add(UiKit.labeled("Player name", nameField));
        JButton select = UiKit.button("Use offline profile", CTButton.Kind.GHOST);
        select.addActionListener(event -> selectOffline(accounts, settings));
        row.add(select);

        row.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        status.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        form.add(row);
        form.add(Box.createVerticalStrut(10));
        form.add(status);
        card.add(form, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);
    }

    private void selectOffline(AccountProvider accounts, Optional<SettingsAccess> settings) {
        try {
            Account offline = Account.offline(nameField.getText());
            settings.ifPresent(access -> {
                try {
                    access.setText("offline.playerName", offline.name());
                } catch (java.io.IOException exception) {
                    status.setText("The offline name could not be saved: " + exception.getMessage());
                }
            });
            accounts.setCurrent(offline);
            status.setText("Offline profile selected: " + offline.label() + " (" + offline.uuid() + ")");
        } catch (IllegalArgumentException exception) {
            status.setText(exception.getMessage());
        }
    }
}
