package com.telenor.possumlib;

import android.content.Context;

import junit.framework.Assert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.telenor.possumlib.utils.FileUtil;

/**
 * Helper class for testing, used for manipulating files easier
 */
public class FileManipulator {
    public static void fillFile(File file) throws Exception {
        if (file.exists()) {
            Assert.assertTrue(file.delete());
        }
        Assert.assertTrue(file.createNewFile());
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < 10; i++) {
                writer.append("test test test\r\n");
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public static File getDataDir(Context context) {
        File dataDir = new File(context.getFilesDir().getAbsolutePath()+"/data");
        if (dataDir.exists()) {
            FileUtil.clearDirectory(context, dataDir);
            Assert.assertTrue(dataDir.delete());
        }
        Assert.assertTrue(dataDir.mkdir());
        return dataDir;
    }

    public static File getFileWithName(Context context, String filename) throws Exception{
        File file = new File(getDataDir(context).getAbsolutePath()+"/"+filename);
        if (!file.exists()) {
            Assert.assertTrue(file.createNewFile());
        }
        return file;
    }
}
