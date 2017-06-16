package com.telenor.possumlib.asynctasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.telenor.possumlib.constants.Constants;
import com.telenor.possumlib.interfaces.IOnVerify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Downloads the verification file content into a string and returns it
 */
public class AsyncAmazonVerification extends AsyncTask<AmazonS3Client, Void, String> {
    private String bucketKey;
    private IOnVerify listener;

    private static final String tag = AsyncAmazonVerification.class.getName();

    public AsyncAmazonVerification(@NonNull String bucketKey, @NonNull IOnVerify listener) {
        this.bucketKey = bucketKey;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(AmazonS3Client... params) {
        AmazonS3Client amazonS3Client = params[0];
        S3Object object = amazonS3Client.getObject(Constants.BUCKET, bucketKey);
        BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
        try {
            String fileContent = reader.readLine();
            Log.i(tag, "Content of verification:"+fileContent);
            return fileContent;
        } catch (IOException e) {
            Log.e(tag, "Failed to read:",e);
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e(tag, "Failed to close reader");
            }
        }
    }
    @Override
    protected void onPostExecute(String answer) {
        if (answer == null) {
            listener.allowAwesomePossumToRun();
        } else {
            try {
                boolean killAwesomePossum = Boolean.parseBoolean(answer);
                if (killAwesomePossum) {
                    listener.terminateAllAwesomeness();
                } else {
                    listener.allowAwesomePossumToRun();
                }
            } catch (Exception e) {
                Log.d(tag, "Exception on parsing file:",e);
                listener.allowAwesomePossumToRun();
            }
        }
    }
}
