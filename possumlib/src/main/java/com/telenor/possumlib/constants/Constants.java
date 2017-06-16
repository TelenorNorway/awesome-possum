package com.telenor.possumlib.constants;

public class Constants {
    public static final String SHARED_PREFERENCES = "AwesomePossumPrefs";
    public static final int AwesomePossumPermissionsRequestCode = 1337;

    // status constants
    public static final String ENCRYPTED_KURT = "encryptedKurt"; // Stored Kurt, Confirmed
    public static final String ENCRYPTED_TEMP_KURT = "tempEncryptedKurt"; // Temp store until confirmed from S3
    public static final String IS_LEARNING = "isLearning";

    // These should not be part of the library...I think...
    public static final String START_TIME = "startTime";
    public static final String BUCKET = "telenor-nr-awesome-possum";
}