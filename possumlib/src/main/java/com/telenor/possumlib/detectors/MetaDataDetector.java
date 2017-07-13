package com.telenor.possumlib.detectors;

import android.content.Context;
import android.support.annotation.NonNull;

import com.telenor.possumlib.abstractdetectors.AbstractEternalEventDetector;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;

/**
 * Sensor meant to take in different events regarding the apps events
 */
public class MetaDataDetector extends AbstractEternalEventDetector {
    /**
     * Constructor for the MetaDataDetector
     *
     * @param context a valid android context
     * @param eventBus an event bus for internal messages
     */
    public MetaDataDetector(Context context, @NonNull PossumBus eventBus) {
        super(context, eventBus);
    }

    @Override
    public void eventReceived(PossumEvent object) {
        if (object instanceof MetaDataChangeEvent && isListening() && !isAuthenticating()) {
            super.eventReceived(object);
        }
    }

    @Override
    public int detectorType() {
        return DetectorType.MetaData;
    }

    @Override
    public String detectorName() {
        return "MetaData";
    }
}