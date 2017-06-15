package com.telenor.possumlib.detectors;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

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
     * @param context        a valid android context
     * @param identification the encrypted kurt
     * @param eventBus       the event bus used for sending messages to and from
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public HardwareDetector(Context context, String identification, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, identification, eventBus, authenticating);
        findHardwareSpecs();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String requiredPermission() {
        return null;
    }

    @Override
    public int detectorType() {
        return DetectorType.Hardware;
    }

    @Override
    public String detectorName() {
        return "Hardware";
    }

    private void findHardwareSpecs() {
        // It should be sent for each time the app is instantiated, in case he updates his android
        sessionValues.add("HARDWARE_INFO START");
        sessionValues.add("Board:"+ Build.BOARD);
        sessionValues.add("Brand:"+Build.BRAND);
        sessionValues.add("Device:"+Build.DEVICE);
        sessionValues.add("Display:"+Build.DISPLAY);
        sessionValues.add("Fingerprint:"+Build.FINGERPRINT);
        sessionValues.add("Hardware:"+Build.HARDWARE);
        sessionValues.add("Host:"+Build.HOST);
        sessionValues.add("Id:"+Build.ID);
        sessionValues.add("Manufacturer:"+Build.MANUFACTURER);
        sessionValues.add("Model:"+Build.MODEL);
        sessionValues.add("Product:"+Build.PRODUCT);
        sessionValues.add("Serial:"+Build.SERIAL);
        sessionValues.add("Version:"+Build.VERSION.SDK_INT+" ("+Build.VERSION.CODENAME+")");
        sessionValues.add("SupportedABIS:"+ Get.supportedABISString());
        sessionValues.add("HARDWARE_INFO STOP");
        storeData();
    }
}