package com.telenor.possumlib.services;

import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonObject;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles sending the encrypted kurt to the service, storing it
 */
public class SendKurtService extends BasicUploadService {
    private static final String tag = SendKurtService.class.getName();
    private String encryptedKurt;
    private String secretHash;
    ////telenor-nr-awesome-possum/consent/ her skal det lagres

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        encryptedKurt = intent.getStringExtra("encryptedKurt");
        secretHash = intent.getStringExtra("secretKeyHash");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public List<File> filesDesiredForUpload() {
        List<File> list = new ArrayList<>();
        JsonObject object = new JsonObject();
        object.addProperty("time", DateTime.now().getMillis());
        object.addProperty("secretHash", secretHash);
        object.addProperty("encryptedKurt", encryptedKurt);
        File tempFile = new File(getFilesDir().getAbsolutePath()+"/kurtFile");
        if (tempFile.exists()) {
            if (!tempFile.delete()) {
                Log.e(tag, "Screeech - failed to delete kurt file, panic!!");
                return list;
            }
        }
        try {
            if (!tempFile.createNewFile()) {
                Log.e(tag, "Failed to create kurtFile, panic!!");
                return list;
            }
        } catch (IOException e) {
            Log.e(tag, "Failed to create kurtFile, panic!!:",e);
            return list;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            fos.write(object.toString().getBytes());
            list.add(tempFile);
            Log.i(tag, "Successfully wrote and added kurtFile to list, returning it");
        } catch (FileNotFoundException e) {
            Log.e(tag, "Missing file to write to:",e);
            return list;
        } catch (Exception e) {
            Log.e(tag, "Failed to write to file:",e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(tag, "Failed to close file:",e);
                }
            }
        }
        // TODO: Needs to add the file with json message here
        return list;
    }
}
