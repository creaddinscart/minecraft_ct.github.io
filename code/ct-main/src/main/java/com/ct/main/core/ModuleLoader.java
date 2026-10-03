package com.ct.main.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class ModuleLoader {
    private ModuleLoader() {
    }

    public static ModuleCatalog scan(Path modulesDirectory) throws IOException {
        List<Path> jars = listModuleJars(modulesDirectory);
        Map<String, Candidate> index = new LinkedHashMap<>();
        List<SkippedModule> skipped = new ArrayList<>();
        for (Path jar : jars) {
            try {
                ModuleManifest read = ModuleManifest.read(jar);
                Candidate existing = index.get(read.id());
                if (existing != null) {
                    skipped.add(new SkippedModule(read.id(), jar, "duplicate module id; keeping "
                            + existing.jar().getFileName() + " and ignoring " + jar.getFileName()
                            + " — remove the older jar or run the newer one to replace it"));
                    continue;
                }
                index.put(read.id(), new Candidate(new ModuleManifest(read.id(), read.name(),
                        read.version(), read.description(), read.entrypoint(), read.requires()), jar));
            } catch (IOException exception) {
                skipped.add(new SkippedModule(jar.getFileName().toString(), jar, exception.getMessage()));
            }
        }
        return resolveOrder(index, skipped);
    }

    private static ModuleCatalog resolveOrder(Map<String, Candidate> index,
            List<SkippedModule> skipped) {
        List<Candidate> pending = index.values().stream()
                .sorted(Comparator.comparing(candidate -> candidate.manifest().id()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        List<Candidate> ordered = new ArrayList<>();
        Map<String, String> reasons = new LinkedHashMap<>();
        for (SkippedModule skippedModule : skipped) {
            reasons.put(skippedModule.id(), skippedModule.reason());
        }

        boolean progress = true;
        while (!pending.isEmpty() && progress) {
            progress = false;
            Iterator<Candidate> iterator = pending.iterator();
            while (iterator.hasNext()) {
                Candidate candidate = iterator.next();
                String missing = null;
                String blocked = null;
                boolean waiting = false;
                for (String require : candidate.manifest().requires()) {
                    if (!index.containsKey(require)) {
                        missing = require;
                        break;
                    }
                    if (reasons.containsKey(require)) {
                        blocked = require;
                    } else if (notOrdered(ordered, require)) {
                        waiting = true;
                    }
                }
                String id = candidate.manifest().id();
                if (missing != null) {
                    reasons.put(id, "requires missing module '" + missing + "'");
                    skipped.add(new SkippedModule(id, candidate.jar(), reasons.get(id)));
                    iterator.remove();
                    progress = true;
                } else if (blocked != null && !waiting) {
                    reasons.put(id, "requires module '" + blocked + "', which did not load: "
                            + reasons.get(blocked));
                    skipped.add(new SkippedModule(id, candidate.jar(), reasons.get(id)));
                    iterator.remove();
                    progress = true;
                } else if (missing == null && blocked == null && !waiting) {
                    ordered.add(candidate);
                    iterator.remove();
                    progress = true;
                }
            }
        }
        for (Candidate candidate : pending) {
            skipped.add(new SkippedModule(candidate.manifest().id(), candidate.jar(),
                    "dependency cycle involving " + candidate.manifest().requires()));
        }
        List<ModuleCandidate> orderedCandidates = ordered.stream()
                .map(candidate -> new ModuleCandidate(candidate.manifest(), candidate.jar()))
                .toList();
        return new ModuleCatalog(orderedCandidates, skipped);
    }

    private static boolean notOrdered(List<Candidate> ordered, String id) {
        return ordered.stream().noneMatch(candidate -> candidate.manifest().id().equals(id));
    }

    private static List<Path> listModuleJars(Path modulesDirectory) throws IOException {
        if (!Files.isDirectory(modulesDirectory)) {
            Files.createDirectories(modulesDirectory);
            return List.of();
        }
        try (Stream<Path> files = Files.list(modulesDirectory)) {
            return files.filter(ModuleManifest::isModuleJar)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    private record Candidate(ModuleManifest manifest, Path jar) {
    }
}
