package com.telenor.possumlib.abstractdetectors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.telenor.possumlib.models.CountingOutputStream;
import com.telenor.possumlib.models.PossumBus;

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

    /**
     * Constructor for all android sensor zipping detectors (all the heavy duty ones, like
     * accelerometer and gyroscope etc)
     *
     * @param context    Any android context
     * @param sensorType The Sensor.Type you wish to use for this sensor
     * @param uniqueUserId the unique user id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    protected AbstractZippingAndroidDetector(Context context, int sensorType, String uniqueUserId, PossumBus eventBus, boolean authenticating) {
        super(context, sensorType, uniqueUserId, eventBus, authenticating);
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

    /**
     * Overridden basic store to file due to zipping nature. Question is: Do we make it use
     * zipStream still or is that unnecessary now that it shouldn't record for hours?
     * @param file file to store data in
     */
    @Override
    protected void storeData(@NonNull File file) {
        for (JsonArray value : sessionValues) {
            try {
                if (outerStream != null) {
                    outerStream.write(value.toString().getBytes());
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