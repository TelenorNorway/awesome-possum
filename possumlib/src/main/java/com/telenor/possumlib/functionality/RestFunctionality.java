package com.telenor.possumlib.functionality;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonObject;
import com.telenor.possumlib.interfaces.IRestListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Handles communication with server, posting to it
 */
public class RestFunctionality extends AsyncTask<JsonObject, Void, Exception> {
    private static final String tag = RestFunctionality.class.getName();
    private URL url;
    private IRestListener listener;

    public RestFunctionality(IRestListener listener) {
        this.listener = listener;
    }

    public void postData(@NonNull String url, @NonNull JsonObject object) throws MalformedURLException {
        this.url = new URL(url);
        execute(object);
    }

    @Override
    protected Exception doInBackground(JsonObject... params) {
        OutputStream os = null;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            os = urlConnection.getOutputStream();
            // TODO: Post to stream
            JsonObject object = params[0];
            os.write(object.toString().getBytes());
            int responseCode = urlConnection.getResponseCode();
            String responseMessage = urlConnection.getResponseMessage();

            Log.i(tag, "Response:" + responseCode + " -> " + responseMessage);
        } catch (IOException e) {
            Log.e(tag, "IOEx:", e);
            return e;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(tag, "Failed to close stream:", e);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Exception exception) {
        if (exception != null) {
            // Failed
            listener.failedToPush(exception);
        } else {
            // Success
            listener.successfullyPushed();
        }
    }
}