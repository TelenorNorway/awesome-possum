package com.telenor.possumlib.constants;

import android.hardware.Sensor;

/***
 * Short hand implementation of sensor types used in the project
 */
public class DetectorType {
    public static final int Accelerometer = Sensor.TYPE_ACCELEROMETER;
    public static final int Gyroscope = Sensor.TYPE_GYROSCOPE;
    public static final int Magnetometer = Sensor.TYPE_MAGNETIC_FIELD;
    public static final int Significant = Sensor.TYPE_SIGNIFICANT_MOTION;
    public static final int Undefined = 999;
    public static final int Wifi = 100;
    public static final int Bluetooth = 101;
    public static final int Nfc = 103;
    public static final int Position = 104;
    public static final int GpsStatus = 105;
    public static final int Image = 106;
    public static final int Keyboard = 107;
    public static final int MetaData = 108;
    public static final int Gesture = 109;
    public static final int Hardware = 110;
    public static final int Audio = 111;
}