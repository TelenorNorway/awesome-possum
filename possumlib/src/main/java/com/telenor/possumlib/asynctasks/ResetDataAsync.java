package com.telenor.possumlib.asynctasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Quick and dirty async task for doing rest call that resets all data
 */
public class ResetDataAsync extends AsyncTask<String, Void, Exception> {
    private String uniqueUserId;
    private String apiKey;
    private JsonArray detectorsToReset;
    private static final String tag = ResetDataAsync.class.getName();
    public ResetDataAsync(@NonNull String uniqueUserId, @NonNull String apiKey, @NonNull JsonArray detectorsToReset) {
        this.uniqueUserId = uniqueUserId;
        this.apiKey = apiKey;
        this.detectorsToReset = detectorsToReset;
    }

    @Override
    protected Exception doInBackground(String... params) {
        String url = params[0];
        if (url == null) return new IllegalArgumentException("Missing url");
        try {
            URL myUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) myUrl.openConnection();
            JsonObject object = new JsonObject();
            object.addProperty("connectId", uniqueUserId);
            object.add("sensors", detectorsToReset);
            urlConnection.setRequestProperty("x-api-key", apiKey);
            urlConnection.setRequestMethod("POST");
            byte[] data = object.toString().getBytes();
            urlConnection.setFixedLengthStreamingMode(data.length);
            urlConnection.connect();

            OutputStream os = urlConnection.getOutputStream();
            os.write(data);
            Log.i(tag, "Response:"+urlConnection.getResponseCode()+", "+urlConnection.getResponseMessage()+", apiKey:"+apiKey);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Exception result) {
        if (result == null) {
            Log.i(tag, "Successfully removed data");
        } else {
            Log.e(tag, "Failed to reset:",result);
        }
    }
}
