package com.telenor.possumlib.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service to handle reboot of sensor at given interval
 */
public class RebootTriggerSensorService extends Service {
    private static final String tag = RebootTriggerSensorService.class.getName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(tag, "Testus: Reboot service kicking off");
        int sensorToReboot = intent.getIntExtra("sensor", 0);
        if (sensorToReboot > 0) {
//            AbstractDetector detector = AwesomePossum.getDetectorByType(sensorToReboot);
//            if (detector instanceof AbstractAndroidTriggerDetector) {
//                Log.i(tag, "Testus: Rebooting sensor - "+ DetectorUtil.detectorTypeString(this, detector.detectorType()));
//                AbstractAndroidTriggerDetector triggerDetector = (AbstractAndroidTriggerDetector)detector;
//                triggerDetector.startListening();
//            }
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}