package com.ct.module.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class Theme {
    public static final Color BACKGROUND = new Color(0x0F1512);
    public static final Color SURFACE = new Color(0x171F1A);
    public static final Color FIELD = new Color(0x101814);
    public static final Color SURFACE_ALT = new Color(0x1B2620);
    public static final Color BORDER = new Color(0x2A3630);
    public static final Color BORDER_STRONG = new Color(0x3B4C42);
    public static final Color TEXT = new Color(0xEAF2EC);
    public static final Color TEXT_MUTED = new Color(0x93A69A);
    public static final Color TEXT_FAINT = new Color(0x5F7166);
    public static final Color ACCENT = new Color(0xD8F05A);
    public static final Color ACCENT_HOVER = new Color(0xE4F87C);
    public static final Color ACCENT_PRESSED = new Color(0xC2DA45);
    public static final Color ON_ACCENT = new Color(0x16210F);
    public static final Color TEAL = new Color(0x46C8B8);
    public static final Color CORAL = new Color(0xEE765D);
    public static final Color BUTTON = new Color(0x1E2A23);
    public static final Color BUTTON_HOVER = new Color(0x27362E);
    public static final Color BUTTON_PRESSED = new Color(0x18221C);
    public static final Color CONSOLE = new Color(0x0C120F);
    public static final Color CONSOLE_TEXT = new Color(0xC7D6CB);
    public static final Color TRACK = new Color(0x1E2A23);
    public static final Color SELECTION = new Color(0x2E5A50);

    private static final String UI_FAMILY = pickFamily(List.of("SF Pro Text", "Helvetica Neue",
            "Segoe UI", "Inter", "Roboto", "Noto Sans", "Arial"), Font.SANS_SERIF);
    private static final String MONO_FAMILY = pickFamily(List.of("SF Mono", "Menlo", "Consolas",
            "JetBrains Mono", "Cascadia Mono", "DejaVu Sans Mono", "Courier New"), Font.MONOSPACED);

    private Theme() {
    }

    public static Font ui(int size) {
        return new Font(UI_FAMILY, Font.PLAIN, size);
    }

    public static Font uiBold(int size) {
        return new Font(UI_FAMILY, Font.BOLD, size);
    }

    public static Font mono(int size) {
        return new Font(MONO_FAMILY, Font.PLAIN, size);
    }

    public static Font monoBold(int size) {
        return new Font(MONO_FAMILY, Font.BOLD, size);
    }

    public static Color alpha(Color color, int value) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), value);
    }

    private static String pickFamily(List<String> preferred, String fallback) {
        Set<String> available = new HashSet<>(Arrays.asList(GraphicsEnvironment
                .getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ROOT)));
        for (String family : preferred) {
            if (available.contains(family)) {
                return family;
            }
        }
        return fallback;
    }
}
