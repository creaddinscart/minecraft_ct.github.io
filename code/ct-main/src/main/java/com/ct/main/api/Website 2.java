package com.ct.main.api;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public final class Website {
    public static final String HOME = "https://ct.shit.pub";

    private Website() {
    }

    public static void open(String path) {
        String address = path == null || path.isBlank() ? HOME : HOME + path;
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(URI.create(address));
                return;
            } catch (IOException | RuntimeException ignored) {
                return;
            }
        }
        System.out.println("[CT-Main] Open " + address);
    }
}
