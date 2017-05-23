package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.AmbientSoundChangeEvent;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.constants.DetectorType;

/***
 * Uses microphone for ambient sound analysis.
 */
public class AmbientSoundDetector extends AbstractEventDrivenDetector {
    private AudioManager audioManager;
    private MediaRecorder mediaRecorder;
    private Handler audioHandler;
    private boolean isRecording;
    private boolean supportsUnprocessed;

    public AmbientSoundDetector(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) throws IllegalArgumentException {
        super(context, identification, secretKeyHash, eventBus);
        isRecording = false;
        audioHandler  = new Handler(Looper.getMainLooper());
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        supportsUnprocessed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Boolean.parseBoolean(audioManager.getProperty("android.media.property.SUPPORT_AUDIO_SOURCE_UNPROCESSED"));
    }

    @Override
    protected boolean storeWithInterval() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // TODO: Criteria for voice enabled
        return audioManager != null;
    }

    private boolean supportsUnprocessed() {
        return supportsUnprocessed;
    }

    @Override
    public boolean isPermitted() {
        return ContextCompat.checkSelfPermission(context(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean isAvailable() {
        // TODO: Criteria for voice available
        return isPermitted() && !audioManager.isMusicActive();
    }

    @Override
    public int detectorType() {
        return DetectorType.Audio;
    }

    @Override
    public String detectorName() {
        return "AmbientSound";
    }

    private boolean isRecording() {
        return isRecording;
    }

    @Override
    public void eventReceived(BasicChangeEvent object) {
        if (object instanceof AmbientSoundChangeEvent) {
            listenForSounds();
        }
    }

    @Override
    public boolean startListening() {
        boolean started = super.startListening();
        if (started) {
            listenForSounds();
        }
        return started;
    }

    private void listenForSounds() {
        Log.d(tag, "Start recording ambient sound");
        if (audioManager.isMicrophoneMute()) {
            audioManager.setMicrophoneMute(false);
        }
        if (isPermitted()) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(storedData().getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                audioHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopListening();
                    }
                }, listenInterval());
            } catch (Exception e) {
                Log.e(tag, "Failed to start recording:",e);
            }
        }
    }

    @Override
    public void stopListening() {
        if (isRecording()) {
            Log.d(tag, "Stopping recording of voice");
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
    }

    private long listenInterval() {
        return 5000; // 5 seconds listen interval
    }
}