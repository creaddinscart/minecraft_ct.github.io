package com.ct.module.modman;

import java.nio.file.Path;

public record ModEntry(Path file, String fileName, Kind kind, String modId, String name,
        String version, long sizeBytes, String detail) {

    public enum Kind {
        CT("CT mod"),
        FABRIC("Fabric mod"),
        UNKNOWN("Unrecognized"),
        BROKEN("Unreadable");

        private final String label;

        Kind(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public String title() {
        String base = name == null || name.isBlank() ? fileName : name;
        return version == null || version.isBlank() ? base : base + " " + version;
    }

    public String sizeLabel() {
        long bytes = sizeBytes;
        if (bytes < 1024) {
            return bytes + " B";
        }
        double value = bytes;
        for (String unit : new String[] {"KB", "MB", "GB"}) {
            value /= 1024;
            if (value < 1024) {
                return String.format("%.1f %s", value, unit);
            }
        }
        return String.format("%.1f TB", value / 1024);
    }
}
