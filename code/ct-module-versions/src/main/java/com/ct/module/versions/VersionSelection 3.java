package com.ct.module.versions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class VersionSelection {
    private final List<String> catalogVersions = new ArrayList<>();
    private final List<Consumer<String>> listeners = new ArrayList<>();
    private String selected;

    public void setCatalog(List<String> versionIds, String defaultSelection) {
        catalogVersions.clear();
        catalogVersions.addAll(versionIds);
        if (selected == null || !catalogVersions.contains(selected)) {
            selected = defaultSelection;
        }
        notifyListeners();
    }

    public void setSelection(String version) {
        selected = version;
        notifyListeners();
    }

    public String selected() {
        return selected;
    }

    public boolean isKnownVersion(String candidate) {
        return catalogVersions.contains(candidate);
    }

    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
        listener.accept(selected);
    }

    private void notifyListeners() {
        for (Consumer<String> listener : List.copyOf(listeners)) {
            listener.accept(selected);
        }
    }
}
