package com.ct.main.ui;

import com.ct.main.api.Website;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

public final class LinkLabel extends JLabel {
    public LinkLabel(String text, String path) {
        super(text);
        setForeground(BareTheme.LINK);
        setFont(BareTheme.ui(13));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                Website.open(path);
            }
        });
    }

    public LinkLabel withFont(Font font) {
        setFont(font);
        return this;
    }
}
