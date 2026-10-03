package com.ct.module.modman;

import com.ct.module.theme.Theme;
import com.ct.module.theme.UiKit;
import com.ct.module.theme.widget.Canvas;
import com.ct.module.theme.widget.CTButton;
import com.ct.module.theme.widget.FlatScrollBarUi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

final class ModManagerPanel extends JPanel {
    private final Path gameDirectory;
    private final Path modsDirectory;
    private final JList<ModEntry> modList = new JList<>();
    private final JLabel status = UiKit.muted("Scanning the mods folder.");
    private final JLabel pathLabel = new JLabel();

    ModManagerPanel(Path gameDirectory) {
        this.gameDirectory = gameDirectory;
        this.modsDirectory = ModScanner.modsDirectory(gameDirectory);
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel card = UiKit.card("Mods folder");
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);

        pathLabel.setFont(Theme.mono(11));
        pathLabel.setForeground(Theme.TEXT_MUTED);
        pathLabel.setText(modsDirectory.toString());
        body.add(pathLabel, BorderLayout.NORTH);

        modList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modList.setVisibleRowCount(12);
        modList.setFont(Theme.ui(13));
        modList.setBackground(Theme.CONSOLE);
        modList.setForeground(Theme.CONSOLE_TEXT);
        modList.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        modList.setCellRenderer(new ModRenderer());
        body.add(themedScroll(modList), BorderLayout.CENTER);

        javax.swing.JButton refresh = UiKit.button("Refresh", CTButton.Kind.GHOST);
        refresh.addActionListener(event -> refresh());
        javax.swing.JButton open = UiKit.button("Open mods folder", CTButton.Kind.GHOST);
        open.addActionListener(event -> openFolder());
        javax.swing.JButton delete = UiKit.button("Delete selected", CTButton.Kind.GHOST);
        delete.addActionListener(event -> deleteSelected());

        JPanel actions = UiKit.row(10);
        actions.add(refresh);
        actions.add(open);
        actions.add(delete);
        body.add(actions, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);

        JPanel foot = new JPanel(new BorderLayout(0, 6));
        foot.setOpaque(false);
        foot.add(status, BorderLayout.NORTH);
        JLabel help = UiKit.muted(
                "CT mods carry a ct.mod.json. Fabric mods carry a fabric.mod.json. "
                        + "Drop mod jars into the mods folder of the game directory.");
        foot.add(help, BorderLayout.SOUTH);
        wrapper.add(foot, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        List<ModEntry> entries;
        try {
            entries = ModScanner.scan(gameDirectory);
        } catch (IOException exception) {
            status.setText("The mods folder could not be read: " + exception.getMessage());
            return;
        }
        modList.setListData(entries.toArray(new ModEntry[0]));
        long ct = entries.stream().filter(entry -> entry.kind() == ModEntry.Kind.CT).count();
        long fabric = entries.stream().filter(entry -> entry.kind() == ModEntry.Kind.FABRIC).count();
        status.setText(entries.size() + " jar file(s): " + ct + " CT mod(s), " + fabric
                + " Fabric mod(s).");
    }

    private void openFolder() {
        try {
            Files.createDirectories(modsDirectory);
            Desktop.getDesktop().open(modsDirectory.toFile());
        } catch (IOException exception) {
            status.setText("The mods folder could not be opened: " + exception.getMessage());
        }
    }

    private void deleteSelected() {
        ModEntry selected = modList.getSelectedValue();
        if (selected == null) {
            status.setText("Select a mod jar to delete.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete " + selected.fileName() + "? This cannot be undone.",
                "Delete mod", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Files.deleteIfExists(selected.file());
            status.setText("Deleted " + selected.fileName() + ".");
        } catch (IOException exception) {
            status.setText("The mod jar could not be deleted: " + exception.getMessage());
        }
        refresh();
    }

    private static JScrollPane themedScroll(JList<ModEntry> list) {
        JScrollPane pane = new JScrollPane(list) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Canvas.prepare(graphics);
                Canvas.roundedRect(g, 0, 0, getWidth(), getHeight(), 14, Theme.CONSOLE,
                        Theme.BORDER);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        pane.setOpaque(false);
        pane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        pane.getViewport().setOpaque(false);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        JScrollBar bar = pane.getVerticalScrollBar();
        bar.setUI(new FlatScrollBarUi());
        bar.setOpaque(false);
        bar.setUnitIncrement(28);
        return pane;
    }

    private static final class ModRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean selected, boolean focused) {
            Component component = super.getListCellRendererComponent(list, value, index, selected,
                    focused);
            if (value instanceof ModEntry entry) {
                String line = entry.title() + "  —  " + entry.kind().label() + "  —  "
                        + entry.fileName() + "  (" + entry.sizeLabel() + ")";
                setText(line);
                if (selected) {
                    setForeground(Theme.TEXT);
                    setBackground(Theme.SELECTION);
                } else {
                    setForeground(colorFor(entry));
                    setBackground(Theme.CONSOLE);
                }
                setOpaque(true);
                setToolTipText(entry.detail() == null ? entry.file().toString() : entry.detail());
            }
            return component;
        }

        private static Color colorFor(ModEntry entry) {
            return switch (entry.kind()) {
                case CT -> Theme.ACCENT;
                case FABRIC -> Theme.TEAL;
                case BROKEN -> Theme.CORAL;
                case UNKNOWN -> Theme.TEXT_MUTED;
            };
        }
    }
}
