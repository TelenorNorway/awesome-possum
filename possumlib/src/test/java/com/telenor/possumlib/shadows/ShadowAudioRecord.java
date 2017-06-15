package com.telenor.possumlib.shadows;

import android.media.AudioRecord;
import android.support.annotation.NonNull;
import android.util.Log;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Shadow for {@link android.media.AudioRecord}.
 */
@Implements(AudioRecord.class)
public class ShadowAudioRecord {
    private static FileInputStream fis = null;
    private static String filePath = null;
    private static boolean hasRead = false;
    private static final String tag = ShadowAudioRecord.class.getName();

    public ShadowAudioRecord() {

    }
    public ShadowAudioRecord(int audioSource, int sampleRate, int channelConfig, int audioFormat, int bufferSize) {

        try {
            fis = new FileInputStream(new File(filePath));
        } catch (Exception e) {
            Log.e(tag, "Failed:",e);
        }
    }

    public static void setFilePath(String newFilePath) {
        filePath = newFilePath;
    }

    @Implementation
    public int read(@NonNull short[] audioData, int offsetInShorts, int sizeInShorts) {
        hasRead = true;
        byte[] byteArr = new byte[sizeInShorts/2];
        try {
            return fis.read(byteArr, offsetInShorts/2, sizeInShorts/2);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file:",e);
        }
    }

    @Resetter
    public static void reset() {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                Log.e(tag, "failed to close:", e);
            }
        }
        filePath = null;
        hasRead = false;
    }

    public static boolean hasReadFromStream() {
        return hasRead;
    }
}