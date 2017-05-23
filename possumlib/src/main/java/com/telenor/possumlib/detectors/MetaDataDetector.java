package com.telenor.possumlib.detectors;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEternalEventDetector;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.constants.DetectorType;

/**
 * Sensor meant to take in different events regarding the apps events
 */
public class MetaDataDetector extends AbstractEternalEventDetector {
    public MetaDataDetector(Context context, String encryptedKurt, String secretKeyHash, @NonNull EventBus eventBus) throws IllegalArgumentException {
        super(context, encryptedKurt, secretKeyHash, eventBus);
    }

    @Override
    public void eventReceived(BasicChangeEvent object) {
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