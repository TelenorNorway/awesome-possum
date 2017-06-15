package com.telenor.possumlib.services;

import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonObject;
import com.telenor.possumlib.abstractservices.AbstractAmazonUploadService;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.utils.FileUtil;
import com.telenor.possumlib.utils.Send;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles sending the encrypted kurt to the service, storing it
 */
final public class SendKurtService extends AbstractAmazonUploadService {
    private static final String tag = SendKurtService.class.getName();
    private String encryptedKurt;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        encryptedKurt = intent.getStringExtra("encryptedKurt");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public String successMessageType() {
        return Messaging.VERIFICATION_SUCCESS;
    }

    @Override
    public List<File> filesDesiredForUpload() {
        List<File> list = new ArrayList<>();
        JsonObject object = new JsonObject();
        object.addProperty("time", DateTime.now().getMillis());
        object.addProperty("encryptedKurt", encryptedKurt);
        File tempFile = FileUtil.toUploadFile(this, "consent/"+encryptedKurt);
        String errorMsg;
        if (tempFile.exists()) {
            if (!tempFile.delete()) {
                errorMsg = "Failed to delete old kurt file, panic!!";
                Log.e(tag, errorMsg);
                Send.messageIntent(this, Messaging.VERIFICATION_FAILED, errorMsg);
                return list;
            }
        }
        try {
            if (!tempFile.createNewFile()) {
                errorMsg = "Failed to create kurtFile, panic!!";
                Log.e(tag, errorMsg);
                Send.messageIntent(this, Messaging.VERIFICATION_FAILED, errorMsg);
                return list;
            }
        } catch (IOException e) {
            errorMsg = "Failed to create kurtFile, panic!!:"+e.toString();
            Log.e(tag, errorMsg);
            Send.messageIntent(this, Messaging.VERIFICATION_FAILED, errorMsg);
            return list;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            fos.write(object.toString().getBytes());
            list.add(tempFile);
            Log.d(tag, "Successfully wrote and added kurtFile to list, returning it");
        } catch (Exception e) {
            errorMsg = "Failed to write to file:"+e.toString();
            Log.e(tag, errorMsg);
            Send.messageIntent(this, Messaging.VERIFICATION_FAILED, errorMsg);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(tag, "Failed to close file:",e);
                }
            }
        }
        return list;
    }
}