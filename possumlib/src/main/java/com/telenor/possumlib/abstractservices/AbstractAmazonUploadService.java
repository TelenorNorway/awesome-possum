package com.telenor.possumlib.abstractservices;

import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.telenor.possumlib.asynctasks.AmazonAsyncUpload;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.interfaces.IWrite;
import com.telenor.possumlib.utils.Send;

import java.io.File;
import java.util.List;

/**
 * Handles all upload to the amazon cloud
 */
public abstract class AbstractAmazonUploadService extends AbstractAmazonService implements IWrite {
    private AmazonAsyncUpload amazonAsyncUpload;

    /**
     * Starts the actual upload, unless it has already started
     */
    @Override
    public void foundAmazonIdentity(AmazonS3Client amazonS3Client) {
        if (!taskStarted.get()) {
            taskStarted.set(true);
            amazonAsyncUpload = new AmazonAsyncUpload(this, this,
                    new TransferUtility(amazonS3Client, this),
                    filesDesiredForUpload());
            amazonAsyncUpload.execute((Void) null);
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (amazonAsyncUpload != null && !amazonAsyncUpload.isCancelled()) {
            amazonAsyncUpload.cancel(true);
        }
    }

    /**
     * Method handling what happens when the upload is complete, whether or not it failed
     * @param exception should the upload have failed for any reason, this will not be null
     * @param message the message you want relayed to the user when it is successful or not having
     *                an error
     */
    public void uploadComplete(Exception exception, String message) {
        Log.d(tag, "Upload complete:" + exception + " - " + message);
        if (exception == null) {
            Send.messageIntent(this, successMessageType(), message);
        } else {
            Send.messageIntent(this, Messaging.UPLOAD_FAILED, exception.toString());
        }
        stopSelf();
    }

    /**
     * Override this to tell what message type it should send on success
     *
     * @return a text string for success in uploading
     */
    public abstract String successMessageType();

    /**
     * Method that must be overridden, this will show the extended service which files it is to use
     * for uploading
     *
     * @return a list of file you want to upload
     */
    public abstract List<File> filesDesiredForUpload();

}