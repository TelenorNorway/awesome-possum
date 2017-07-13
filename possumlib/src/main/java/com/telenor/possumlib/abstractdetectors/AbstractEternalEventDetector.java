package com.telenor.possumlib.abstractdetectors;

import android.content.Context;
import com.telenor.possumlib.models.PossumBus;

/**
 * Abstraction of an eternal event detector, in effect one that always listens and never turns off.
 * This to be able to f.ex. detect events while listening is paused or off. Ideal for meta detection
 * or similar detectors.
 */
public abstract class AbstractEternalEventDetector extends AbstractEventDrivenDetector {
    /**
     * Constructor for AbstractEternalEventDetector
     *
     * @param context a valid android context
     * @param eventBus an event bus for internal messages
     */
    public AbstractEternalEventDetector(Context context, PossumBus eventBus) {
        super(context, eventBus);
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