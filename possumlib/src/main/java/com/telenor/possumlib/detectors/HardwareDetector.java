package com.telenor.possumlib.detectors;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.Get;

/**
 * Detector meant to detect hardware info, storing it in a seperate file, instead of storing it in
 * the metaData
 */
public class HardwareDetector extends AbstractDetector {
    /**
     * Constructor for the Hardware Detector
     *
     * @param context  a valid android context
     * @param eventBus the event bus used for sending messages to and from
     */
    public HardwareDetector(Context context, @NonNull PossumBus eventBus) {
        super(context, eventBus);
    }

    @Override
    public int detectorType() {
        return DetectorType.Hardware;
    }

    @Override
    public String detectorName() {
        return "hardware";
    }

    private void findHardwareSpecs() {
        // It should be sent for each time the app is instantiated, in case he updates his android
        if (isAuthenticating()) return;
        JsonArray array = new JsonArray();
        array.add("HARDWARE_INFO START");
        array.add("Board:" + Build.BOARD);
        array.add("Brand:" + Build.BRAND);
        array.add("Device:" + Build.DEVICE);
        array.add("Display:" + Build.DISPLAY);
        array.add("Fingerprint:" + Build.FINGERPRINT);
        array.add("Hardware:" + Build.HARDWARE);
        array.add("Host:" + Build.HOST);
        array.add("Id:" + Build.ID);
        array.add("Manufacturer:" + Build.MANUFACTURER);
        array.add("Model:" + Build.MODEL);
        array.add("Product:" + Build.PRODUCT);
        array.add("Serial:" + Build.SERIAL);
        array.add("Version:" + Build.VERSION.SDK_INT + " (" + Build.VERSION.CODENAME + ")");
        array.add("SupportedABIS:" + Get.supportedABISString());
        array.add("HARDWARE_INFO STOP");
        sessionValues.add(array);
        storeData();
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            findHardwareSpecs();
        }
        return listen;
    }
}