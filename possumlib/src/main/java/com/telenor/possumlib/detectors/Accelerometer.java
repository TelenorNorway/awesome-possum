package com.telenor.possumlib.detectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.annotation.NonNull;

import com.telenor.possumlib.abstractdetectors.AbstractZippingAndroidDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.constants.ReqCodes;
import com.telenor.possumlib.models.PossumBus;

/***
 * Uses accelerometer to determine the movement/gait of the user
 */
public class Accelerometer extends AbstractZippingAndroidDetector {
    /**
     * Constructor for Accelerometer
     *
     * @param context           Any android context
     * @param encryptedKurt     the encrypted kurt id
     * @param eventBus          an event bus for internal messages
     * @param authenticating    whether the detector is used for authentication or data gathering
     */
    public Accelerometer(Context context, String encryptedKurt, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, Sensor.TYPE_ACCELEROMETER, encryptedKurt, eventBus, authenticating);
    }

    @Override
    public long guaranteedListenInterval() {
        return 1680000; //28*60*1000;
    }

    @Override
    protected int detectorRequestCode() {
        return ReqCodes.ACCELEROMETER;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isInvalid(event)) return;
        sessionValues.add(timestamp(event) + " " + event.values[0] + " " + event.values[1] + " " + event.values[2]);
        super.onSensorChanged(event);
    }

    @Override
    public String requiredPermission() {
        return null;
    }

    @Override
    public int detectorType() {
        return DetectorType.Accelerometer;
    }

    @Override
    public String detectorName() {
        return "Accelerometer";
    }
}