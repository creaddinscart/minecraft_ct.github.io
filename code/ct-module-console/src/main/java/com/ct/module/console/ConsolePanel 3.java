package com.ct.module.console;

import com.ct.module.theme.UiKit;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

final class ConsolePanel extends JPanel {
    private static final int CONSOLE_CHARACTER_LIMIT = 200_000;

    private final JTextArea area = UiKit.consoleArea();

    ConsolePanel() {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        JPanel card = UiKit.card("Launcher output");
        JScrollPane scroll = UiKit.consoleScroll(area);
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    void appendLine(String line) {
        area.append(line);
        area.append("\n");
        int length = area.getDocument().getLength();
        if (length > CONSOLE_CHARACTER_LIMIT) {
            area.replaceRange("", 0, length - CONSOLE_CHARACTER_LIMIT);
        }
        area.setCaretPosition(area.getDocument().getLength());
    }
}
