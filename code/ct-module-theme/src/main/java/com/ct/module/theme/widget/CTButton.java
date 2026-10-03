package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public final class CTButton extends JButton {
    private static final long serialVersionUID = 1L;

    public enum Kind {
        PRIMARY,
        SECONDARY,
        GHOST
    }

    private final Kind kind;
    private final int height;

    public CTButton(String text, Kind kind, int height) {
        super(text);
        this.kind = kind;
        this.height = height;
        setFont(kind == Kind.PRIMARY ? Theme.uiBold(13) : Theme.ui(13));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(0, 18, 0, 18));
        getModel().addChangeListener(event -> repaint());
    }

    public CTButton(String text, Kind kind) {
        this(text, kind, 34);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Canvas.prepare(graphics);
        boolean pressed = getModel().isPressed() && getModel().isArmed();
        boolean hovered = getModel().isRollover() && isEnabled();
        int width = getWidth();
        int componentHeight = getHeight();
        Color fill = null;
        Color border = null;
        Color text;
        switch (kind) {
            case PRIMARY -> {
                if (!isEnabled()) {
                    fill = Theme.SURFACE_ALT;
                    text = Theme.TEXT_FAINT;
                } else {
                    fill = pressed ? Theme.ACCENT_PRESSED
                            : hovered ? Theme.ACCENT_HOVER : Theme.ACCENT;
                    text = Theme.ON_ACCENT;
                }
            }
            case SECONDARY -> {
                if (!isEnabled()) {
                    fill = Theme.SURFACE_ALT;
                    border = Theme.BORDER;
                    text = Theme.TEXT_FAINT;
                } else {
                    fill = pressed ? Theme.BUTTON_PRESSED : hovered ? Theme.BUTTON_HOVER : Theme.BUTTON;
                    border = hovered ? Theme.BORDER_STRONG : Theme.BORDER;
                    text = Theme.TEXT;
                }
            }
            default -> {
                if (!isEnabled()) {
                    text = Theme.TEXT_FAINT;
                } else {
                    if (hovered) {
                        fill = pressed ? Theme.BUTTON_PRESSED : Theme.BUTTON;
                    }
                    text = hovered ? Theme.TEXT : Theme.TEXT_MUTED;
                }
            }
        }
        Canvas.roundedRect(g, 0, 0, width, componentHeight, Canvas.radius(), fill, border);
        if (isFocusOwner() && isEnabled()) {
            Canvas.roundedRect(g, 2, 2, width - 4, componentHeight - 4, Canvas.radius() - 2, null,
                    Canvas.alpha(kind == Kind.PRIMARY ? Theme.ON_ACCENT : Theme.ACCENT, 120));
        }
        if (getText() != null) {
            g.setFont(getFont());
            g.setColor(text);
            FontMetrics metrics = g.getFontMetrics();
            int x = (width - metrics.stringWidth(getText())) / 2;
            int y = (componentHeight - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(getText(), x, y);
        }
        g.dispose();
    }
}
