package com.ct.main.core;

import java.nio.file.Path;

public record SkippedModule(String id, Path jar, String reason) {
}
