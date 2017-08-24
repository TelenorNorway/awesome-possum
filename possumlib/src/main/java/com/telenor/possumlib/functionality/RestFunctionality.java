package com.telenor.possumlib.functionality;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonObject;
import com.telenor.possumlib.interfaces.IRestListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private String successMessage;
    private String apiKey;
    private IRestListener listener;

    public RestFunctionality(IRestListener listener, @NonNull String url, @NonNull String apiKey) throws MalformedURLException {
        this.listener = listener;
        this.url = new URL(url);
        this.apiKey = apiKey;
    }

    @Override
    protected Exception doInBackground(JsonObject... params) {
        OutputStream os = null;
        InputStream is = null;
        Exception exception = null;
        JsonObject object = params[0];
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("x-api-key", apiKey);
            urlConnection.setRequestMethod("POST");
            byte[] data = object.toString().getBytes();
//            longLog(object.toString());
            urlConnection.setFixedLengthStreamingMode(data.length);
            urlConnection.connect();

            os = urlConnection.getOutputStream();
            // TODO: Post to stream
            os.write(data);
            //int responseCode = urlConnection.getResponseCode();
            //String responseMessage = urlConnection.getResponseMessage();
            //Log.d(tag, responseCode + " -> " + responseMessage);
            is = urlConnection.getInputStream();
            successMessage = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                successMessage += line;
            }
            reader.close();
        } catch (Exception e) {
            Log.e(tag, "Ex:", e);
            exception = e;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(tag, "Failed to close output stream:", e);
                    exception = e;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(tag, "Failed to close input stream:", e);
                    exception = e;
                }
            }
        }
        return exception;
    }

    private void longLog(String message) {
        // Split by line, then ensure each line can fit into Log's maximum length.
        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + 1000);
                Log.d(tag, "TestAuth:"+message.substring(i, end));
                i = end;
            } while (i < newline);
        }
    }

    @Override
    protected void onPostExecute(Exception exception) {
        if (exception != null) {
            // Failed
            listener.failedToPush(exception);
        } else {
            // Success
            listener.successfullyPushed(successMessage);
        }
    }
}