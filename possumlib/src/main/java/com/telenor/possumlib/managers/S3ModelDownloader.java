package com.telenor.possumlib.managers;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Handles checking for new versions of models
 */
public class S3ModelDownloader {
    public static final String S3_BUCKET = "telenor-nr-awesome-possum";
    private static boolean init;
    private static final String tag = S3ModelDownloader.class.getName();
    private static String secretKeyHash;


    public static void init(@NonNull Context context, @NonNull String secretKey) {
        secretKeyHash = secretKey;
        init(context);
    }

    public static void init(@NonNull Context context) {
        if (init) {
            return;
        }
        init = true;
//        if (secretKeyHash == null) {
//            setSecretKey(context);
//        }
    }

//    private static void setSecretKey(Context context) {
//        PreferenceUtil.init(context);
//        try {
//            String secretKeyHash = PreferenceUtil.preferences().getString(Constants.SECRET_KEY_KEY, null);
//            if (secretKeyHash == null) {
//                secretKeyHash = createSecretKey();
//                PreferenceUtil.preferences().edit().putString(Constants.SECRET_KEY_KEY, secretKeyHash).apply();
//            }
//            secretKeyHash = secureHash(secretKeyHash);
//        } catch (NotInitializedException ignore) {
//            Log.e(tag, "Unable to set secret key - preferences not initialized");
//        }
//    }

//
//    private static TransferUtility transferUtility(@NonNull Context context) {
//        return new TransferUtility(new AmazonS3Client(new CognitoCachingCredentialsProvider(
//                context,
//                "us-east-1:69a144f2-4edd-41c1-bede-7dafd56ed16a", // Identity Pool ID
//                Regions.US_EAST_1 // Region
//        )), context);
//    }

//    public static String compositeId(Context context) {
//        if (!init) throw new RuntimeException("Need to init class first");
//        return Get.uniqueId(context) + "/" + secretKeyHash;
//    }
}
