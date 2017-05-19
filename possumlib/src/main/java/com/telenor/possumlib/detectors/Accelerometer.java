package com.telenor.possumlib.detectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractZippingAndroidDetector;
import com.telenor.possumlib.changeevents.LocationChangeEvent;
import com.telenor.possumlib.changeevents.WifiChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.constants.ReqCodes;

/***
 * Uses accelerometer to determine the movement for the user
 */
public class Accelerometer extends AbstractZippingAndroidDetector {
    public Accelerometer(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) throws IllegalArgumentException {
        super(context, Sensor.TYPE_ACCELEROMETER, identification, secretKeyHash, eventBus);
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
        return ReqCodes.ACCELEROMETER;
    }

    @Override
    public void detectorWakelockActivated() {
        // Accelerometer, which has the most probability of existing has responsibility of firing an location/wifi/bluetooth event each wakelock active
        eventBus().post(new LocationChangeEvent());
        eventBus().post(new WifiChangeEvent());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isInvalid(event)) return;
        sessionValues.add(timestamp(event) + " " + event.values[0] + " " + event.values[1] + " " + event.values[2]);
        super.onSensorChanged(event);
    }

    @Override
    public int detectorType() {
        return DetectorType.Accelerometer;
    }
}