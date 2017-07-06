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
     * @param uniqueUserId the unique user id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public MetaDataDetector(Context context, String uniqueUserId, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, uniqueUserId, eventBus, authenticating);
    }

    @Override
    public String requiredPermission() {
        return null;
    }

    @Override
    public void eventReceived(PossumEvent object) {
        if (object instanceof MetaDataChangeEvent && isListening()) {
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