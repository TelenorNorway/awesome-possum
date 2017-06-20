package com.telenor.possumlib.utils.sound;

import android.util.Log;

import com.google.gson.JsonArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundFeatureExtractor {
    private static String tag = "SoundFeatureExtractor";
    private static int window_size = 86; // Window size in milliseconds
    private static int lpc_dimensions = 10;
    public static int number_of_features = FeatureExtractor.numCepstra + lpc_dimensions + 6;

    /**
     * Takes a sound sample, divides it into partially overlapping windows, and returns an array of
     * sound features for every window.
     * @param samples       Audio sample in the PCM 16 bit format
     * @param sample_size   Size of the audio sample
     * @param sampling_rate Sampling rate (Hz)
     * @return  List of audio samples of length windows. Each item in the list is an array of
     * of length number_of_features.
     */
    public static List<double[]> getFeaturesFromSample(short[] samples, int sample_size,
                                                       int sampling_rate) {
        final float window_overlap = 0.5f;
        // Convert source to double and absolute values (for SoundFeatureExtractor)
        double[] samples_double_abs = new double[sample_size];
        double[] samples_double = new double[sample_size];
        for (int i=0; i<sample_size; i++) {
            samples_double_abs[i] = Math.abs((double) samples[i]);
            samples_double[i] = (double) samples[i];
        }
        List<double[]> features = new ArrayList<>();
        long time = System.currentTimeMillis();
        int current = 0;
        // Window size hardcoded since it need to be a power of 2 for FFT
        //int window_size = 4096; // This is a window of 85 ms @ 48000 hz sample rate
        int window_size = 2048; // This is a window of 46 ms @ 44100 hz sample rate
        while (current + window_size <= sample_size) {
            try {
                double [] window_data_abs = Arrays.copyOfRange(samples_double_abs,
                        current, current+window_size);
                double [] window_data = Arrays.copyOfRange(samples_double,
                        current, current+window_size);
                // Get SoundFeatureExtractor features
                double [] mfcc_features = get_mfcc(window_data_abs, (double) sampling_rate);
                // Get LPC features
                double [] lpc_features = get_lpc(window_data);
                // Get time-domain features
                double zcr = get_zcr(window_data);
                double ste = get_ste(window_data);
                // Compute FFT and get frequency domain features
                FFT.computeFFT(window_data);
                double [] fft_real = FFT.real;
                double [] fft_imag = FFT.imag;
                double [] fft = new double[fft_real.length];
                // Convert complex number to magnitude
                for (int j=0; j<fft_real.length; j++) {
                    fft[j] = Math.sqrt(fft_imag[j]*fft_imag[j]+fft_real[j]*fft_real[j]);
                }
                // Get frequency domain features
                double sc = get_sc(fft);
                double peak = get_peak(fft);
                double sf = get_flatness(fft);
                double [] tmp = new double [number_of_features];
                // Make time the first column
                tmp[0] = time;
                // Copy in features to result array
                System.arraycopy(mfcc_features, 0, tmp, 1, FeatureExtractor.numCepstra);
                tmp[FeatureExtractor.numCepstra+1] = zcr;
                tmp[FeatureExtractor.numCepstra+2] = ste;
                tmp[FeatureExtractor.numCepstra+3] = sc;
                tmp[FeatureExtractor.numCepstra+4] = sf;
                tmp[FeatureExtractor.numCepstra+5] = peak;
                System.arraycopy(lpc_features, 0, tmp, FeatureExtractor.numCepstra+6, lpc_dimensions);
                // Add to list of features
                features.add(tmp);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            current += window_size*(1-window_overlap);
        }
        return features;
    }

    /**
     * Writes the supplied list of features to a space separated ASCII file in the External Storage
     * Directory with each item on the list on its own line.
     * @param feature_list  List of features
     * @param outputFile    The file to output to
     */
    public static void writeFeaturesToFile(List<double[]> feature_list, File outputFile) {
        FileOutputStream feature_file = null;
        try {
            feature_file = new FileOutputStream(outputFile);
            for (double [] features: feature_list) {
                String feature_string = "";
                for (double feature : features) {
                    feature_string += " " + feature;
                }
                feature_string += "\n";
                feature_file.write(feature_string.getBytes());
            }
            feature_file.flush();
        } catch (Exception e) {
            Log.e(tag, "Failed to write to file:",e);
        } finally {
            if (feature_file != null) {
                try {
                    feature_file.close();
                } catch (IOException e) {
                    Log.e(tag, "Failed to close file:",e);
                }
            }
        }
    }

    /**
     * Returns a json array of json arrays (number of time windows, number of features). Casts all
     * numbers to string to avoid rounding.
     * @param feature_list List of features
     * @return  json array of json arrays containing features
     */
    public static JsonArray writeFeaturesToJsonArray(List<double[]> feature_list) {
        JsonArray feature_list_json = new JsonArray();
        for (double [] features: feature_list) {
            JsonArray features_array = new JsonArray();
            for (int i=0; i<number_of_features; i++) {
                features_array.add(Double.toString(features[i]));
            }
            feature_list_json.add(features_array);
        }
        return feature_list_json;
    }

    /**
     * Returns the MFCC features of a supplied audio sample
     * @param samples       The audio signal for the sample
     * @param sampling_rate The sampling rate in Hz
     * @return MFCC coefficients
     * @throws Exception
     */
    private static double[] get_mfcc(double[] samples, double sampling_rate) throws Exception {
        int[] cbin = FeatureExtractor.fftBinIndices(sampling_rate);
        double[] fbank = FeatureExtractor.melFilter(samples, cbin);
        double[] f = FeatureExtractor.nonLinearTransformation(fbank);
        return FeatureExtractor.cepCoefficients(f);
    }

    /**
     * Returns the Linear Prediction Cepstral coefficients
     * @param samples       The audio signal for the sampele
     * @return  LPC coefficients (lenght lpc_dimensions)
     * @throws Exception
     */
    private static double[] get_lpc(double[] samples) throws Exception {
        final double lambda = 0.0;
        // find the order-P autocorrelation array, R, for the sequence x of
        // length L and warping of lambda
        // Autocorrelate(&pfSrc[stIndex],siglen,R,P,0);

        double[] R = new double[lpc_dimensions + 1];
        double K[] = new double[lpc_dimensions];
        double A[] = new double[lpc_dimensions];
        double[] dl = new double[samples.length];
        double[] Rt = new double[samples.length];
        double r1, r2, r1t;
        R[0] = 0;
        Rt[0] = 0;
        r1 = 0;
        r2 = 0;
        r1t = 0;
        for (int k = 0; k < samples.length; k++) {
            Rt[0] += samples[k] * samples[k];

            dl[k] = r1 - lambda * (samples[k] - r2);
            r1 = samples[k];
            r2 = dl[k];
        }
        for (int i = 1; i < R.length; i++) {
            Rt[i] = 0;
            r1 = 0;
            r2 = 0;
            for (int k = 0; k < samples.length; k++) {
                Rt[i] += dl[k] * samples[k];

                r1t = dl[k];
                dl[k] = r1 - lambda * (r1t - r2);
                r1 = r1t;
                r2 = dl[k];
            }
        }
        for (int i = 0; i < R.length; i++)
            R[i] = Rt[i];

        // LevinsonRecursion(unsigned int P, float *R, float *A, float *K)
        double Am1[] = new double[62];
        ;

        if (R[0] == 0.0) {
            for (int i = 1; i < lpc_dimensions; i++) {
                K[i] = 0.0;
                A[i] = 0.0;
            }
        } else {
            double km, Em1, Em;
            int k, s, m;
            for (k = 0; k < lpc_dimensions; k++) {
                A[0] = 0;
                Am1[0] = 0;
            }
            A[0] = 1;
            Am1[0] = 1;
            km = 0;
            Em1 = R[0];
            for (m = 1; m < lpc_dimensions; m++) // m=2:N+1
            {
                double err = 0.0f; // err = 0;
                for (k = 1; k <= m - 1; k++)
                    // for k=2:m-1
                    err += Am1[k] * R[m - k]; // err = err + am1(k)*R(m-k+1);
                km = (R[m] - err) / Em1; // km=(R(m)-err)/Em1;
                K[m - 1] = -km;
                A[m] = km; // am(m)=km;
                for (k = 1; k <= m - 1; k++)
                    // for k=2:m-1
                    A[k] = Am1[k] - km * Am1[m - k]; // am(k)=am1(k)-km*am1(m-k+1);
                Em = (1 - km * km) * Em1; // Em=(1-km*km)*Em1;
                for (s = 0; s < lpc_dimensions; s++)
                    // for s=1:N+1
                    Am1[s] = A[s]; // am1(s) = am(s)
                Em1 = Em; // Em1 = Em;
            }
        }
        return K;
    }

    /**
     * Returns the Zero Crossing Rate of a supplied audio sample
     * @param samples       The audio signal for the sample
     * @return  Zero Crossing Rate
     */
    private static double get_zcr(double [] samples) {
        double zcr = 0.0;
        for (int i=1; i<samples.length; i++) {
            zcr += Math.abs(Math.signum(samples[i]) - Math.signum(samples[i-1]));
        }
        return zcr / samples.length;
    }

    /**
     * Returns the Short-time Energy of a supplied audio sample
     * @param samples       The audio signal for the sample
     * @return  Short-time Energy
     */
    private static double get_ste(double [] samples) {
        double ste = 0.0;
        for (int i=0; i<samples.length; i++) {
            ste += samples[i]*samples[i];
        }
        return ste / samples.length;
    }

    /**
     * Returns the Spectral Centroid of the supplied audio sample
     * @param spectrum  The Fourier spectrum of the audio sample
     * @return  Spectral Centroid
     */
    private static double get_sc(double [] spectrum) {
        double num = 0.0;
        double den = 0.0;
        for (int i=0; i<spectrum.length; i++) {
            num += i*spectrum[i];
            den += spectrum[i];
        }
        return num/den;
    }

    /**
     * Returns the spectral peak, i.e. the frequency with the largest amplitude
     * @param spectrum  The Fourier spectrum of the audio sample
     * @return  Spectral Peak
     */
    private static double get_peak(double [] spectrum) {
        double max = 0.0;
        int peak = 0;
        for (int i=0; i<spectrum.length; i++) {
            if (spectrum[i] > max) {
                peak = i;
                max = spectrum[i];
            }
        }
        return peak;
    }

    /**
     * Returns the spectral flatness of a supplied audio sample
     * @param spectrum  The Fourier spectrum of the audio sample
     * @return  Spectral Flatness
     */
    private static double get_flatness(double [] spectrum) {
        double sum = 0.0;
        double sumln = 0.0;
        double denom = 1.0/spectrum.length;
        for (int i=0; i<spectrum.length; i++) {
            sum += spectrum[i];
            sumln += Math.log(spectrum[i]);
        }
        return Math.exp(denom*sumln)/denom/sum;
    }
}