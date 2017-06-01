package com.telenor.possumlib.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.IdentityChangedListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.telenor.possumlib.asynctasks.AsyncUpload;
import com.telenor.possumlib.asynctasks.RequestNewIdentity;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.interfaces.IWrite;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic upload service handling upload to S3
 */
public abstract class BasicUploadService extends Service implements IWrite, IdentityChangedListener {
    private TransferUtility transferUtility;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AsyncUpload asyncUpload;
    private String uploadArea;
    private CognitoCachingCredentialsProvider cognitoProvider;
    private static final String tag = BasicUploadService.class.getName();

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
        uploadArea = intent.getStringExtra("uploadArea");
        cognitoProvider = new CognitoCachingCredentialsProvider(
                this,
                intent.getStringExtra("bucketKey"), // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        if (cognitoProvider.getCachedIdentityId() == null) {
            cognitoProvider.registerIdentityChangedListener(this);
            new RequestNewIdentity(cognitoProvider).execute((Void) null);
        } else {
            startUpload();
        }
        return START_NOT_STICKY;
    }

    public boolean startUpload() {
        if (isRunning.get()) {
            Log.w(tag, "Already running upload, ignoring upload");
            return false;
        }
        isRunning.set(true);
        transferUtility = new TransferUtility(new AmazonS3Client(cognitoProvider), this);
        asyncUpload = new AsyncUpload(this, this, transferUtility, uploadArea, filesDesiredForUpload());
        asyncUpload.execute((Void) null);
        return true;
    }

    public void bytesWritten(int progress) {

    }

    public void uploadComplete(Exception exception, String message) {
        Log.d(tag, "Upload complete:" + exception + " - " + message);
        sendIntent("success", message);
        stopSelf();
    }

    /**
     * Method that must be overridden, this will show the extended service which files it is to use
     * for uploading
     *
     * @return a list of file you want to upload
     */
    public abstract List<File> filesDesiredForUpload();

    @Override
    public void identityChanged(String oldIdentityId, String newIdentityId) {
        cognitoProvider.unregisterIdentityChangedListener(this);
        if (cognitoProvider.getCachedIdentityId() != null) {
            startUpload();
        } else {
            String errorMsg = "Failed to get identity:"+oldIdentityId+" -"+newIdentityId;
            Log.e(tag, errorMsg);
            sendIntent("error", errorMsg);
            stopSelf();
        }
    }

    /**
     * Sends intent to AwesomePossum library where it can be handled
     * @param key the key/type of the message
     * @param value the actual message to be sent
     */
    protected void sendIntent(String key, String value) {
        Intent intent = new Intent(Messaging.POSSUM_MESSAGE);
        intent.putExtra(key, value);
//        intent.setPackage(getPackageName()); // Locks it to the same package
        Log.d(tag, "The package is:"+getPackageName());
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "Destroying basic upload service");
        if (asyncUpload != null && !asyncUpload.isCancelled()) {
            asyncUpload.cancel(true);
        }
    }
}