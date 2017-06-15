package com.telenor.possumlib.abstractservices;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.Get;
import com.telenor.possumlib.utils.Send;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.concurrent.ConcurrentLinkedQueue;

/***
 * Service that handles all actions pertaining to collecting the data from the sensors.
 */
public abstract class AbstractCollectionService extends Service {
    protected String encryptedKurt;
    private static final ConcurrentLinkedQueue<AbstractDetector> detectors = new ConcurrentLinkedQueue<>();
    private BroadcastReceiver receiver;
    private PossumBus eventBus = new PossumBus();
    private Handler terminationHandler = new Handler(Looper.getMainLooper());
    private final static Messenger messenger = new Messenger(new PossumHandler());
    private static final String tag = AbstractCollectionService.class.getName();

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
        encryptedKurt = intent.getStringExtra("encryptedKurt");
        if (encryptedKurt == null) {
            Log.e(tag, "Missing needed value in intent. EncryptedKurt is null. Terminating service..");
            Send.messageIntent(this, Messaging.COLLECTION_FAILED, "Missing kurtId in service");
            stopSelf();
        } else {
            clearAllDetectors();
            Log.d(tag, "Start collection:"+isAuthenticating());
            // Adding all detectors
            detectors.addAll(Get.Detectors(this, encryptedKurt, eventBus, isAuthenticating()));
            for (AbstractDetector detector : detectors) {
                detector.startListening();
            }
            if (timeSpentGathering() > 0) {
                terminationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performPostAction();
                    }
                }, timeSpentGathering());
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * Defines whether the service is authenticating or data gathering
     * @return true if authenticating, false if gathering
     */
    protected abstract boolean isAuthenticating();

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
                    Log.d(tag, "Service received message:" + message);
            }
        }
    }

    /**
     * Defines function to do something after collection is complete
     */
    public abstract void performPostAction();

    /**
     * onCreate - starts up all relevant sensors and setting service as a foreground service
     * Important to note: onCreate is always started before onStartCommand, in effect initialising
     * all variables and starting the service in the foreground
     */
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
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
                JsonArray detectorObjects = new JsonArray();
                for (AbstractDetector detector : detectors) {
                    detectorObjects.add(detector.toJson());
                }
                Send.messageIntent(this, Messaging.DETECTORS_STATUS, detectorObjects.toString());
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
        Log.d(tag, "Destroying Collector service:"+this);
        unregisterReceiver(receiver);
        receiver = null;
        clearAllDetectors();
    }

    /**
     * The time spent gathering data before it either stops itself or uses the data for an
     * authentication attempt. If time spent is 0, it equals waiting until terminated
     * @return the time in milliseconds
     */
    public abstract long timeSpentGathering();

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
}