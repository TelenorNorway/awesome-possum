package com.telenor.possumlib.constants;

public class Constants {
    public static final String SHARED_PREFERENCES = "AwesomePossumPrefs";
    public static final int AwesomePossumPermissionsRequestCode = 1337;

    // status constants
    public static final String IS_GATHERING = "serviceGathering";
    public static final String ALLOW_GATHERING = "allowGathering";
    public static final String IS_LEARNING = "isLearning";

    // Important events
    public static final String UPLOAD_EVENT = "uploadEvent";
    public static final String BLUETOOTH_EVENT = "BLUETOOTH_EVENT";
    public static final String START_UPLOAD = "startUpload";
    public static final String UPLOAD_LAST = "uploadLast";
    public static final String GATHER_STARTED = "GATHER STARTED";
    public static final String GATHER_PAUSED = "GATHER PAUSED";

    // These should not be part of the library...I think...
    public static final String START_TIME = "startTime";
    public static final String TOTAL_DATA_UPLOADED = "totalDataUploaded";
    public static final String UPLOAD_TIMES = "uploadTimes";
    public static final String UPLOAD_SIZES = "uploadSizes";

    public static final String SECRET_KEY_KEY = "secretKeyHash";
    public static final String SOFT_ID = "softId";
    public static final String HARDWARE_STORED = "HardwareStored";
}