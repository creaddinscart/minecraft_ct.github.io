package com.ct.module.theme;

import com.ct.module.theme.widget.BrandIcon;
import com.ct.module.theme.widget.CTButton;
import com.ct.module.theme.widget.CTCheckBox;
import com.ct.module.theme.widget.CTComboBox;
import com.ct.module.theme.widget.CTField;
import com.ct.module.theme.widget.CTProgressBar;
import com.ct.module.theme.widget.CTSpinner;
import com.ct.module.theme.widget.Canvas;
import com.ct.module.theme.widget.CheckIcon;
import com.ct.module.theme.widget.DotIcon;
import com.ct.module.theme.widget.FlatScrollBarUi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public final class UiKit {
    private UiKit() {
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.ui(13));
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.ui(12));
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }

    public static JLabel caption(String text) {
        JLabel label = new JLabel(text.toUpperCase(java.util.Locale.ROOT));
        label.setFont(Theme.mono(10));
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }

    public static JPanel row(int gap) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        row.setOpaque(false);
        return row;
    }

    public static JPanel labeled(String caption, JComponent component) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        group.add(caption(caption), BorderLayout.NORTH);
        group.add(component, BorderLayout.CENTER);
        return group;
    }

    public static JPanel card(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Canvas.prepare(graphics);
                Canvas.roundedRect(g, 0, 0, getWidth(), getHeight(), 16, Theme.SURFACE, Theme.BORDER);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 16, 16));
        if (title != null && !title.isBlank()) {
            card.add(sectionTitle(title), BorderLayout.NORTH);
        }
        return card;
    }

    public static JPanel sectionTitle(String text) {
        JPanel row = row(8);
        row.add(new JLabel(new DotIcon()));
        row.add(caption(text));
        return row;
    }

    public static JPanel pill(String text) {
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Canvas.prepare(graphics);
                Canvas.roundedRect(g, 0, 0, getWidth(), getHeight(), getHeight(),
                        Theme.alpha(Theme.ACCENT, 32), Theme.alpha(Theme.ACCENT, 110));
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        pill.setOpaque(false);
        pill.setBorder(new EmptyBorder(3, 9, 3, 9));
        JLabel label = new JLabel(text);
        label.setFont(Theme.monoBold(10));
        label.setForeground(Theme.ACCENT);
        pill.add(label);
        return pill;
    }

    public static JTextField field(int columns) {
        return new CTField(columns, true);
    }

    public static CTComboBox comboBox() {
        return new CTComboBox();
    }

    public static JSpinner spinner(SpinnerNumberModel model) {
        return new CTSpinner(model);
    }

    public static JCheckBox checkBox(String text) {
        return new CTCheckBox(text);
    }

    public static JButton button(String text, CTButton.Kind kind) {
        return new CTButton(text, kind);
    }

    public static JButton button(String text, CTButton.Kind kind, int height) {
        return new CTButton(text, kind, height);
    }

    public static JTextArea consoleArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(Theme.mono(12));
        area.setForeground(Theme.CONSOLE_TEXT);
        area.setCaretColor(Theme.ACCENT);
        area.setSelectionColor(Theme.SELECTION);
        area.setSelectedTextColor(Theme.TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(false);
        area.setBorder(new EmptyBorder(8, 10, 8, 6));
        return area;
    }

    public static JScrollPane consoleScroll(JTextArea area) {
        JScrollPane pane = new JScrollPane(area) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Canvas.prepare(graphics);
                Canvas.roundedRect(g, 0, 0, getWidth(), getHeight(), 14, Theme.CONSOLE, Theme.BORDER);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        pane.setOpaque(false);
        pane.setBorder(new EmptyBorder(2, 2, 2, 2));
        pane.getViewport().setOpaque(false);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        JScrollBar bar = pane.getVerticalScrollBar();
        bar.setUI(new FlatScrollBarUi());
        bar.setOpaque(false);
        bar.setUnitIncrement(28);
        return pane;
    }

    public static JProgressBar progressBar() {
        return new CTProgressBar();
    }

    public static JComponent center(JComponent component) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(component);
        return wrapper;
    }

    public static Icon brandIcon(Image image) {
        return new BrandIcon(image);
    }

    public static Color coral() {
        return Theme.CORAL;
    }

    public static Color textMuted() {
        return Theme.TEXT_MUTED;
    }

    public static FontMetrics metricsOf(Component component) {
        return component.getFontMetrics(Theme.ui(13));
    }
}
