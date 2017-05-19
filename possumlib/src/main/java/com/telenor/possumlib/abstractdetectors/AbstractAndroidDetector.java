package com.telenor.possumlib.abstractdetectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;

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
     * Constructor for all abstract sensors. Note that it is abstract, requiring you to extend it
     * for each sensor you wish to listen to.
     *
     * @param context    Any android context
     * @param sensorType The Sensor.Type you wish to use for this sensor
     */
    protected AbstractAndroidDetector(Context context, int sensorType, String identification, String secretKeyHash, @NonNull EventBus eventBus) {
        super(context, identification, secretKeyHash, eventBus);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            isEnabled = false;
            return;
        }
        sensor = sensorManager.getDefaultSensor(sensorType);
        isEnabled = sensor != null;
    }

    public abstract long guaranteedListenInterval();

    public abstract long restartInterval();

    /**
     * The request code for the pending intent. Need to be unique for each detector
     *
     * @return an integer for the request code used in the PendingIntent
     */
    protected abstract int detectorRequestCode();

    /**
     * Fired each time the sensor starts a wakeperiod and on start listen. Useful for oneshot
     * methods like singleLocationUpdate or a scan.
     */
    public abstract void detectorWakelockActivated();

    /**
     * Yield the power in mA used by the sensor while in use
     *
     * @return power consumption in mA
     */
    public float powerUsage() {
        return sensor != null ? sensor.getPower() : -1;
    }

    /**
     * This implementation of isEnabled relies on there being a sensor to listen to. Should the
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
     * Returns whether the sensor is of type wakeup or not. Wakeup sensors will awaken the processor
     * to deliver the data, while non-wakeup will store internally until full then replace
     *
     * @return true if it wakes up processor, false if not
     */
    public boolean isWakeUpDetector() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && sensor != null && sensor.isWakeUpSensor();
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