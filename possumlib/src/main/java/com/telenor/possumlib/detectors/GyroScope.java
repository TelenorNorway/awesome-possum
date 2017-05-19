package com.telenor.possumlib.detectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractZippingAndroidDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.constants.ReqCodes;


/**
 * Detects changes in the gyroscope
 */
public class GyroScope extends AbstractZippingAndroidDetector {
    public GyroScope(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) {
        super(context, Sensor.TYPE_GYROSCOPE, identification, secretKeyHash, eventBus);
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
        return ReqCodes.GYROSCOPE;
    }

    @Override
    public void detectorWakelockActivated() { // Ignore this since it is continuous
    }

    /**
     * Stores event data with x, y and z
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isInvalid(event)) return;
        sessionValues.add(timestamp(event)+" "+event.values[0] + " " + event.values[1] + " " + event.values[2]);
        super.onSensorChanged(event);
    }

    @Override
    public int detectorType() {
        return DetectorType.Gyroscope;
    }
}