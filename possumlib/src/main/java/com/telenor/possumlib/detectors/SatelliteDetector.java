package com.telenor.possumlib.detectors;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.changeevents.SatelliteChangeEvent;
import com.telenor.possumlib.constants.DetectorType;

public class SatelliteDetector extends AbstractEventDrivenDetector {
    public SatelliteDetector(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) throws IllegalArgumentException {
        super(context, identification, secretKeyHash, eventBus);
    }

    @Override
    protected boolean storeWithInterval() {
        return true;
    }

    @Override
    public void eventReceived(BasicChangeEvent object) {
        if (object instanceof SatelliteChangeEvent) {
            sessionValues.add(object.message());
            super.eventReceived(object);
        }
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isValidSet() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public int detectorType() {
        return DetectorType.GpsStatus;
    }
}