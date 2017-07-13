package com.telenor.possumlib.services;

import android.content.Intent;
import android.util.Log;

import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.abstractservices.AbstractAmazonUploadService;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.FileUtil;
import com.telenor.possumlib.utils.Get;
import com.telenor.possumlib.utils.Send;

import java.io.File;
import java.util.List;

/**
 * Service that handles the upload of all unsent data. Meant to keep the app alive while the transfer is done. The transfer itself
 * is done in an asyncTask from the service since a service runs on main thread per se.
 */
public class DataUploadService extends AbstractAmazonUploadService {
    private String uniqueUserId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uniqueUserId = intent.getStringExtra("uniqueUserId");
        if (uniqueUserId == null) {
            Send.messageIntent(this, Messaging.UPLOAD_FAILED, "Missing unique user id");
            Log.e(tag, "Missing unique user id on upload start");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public String successMessageType() {
        return Messaging.UPLOAD_SUCCESS;
    }

    @Override
    public List<File> filesDesiredForUpload() {
        PossumBus possumBus = new PossumBus();
        for (AbstractDetector detector : Get.Detectors(this, possumBus)) {
            detector.setUniqueUser(uniqueUserId);
            detector.prepareUpload();
            detector.terminate();
        }
        return FileUtil.getFilesReadyForUpload(this);
    }
}