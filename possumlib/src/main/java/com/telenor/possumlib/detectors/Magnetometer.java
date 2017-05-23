package com.telenor.possumlib.detectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractZippingAndroidDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.constants.ReqCodes;

/**
 * Measures magnetic field variations detected by the device. Not used atm.
 */
public class Magnetometer extends AbstractZippingAndroidDetector implements SensorEventListener {
    public Magnetometer(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) {
        super(context, Sensor.TYPE_MAGNETIC_FIELD, identification, secretKeyHash, eventBus);
    }

    @Override
    public long guaranteedListenInterval() {
        return 1680000; //28*60*1000;
    }

    @Override
    public long restartInterval() {
        return 2940000; //49*60*1000;
    }

    @Override
    protected int detectorRequestCode() {
        return ReqCodes.MAGNETOMETER;
    }

    @Override
    public void detectorWakelockActivated() { // Ignore this since it is continuous
    }

    /**
     * Stores event data in x,y,z
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isInvalid(event)) return;
        sessionValues.add(timestamp(event)+" "+event.values[0] + " " + event.values[1] + " " + event.values[2]);
        super.onSensorChanged(event);
    }

    @Override
    public int detectorType() {
        return DetectorType.Magnetometer;
    }

    @Override
    public String detectorName() {
        return "Magnetometer";
    }
}