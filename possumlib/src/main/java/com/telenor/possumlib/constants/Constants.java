package com.telenor.possumlib.constants;

public class Constants {
    public static final String SHARED_PREFERENCES = "AwesomePossumPrefs";
    public static final int AwesomePossumPermissionsRequestCode = 1337;

    // status constants
    public static final String UNIQUE_USER_ID = "uniqueUserId"; // the unique user id
    public static final String TEMP_UNIQUE_USER_ID = "tempUniqueUserId"; // Temp store until confirmed from S3
    public static final String IS_LEARNING = "isLearning";

    // These should not be part of the library...I think...
    public static final String START_TIME = "startTime";
    public static final String BUCKET = "telenor-nr-awesome-possum";

    // Defines how long it should spend trying to gather data for authentication. Default = 5 seconds (in milliseconds)
    public static final int AUTHENTICATION_TIME = 3000;
}