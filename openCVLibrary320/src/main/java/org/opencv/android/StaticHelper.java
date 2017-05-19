package org.opencv.android;

import android.content.Context;
import android.util.Log;

import com.getkeepsafe.relinker.ReLinker;

import java.util.StringTokenizer;

class StaticHelper {

    static boolean initOpenCV(Context context) { //, boolean InitCuda
        boolean result;
        String libs = "";

        // We never use Cuda, so this can be closed off
//        if (InitCuda) {
//            loadLibrary(context, "cudart");
//            loadLibrary(context, "nppc");
//            loadLibrary(context, "nppi");
//            loadLibrary(context, "npps");
//            loadLibrary(context, "cufft");
//            loadLibrary(context, "cublas");
//        }

//        Log.d(TAG, "Trying to get library list");

        // This library is not included, no point in including this check
//        if (loadLibrary(context, "opencv_info")) {
//            Log.d(TAG, "OpenCV loaded opencv_info");
//            libs = getLibraryList();
//        } else {
//            Log.e(TAG, "OpenCV error: Cannot load info library for OpenCV");
//        }

        Log.d(TAG, "Library list: \"" + libs + "\"");
        Log.d(TAG, "First attempt to load libs");
        if (initOpenCVLibs(context, libs)) {
            Log.d(TAG, "First attempt to load libs is OK");
//            String eol = System.getProperty("line.separator");
//            for (String str : Core.getBuildInformation().split(eol))
//                Log.i(TAG, str);

            result = true;
        } else {
            Log.d(TAG, "First attempt to load libs fails");
            result = false;
        }

        return result;
    }

    private static boolean loadLibrary(Context context, String Name) {
        boolean result = true;

        Log.d(TAG, "Trying to load library " + Name);
        try {
            ReLinker.loadLibrary(context, Name);
            Log.d(TAG, "Library " + Name + " loaded");
        } catch (Exception e) {
            Log.e(TAG, "Cannot load library \"" + Name + "\":",e);
            result = false;
        }
        return result;
    }

    private static boolean initOpenCVLibs(Context context, String Libs) {
        Log.d(TAG, "Trying to init OpenCV libs");

        boolean result = true;

        if ((null != Libs) && (Libs.length() != 0)) {
            Log.d(TAG, "Trying to load libs by dependency list");
            StringTokenizer splitter = new StringTokenizer(Libs, ";");
            while (splitter.hasMoreTokens()) {
                result &= loadLibrary(context, splitter.nextToken());
            }
        } else {
            // If dependencies list is not defined or empty.
            result = loadLibrary(context, "opencv_java3");
        }

        return result;
    }

    private static final String TAG = "OpenCV/StaticHelper";

    @SuppressWarnings("all")
    private static native String getLibraryList();
}
