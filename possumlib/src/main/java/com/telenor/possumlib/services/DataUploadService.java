package com.telenor.possumlib.services;

import android.content.Intent;

import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.abstractservices.AbstractAmazonUploadService;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.FileUtil;
import com.telenor.possumlib.utils.Get;

import java.io.File;
import java.util.List;

/**
 * Service that handles the upload of all unsent data. Meant to keep the app alive while the transfer is done. The transfer itself
 * is done in an asyncTask from the service since a service runs on main thread per se.
 */
final public class DataUploadService extends AbstractAmazonUploadService {
    private String encryptedKurt;

//    private static final String tag = DataUploadService.class.getName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        encryptedKurt = intent.getStringExtra("encryptedKurt");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public String successMessageType() {
        return Messaging.UPLOAD_SUCCESS;
    }

    @Override
    public List<File> filesDesiredForUpload() {
        for (AbstractDetector detector : Get.Detectors(this, encryptedKurt, new PossumBus(), false)) {
            detector.prepareUpload();
            detector.terminate();
        }
        return FileUtil.getFilesReadyForUpload(this);
    }
}