package com.telenor.possumlib.abstractservices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.telenor.possumlib.asynctasks.AsyncUpload;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.interfaces.IWrite;
import com.telenor.possumlib.utils.Send;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic upload service handling all general upload
 */
public abstract class AbstractUploadService extends Service implements IWrite {
    protected AtomicBoolean isRunning = new AtomicBoolean(false);
    protected AsyncUpload asyncUpload;
    private static final String tag = AbstractUploadService.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    /**
     * Should there be a need for fine-grained control over progress, this method can do stuff
     * while the actual upload is in progress. Not used atm because not sticky.
     * @param progress shows percentage complete of upload
     */
    public void bytesWritten(int progress) {

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

    /**
     * Ensures all uploads automatically kill upload if forced to die
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "Destroying upload service:"+this);
        if (asyncUpload != null && !asyncUpload.isCancelled()) {
            asyncUpload.cancel(true);
        }
    }
}