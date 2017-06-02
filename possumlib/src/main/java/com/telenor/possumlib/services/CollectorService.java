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
import com.telenor.possumlib.utils.Get;

import java.util.concurrent.ConcurrentLinkedQueue;

/***
 * Service that handles all actions pertaining to collecting the data from the sensors.
 */
public class CollectorService extends Service {
    private static final ConcurrentLinkedQueue<AbstractDetector> detectors = new ConcurrentLinkedQueue<>();
    private BroadcastReceiver receiver;
    private EventBus eventBus = new EventBus();
    private final static Messenger messenger = new Messenger(new PossumHandler());
    private static final String tag = CollectorService.class.getName();

    /**
     * onStartCommand - ensuring that the service is NOT sticky, initializing the detectors and
     * starts the listening for data
     *
     * @param intent      Contains eventual extra information from startService call
     * @param flags       Sent on PendingIntent or via startService
     * @param requestCode Can be used to send concrete messages on startService
     * @return the relevant integer for sticky service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int requestCode) {
        super.onStartCommand(intent, flags, requestCode);
        // Ensures all detectors are terminated and cleared before adding new ones
        clearAllDetectors();

        Log.d(tag, "Start collection");
        // Adding all detectors
        detectors.addAll(Get.Detectors(this,
                intent.getStringExtra("encryptedKurt"),
                intent.getStringExtra("secretKeyHash"), eventBus));
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
            detector.terminate();
            detector.prepareUpload();
        }
        detectors.clear();
    }

    private static class PossumHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                default:
                    Log.d(tag, "Message received:" + message);
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
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                handleIntent(intent.getStringExtra(Messaging.POSSUM_MESSAGE_TYPE));
            }
        };
        registerReceiver(receiver, new IntentFilter(Messaging.POSSUM_MESSAGE));
    }

    private void handleIntent(String action) {
        if (action == null) return;
        switch (action) {
            case Messaging.REQUEST_DETECTORS:
                Intent intent = new Intent(Messaging.POSSUM_MESSAGE);
                JsonArray detectorObjects = new JsonArray();
                for (AbstractDetector detector : detectors) {
                    detectorObjects.add(detector.toJson());
                }
                intent.putExtra(Messaging.POSSUM_MESSAGE_TYPE, Messaging.DETECTORS_STATUS);
                intent.putExtra(Messaging.DETECTORS, detectorObjects.toString());
                sendBroadcast(intent);
                break;
            default:
        }
    }

    /**
     * onDestroy - when service is about to die or is closed by upload, all sensors are
     * "decommissioned" and unListened to.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "Destroying Collector service");
        unregisterReceiver(receiver);
        clearAllDetectors();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
}