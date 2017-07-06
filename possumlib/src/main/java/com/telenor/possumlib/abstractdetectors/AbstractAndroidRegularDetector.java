package com.telenor.possumlib.abstractdetectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.SystemClock;

import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.models.PossumBus;

import org.joda.time.DateTime;

public abstract class AbstractAndroidRegularDetector extends AbstractAndroidDetector implements SensorEventListener {
    private static final int MIN_INTERVAL_MILLI = 50;
    private static final int MIN_INTERVAL_MICRO = MIN_INTERVAL_MILLI * 1000;
    private static final long MIN_INTERVAL_NANO = MIN_INTERVAL_MICRO * 1000;
    private long lastRecord;

    /**
     * Constructor for regular android sensor detectors
     * @param context    Any android context
     * @param sensorType The Sensor.Type you wish to use for this sensor
     * @param uniqueUserId the unique user id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    protected AbstractAndroidRegularDetector(Context context, int sensorType, String uniqueUserId, PossumBus eventBus, boolean authenticating) {
        super(context, sensorType, uniqueUserId, eventBus, authenticating);
        if (sensor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                eventBus.post(new MetaDataChangeEvent(DateTime.now().getMillis()+" "+ detectorName() + " FIFO SIZE " + sensor.getFifoMaxEventCount() + " " + sensor.getFifoReservedEventCount()));
            } else {
                eventBus.post(new MetaDataChangeEvent(DateTime.now().getMillis()+" "+ detectorName() + " FIFO SIZE NOT AVAILABLE - BELOW API 19"));
            }
        }
    }

    /**
     * Checks whether timestamp has passed a minimum of milliseconds.
     *
     * @return true if it has passed the minimum, false if not
     */
    protected boolean isInvalid(SensorEvent event) {
        if ((event.timestamp - lastRecord) <= MIN_INTERVAL_NANO) return true;
        lastRecord = event.timestamp;
        return false;
    }

    /**
     * startListening uses its ancestors method as base, it also registers the clas as a listener
     * to the given type should it actually startListening
     *
     * @return whether or not it starts to startListening
     */
    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen && sensor != null) {
            sensorManager.registerListener(this, sensor, MIN_INTERVAL_MICRO);
        }
        return listen;
    }

    /**
     * Returns an estimated current time
     *
     * @param event the sensorevent you want to get timestamp from
     * @return the timestamp in epoch timestamp format
     */
    protected long timestamp(SensorEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return super.now() + ((event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L);
        } else return super.now() + ((event.timestamp - SystemClock.elapsedRealtime()*1000) /1000000L);
    }

    /**
     * Unregisters the sensor as a listener to sensorData from the given sensor type and handles
     * the data retrieved.
     */
    @Override
    public void stopListening() {
        boolean isListening = isListening();
        super.stopListening();
        if (isListening && sensorManager != null) {
            sensorManager.unregisterListener(this, sensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!isListening()) {
            return;
        }
        storedValues++;
        long queue;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            queue = sensor.getFifoMaxEventCount() > 0?sensor.getFifoMaxEventCount():MINIMUM_SAMPLES;
        } else {
            queue = MINIMUM_SAMPLES;
        }
        if (storedValues >= queue) {
            storeData(storedData());
            storedValues = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*
         * Default handling (or refusal to handle) the accuracyChanged part of the sensor listening
         * This should be handled in some way in a later version, as it can severely effect whether
         * the data is valuable or simply trash.
         */
    }
}