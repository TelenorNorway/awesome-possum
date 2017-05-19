package com.telenor.possumlib;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.TriggerEvent;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;

/**
 * Class for handlign different sensorevents for testing purposes
 */
public class SensorEvents {
    public static SensorEvent createSensorEvent(Sensor sensor, long timestamp, int accuracy, float x, float y, float z) throws Exception {
        SensorEvent sensorEvent = mock(SensorEvent.class);
        sensorEvent.timestamp = timestamp;
        sensorEvent.sensor = sensor;
        sensorEvent.accuracy = accuracy;
        Field valuesField = SensorEvent.class.getField("values");
        valuesField.setAccessible(true);
        valuesField.set(sensorEvent, new float[]{x, y, z});
        return sensorEvent;
    }

    public static TriggerEvent createTriggerEvent(Sensor sensor, long timestamp, float x, float y, float z) throws Exception {
        TriggerEvent triggerEvent = mock(TriggerEvent.class);
        triggerEvent.sensor = sensor;
        triggerEvent.timestamp = timestamp;
        Field valuesField = TriggerEvent.class.getDeclaredField("values");
        valuesField.setAccessible(true);
        valuesField.set(triggerEvent, new float[]{x, y, z});
        return triggerEvent;
    }

}
