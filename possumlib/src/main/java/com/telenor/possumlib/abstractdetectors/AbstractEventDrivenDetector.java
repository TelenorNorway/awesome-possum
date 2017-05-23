package com.telenor.possumlib.abstractdetectors;

import android.content.Context;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.telenor.possumlib.changeevents.BasicChangeEvent;

/**
 * Abstract reactive detector, meant to fire only upon a given criteria fulfilled.
 * F.ex. a camera background service that fires when learning decides upon a certain movement
 * being done
 */
public abstract class AbstractEventDrivenDetector extends AbstractDetector {
    public AbstractEventDrivenDetector(Context context, String encryptedKurt, String secretKeyHash, EventBus eventBus) throws IllegalArgumentException {
        super(context, encryptedKurt, secretKeyHash, eventBus);
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            eventBus().register(this);
        }
        return listen;
    }

    @Override
    public void stopListening() {
        if (isListening()) {
            eventBus().unregister(this);
        }
        super.stopListening();
    }

    protected abstract boolean storeWithInterval();

    /**
     * General eventSubscribe method. Will store data based on whether it is immediate or interval
     * based. This method (subclasses super) must be called for it to actually store data.
     *
     * @param object a changeObject of general type so that the abstractions can implement the
     *               desired types
     */
    @Subscribe
    public void eventReceived(BasicChangeEvent object) {
        if (object.message() != null && isListening()) {
            sessionValues.add(object.message());
            if (storeWithInterval()) {
                storedValues++;
                if (storedValues > MINIMUM_SAMPLES) {
                    storeData();
                    storedValues = 0;
                }
            } else {
                storeData();
            }
        }
    }
}