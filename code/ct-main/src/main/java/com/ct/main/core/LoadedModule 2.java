package com.ct.main.core;

import com.ct.main.api.CtModule;
import java.nio.file.Path;

public record LoadedModule(ModuleManifest manifest, Path jar, CtModule instance) {
}
