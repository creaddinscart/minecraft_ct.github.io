package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;

public final class DotIcon implements Icon {
    @Override
    public int getIconWidth() {
        return 8;
    }

    @Override
    public int getIconHeight() {
        return 8;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = Canvas.prepare(graphics);
        Canvas.roundedRect(g, x, y, 8, 8, 3, Theme.ACCENT, null);
        g.dispose();
    }
}
