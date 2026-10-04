package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import javax.swing.JCheckBox;

public final class CTCheckBox extends JCheckBox {
    private static final long serialVersionUID = 1L;

    public CTCheckBox(String text) {
        super(text);
        setOpaque(false);
        setFont(Theme.ui(13));
        setForeground(Theme.TEXT);
        setFocusPainted(false);
        setIcon(new CheckIcon(false));
        setSelectedIcon(new CheckIcon(true));
        setIconTextGap(9);
    }
}
