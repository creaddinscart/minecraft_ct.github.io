package com.ct.main.core;

import java.util.List;

public record ModuleCatalog(List<ModuleCandidate> ordered, List<SkippedModule> skipped) {
    public ModuleCatalog {
        ordered = List.copyOf(ordered);
        skipped = List.copyOf(skipped);
    }
}
