package com.telenor.possumlib.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.abstractservices.AbstractBasicService;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.functionality.GatheringFunctionality;
import com.telenor.possumlib.functionality.RestFunctionality;
import com.telenor.possumlib.interfaces.IPollComplete;
import com.telenor.possumlib.interfaces.IRestListener;
import com.telenor.possumlib.utils.Send;

import java.net.MalformedURLException;

/***
 * Service that handles all actions pertaining to collecting the data from the sensors.
 */
public class CollectionService extends AbstractBasicService implements IRestListener, IPollComplete {
    protected GatheringFunctionality gatheringFunctionality;
    private BroadcastReceiver receiver;
    private String uniqueUserId;
    private boolean isAuthenticating;
    private String url;
    private String apiKey;
    private Handler authHandler = new Handler(Looper.getMainLooper());
    private static final String tag = CollectionService.class.getName();

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
        // Ensures all detectors are terminated and cleared before adding new ones
        uniqueUserId = intent.getStringExtra("uniqueUserId");
        url = intent.getStringExtra("url");
        apiKey = intent.getStringExtra("apiKey");
        isAuthenticating = intent.getBooleanExtra("authenticating", false);
        if (uniqueUserId == null) {
            Log.e(tag, "Missing needed value in intent. Unique user id is null. Terminating service..");
            Send.messageIntent(this, Messaging.COLLECTION_FAILED, "Missing unique user id in service");
            stopSelf();
        } else {
            gatheringFunctionality.setDetectorsWithId(this, uniqueUserId, isAuthenticating, this);
            if (gatheringFunctionality.isGathering()) {
                gatheringFunctionality.stopGathering();
                gatheringFunctionality.clearData();
            }
            gatheringFunctionality.startGathering();
            if (isAuthenticating) {
                authHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                }, authTime());
            }
        }
        return super.onStartCommand(intent, flags, requestCode);
    }

    /**
     * onCreate - starts up all relevant sensors and setting service as a foreground service
     * Important to note: onCreate is always started before onStartCommand, in effect initialising
     * all variables and starting the service in the foreground
     */
    @Override
    public void onCreate() {
        super.onCreate();
        gatheringFunctionality = new GatheringFunctionality();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                handleIntent(intent.getStringExtra(Messaging.POSSUM_MESSAGE_TYPE));
            }
        };
        registerReceiver(receiver, new IntentFilter(Messaging.POSSUM_MESSAGE));
    }

    private int authTime() {
        return 5000;
    }

    private void handleIntent(String action) {
        if (action == null) return;
        switch (action) {
            case Messaging.REQUEST_DETECTORS:
                Send.messageIntent(this, Messaging.DETECTORS_STATUS, gatheringFunctionality.detectorsAsJson().toString());
                break;
            default:
        }
    }

    public void pollComplete(AbstractDetector detector) {
    }

    /**
     * onDestroy - when service is about to die or is closed by upload, all sensors are
     * "decommissioned" and unListened to.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.d(tag, "Destroying Collector service:"+this);
        unregisterReceiver(receiver);
        gatheringFunctionality.stopGathering();
        receiver = null;
        try {
            if (isAuthenticating) {
                RestFunctionality restFunctionality = new RestFunctionality(CollectionService.this, url, uniqueUserId, apiKey);
                restFunctionality.execute(gatheringFunctionality.detectors());
            }
        } catch (MalformedURLException e) {
            Log.e(tag, "Failed to post data due to malformed url:", e);
        }
    }

    @Override
    public void successfullyPushed(String message) {
        JsonParser parser = new JsonParser();
        Log.i(tag, "Pushed data to rest service:" + message);
        JsonObject object = (JsonObject) parser.parse(message);
        if (object.get("errorMessage") != null) {
            Log.d(tag, "Failed to access:" + object);
            return;
        }
        Intent intent = new Intent(Messaging.POSSUM_TRUST);
        intent.putExtra("message", object.toString());
        sendBroadcast(intent);
        Log.d(tag, "Sent broadcast");
        // Data is not stored to file, so just let it die
        stopSelf();
    }

    @Override
    public void failedToPush(Exception exception) {
        Log.e(tag, "Failed to push to rest service:", exception);
        // Data is not stored to file, so just let it die
        stopSelf();
    }
}