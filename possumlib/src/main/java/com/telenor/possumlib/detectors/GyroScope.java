package com.telenor.possumlib.detectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractZippingAndroidDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.constants.ReqCodes;
import com.telenor.possumlib.models.PossumBus;


/**
 * Detects changes in the gyroscope
 */
public class GyroScope extends AbstractZippingAndroidDetector {
    /**
     * Constructor for the gyroscope
     *
     * @param context    Any android context
     * @param uniqueUserId the unique user id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public GyroScope(Context context, String uniqueUserId, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, Sensor.TYPE_GYROSCOPE, uniqueUserId, eventBus, authenticating);
    }

    @Override
    public String requiredPermission() {
        return null;
    }

    @Override
    public long authenticationListenInterval() {
        return 3000;
    }

    @Override
    protected int detectorRequestCode() {
        return ReqCodes.GYROSCOPE;
    }

    /**
     * Stores event data with x, y and z
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isInvalid(event)) return;
        JsonArray array = new JsonArray();
        array.add(""+timestamp(event));
        array.add(""+event.values[0]);
        array.add(""+event.values[1]);
        array.add(""+event.values[2]);
        sessionValues.add(array);
        super.onSensorChanged(event);
    }

    @Override
    public int detectorType() {
        return DetectorType.Gyroscope;
    }

    @Override
    public String detectorName() {
        return "gyroscope";
    }
}