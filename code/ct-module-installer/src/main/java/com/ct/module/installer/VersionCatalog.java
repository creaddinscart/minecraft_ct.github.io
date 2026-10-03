package com.ct.module.installer;

import java.util.List;

public record VersionCatalog(String latestRelease, List<String> versionIds) {
    public VersionCatalog {
        versionIds = List.copyOf(versionIds);
    }
}
