package com.telenor.possumlib.utils.sound;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MFCC {
    private static String tag = "MFCC";

    public static double[] extractFeature(double[] samples, double sampling_rate) throws Exception { //double[][] other_feature_values
        int[] cbin = FeatureExtractor.fftBinIndices(sampling_rate);// other_feature_values[0].length);
        double[] fbank = FeatureExtractor.melFilter(samples, cbin); // other_feature_values[0]
        double[] f = FeatureExtractor.nonLinearTransformation(fbank);
        return FeatureExtractor.cepCoefficients(f);
    }

    public static List<double[]> getFeaturesFromRecording(short[] samples, int sample_size, int sampling_rate,
                                                          int window_size) {
        // Convert source to double
        double[] samples_double = new double[sample_size];
        for (int i=0; i<sample_size; i++) {
            // short has range [-32768, 32767]. Avoid negative numbers by adding 32768
            samples_double[i] = Math.abs((double) samples[i]);
        }
        List<double[]> features = new ArrayList<>();
        int current = 0;
        while (current + window_size < sample_size) {
            try {
                double [] tmp = extractFeature(Arrays.copyOfRange(samples_double, current, current+window_size),
                        (double) sampling_rate);
                features.add(tmp);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            current += window_size;
        }
        return features;
    }

    public static void writeFeaturesToFile(List<double[]> feature_list, File feature_file) {
        long time = System.currentTimeMillis();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(feature_file);
            for (double [] features: feature_list) {
                String feature_string = "" + time;
                for (int i=0; i<13; i++) {
                    feature_string += " " + features[i];
                }
                feature_string += "\n";
                fos.write(feature_string.getBytes());
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.i(tag, "Failed to close file:",e);
                }
            }
        }
    }

}