package com.telenor.possumlib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Base64;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Chars;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.Constants;
import com.telenor.possumlib.detectors.Accelerometer;
import com.telenor.possumlib.detectors.AmbientSoundDetector;
import com.telenor.possumlib.detectors.BluetoothDetector;
import com.telenor.possumlib.detectors.GestureDetector;
import com.telenor.possumlib.detectors.GyroScope;
import com.telenor.possumlib.detectors.HardwareDetector;
import com.telenor.possumlib.detectors.ImageDetector;
import com.telenor.possumlib.detectors.LocationDetector;
import com.telenor.possumlib.detectors.Magnetometer;
import com.telenor.possumlib.detectors.MetaDataDetector;
import com.telenor.possumlib.detectors.NetworkDetector;
import com.telenor.possumlib.detectors.SatelliteDetector;
import com.telenor.possumlib.detectors.TypingRecognitionSensor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Get {
    private static String secretKeyHash;
    /**
     * Lookup table for computing the secret key checksum character.
     * It's web safe base 64 with some adjustments to avoid easily confused characters.
     * @see #encodeReadably
     */
    private static final char READABLE_ENCODER[] = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', '!', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', '#', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '$', '*', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_',
    };

    /**
     * Handy method for returning the supported ABI's
     *
     * @return a formatted text string with supported abis
     */
    public static String supportedABISString() {
        String output = "";
        List<String> supported = supportedABIList();
        for (int i = 0; i < supported.size(); i++) {
            if (i > 0) {
                output += ", ";
            }
            output += supported.get(i);
        }
        return output;
    }

    /**
     * Yields a list of all supported ABIs
     *
     * @return a list of supported abis
     */
    private static List<String> supportedABIList() {
        List<String> supported = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Collections.addAll(supported, Build.SUPPORTED_ABIS);
        } else {
            supported.add(Build.CPU_ABI);
            supported.add(Build.CPU_ABI2);
        }
        return supported;
    }

    /**
     * Returns a secret key. NOTE: This CANNOT be called in separate processes, meaning it can only
     * be called from the AwesomePossum in main thread part.
     * @param preferences a shared preferences to search for key in
     * @return a secret key hash
     */
    @MainThread
    public static String secretKeyHash(@NonNull SharedPreferences preferences) {
        if (secretKeyHash != null) return secretKeyHash;
        String secretKey = preferences.getString(Constants.SECRET_KEY_KEY, null);
        if (secretKey == null) {
            secretKey = createSecretKey();
            preferences.edit().putString(Constants.SECRET_KEY_KEY, secretKey).apply();
        }
        secretKeyHash = secureHash(secretKey);
        return secretKeyHash;
    }

    @NonNull
    @VisibleForTesting
    private static String createSecretKey() {
        byte[] bytes = new byte[15];
        new SecureRandom().nextBytes(bytes);
        String secretPart = encodeReadably(bytes);
        return secretPart + checksum(secretPart);
    }

    private static String secureHash(String secretKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] tmp = digest.digest(secretKey.getBytes(Charsets.UTF_8));
            return encode(tmp, 0, tmp.length / 2); // Only use the first 192 bits
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // A variant of the Damm algorithm, cf. https://stackoverflow.com/a/23433934.
    @VisibleForTesting
    private static char checksum(String secretPart) {
        int res = 0;
        for (char c : secretPart.toCharArray()) {
            int i = Chars.indexOf(READABLE_ENCODER, c);
            res = (res ^ i) * 2;
            if (res >= 64) {
                res = res ^ 67;
            }
        }
        return READABLE_ENCODER[res];
    }

    /**
     * @see #READABLE_ENCODER
     */
    @VisibleForTesting
    private static String encodeReadably(byte[] bytes) {
        return encode(bytes, 0, bytes.length)
                // Avoid similar looking characters
                .replace('O', '!').replace('l', '#')
                .replace('0', '$').replace('1', '*');
    }

    private static String encode(byte[] bytes, int offset, int length) {
        return Base64.encodeToString(bytes, offset, length, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
    }

    /**
     * Gets the version currently being used
     * @param context a valid android context
     * @return a string with the current version
     */
    public static String version(@NonNull Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "invalid";
        }
    }

    public static List<AbstractDetector> Detectors(@NonNull Context context, String encryptedKurt, String secretKeyHash, List<Class<? extends AbstractDetector>> ignoreList, @NonNull EventBus eventBus) {
        List<AbstractDetector> detectors = new ArrayList<>();
        if (ignoreList == null || !ignoreList.contains(MetaDataDetector.class))
            detectors.add(new MetaDataDetector(context, encryptedKurt, secretKeyHash, eventBus)); // Should always be first in line
        if (ignoreList == null || !ignoreList.contains(HardwareDetector.class))
            detectors.add(new HardwareDetector(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(Accelerometer.class))
            detectors.add(new Accelerometer(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(GyroScope.class))
            detectors.add(new GyroScope(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(LocationDetector.class))
            detectors.add(new LocationDetector(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(BluetoothDetector.class))
            detectors.add(new BluetoothDetector(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(NetworkDetector.class))
            detectors.add(new NetworkDetector(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(AmbientSoundDetector.class))
            detectors.add(new AmbientSoundDetector(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(ImageDetector.class))
            detectors.add(new ImageDetector(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(TypingRecognitionSensor.class))
            detectors.add(new TypingRecognitionSensor(context, encryptedKurt, secretKeyHash, eventBus));
        if (ignoreList == null || !ignoreList.contains(GestureDetector.class))
            detectors.add(new GestureDetector(context, encryptedKurt, secretKeyHash, eventBus));
        return detectors;
    }

    public static List<Class<? extends AbstractDetector>> ignoredDetectors(List<String> refused) {
        List<Class<? extends AbstractDetector>> ignoreList = new ArrayList<>();
        if (refused.contains("Accelerometer")) ignoreList.add(Accelerometer.class);
        if (refused.contains("AmbientSounds")) ignoreList.add(AmbientSoundDetector.class);
        if (refused.contains("Bluetooth")) ignoreList.add(BluetoothDetector.class);
        if (refused.contains("Gesture")) ignoreList.add(GestureDetector.class);
        if (refused.contains("Gyroscope")) ignoreList.add(GyroScope.class);
        if (refused.contains("HardwareDetector")) ignoreList.add(HardwareDetector.class);
        if (refused.contains("Image")) ignoreList.add(ImageDetector.class);
        if (refused.contains("Keyboard")) ignoreList.add(TypingRecognitionSensor.class);
        if (refused.contains("Magnetometer")) ignoreList.add(Magnetometer.class);
        if (refused.contains("MetaData")) ignoreList.add(MetaDataDetector.class);
        if (refused.contains("Network")) ignoreList.add(NetworkDetector.class);
        if (refused.contains("Position")) ignoreList.add(LocationDetector.class);
        if (refused.contains("Satellites")) ignoreList.add(SatelliteDetector.class);
        return ignoreList;
    }
}