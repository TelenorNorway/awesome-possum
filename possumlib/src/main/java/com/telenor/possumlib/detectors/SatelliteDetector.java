package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

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
            super.eventReceived(object);
        }
    }

    /**
     * Confirms whether detector is permitted to be used
     * @return true if allowed, else false
     */
    @Override
    public boolean isPermitted() {
        return ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
        return isPermitted();
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