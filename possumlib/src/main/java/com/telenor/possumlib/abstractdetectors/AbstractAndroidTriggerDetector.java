package com.telenor.possumlib.abstractdetectors;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.interfaces.ITrigger;
import com.telenor.possumlib.services.RebootTriggerSensorService;

import org.joda.time.DateTime;

/**
 * Handles all sensors that are wakeup or otherwise triggered/sequencial
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class AbstractAndroidTriggerDetector extends AbstractAndroidDetector implements ITrigger {
    private TriggerEventListener triggerListener;
    private AlarmManager alarmManager;
    private Intent intent;

    public AbstractAndroidTriggerDetector(Context context, int sensorType, String identification, String secretKeyHash, EventBus eventBus) {
        super(context, sensorType, identification, secretKeyHash, eventBus);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, RebootTriggerSensorService.class);
        intent.putExtra("sensor", detectorType());
        triggerListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                AbstractAndroidTriggerDetector.this.onTrigger(event);
                alarmManager.set(AlarmManager.RTC_WAKEUP, DateTime.now().getMillis() + restartInterval(), PendingIntent.getService(context(), detectorRequestCode(), intent, 0));
            }
        };
    }

    /**
     * Returns a valid timestamp from an event. Will never be 100%, but best possible
     *
     * @param event The triggerEvent you want a timestamp from
     * @return a unix timestamp equals to System.currentTimeMillis()
     */
    protected long timestamp(TriggerEvent event) {
        return DateTime.now().getMillis() + ((event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L);
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            sensorManager.requestTriggerSensor(triggerListener, sensor);
        }
        return listen;
    }

    /**
     * Cancels the trigger event
     */
    @Override
    public void stopListening() {
        super.stopListening();
        sensorManager.cancelTriggerSensor(triggerListener, sensor);
    }
}