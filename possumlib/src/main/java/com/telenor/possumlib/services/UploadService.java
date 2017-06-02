package com.telenor.possumlib.services;

import android.content.Intent;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.utils.FileUtil;
import com.telenor.possumlib.utils.Get;

import java.io.File;
import java.util.List;

/**
 * Service that handles the upload of all unsent data. Meant to keep the app alive while the transfer is done. The transfer itself
 * is done in an asyncTask from the service since a service runs on main thread per se.
 */
final public class UploadService extends BasicUploadService {
    private String secretKeyHash;
    private String encryptedKurt;

    private static final String tag = UploadService.class.getName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        secretKeyHash = intent.getStringExtra("secretKeyHash");
        encryptedKurt = intent.getStringExtra("encryptedKurt");
        Log.i(tag, "EncryptedKurt on startCommand:"+encryptedKurt);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public String successMessageType() {
        return Messaging.UPLOAD_SUCCESS;
    }

    @Override
    public List<File> filesDesiredForUpload() {
        Log.i(tag, "EncryptedKurt on filesDesired:"+encryptedKurt);
        for (AbstractDetector detector : Get.Detectors(this, encryptedKurt, secretKeyHash, new EventBus())) {
            detector.prepareUpload();
            detector.terminate();
        }
        return FileUtil.getFilesReadyForUpload(this);
    }
}