package com.telenor.possumlib;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.constants.Constants;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;
import com.telenor.possumlib.interfaces.IPossumTrust;
import com.telenor.possumlib.managers.EventManager;
import com.telenor.possumlib.managers.S3ModelDownloader;
import com.telenor.possumlib.detectors.MetaDataDetector;
import com.telenor.possumlib.services.CollectorService;
import com.telenor.possumlib.services.UploadService;
import com.telenor.possumlib.utils.Get;
import com.telenor.possumlib.utils.Has;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * SDK for handling all things related to the Awesome Possum project
 * <p>
 * The project is owned by Telenor Digital AS in collaboration with NR (Norsk Regnesentral).
 * The goal of the project is to help alleviate/remove passwords and the problem they cause by
 * identifying the user of the phone with the sensors available. Using these, it can perform
 * * Gait analysis
 * * Face recognition
 * * Ambient sound patterning
 * * Positional placement over time
 * * Behavioural analysis
 * And more to come. Combined with Tensorflow and neural networks, it calculates the trustscore
 * (or likelyhood of accuracy) of you being you and by summing up all the different sensors input
 * can return a score granting you verification that you are you.
 */
public final class AwesomePossum {
    private static boolean initComplete = false;
    private static EventManager eventManager;
    private static EventBus eventBus = new EventBus();
    private static BroadcastReceiver trustReceiver;
    private static final String tag = AwesomePossum.class.getName();
    private static Set<IPossumTrust> trustListeners = new ConcurrentSkipListSet<>();
    private static SharedPreferences preferences;

