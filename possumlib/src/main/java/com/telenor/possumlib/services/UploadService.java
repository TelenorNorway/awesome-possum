package com.telenor.possumlib.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.IdentityChangedListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.asynctasks.RequestNewIdentity;
import com.telenor.possumlib.asynctasks.UploadConnection;
import com.telenor.possumlib.interfaces.IWrite;
import com.telenor.possumlib.utils.Get;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Service that handles the upload of all unsent data. Meant to keep the app alive while the transfer is done. The transfer itself
 * is done in an asyncTask from the service since a service runs on main thread per se.
 */
public class UploadService extends Service implements IWrite, IdentityChangedListener {
    private static final ConcurrentLinkedQueue<AbstractDetector> detectors = new ConcurrentLinkedQueue<>();
    private UploadConnection connection;
    private CognitoCachingCredentialsProvider cognitoProvider;
    private static final String tag = UploadService.class.getName();
    private String secretKeyHash;
    private String refusedDetectors;
    private String encryptedKurt;
    ////telenor-nr-awesome-possum/consent/ her skal det lagres
    @Override
    public int onStartCommand(Intent intent, int flags, int startCommand) {
        secretKeyHash = intent.getStringExtra("secretKeyHash");
        refusedDetectors = intent.getStringExtra("refusedDetectors");
        encryptedKurt = intent.getStringExtra("encryptedKurt");
        String bucketKey = intent.getStringExtra("bucketKey");
        Log.i(tag, "Bucket key:" + bucketKey);
        clearAllDetectors();
        cognitoProvider = new CognitoCachingCredentialsProvider(
                this,
                bucketKey, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        if (cognitoProvider.getCachedIdentityId() == null) {
            cognitoProvider.registerIdentityChangedListener(this);
            new RequestNewIdentity(cognitoProvider).execute((Void) null);
        }
//        if (cognitoProvider.getCachedIdentityId() != null) {
//            startUpload(new TransferUtility(new AmazonS3Client(cognitoProvider), this));
//        }
        return START_NOT_STICKY;
    }

    private void startUpload(@NonNull TransferUtility transferUtility, String refusedDetectors) {
        Log.i(tag, "Starting upload now that things is in order");
        List<Class<? extends AbstractDetector>> ignoreList = null;
        if (refusedDetectors != null) {
            List<String> refused = new ArrayList<>(Arrays.asList(refusedDetectors.split(",")));
            ignoreList = Get.ignoredDetectors(refused);
        }
        detectors.addAll(Get.Detectors(this, encryptedKurt, secretKeyHash, ignoreList, new EventBus()));
        try {
            connection = new UploadConnection(this, this, transferUtility, detectors);
            connection.execute((Void) null);
        } catch (Exception e) {
            uploadComplete(e, null);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "Destroying upload service");
        if (connection != null && !connection.isCancelled()) {
            connection.cancel(true);
        }
        clearAllDetectors();
    }

    /**
     * Pushes all stored detectors to upload and terminates them
     */
    private void clearAllDetectors() {
        for (AbstractDetector detector : detectors) {
            detector.terminate();
        }
        detectors.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void bytesWritten(int progress) {
    }

    @Override
    public void uploadComplete(Exception exception, String message) {
        Log.i(tag, "Upload complete:" + exception + " - " + message);
        stopSelf();
    }

    @Override
    public void identityChanged(String oldIdentityId, String newIdentityId) {
        cognitoProvider.unregisterIdentityChangedListener(this);
        if (cognitoProvider.getCachedIdentityId() != null) {
            startUpload(new TransferUtility(new AmazonS3Client(cognitoProvider), this), refusedDetectors);
        } else
            throw new RuntimeException("Failed to get identity:" + oldIdentityId + " - " + newIdentityId);
    }
}