package com.ct.main.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ModuleSorter {
    private ModuleSorter() {
    }

    static List<ModuleManifest> order(Map<String, ModuleManifest> manifests) throws ModuleSortException {
        List<String> ids = manifests.keySet().stream().sorted().toList();
        Map<String, Integer> states = new HashMap<>();
        List<ModuleManifest> result = new ArrayList<>();
        Set<String> path = new HashSet<>();
        for (String id : ids) {
            visit(id, manifests, states, result, path);
        }
        return result;
    }

    private static void visit(String id, Map<String, ModuleManifest> manifests,
            Map<String, Integer> states, List<ModuleManifest> result, Set<String> path)
            throws ModuleSortException {
        int state = states.getOrDefault(id, 0);
        if (state == 2) {
            return;
        }
        if (state == 1) {
            path.add(id);
            throw new ModuleSortException("Module dependency cycle: " + String.join(" -> ", path) + ".");
        }
        states.put(id, 1);
        path.add(id);
        for (String dependency : manifests.get(id).requires().stream().sorted().toList()) {
            if (!manifests.containsKey(dependency)) {
                path.remove(id);
                states.put(id, 2);
                throw new ModuleSortException("Module '" + id + "' requires missing module '"
                        + dependency + "'.");
            }
            visit(dependency, manifests, states, result, path);
        }
        path.remove(id);
        states.put(id, 2);
        result.add(manifests.get(id));
    }

    static final class ModuleSortException extends Exception {
        ModuleSortException(String message) {
            super(message);
        }
    }

    static Map<String, ModuleManifest> indexById(List<ModuleManifest> manifests) {
        Map<String, ModuleManifest> index = new LinkedHashMap<>();
        for (ModuleManifest manifest : manifests) {
            index.putIfAbsent(manifest.id(), manifest);
        }
        return index;
    }
}
