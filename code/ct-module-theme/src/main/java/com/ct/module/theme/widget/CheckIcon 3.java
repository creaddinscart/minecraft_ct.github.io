package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

public final class CheckIcon implements Icon {
    private final boolean checked;

    public CheckIcon(boolean checked) {
        this.checked = checked;
    }

    @Override
    public int getIconWidth() {
        return 18;
    }

    @Override
    public int getIconHeight() {
        return 18;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = Canvas.prepare(graphics);
        if (checked) {
            Canvas.roundedRect(g, x, y, 18, 18, 6, Theme.ACCENT, null);
            g.setColor(Theme.ON_ACCENT);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D path = new Path2D.Float();
            path.moveTo(x + 4.6f, y + 9.6f);
            path.lineTo(x + 7.8f, y + 12.8f);
            path.lineTo(x + 13.8f, y + 5.4f);
            g.draw(path);
        } else {
            Canvas.roundedRect(g, x, y, 17, 17, 6, Theme.FIELD, Theme.BORDER_STRONG);
        }
        g.dispose();
    }
}
