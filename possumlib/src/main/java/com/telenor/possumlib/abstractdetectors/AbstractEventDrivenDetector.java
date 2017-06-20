package com.telenor.possumlib.abstractdetectors;

import android.content.Context;

import com.google.gson.JsonArray;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.models.PossumBus;

/**
 * Abstract reactive detector, meant to fire only upon a given criteria fulfilled.
 * F.ex. a camera background service that fires when learning decides upon a certain movement
 * being done
 */
public abstract class AbstractEventDrivenDetector extends AbstractDetector {
    /**
     * Constructor for the Abstract EventDriven Detector
     *
     * @param context a valid android context
     * @param encryptedKurt the encrypted kurt id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public AbstractEventDrivenDetector(Context context, String encryptedKurt, PossumBus eventBus, boolean authenticating) {
        super(context, encryptedKurt, eventBus, authenticating);
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
    @Override
    public void eventReceived(PossumEvent object) {
        if (object.message() != null && isListening()) {
            JsonArray eventArray = new JsonArray();
            eventArray.add(object.message());
            sessionValues.add(eventArray);
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