package com.telenor.possumlib.abstractdetectors;

import android.content.Context;

import com.google.common.eventbus.EventBus;

/**
 * Abstraction of an eternal event detector, in effect one that always listens and never turns off.
 * This to be able to f.ex. detect events while listening is paused or off. Ideal for meta detection
 * or similar detectors.
 */
public abstract class AbstractEternalEventDetector extends AbstractEventDrivenDetector {
    public AbstractEternalEventDetector(Context context, String identification, String secretKeyHash, EventBus eventBus) throws IllegalArgumentException {
        super(context, identification, secretKeyHash, eventBus);
        super.startListening();
    }

    @Override
    public boolean startListening() {
        return true;
    }

    @Override
    protected boolean storeWithInterval() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isValidSet() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void stopListening() {
        // Empty to override
    }

    @Override
    public void terminate() {
        super.stopListening(); // Before stopListening since stopListening is overridden
        super.terminate();
    }
}