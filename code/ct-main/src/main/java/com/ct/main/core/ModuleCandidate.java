package com.ct.main.core;

import java.nio.file.Path;

public record ModuleCandidate(ModuleManifest manifest, Path jar) {
}
