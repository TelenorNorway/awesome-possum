package com.telenor.possumlib.abstractservices;

import android.content.Intent;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.telenor.possumlib.asynctasks.AsyncUpload;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.interfaces.IAmazonIdentityConfirmed;
import com.telenor.possumlib.utils.AmazonFunctionality;
import com.telenor.possumlib.utils.Send;

/**
 * Handles all upload to the amazon cloud
 */
public abstract class AbstractAmazonUploadService extends AbstractUploadService implements IAmazonIdentityConfirmed {
    private AmazonFunctionality amazonFunctionality;

    private static final String tag = AbstractAmazonUploadService.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        amazonFunctionality = new AmazonFunctionality(this, this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        amazonFunctionality.setCognitoProviderWithBucket(intent.getStringExtra("bucket"));
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Starts the actual upload, unless it has already started
     */
    public void foundAmazonIdentity() {
        if (isRunning.get()) {
            Log.w(tag, "Already running upload, ignoring upload");
        }
        isRunning.set(true);
        asyncUpload = new AsyncUpload(this, this,
                new TransferUtility(amazonFunctionality.amazonClient(), this),
                filesDesiredForUpload());
        asyncUpload.execute((Void) null);
    }

    /**
     * Failed to interact with cognito, stopping and returning intent
     */
    public void failedToFindAmazonIdentity() {
        String errorMsg = "Failed to get amazon identity";
        Log.e(tag, errorMsg);
        Send.messageIntent(this, Messaging.UPLOAD_FAILED, errorMsg);
        stopSelf();
    }
}
