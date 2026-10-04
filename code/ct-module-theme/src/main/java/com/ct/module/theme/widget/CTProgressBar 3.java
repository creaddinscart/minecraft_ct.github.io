package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public final class CTProgressBar extends JProgressBar {
    private static final long serialVersionUID = 1L;
    private final Timer timer;

    public CTProgressBar() {
        super();
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        timer = new Timer(24, event -> {
            if (isIndeterminate() && isShowing()) {
                repaint();
            }
        });
        timer.setCoalesce(true);
    }

    @Override
    public void setIndeterminate(boolean value) {
        super.setIndeterminate(value);
        if (timer == null) {
            return;
        }
        if (value) {
            if (!timer.isRunning()) {
                timer.start();
            }
        } else {
            timer.stop();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Canvas.prepare(graphics);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            g.dispose();
            return;
        }
        int radius = height;
        g.setColor(Theme.TRACK);
        g.fillRoundRect(0, 0, width, height, radius, radius);
        Shape clip = new RoundRectangle2D.Float(0, 0, width, height, radius, radius);
        g.clip(clip);
        g.setColor(Theme.ACCENT);
        if (isIndeterminate()) {
            int span = Math.max(64, width / 3);
            double phase = (System.currentTimeMillis() % 1500) / 1500.0;
            int x = (int) Math.round(phase * (width + span)) - span;
            g.fillRoundRect(x, 0, span, height, radius, radius);
        } else {
            int range = getMaximum() - getMinimum();
            int filled = range <= 0 ? 0
                    : (int) Math.round(width * (getValue() - getMinimum()) / (double) range);
            if (filled > 0) {
                g.fillRoundRect(0, 0, Math.max(filled, height), height, radius, radius);
            }
        }
        g.dispose();
    }
}
