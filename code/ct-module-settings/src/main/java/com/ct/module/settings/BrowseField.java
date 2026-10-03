package com.ct.module.settings;

import com.ct.module.theme.UiKit;
import com.ct.module.theme.widget.CTButton;
import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

final class BrowseField extends JPanel {
    private final JTextField field;

    BrowseField(int columns, boolean directoriesOnly) {
        super(new BorderLayout(8, 0));
        setOpaque(false);
        field = UiKit.field(columns);
        CTButton browse = new CTButton("Browse", CTButton.Kind.SECONDARY);
        browse.addActionListener(event -> choose(directoriesOnly));
        add(field, BorderLayout.CENTER);
        add(browse, BorderLayout.EAST);
    }

    private void choose(boolean directoriesOnly) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(directoriesOnly
                ? JFileChooser.DIRECTORIES_ONLY
                : JFileChooser.FILES_ONLY);
        if (!directoriesOnly) {
            chooser.setFileFilter(new FileNameExtensionFilter("Java executable", "exe"));
        }
        Path current = field.getText().isBlank() ? null : Path.of(field.getText().trim());
        if (current != null && Files.exists(current)) {
            chooser.setSelectedFile(current.toFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    JComponent asComponent() {
        return this;
    }

    JTextField field() {
        return field;
    }
}
