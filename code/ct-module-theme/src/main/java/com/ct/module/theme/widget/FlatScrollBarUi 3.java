package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class FlatScrollBarUi extends BasicScrollBarUI {
    @Override
    protected JButton createDecreaseButton(int orientation) {
        return zeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return zeroButton();
    }

    private static JButton zeroButton() {
        JButton button = new JButton();
        Dimension zero = new Dimension(0, 0);
        button.setPreferredSize(zero);
        button.setMinimumSize(zero);
        button.setMaximumSize(zero);
        return button;
    }

    @Override
    protected void paintTrack(java.awt.Graphics graphics, JComponent component, Rectangle bounds) {
    }

    @Override
    protected void paintThumb(java.awt.Graphics graphics, JComponent component, Rectangle bounds) {
        if (bounds.height <= 8 || bounds.width <= 8) {
            return;
        }
        java.awt.Graphics2D g = Canvas.prepare(graphics);
        Canvas.roundedRect(g, bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4,
                bounds.width - 4, isThumbRollover() ? Theme.BORDER_STRONG : Theme.BORDER, null);
        g.dispose();
    }

    @Override
    public Dimension getPreferredSize(JComponent component) {
        return new Dimension(10, 40);
    }
}
