package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.Icon;

public final class BrandIcon implements Icon {
    private final Image image;

    public BrandIcon(Image image) {
        this.image = image;
    }

    @Override
    public int getIconWidth() {
        return 44;
    }

    @Override
    public int getIconHeight() {
        return 44;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = Canvas.prepare(graphics);
        Canvas.roundedRect(g, x, y, 44, 44, 13, Theme.SURFACE_ALT, Theme.BORDER);
        if (image != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, x + 7, y + 7, 30, 30, null);
        } else {
            g.setFont(Theme.uiBold(15));
            g.setColor(Theme.ACCENT);
            FontMetrics metrics = g.getFontMetrics();
            g.drawString("CT", x + (44 - metrics.stringWidth("CT")) / 2,
                    y + (44 - metrics.getHeight()) / 2 + metrics.getAscent());
        }
        g.dispose();
    }
}
