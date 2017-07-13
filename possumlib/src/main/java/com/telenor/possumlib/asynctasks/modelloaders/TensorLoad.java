package com.telenor.possumlib.asynctasks.modelloaders;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IModelLoaded;
import com.telenor.possumlib.tensorflow.TensorFlowInferenceInterface;

import java.io.IOException;

public class TensorLoad extends AsyncTask<Void, Void, Boolean> {
    private IModelLoaded listener;
    private Context context;
    private TensorFlowInferenceInterface tensorInterface;
    private static final String fileName = "tensorflow_facerecognition.pb";
    private static final String fullPath = "file:///android_asset/" + fileName;
    private static final String tag = TensorLoad.class.getName();

    public TensorLoad(Context context, IModelLoaded listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] paths;
            try {
                paths = context.getAssets().list("");
            } catch (IOException e) {
                return false;
            }
            if (paths.length == 0) {
                Log.w(tag, "No tensorFlow file found - no assets, ignoring image detector");
                return false;
            } else {
                boolean tensorFlowIsFound = false;
                for (String file : paths) {
                    if (file.equals(fileName)) tensorFlowIsFound = true;
                }
                if (!tensorFlowIsFound) {
                    Log.w(tag, "No tensorFlow file is found in assets, ignoring image detector");
                    return false;
                }
            }
            Log.d(tag, "Starting initialize of TensorFlow");
            try {
                tensorInterface = new TensorFlowInferenceInterface();
                if (tensorInterface.initialize(context)) {
                    final int status = tensorInterface.initializeTensorFlow(context.getAssets(),
                            fullPath);
                    if (status != 0) {
                        Log.e(tag, "TF init status: " + status);
                        return false;
                    }
                    Log.d(tag, "Model loaded");
                    return true;
                } else {
                    Log.w(tag, "Failed to initialize TensorFlow");
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) listener.modelLoaded(DetectorType.Image, tensorInterface);
    }
}