package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTextFieldUI;

public final class CTField extends JTextField {
    private static final long serialVersionUID = 1L;
    private final boolean outlined;

    public CTField(int columns, boolean outlined) {
        super(columns);
        this.outlined = outlined;
        setUI(new BasicTextFieldUI());
        setOpaque(false);
        setFont(Theme.ui(13));
        setForeground(Theme.TEXT);
        setDisabledTextColor(Theme.TEXT_FAINT);
        setCaretColor(Theme.ACCENT);
        setSelectionColor(Theme.SELECTION);
        setSelectedTextColor(Theme.TEXT);
        if (outlined) {
            setBorder(new EmptyBorder(7, 10, 7, 10));
        } else {
            setBorder(new EmptyBorder(6, 10, 6, 8));
        }
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent event) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Canvas.prepare(graphics);
        java.awt.Color border = outlined && isEnabled()
                ? (isFocusOwner() ? Theme.ACCENT : Theme.BORDER) : null;
        Canvas.roundedRect(g, 0, 0, getWidth(), getHeight(), Canvas.radius(),
                isEnabled() ? Theme.FIELD : Theme.SURFACE_ALT, border);
        g.dispose();
        super.paintComponent(graphics);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        if (outlined) {
            size.height = Math.max(size.height, 34);
        }
        return size;
    }
}
