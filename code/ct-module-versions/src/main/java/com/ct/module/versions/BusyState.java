package com.ct.module.versions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

final class BusyState {
    private final List<Consumer<Boolean>> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean busy;

    boolean isBusy() {
        return busy;
    }

    void setBusy(boolean value) {
        busy = value;
        for (Consumer<Boolean> listener : listeners) {
            listener.accept(value);
        }
    }

    void addListener(Consumer<Boolean> listener) {
        listeners.add(listener);
        listener.accept(busy);
    }
}
