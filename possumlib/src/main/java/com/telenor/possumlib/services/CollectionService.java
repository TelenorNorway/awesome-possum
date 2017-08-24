package com.telenor.possumlib.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.abstractservices.AbstractBasicService;
import com.telenor.possumlib.asynctasks.modelloaders.TensorLoad;
import com.telenor.possumlib.constants.Constants;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.detectors.BluetoothDetector;
import com.telenor.possumlib.functionality.GatheringFunctionality;
import com.telenor.possumlib.functionality.RestFunctionality;
import com.telenor.possumlib.interfaces.IRestListener;
import com.telenor.possumlib.utils.Send;

import java.net.MalformedURLException;

/***
 * Service that handles all actions pertaining to collecting the data from the sensors.
 */
public class CollectionService extends AbstractBasicService implements IRestListener {
    protected GatheringFunctionality gatheringFunctionality;
    private BroadcastReceiver receiver;
    private static boolean isAuthenticating;
    private static String url;
    private static String apiKey;
    private long startTime;
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
        final String uniqueUserId = intent.getStringExtra("uniqueUserId");
        startTime = intent.getLongExtra("startTime", 0);
        url = intent.getStringExtra("url");
        apiKey = intent.getStringExtra("apiKey");
        isAuthenticating = intent.getBooleanExtra("authenticating", false);
        if (uniqueUserId != null) {
            gatheringFunctionality.setAuthenticationState(isAuthenticating);
            gatheringFunctionality.setUniqueUserId(uniqueUserId);
            if (gatheringFunctionality.isGathering()) {
                gatheringFunctionality.stopGathering(false);
//                gatheringFunctionality.clearData();
            }
            gatheringFunctionality.startGathering();
            if (isAuthenticating) {
                authHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performAuth(uniqueUserId);
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
        gatheringFunctionality = new GatheringFunctionality(this);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                handleIntent(intent.getStringExtra(Messaging.POSSUM_MESSAGE_TYPE));
            }
        };
        getApplicationContext().registerReceiver(receiver, new IntentFilter(Messaging.POSSUM_MESSAGE));
        // Loading all the models, letting the listener be the gatherer
        TensorLoad tensorLoad = new TensorLoad(this, gatheringFunctionality);
        tensorLoad.execute((Void)null);
        AwesomePossum.sendDetectorStatus(this);
    }

    private int authTime() {
        return Constants.AUTHENTICATION_TIME;
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

    /**
     * onDestroy - when service is about to die. Ideally happens by android recycling it
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "Destroying Collector service:"+this);
        getApplicationContext().unregisterReceiver(receiver);
        gatheringFunctionality.stopGathering(true);
        receiver = null;
    }

    private void performAuth(String uniqueUserId) {
        try {
            Send.messageIntent(this, Messaging.START_SERVER_DATA_SEND, ""+System.currentTimeMillis());
            if (isAuthenticating) {
                JsonObject object = new JsonObject();
                object.addProperty("connectId", uniqueUserId);

                for (AbstractDetector detector : gatheringFunctionality.detectors()) {
                    detector.stopListening();
                    JsonArray jsonData = detector.jsonData();
                    object.add(detector.detectorName(), jsonData);
                    if (detector instanceof BluetoothDetector) {
                        Send.messageIntent(this, Messaging.POSSUM_MESSAGE, "Found bluetooth:"+jsonData.toString());
                    }
                    detector.clearData();
                }
                RestFunctionality restFunctionality = new RestFunctionality(this, url, apiKey);
                restFunctionality.execute(object);
//                Send.messageIntent(this, Messaging.WAITING_FOR_SERVER_RESPONSE, "Time spent since auth start to send start:"+(System.currentTimeMillis()-startTime));
                Send.messageIntent(this, Messaging.WAITING_FOR_SERVER_RESPONSE, ""+System.currentTimeMillis());
            }
        } catch (MalformedURLException e) {
            Log.e(tag, "Failed to post data due to malformed url:", e);
        }
    }

    @Override
    public void successfullyPushed(String message) {
        JsonParser parser = new JsonParser();
        //Log.d(tag, "Pushed data to rest service:" + message);
        JsonObject object = (JsonObject) parser.parse(message);
        if (object.get("errorMessage") != null) {
            Log.d(tag, "Failed to access:" + object);
            return;
        }
        Intent intent = new Intent(Messaging.POSSUM_TRUST);
        intent.putExtra("message", object.toString());
        sendBroadcast(intent);
        Send.messageIntent(this, Messaging.AUTH_DONE, null);
        // Data is not stored to file, so just let it die
    }

    @Override
    public void failedToPush(Exception exception) {
        Log.e(tag, "Failed to push to rest service:", exception);
        // Data is not stored to file, so just let it die
    }
}