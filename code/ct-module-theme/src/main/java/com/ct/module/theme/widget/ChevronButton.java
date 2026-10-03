package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public final class ChevronButton extends JButton {
    private static final long serialVersionUID = 1L;

    public enum Direction {
        UP,
        DOWN
    }

    private final Direction direction;
    private final int width;

    public ChevronButton(Direction direction, int width) {
        this.direction = direction;
        this.width = width;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        getModel().addChangeListener(event -> repaint());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, 18);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Canvas.prepare(graphics);
        int componentWidth = getWidth();
        int componentHeight = getHeight();
        if (getModel().isRollover() && isEnabled()) {
            Canvas.roundedRect(g, 2, 2, componentWidth - 4, componentHeight - 4, 6,
                    getModel().isPressed() ? Theme.BUTTON_PRESSED : Theme.BUTTON, null);
        }
        g.setColor(isEnabled() ? Theme.TEXT_MUTED : Theme.TEXT_FAINT);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int centerX = componentWidth / 2;
        int centerY = componentHeight / 2;
        float half = 3.6f;
        Path2D path = new Path2D.Float();
        if (direction == Direction.DOWN) {
            path.moveTo(centerX - half, centerY - half / 2f);
            path.lineTo(centerX, centerY + half / 2f);
            path.lineTo(centerX + half, centerY - half / 2f);
        } else {
            path.moveTo(centerX - half, centerY + half / 2f);
            path.lineTo(centerX, centerY - half / 2f);
            path.lineTo(centerX + half, centerY + half / 2f);
        }
        g.draw(path);
        g.dispose();
    }
}
