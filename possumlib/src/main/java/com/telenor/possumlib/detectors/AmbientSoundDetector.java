package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.sound.SoundFeatureExtractor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/***
 * Uses microphone for ambient sound analysis. Will be switched with AudioRecord instead of
 * MediaRecord soon, stay tuned..
 */
public class AmbientSoundDetector extends AbstractDetector {
    private AudioManager audioManager;
    private AudioRecord audioRecorder;
    private Handler audioHandler;
    private final int bufferSize;
    private final int recordingSamples;
    private boolean disabledMute;
    private boolean supportsUnprocessed;
    private ExecutorService backgroundService = Executors.newSingleThreadExecutor();

    /**
     * Constructor for an ambient sound detector
     *
     * @param context        a valid android context
     * @param uniqueUserId the unique user id
     * @param eventBus       the event bus used for sending messages to and from
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public AmbientSoundDetector(Context context, String uniqueUserId, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, uniqueUserId, eventBus, authenticating);
//        int windowSamples = sampleRate() * windowSize() / 1000;
        recordingSamples = sampleRate() * ((int) authenticationListenInterval() / 1000);
        bufferSize = AudioTrack.getMinBufferSize(sampleRate(), AudioFormat.CHANNEL_OUT_MONO, audioEncoding());
//        SoundFeatureExtractor mfcc = new SoundFeatureExtractor();
        audioHandler = getAudioHandler();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioRecorder = getAudioRecord();
        supportsUnprocessed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Boolean.parseBoolean(audioManager.getProperty("android.media.property.SUPPORT_AUDIO_SOURCE_UNPROCESSED"));
    }

    protected Handler getAudioHandler() {
        return new Handler(Looper.getMainLooper());
    }

    // TODO: Criteria for voice enabled
    @Override
    public boolean isEnabled() {
        return audioManager != null && audioRecorder != null;
    }

    protected AudioRecord getAudioRecord() {
        return new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate(),
                AudioFormat.CHANNEL_IN_MONO,
                audioEncoding(),
                bufferSize);
    }

    /**
     * Handy function for finding out if the current setup supports
     * unprocessed sound
     *
     * @return true if it does, else false
     */
    public boolean supportsUnprocessed() {
        return supportsUnprocessed;
    }

    /**
     * Handy function for finding out if the mic is muted
     *
     * @return true if muted, false if not
     */
    public boolean isMuted() {
        return audioManager.isMicrophoneMute();
    }

    // TODO: Criteria for sound available ?

    @Override
    public String requiredPermission() {
        return Manifest.permission.RECORD_AUDIO;
    }

    @Override
    public int detectorType() {
        return DetectorType.Audio;
    }

    @Override
    public String detectorName() {
        return "sound";
    }

    /**
     * Returns whether it is actually recording
     *
     * @return true if it is recording
     */
    public boolean isRecording() {
        return audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    @Override
    public boolean startListening() {
        boolean started = super.startListening();
        if (started && isAvailable()) {
            if (isMuted()) {
                audioManager.setMicrophoneMute(false);
                disabledMute = true;
            }
            Log.d(tag, "Start recording ambient sound");
            audioRecorder.startRecording();
            audioHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopListening();
                }
            }, authenticationListenInterval());
            backgroundService.submit(new RecordThread());
        }
        return started;
    }

    private class RecordThread implements Runnable {
        @Override
        public void run() {
            Log.i(tag, "Starting to read from audio stream");
            short[] buffer = new short[bufferSize];
            int recordedSamples = 0;
            int readSize;
            while (isListening() && isRecording() && recordedSamples < recordingSamples) {
                if ((readSize = audioRecorder.read(buffer, 0, bufferSize)) != AudioRecord.ERROR_INVALID_OPERATION) {
                    // Calculate features
                    for (double[] window : SoundFeatureExtractor.getFeaturesFromSample(buffer, readSize, sampleRate())) {
                        sessionValues().add(SoundFeatureExtractor.writeFeatureWindowToJsonArray(window));
                    }
                    recordedSamples += readSize;
                }
            }
        }
    }

    /**
     * The presently used audio encoding. Override to change
     *
     * @return value of encoding used
     */
    public int audioEncoding() {
        return AudioFormat.ENCODING_PCM_16BIT;
    }

    /**
     * The presently used sampleRate in Hertz. Override to change
     *
     * @return int value of present sampleRate
     */
    public int sampleRate() {
        return 48000;
    }

    /**
     * the MFCC window size in milliseconds, presently default is 64. Override to change
     *
     * @return the window size in milliseconds
     */
    public int windowSize() {
        return 64;
    }

    private void stopRecording() {
        if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            Log.d(tag, "Stopping recording of ambient sound");
            audioRecorder.stop();
        }
    }

    @Override
    public void stopListening() {
        super.stopListening();
        if (disabledMute) {
            // Since I disabled mute to record, here I re-enable mute
            audioManager.setMicrophoneMute(true);
            disabledMute = false;
        }
        if (isRecording() && audioRecorder != null) {
            stopRecording();
        }
    }

    public long authenticationListenInterval() {
        return 3000;
    }
}