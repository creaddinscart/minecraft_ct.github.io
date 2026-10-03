package com.ct.main.ui;

import java.awt.Color;
import java.awt.Font;

final class BareTheme {
    static final Color BACKGROUND = new Color(0x0F1512);
    static final Color SURFACE = new Color(0x171F1A);
    static final Color BORDER = new Color(0x2A3630);
    static final Color TEXT = new Color(0xEAF2EC);
    static final Color TEXT_MUTED = new Color(0x93A69A);
    static final Color ACCENT = new Color(0xD8F05A);
    static final Color LINK = new Color(0x9BD48A);

    private static final String UI_FAMILY = pickFamily(new String[] {"SF Pro Text", "Helvetica Neue",
        "Segoe UI", "Inter", "Roboto", "Noto Sans", "Arial"});
    private static final String MONO_FAMILY = pickFamily(new String[] {"SF Mono", "Menlo", "Consolas",
        "JetBrains Mono", "Cascadia Mono", "DejaVu Sans Mono", "Courier New"});

    private BareTheme() {
    }

    static Font ui(int size) {
        return new Font(UI_FAMILY, Font.PLAIN, size);
    }

    static Font uiBold(int size) {
        return new Font(UI_FAMILY, Font.BOLD, size);
    }

    static Font mono(int size) {
        return new Font(MONO_FAMILY, Font.PLAIN, size);
    }

    static Font monoBold(int size) {
        return new Font(MONO_FAMILY, Font.BOLD, size);
    }

    private static String pickFamily(String[] preferred) {
        java.util.Set<String> available = new java.util.HashSet<>(java.util.Arrays.asList(
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames(java.util.Locale.ROOT)));
        for (String family : preferred) {
            if (available.contains(family)) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }
}
