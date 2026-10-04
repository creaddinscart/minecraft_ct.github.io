package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.FontMetrics;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class CTComboBox extends JComboBox<String> {
    private static final long serialVersionUID = 1L;

    public CTComboBox() {
        setUI(new ComboUi());
        setOpaque(false);
        setFont(Theme.ui(13));
        setForeground(Theme.TEXT);
        setBorder(new EmptyBorder(2, 2, 2, 2));
        setMaximumRowCount(22);
        setRenderer(new ComboRenderer());
        setEditor(new DarkComboEditor());
        JTextField editor = (JTextField) getEditor().getEditorComponent();
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                putClientProperty("ct.focused", Boolean.TRUE);
                repaint();
            }

            @Override
            public void focusLost(FocusEvent event) {
                putClientProperty("ct.focused", Boolean.FALSE);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Canvas.prepare(graphics);
        boolean focused = isFocusOwner() || Boolean.TRUE.equals(getClientProperty("ct.focused"));
        Canvas.roundedRect(g, 0, 0, getWidth(), getHeight(), Canvas.radius(),
                isEnabled() ? Theme.FIELD : Theme.SURFACE_ALT,
                focused ? Theme.ACCENT : Theme.BORDER);
        g.dispose();
        super.paintComponent(graphics);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, Math.max(size.height, 34));
    }

    private static final class ComboRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean selected, boolean focused) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
            label.setOpaque(true);
            label.setFont(Theme.ui(13));
            label.setBorder(new EmptyBorder(6, 10, 6, 10));
            label.setBackground(selected ? Theme.BUTTON_HOVER : Theme.SURFACE);
            label.setForeground(selected ? Theme.ACCENT : Theme.TEXT);
            return label;
        }
    }

    private static final class DarkComboEditor extends BasicComboBoxEditor {
        @Override
        protected JTextField createEditorComponent() {
            return new CTField(16, false);
        }
    }

    private static final class ComboUi extends BasicComboBoxUI {
        @Override
        protected javax.swing.JButton createArrowButton() {
            return new ChevronButton(ChevronButton.Direction.DOWN, 28);
        }

        @Override
        public void paintCurrentValueBackground(Graphics graphics, Rectangle bounds, boolean hasFocus) {
        }

        @Override
        public void paintCurrentValue(Graphics graphics, Rectangle bounds, boolean hasFocus) {
            Object value = comboBox.getSelectedItem();
            if (value == null) {
                return;
            }
            Graphics2D g = Canvas.prepare(graphics);
            g.setFont(comboBox.getFont());
            g.setColor(comboBox.isEnabled() ? Theme.TEXT : Theme.TEXT_FAINT);
            FontMetrics metrics = g.getFontMetrics();
            int y = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(value.toString(), bounds.x + 12, y);
            g.dispose();
        }
    }
}
