package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class Canvas {
    private Canvas() {
    }

    public static Graphics2D prepare(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    public static void roundedRect(Graphics2D g, int x, int y, int width, int height, int radius,
            Color fill, Color border) {
        if (fill != null) {
            g.setColor(fill);
            g.fillRoundRect(x, y, width, height, radius, radius);
        }
        if (border != null) {
            g.setColor(border);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    public static int radius() {
        return 10;
    }

    public static Color alpha(Color color, int value) {
        return Theme.alpha(color, value);
    }
}
