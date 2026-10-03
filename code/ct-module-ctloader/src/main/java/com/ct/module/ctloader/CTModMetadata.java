package com.ct.module.ctloader;

import java.util.List;

public record CTModMetadata(String id, String name, String version, List<String> entrypoints,
        List<String> depends) {
    public CTModMetadata {
        entrypoints = List.copyOf(entrypoints);
        depends = List.copyOf(depends);
    }
}
