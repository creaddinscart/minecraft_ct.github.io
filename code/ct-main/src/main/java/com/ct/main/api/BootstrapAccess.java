package com.ct.main.api;

import java.nio.file.Path;
import java.util.Optional;

public interface BootstrapAccess {
    Optional<Path> bootstrapJar();
}
