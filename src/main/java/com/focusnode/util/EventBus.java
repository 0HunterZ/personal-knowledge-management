package com.focusnode.util;

import javafx.application.Platform;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus {
    public enum EventType {
        DATA_CHANGED,
        THEME_CHANGED
    }

    private static final Map<EventType, List<WeakReference<Consumer<Void>>>> listeners = new ConcurrentHashMap<>();

    public static void subscribe(EventType type, Consumer<Void> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(new WeakReference<>(listener));
    }

    public static void publish(EventType type) {
        if (!listeners.containsKey(type)) return;

        Platform.runLater(() -> {
            List<WeakReference<Consumer<Void>>> currentListeners = listeners.get(type);
            List<WeakReference<Consumer<Void>>> deadReferences = new ArrayList<>();

            for (WeakReference<Consumer<Void>> ref : currentListeners) {
                Consumer<Void> listener = ref.get();
                if (listener != null) {
                    listener.accept(null);
                } else {
                    deadReferences.add(ref);
                }
            }
            currentListeners.removeAll(deadReferences);
        });
    }
}
