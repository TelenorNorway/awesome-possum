package com.telenor.possumlib.abstractdetectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import com.telenor.possumlib.models.PossumBus;

/***
 * AbstractAndroidDetector class that handles all detecting of sensor changes from the android
 * sensor manager. Note that OnSensorChanged is NOT implemented here, it will need to be
 * in all usages of this class. The important thing it will need to do is to
 */
public abstract class AbstractAndroidDetector extends AbstractDetector {
    protected Sensor sensor;
    private final boolean isEnabled;
    SensorManager sensorManager;

    /**
     * Constructor for all android sensor detectors. Note that it is abstract, requiring you to extend it
     * for each sensor you wish to startListening to.
     *
     * @param context    Any android context
     * @param sensorType The Sensor.Type you wish to use for this sensor
     * @param encryptedKurt the encrypted kurt id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    protected AbstractAndroidDetector(Context context, int sensorType, String encryptedKurt, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, encryptedKurt, eventBus, authenticating);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            isEnabled = false;
            return;
        }
        sensor = sensorManager.getDefaultSensor(sensorType);
        isEnabled = sensor != null;
    }

    public abstract long guaranteedListenInterval();

    /**
     * The request code for the pending intent. Need to be unique for each detector
     *
     * @return an integer for the request code used in the PendingIntent
     */
    protected abstract int detectorRequestCode();

    /**
     * Yield the power in mA used by the sensor while in use
     *
     * @return power consumption in mA
     */
    public float powerUsage() {
        return sensor != null ? sensor.getPower() : -1;
    }

    /**
     * This implementation of isEnabled relies on there being a sensor to startListening to. Should the
     * constructor fail to find the given sensor it throw a missing sensor exception and will not
     * enable this as a sensor. Consequently it will not allow any startListening
     *
     * @return whether the sensor is enabled
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Inherited from AbstractDetector - will always be true, as there is currently no way to
     * disable or loose certain sensors. A possibility is to use accuracy, but it is always on
     * (if it is enabled/found)
     *
     * @return always true
     */
    @Override
    public boolean isAvailable() {
        return true;
    }
}