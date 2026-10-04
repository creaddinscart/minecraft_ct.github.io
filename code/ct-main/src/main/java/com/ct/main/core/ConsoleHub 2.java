package com.ct.main.core;

import com.ct.main.api.ConsoleWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public final class ConsoleHub implements ConsoleWriter {
    private static final int REPLAY_LIMIT = 500;

    private final List<String> history = new ArrayList<>();
    private final List<Consumer<String>> sinks = new ArrayList<>();

    @Override
    public void writeLine(String line) {
        String text = line == null ? "" : line;
        synchronized (this) {
            history.add(text);
            if (history.size() > REPLAY_LIMIT) {
                history.remove(0);
            }
        }
        System.out.println(text);
        List<Consumer<String>> targets;
        synchronized (this) {
            targets = List.copyOf(sinks);
        }
        for (Consumer<String> sink : targets) {
            sink.accept(text);
        }
    }

    public void addSink(Consumer<String> sink) {
        synchronized (this) {
            sinks.add(sink);
        }
        List<String> replay;
        synchronized (this) {
            replay = List.copyOf(history);
        }
        for (String line : replay) {
            sink.accept(line);
        }
    }

    public void removeSink(Consumer<String> sink) {
        synchronized (this) {
            ListIterator<Consumer<String>> iterator = sinks.listIterator();
            while (iterator.hasNext()) {
                if (iterator.next() == sink) {
                    iterator.remove();
                }
            }
        }
    }

    public List<String> history() {
        synchronized (this) {
            return List.copyOf(history);
        }
    }
}
