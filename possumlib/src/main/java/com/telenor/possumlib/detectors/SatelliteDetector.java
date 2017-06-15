package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;

import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.changeevents.SatelliteChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;

/**
 * A satellite detector notifying about the positions of the gps satellites. Not used atm.
 */
public class SatelliteDetector extends AbstractEventDrivenDetector {
    /**
     * Constructor for SatelliteDetector
     * @param context a valid android context
     * @param encryptedKurt the encrypted kurt id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public SatelliteDetector(Context context, String encryptedKurt, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, encryptedKurt, eventBus, authenticating);
    }

    @Override
    protected boolean storeWithInterval() {
        return true;
    }

    @Override
    public void eventReceived(PossumEvent object) {
        if (object instanceof SatelliteChangeEvent) {
            super.eventReceived(object);
        }
    }

    // Is hard-disabled atm
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isValidSet() {
        return true;
    }

    @Override
    public String requiredPermission() {
        return Manifest.permission.ACCESS_FINE_LOCATION;
    }

    @Override
    public int detectorType() {
        return DetectorType.GpsStatus;
    }

    @Override
    public String detectorName() {
        return "Satellites";
    }
}