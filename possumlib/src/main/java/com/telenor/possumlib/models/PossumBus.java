package com.telenor.possumlib.models;

import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.interfaces.IPossumEventListener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PossumBus {
    private Queue<IPossumEventListener> listeners = new ConcurrentLinkedQueue<>();

    public void post(PossumEvent event) {
        for (IPossumEventListener listener: listeners) {
            listener.eventReceived(event);
        }
    }

    public void register(IPossumEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregister(IPossumEventListener listener) {
        listeners.remove(listener);
    }

    public void clearAll() {
        listeners.clear();
    }
}