    /**
     * Main method of controlling all sensors. This static class is init'ed by calling this method.
     * This is usually done in the application level
     *
     * @param context context for further reference
     */
    private static void init(@NonNull Context context) {
        if (initComplete) return;
        initComplete = true;
        preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        context = context.getApplicationContext();
        JodaTimeAndroid.init(context);
        S3ModelDownloader.init(context);
        trustReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleTrustIntent(intent);
            }
        };
        context.registerReceiver(trustReceiver, new IntentFilter(Messaging.POSSUM_TRUST));
        eventManager = new EventManager(context);

        // In case of first time start, set installation time
        long startTime = preferences.getLong(Constants.START_TIME, 0);
        if (startTime == 0) {
            preferences.edit().putLong(Constants.START_TIME,
                    DateTime.now().getMillis()).apply();
        }
    }

    private static void handleTrustIntent(Intent intent) {
        int detectorType = intent.getIntExtra("detector", 0);
        if (detectorType == 0) return;
        int newTrustScore = intent.getIntExtra("trustScore", 0);
        int combinedTrustScore = intent.getIntExtra("totalTrustScore", 0);
        for (IPossumTrust listener : trustListeners) {
            listener.changeInTrust(detectorType, newTrustScore, combinedTrustScore);
        }
    }

    /**
     * Starts to listen while app is running
     *
     * @throws GatheringNotAuthorizedException If the user hasn't accepted the app, this exception is thrown
     */
    public static void listen(@NonNull Context context) throws GatheringNotAuthorizedException {
        init(context);
        if (allowGathering()) {
            Intent intent = new Intent(context, CollectorService.class);
            intent.putExtra("isLearning", false); //isLearning()
            intent.putExtra("hardwareStored", false);
            intent.putExtra("secretKeyHash", Get.secretKeyHash(preferences));
            context.startService(intent);
        } else {
            throw new GatheringNotAuthorizedException();
        }
    }

    /**
     * Starts an upload of collected data, should it be needed. Otherwise it will start uploads at
     * regular intervals or when it detects that it is about to be shut down.
     *
     * @param context an android context
     * @return true if successfully started, false if unable to start
     */
    public static boolean startUpload(@NonNull Context context, String bucketKey, boolean allowMobile) {
        init(context);
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra("secretKeyHash", Get.secretKeyHash(preferences));
        intent.putExtra("hardwareStored", preferences.getBoolean(Constants.HARDWARE_STORED, false));
        intent.putExtra("bucketKey", bucketKey);
        if (allowMobile) {
            if (Has.network(context)) {
                context.startService(intent);
                return true;
            }
        } else {
            if (Has.wifi(context)) {
                context.startService(intent);
                return true;
            }
        }
        return false;
    }

    /**
     * Enables you to request the needed permissions
     *
     * @param activity an android activity to handle
     * @return true if no permissions are missing, else false
     */
    public static boolean requestNeededPermissions(@NonNull Activity activity) {
        init(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> missingPermissions = new ArrayList<>();
            for (String permission : dangerousPermissions()) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }
            if (missingPermissions.size() > 0) {
                ActivityCompat.requestPermissions(activity, missingPermissions.toArray(new String[]{}), Constants.AwesomePossumPermissionsRequestCode);
                return false;
            } else return true;
        } else return true;
    }

    /**
     * Add listener for trustscore changes. Not implemented yet.
     *
     * @param listener listener for changes to trustscore
     */
    public void addTrustListener(IPossumTrust listener) {
        trustListeners.add(listener);
    }

    /**
     * Remove listener for trustscore changes. Not implemented yet.
     *
     * @param listener listener for changes to trustscore
     */
    public void removeTrustListener(IPossumTrust listener) {
        trustListeners.remove(listener);
    }

    /**
     * This method is essential for terminating all sensors dependencies as well as internal
     * managers. Run this in your application onDestroy
     *
     * @param context the application context
     */
    public static void terminate(@NonNull Context context) {
        if (!initComplete) return;
        initComplete = false;
        stopListening(context);
        context.unregisterReceiver(trustReceiver);
        eventManager.terminate(context);
        trustListeners.clear();
    }

    /**
     * Checks whether the user is learning from the gathering or not
     *
     * @return true if user is learning as well as gathering, false if not or if library not
     * initialized
     */
    public static boolean isLearning() {
        return initComplete && preferences.getBoolean(Constants.IS_LEARNING, false);
    }

    /**
     * Change whether user should learn from the data gathered or not
     *
     * @param context    a valid android context
     * @param isLearning true for learning, false to stop learning
     */
    public static void setLearning(@NonNull Context context, boolean isLearning) {
        init(context);
        preferences.edit().putBoolean(Constants.IS_LEARNING, isLearning).apply();
        Intent intent = new Intent(Messaging.POSSUM_MESSAGE);
        intent.putExtra(Messaging.TYPE, Messaging.LEARNING);
        context.sendBroadcast(intent);
    }

    /**
     * Sends a request to the service (if it is listening) that you want an update on the sensors
     * status. To receive it you will need to listen for a Broadcasted event with the action
     * Messaging.POSSUM_MESSAGE ("PossumMessage")
     * <p>
     * The resulting intent will contain
     *
     * @param context a valid android context
     */
    public static void requestSensorStatus(@NonNull Context context) {
        init(context);
        Intent intent = new Intent(Messaging.POSSUM_MESSAGE);
        intent.putExtra(Messaging.TYPE, Messaging.REQUEST_SENSORS);
        context.sendBroadcast(intent);
    }

    private static List<String> dangerousPermissions() {
        // Populate dangerous permissions
        List<String> dangerousPermissions = new ArrayList<>();
        dangerousPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        dangerousPermissions.add(Manifest.permission.CAMERA);
        dangerousPermissions.add(Manifest.permission.RECORD_AUDIO);
        return dangerousPermissions;
    }

    /**
     * Fire this method to tell the system that the user has approved of using the AwesomePossum
     * library. Until it is done, no data will be collected
     *
     * @return true if authorized, false if not initialized yet
     */
    public static boolean authorizeGathering() {
        if (!initComplete) return false;
        preferences.edit().putBoolean(Constants.ALLOW_GATHERING, true).apply();
        return true;
    }

    /**
     * Call this method to check whether user has allowed the Awesome Possum to gether data
     *
     * @return true if allowed, false if not allowed yet (or if library isn't initialized yet)
     */
    private static boolean allowGathering() {
        return initComplete && preferences.getBoolean(Constants.ALLOW_GATHERING, false);
    }

    /**
     * A default dialog for requesting authorization. Alternatively, manually create a dialog and
     * make sure it calls AwesomePossum.authorizeGathering() then before it starts to listen, calls
     * AwesomePossum.requestNeededPermissions(Activity) to
     *
     * @param activity an android activity
     * @param title    the title of the dialog
     * @param message  the message of the dialog
     * @param ok       the ok button text of the dialog
     * @param cancel   the cancel button text of the dialog
     * @return a dialog that can be show()'ed
     */
    public static Dialog getAuthorizeDialog(@NonNull final Activity activity, String title, String message, String ok, String cancel) {
        init(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AwesomePossum.authorizeGathering();
                AwesomePossum.requestNeededPermissions(activity);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    /**
     * Stops listening for data. This is explicitly called during terminate(@Context), which also
     * cleans up
     *
     * @param context a valid android context
     */
    public static void stopListening(@NonNull Context context) {
//        Intent intent = new Intent(Messaging.POSSUM_INTERNAL_MESSAGE);
//        intent.setPackage(context.getPackageName());
        // TODO: This will NOT reach the service as it is in a different process. Use intent
        eventBus.post(new MetaDataChangeEvent(MetaDataDetector.GENERAL_EVENT, "program terminated listening"));
//        intent.putExtra("")
        context.stopService(new Intent(context, CollectorService.class));
    }
}