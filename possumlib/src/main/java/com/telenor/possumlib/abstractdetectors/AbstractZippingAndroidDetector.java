package com.telenor.possumlib.abstractdetectors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.google.common.io.CountingOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Android detector for the huge detectors with lots of input that automatically sends data to zip stream
 */
public abstract class AbstractZippingAndroidDetector extends AbstractAndroidRegularDetector {
    private volatile ZipOutputStream outerStream;
    private CountingOutputStream innerStream;

    protected AbstractZippingAndroidDetector(Context context, int sensorType, String identification, String secretKeyHash, EventBus eventBus) {
        super(context, sensorType, identification, secretKeyHash, eventBus);
    }

    @Override
    public long fileSize() {
        return uploadFilesSize() + (innerStream != null ? innerStream.getCount() : 0);
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            try {
                openStreamIfNotOpen();
            } catch (IOException e) {
                Log.e(tag, "Stream open failed:", e);
            }
         }
        return listen;
    }

    @Override
    public void stopListening() {
        super.stopListening();
        try {
            closeStreamIfOpen();
        } catch (Exception e) {
            Log.e(tag, "Stream close failed:", e);
        }
    }

    private void openStreamIfNotOpen() throws IOException {
        lock();
        try {
            if (outerStream == null) {
                innerStream = new CountingOutputStream(new FileOutputStream(storedData()));
                outerStream = createZipStream(innerStream);
            }
        } finally {
            unlock();
        }
    }

    private void closeStreamIfOpen() throws IOException {
        lock();
        try {
            if (outerStream != null) {
                outerStream.close();
                outerStream = null;
                stageForUpload(storedData());
            }
        } finally {
            unlock();
        }
    }

    @Override
    protected void storeData(@NonNull File file) {
        for (String value : sessionValues) {
            try {
                if (outerStream != null) {
                    outerStream.write(value.getBytes());
                    outerStream.write("\r\n".getBytes());
                }
            } catch (Exception e) {
                Log.e(tag, "FailedToWrite:", e);
            }
        }
        sessionValues.clear();
    }

    private ZipOutputStream createZipStream(OutputStream innerStream) throws IOException {
        ZipOutputStream zipStream = new ZipOutputStream(innerStream);
        ZipEntry entry = new ZipEntry(storedData().getName());
        zipStream.putNextEntry(entry);
        return zipStream;
    }

    @Override
    public void prepareUpload() {
        lock();
        try {
            if (outerStream != null) {
                closeStreamIfOpen();
                openStreamIfNotOpen();
            }
        } catch (IOException e) {
            Log.e(tag, "Exception preparing upload", e);
        } finally {
            unlock();
        }
    }
}