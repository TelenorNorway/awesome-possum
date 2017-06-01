package com.telenor.possumlib.services;

import android.content.Intent;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.utils.FileUtil;
import com.telenor.possumlib.utils.Get;

import java.io.File;
import java.util.List;

/**
 * Service that handles the upload of all unsent data. Meant to keep the app alive while the transfer is done. The transfer itself
 * is done in an asyncTask from the service since a service runs on main thread per se.
 */
public class UploadService extends BasicUploadService {
//    private static final String tag = UploadService.class.getName();
    private String secretKeyHash;
    private String encryptedKurt;

    @Override
    public int onStartCommand(Intent intent, int flags, int startCommand) {
        secretKeyHash = intent.getStringExtra("secretKeyHash");
        encryptedKurt = intent.getStringExtra("encryptedKurt");
        return super.onStartCommand(intent, flags, startCommand);
    }

    @Override
    public List<File> filesDesiredForUpload() {
        for (AbstractDetector detector : Get.Detectors(this, encryptedKurt, secretKeyHash, new EventBus())) {
            detector.prepareUpload();
            detector.terminate();
        }
        return FileUtil.getFilesReadyForUpload(this);
    }
}