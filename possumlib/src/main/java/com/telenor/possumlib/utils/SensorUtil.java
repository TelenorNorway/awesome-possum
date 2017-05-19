package com.telenor.possumlib.utils;

import com.telenor.possumlib.constants.DetectorType;

public class SensorUtil {
    public static String detectorTypeString(int sensorType) {
        switch (sensorType) {
            case DetectorType.Accelerometer: return "Accelerometer";
            case DetectorType.Gyroscope: return "GyroScope";
            case DetectorType.Magnetometer: return "Magnetometer";
            case DetectorType.Bluetooth: return "Bluetooth";
            case DetectorType.Wifi: return "Network";
            case DetectorType.Position: return "Position";
            case DetectorType.GpsStatus: return "Satellites";
            case DetectorType.Nfc: return "NFC";
            case DetectorType.Image: return "Image";
            case DetectorType.Keyboard: return "Keyboard";
            case DetectorType.MetaData: return "MetaData";
            case DetectorType.Gesture: return "Gesture";
            case DetectorType.Significant: return "SignificantMotion";
            case DetectorType.Hardware: return "Hardware";
            case DetectorType.Audio: return "Audio";
            default:
                throw new RuntimeException("Unknown detectorType:"+sensorType);
        }
    }
}