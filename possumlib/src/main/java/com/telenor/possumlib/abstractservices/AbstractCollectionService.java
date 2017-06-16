package com.telenor.possumlib.abstractservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.functionality.GatheringFunctionality;
import com.telenor.possumlib.functionality.RestFunctionality;
import com.telenor.possumlib.interfaces.IRestListener;
import com.telenor.possumlib.utils.Send;

import java.net.MalformedURLException;

/***
 * Service that handles all actions pertaining to collecting the data from the sensors.
 */
public abstract class AbstractCollectionService extends AbstractBasicService implements IRestListener {
    protected GatheringFunctionality gatheringFunctionality;
    private BroadcastReceiver receiver;
    private RestFunctionality restFunctionality;
    private String encryptedKurt;
    private Handler terminationHandler = new Handler(Looper.getMainLooper());
    private String url;
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
        // Ensures all detectors are terminated and cleared before adding new ones
        encryptedKurt = intent.getStringExtra("encryptedKurt");
        url = intent.getStringExtra("url");
        if (encryptedKurt == null) {
            Log.e(tag, "Missing needed value in intent. EncryptedKurt is null. Terminating service..");
            Send.messageIntent(this, Messaging.COLLECTION_FAILED, "Missing kurtId in service");
            stopSelf();
        } else {
            gatheringFunctionality.setDetectorsWithKurtId(this, encryptedKurt, isAuthenticating());
            gatheringFunctionality.startGathering();
            if (timeSpentGathering() > 0) {
                terminationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAuthenticating()) {
                            try {
                                restFunctionality.postData(url, encryptedKurt, gatheringFunctionality.detectors());
                            } catch (MalformedURLException e) {
                                Log.e(tag, "Failed to post data due to malformed url:",e);
                                stopSelf();
                            }
                        } else {
                            stopSelf();
                        }
                    }
                }, timeSpentGathering());
            }
        }
        return super.onStartCommand(intent, flags, requestCode);
    }

    /**
     * Defines whether the service is authenticating or data gathering
     * @return true if authenticating, false if gathering
     */
    protected abstract boolean isAuthenticating();

    /**
     * onCreate - starts up all relevant sensors and setting service as a foreground service
     * Important to note: onCreate is always started before onStartCommand, in effect initialising
     * all variables and starting the service in the foreground
     */
    @Override
    public void onCreate() {
        super.onCreate();
        gatheringFunctionality = new GatheringFunctionality();
        restFunctionality = new RestFunctionality(this);
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
                Send.messageIntent(this, Messaging.DETECTORS_STATUS, gatheringFunctionality.detectorsAsJson().toString());
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
        gatheringFunctionality.stopGathering();
        receiver = null;
    }

    /**
     * The time spent gathering data before it either stops itself or uses the data for an
     * authentication attempt. If time spent is 0, it equals waiting until terminated
     * @return the time in milliseconds
     */
    public abstract long timeSpentGathering();

    @Override
    public void successfullyPushed() {
        Log.i(tag, "Pushed data to rest service");
        stopSelf();
    }

    @Override
    public void failedToPush(Exception exception) {
        Log.e(tag, "Failed to push to rest service:",exception);
        stopSelf();
    }
}