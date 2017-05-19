package com.telenor.possumlib.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.detectors.HardwareDetector;
import com.telenor.possumlib.managers.S3ModelDownloader;
import com.telenor.possumlib.utils.Get;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/***
 * Service that handles all actions pertaining to collecting the data from the sensors.
 */
public class CollectorService extends Service {
    private static final ConcurrentLinkedQueue<AbstractDetector> detectors = new ConcurrentLinkedQueue<>();
    private BroadcastReceiver receiver;
    private final static Messenger messenger = new Messenger(new PossumHandler());
    private static final String tag = CollectorService.class.getName();

    /**
     * onStartCommand - ensuring that the service is NOT sticky, disabling it from being deleted
     * by system. Also requires foreground to prevent from dying or being killed.
     * Most important, it will start/stop the listening depending on requestCode and setPreferences
     *
     * @param intent      Contains eventual extra information from startService call
     * @param flags       Sent on PendingIntent or via startService
     * @param requestCode Can be used to send concrete messages on startService
     * @return the relevant integer for sticky service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int requestCode) {
        super.onStartCommand(intent, flags, requestCode);
        String secretKeyHash = intent.getStringExtra("secretKeyHash");
        String uniqueId = intent.getStringExtra("uniqueId");
        // Ensures all detectors are terminated and cleared before adding new ones
        clearAllDetectors();

        Log.i(tag, "Start collection");
        // Adding all detectors
        List<Class<? extends AbstractDetector>> ignoreList = new ArrayList<>();
        if (!intent.getBooleanExtra("hardwareStored", false)) {
            ignoreList.add(HardwareDetector.class);
        }
        detectors.addAll(Get.Detectors(this, uniqueId, secretKeyHash, ignoreList, new EventBus()));

        for (AbstractDetector detector : detectors) {
            detector.startListening();
        }

        return START_NOT_STICKY;
    }

    /**
     * Pushes all stored detectors to upload and terminates them
     */
    private void clearAllDetectors() {
        for (AbstractDetector detector : detectors) {
            detector.prepareUpload();
            detector.terminate();
        }
        detectors.clear();
    }

    private static class PossumHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                default:
                    Log.i(tag, "Message received:" + message);
            }
        }
    }

    /**
     * onCreate - starts up all relevant sensors and setting service as a foreground service
     * Important to note: onCreate is always started before onStartCommand, in effect initialising
     * all variables and starting the service in the foreground
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "OnCreate of collector service");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                handleIntent(intent.getStringExtra(Messaging.TYPE));
            }
        };
        registerReceiver(receiver, new IntentFilter(Messaging.POSSUM_MESSAGE));
        JodaTimeAndroid.init(this);
        S3ModelDownloader.init(this);
    }

    private void handleIntent(String action) {
        if (action == null) return;
        switch (action) {
            case Messaging.REQUEST_SENSORS:
                Intent intent = new Intent(Messaging.POSSUM_RETURN_MESSAGE);
                JsonArray detectorObjects = new JsonArray();
                for (AbstractDetector detector : detectors) {
                    detectorObjects.add(detector.toJson());
                }
                intent.putExtra(Messaging.TYPE, Messaging.SENSORS_STATUS);
                intent.putExtra(Messaging.DETECTORS, detectorObjects.toString());
                sendBroadcast(intent);
                break;
            default:
                Log.d(tag, "Unhandled action:" + action);
        }
    }

    /**
     * onDestroy - when service is about to die or is closed by upload, all sensors are
     * "decommissioned" and unListened to.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "Destroying background service");
        unregisterReceiver(receiver);
        clearAllDetectors();
        detectors.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
}