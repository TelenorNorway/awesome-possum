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
import android.util.Log;

import com.telenor.possumlib.constants.Constants;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;
import com.telenor.possumlib.interfaces.IPossumTrust;
import com.telenor.possumlib.services.CollectorService;
import com.telenor.possumlib.services.UploadService;
import com.telenor.possumlib.utils.Get;
import com.telenor.possumlib.utils.Has;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * SDK for handling all things related to the Awesome Possum project
 * <p>
 * The project is owned by Telenor Digital AS in collaboration with NR (Norsk Regnesentral).
 * The goal of the project is to help alleviate/remove passwords and the problem they cause by
 * identifying the user of the phone with the sensors available. Using these, it can perform
 * <br><br>
 * * Gait analysis<br>
 * * Face recognition<br>
 * * Ambient sound patterning<br>
 * * Positional placement over time<br>
 * * Behavioural analysis<br><br>
 * And more to come. Combined with Tensorflow and neural networks, it calculates the trustscore
 * (or likelyhood of accuracy) of you being you and by summing up all the different sensors input
 * can return a score granting you verification that you are you.
 */
public final class AwesomePossum {
    private static boolean initComplete = false;
    private static BroadcastReceiver trustReceiver;
    private static List<String> refusedDetectors;
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
        trustReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleTrustIntent(intent);
            }
        };
        context.registerReceiver(trustReceiver, new IntentFilter(Messaging.POSSUM_TRUST));

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
            intent.putExtra("isLearning", false);
            intent.putExtra("refusedDetectors", preferences.getString("refusedDetectors", null));
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
     * @param context a valid android context
     * @param encryptedKurt the encrypted key identifying the user
     * @param bucketKey the S3 amazon bucket key to upload to
     * @param allowMobile whether you allow the user to use mobile data (or just wifi) when uploading
     * @return true if successfully started, false if unable to start
     */
    public static boolean startUpload(@NonNull Context context, @NonNull String encryptedKurt, @NonNull String bucketKey, boolean allowMobile) {
        init(context);
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra("secretKeyHash", Get.secretKeyHash(preferences));
        intent.putExtra("encryptedKurt", encryptedKurt);
        intent.putExtra("refusedDetectors", preferences.getString("refusedDetectors", null));
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
     * Lets you set what detectors the user himself actually refuses to allow.<br>
     * The list is as follows:<br><br>
     * * "Accelerometer" - measures acceleration in phone, used in gait analysis <br>
     * * "AmbientSound" - uses the sound detected on use to sense surroundings <br>
     * * "Bluetooth" - surrounding bluetooth devices <br>
     * * "Gesture" - measures movement of touches on given components. Not used at the moment<br>
     * * "Gyroscope" - measures rotation of phone, used in gait analysis <br>
     * * "HardwareDetector" - cannot be refused. Stores information about the phone <br>
     * * "Image" - Pictures of the users face for face recognition <br>
     * * "Keyboard" - records time between keystrokes in given components. Not used at the moment. <br>
     * * "Magnetometer" - measures magnetic field in surroundings. Not used at the moment. <br>
     * * "MetaData" - cannot be refused. Stores information about internal events <br>
     * * "Network" - Wifi and network for movement analysis <br>
     * * "Position" - Position of the user for movement analysis <br>
     * * "Satellites" - records the position of satellites during positions. Not used at the moment. <br>
     *
     * @param usersRefusedDetectors list of detectors the user wants to allow. Null in means all.
     *                              If method is not called, all detectors are used.
     */
    public static void setUnwantedDetectors(@NonNull Context context, List<String> usersRefusedDetectors) {
        init(context);
        if (usersRefusedDetectors != null && usersRefusedDetectors.size() > 0) {
            refusedDetectors = usersRefusedDetectors;
            storeUnwanted(usersRefusedDetectors);
        } else {
            storeUnwanted(null);
        }
    }

    private static void storeUnwanted(List<String> unwanted) {
        SharedPreferences.Editor editor = preferences.edit();
        if (unwanted != null) {
            String refusedDetectors = "";
            boolean pastStart = false;
            for (String refused : unwanted) {
                if (pastStart) refusedDetectors+=",";
                refusedDetectors+=refused;
                pastStart = true;
            }
            editor.putString("refusedDetectors", refusedDetectors);
        } else {
            editor.remove("refusedDetectors");
        }
        editor.apply();
    }

    private static List<String> getUnwanted() {
        List<String> unwanted = new ArrayList<>();
        String refused = preferences.getString("refusedDetectors", null);
        if (refused != null) {
            unwanted.addAll(Arrays.asList(refused.split(",")));
        }
        return unwanted;
    }
    /**
     * Enables you to request the needed permissions
     *
     * @param activity an android activity to handle the requesting
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
     * Add listener for trustScore changes. Not implemented yet.
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
     * Change whether user should learn from the data gathered or not. Not used yet.
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
     * status. To receive it you will need to listen for a Broadcast event with the action
     * Messaging.POSSUM_MESSAGE ("PossumMessage")
     * <p>
     * The resulting intent will contain a jsonArray with jsonObjects for all detecotrs used
     *
     * @param context a valid android context
     */
    public static void requestDetectorStatus(@NonNull Context context) {
        init(context);
        Intent intent = new Intent(Messaging.POSSUM_MESSAGE);
        intent.putExtra(Messaging.TYPE, Messaging.REQUEST_DETECTORS);
        context.sendBroadcast(intent);
    }

    private static List<String> dangerousPermissions() {
        // Populate dangerous permissions
        List<String> dangerousPermissions = new ArrayList<>();
        try {
            if (refusedDetectors == null) {
                refusedDetectors = getUnwanted();
            }
            if (!(refusedDetectors.contains("Position") || refusedDetectors.contains("Satellites"))) {
                dangerousPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (!(refusedDetectors.contains("Image"))) {
                dangerousPermissions.add(Manifest.permission.CAMERA);
            }
            if (!(refusedDetectors.contains("AmbientSound"))) {
                dangerousPermissions.add(Manifest.permission.RECORD_AUDIO);
            }
        } catch (Exception e) {
            Log.e(tag, "Failed to get dangerous Permissions:",e);
        }
        return dangerousPermissions;
    }

    /**
     * Fire this method to tell the system that the user has approved of using the AwesomePossum
     * library. Until it is done, no data will be collected
     *
     * @param encryptedKurt the unique id reflecting the user who is authorized
     * @return true if authorized, false if not initialized yet
     */
    public static boolean authorizeGathering(@NonNull Context context, @NonNull String encryptedKurt) {
        init(context);
        preferences.edit().putString(Constants.ENCRYPTED_KURT, encryptedKurt).apply();
        return true;
    }

    /**
     * Call this method to check whether user has allowed the Awesome Possum to gether data
     *
     * @return true if allowed, false if not allowed yet (or if library isn't initialized yet)
     */
    private static boolean allowGathering() {
        return initComplete && preferences.getString(Constants.ENCRYPTED_KURT, null) != null;
    }

    /**
     * A default dialog for requesting authorization. Alternatively, manually create a dialog and
     * make sure it calls AwesomePossum.authorizeGathering() then before it starts to listen, calls
     * AwesomePossum.requestNeededPermissions(Activity) to
     *
     * @param activity an android activity
     * @param encryptedKurt the user id the dialog will authorize
     * @param title    the title of the dialog
     * @param message  the message of the dialog
     * @param ok       the ok button text of the dialog
     * @param cancel   the cancel button text of the dialog
     * @return a dialog that can be show()'ed
     */
    public static Dialog getAuthorizeDialog(@NonNull final Activity activity, @NonNull final String encryptedKurt, String title, String message, String ok, String cancel) {
        init(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AwesomePossum.authorizeGathering(activity, encryptedKurt);
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
        context.stopService(new Intent(context, CollectorService.class));
    }
}