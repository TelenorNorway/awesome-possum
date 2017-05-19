package com.telenor.possumlib.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.detectors.MetaDataDetector;

/**
 * Handles monitoring of different events, time and wifi/power connectivity and uploading of data at nighttime.
 * Aso detects when the user is active with phone, or rather - when the user
 * locks/unlocks the phone/tablet
 */
public class EventManager {
    private EventBus eventBus = new EventBus();
    private int powerPresent = -1;
    private BroadcastReceiver receiver;
    private boolean isRegistered;
    private static final String tag = EventManager.class.getName();

    public EventManager(@NonNull Context context) {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                switch (intent.getAction()) {
                    case Intent.ACTION_POWER_CONNECTED:
                        eventBus.post(new MetaDataChangeEvent(MetaDataDetector.GENERAL_EVENT, "POWER_PLUGGED IN"));
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED:
                        eventBus.post(new MetaDataChangeEvent(MetaDataDetector.GENERAL_EVENT, "POWER_PLUGGED OUT"));
                        break;
                    case Intent.ACTION_BATTERY_CHANGED:
                        int batteryValue = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                        int changedTo = Math.round((float) (batteryValue * 100) / (float) batteryScale);
                        if (powerPresent == -1 || changedTo != powerPresent) {
                            powerPresent = changedTo;
                            eventBus.post(new MetaDataChangeEvent(MetaDataDetector.BATTERY_CHANGED, ""+changedTo));
                        }
                        break;
                    default:
                        Log.i(tag, "Unhandled action:" + intent.getAction());
                }
            }
        };
        if (!isRegistered) {
            isRegistered = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
            intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
            intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            context.registerReceiver(receiver, intentFilter);
        }
    }

    /**
     * Ran when terminating application
     */
    public void terminate(@NonNull Context context) {
        if (isRegistered) {
            isRegistered = false;
            context.unregisterReceiver(receiver);
        }
    }
}