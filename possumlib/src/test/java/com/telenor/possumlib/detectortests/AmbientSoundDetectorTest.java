package com.telenor.possumlib.detectortests;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.AmbientSoundDetector;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.shadows.MyShadowAudioManager;
import com.telenor.possumlib.shadows.ShadowAudioRecord;
import com.telenor.possumlib.shadows.ShadowAudioTrack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Config(shadows = {ShadowAudioTrack.class, MyShadowAudioManager.class, ShadowAudioRecord.class})
@RunWith(PossumTestRunner.class)
public class AmbientSoundDetectorTest {
    private AmbientSoundDetector ambientSoundDetector;
    @Mock
    private AudioRecord mockedAudioRecord;
    @Mock
    private Handler mockedHandler;
    private PossumBus eventBus;
    private File savedFile;
    private File readFileWithExampleData;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        eventBus = new PossumBus();
        readFileWithExampleData = new File(RuntimeEnvironment.application.getFilesDir()+"/exampleData");
        if (readFileWithExampleData.exists()) {
            Assert.assertTrue(readFileWithExampleData.delete());
        }
        ShadowAudioRecord.setFilePath(readFileWithExampleData.getAbsolutePath());
        Assert.assertTrue(readFileWithExampleData.createNewFile());
        FileOutputStream fos = new FileOutputStream(readFileWithExampleData);
        fos.write("0000 0001 0010 0011 0100 0101 0110 0111 1000 1001 1010 1011 1100 1101 1110 1111".getBytes());
        fos.close();
        MyShadowAudioManager.setMicrophoneMuted(false);
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.RECORD_AUDIO);
        ambientSoundDetector = new AmbientSoundDetector(RuntimeEnvironment.application, "id", eventBus, false);
        savedFile = ambientSoundDetector.storedData();
        Assert.assertTrue(savedFile.delete());
    }

    @After
    public void tearDown() throws Exception {
        eventBus = null;
        ambientSoundDetector = null;
        MyShadowAudioManager.reset();
        ShadowAudioRecord.reset();
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(ambientSoundDetector);
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertEquals("AmbientSound", ambientSoundDetector.detectorName());
        Assert.assertTrue(ambientSoundDetector.isEnabled());
        Assert.assertEquals(DetectorType.Audio, ambientSoundDetector.detectorType());
        Assert.assertEquals(3000, ambientSoundDetector.listenInterval());
        Assert.assertEquals(48000, ambientSoundDetector.sampleRate());
        Assert.assertEquals(AudioFormat.ENCODING_PCM_16BIT, ambientSoundDetector.audioEncoding());
        Assert.assertEquals(64, ambientSoundDetector.windowSize());
    }

    @Test
    public void testSupportsUnprocessed() throws Exception {
        Assert.assertFalse(ambientSoundDetector.supportsUnprocessed());
    }

    @Test
    public void testEnabled() throws Exception {
        Assert.assertTrue(ambientSoundDetector.isEnabled());
    }

    @Test
    public void testAvailableWithPermissionDenied() throws Exception {
        ShadowApplication.getInstance().denyPermissions(Manifest.permission.RECORD_AUDIO);
        Assert.assertFalse(ambientSoundDetector.isAvailable());
    }

    @Test
    public void testAvailableWithPermissionGranted() throws Exception {
        Assert.assertTrue(ambientSoundDetector.isAvailable());
    }

    @Test
    public void testListeningWhenNotAvailableAndMuted() throws Exception {
        MyShadowAudioManager.setMicrophoneMuted(true);
        Assert.assertFalse(MyShadowAudioManager.didChangeMicrophoneSettings());
        ShadowApplication.getInstance().denyPermissions(Manifest.permission.RECORD_AUDIO);
        Assert.assertTrue(ambientSoundDetector.isMuted());
        Assert.assertTrue(ambientSoundDetector.startListening());
        Assert.assertFalse(ambientSoundDetector.isRecording());
        Field disabledMuteField = AmbientSoundDetector.class.getDeclaredField("disabledMute");
        disabledMuteField.setAccessible(true);
        Assert.assertFalse(disabledMuteField.getBoolean(ambientSoundDetector));
        Assert.assertFalse(MyShadowAudioManager.didChangeMicrophoneSettings());
    }

    @Test
    public void testListeningWhenAvailableAndMuted() throws Exception {
        MyShadowAudioManager.setMicrophoneMuted(true);
        Assert.assertTrue(ambientSoundDetector.isMuted());
        Assert.assertTrue(ambientSoundDetector.startListening());
        Field disabledMuteField = AmbientSoundDetector.class.getDeclaredField("disabledMute");
        disabledMuteField.setAccessible(true);
        Assert.assertTrue(disabledMuteField.getBoolean(ambientSoundDetector));
    }

    @Test
    public void testListeningWhenNotAvailableAndNotMuted() throws Exception {
        ShadowApplication.getInstance().denyPermissions(Manifest.permission.RECORD_AUDIO);
        Assert.assertFalse(ambientSoundDetector.isMuted());
        Assert.assertTrue(ambientSoundDetector.startListening());
        Assert.assertFalse(ambientSoundDetector.isRecording());
        Field disabledMuteField = AmbientSoundDetector.class.getDeclaredField("disabledMute");
        disabledMuteField.setAccessible(true);
        Assert.assertFalse(disabledMuteField.getBoolean(ambientSoundDetector));
    }

    @Test
    public void testListeningWhenAvailableAndNotMuted() throws Exception {
        Assert.assertFalse(ambientSoundDetector.isMuted());
        Assert.assertTrue(ambientSoundDetector.startListening());
    }

    @Test
    public void testRecording() throws Exception {
        ambientSoundDetector = new AmbientSoundDetector(RuntimeEnvironment.application, "id", eventBus, false) {
            @Override
            protected AudioRecord getAudioRecord() {
                return mockedAudioRecord;
            }
            @Override
            protected Handler getAudioHandler() {
                return mockedHandler;
            }
        };
        Assert.assertTrue(ambientSoundDetector.startListening());
        verify(mockedAudioRecord, times(1)).startRecording();
        verify(mockedHandler, times(1)).postDelayed(any(Runnable.class), eq((long)3000));
    }

    @Test
    public void testStopListeningWhenNotListening() throws Exception {
        ambientSoundDetector = new AmbientSoundDetector(RuntimeEnvironment.application, "id", eventBus, false) {
            @Override
            protected AudioRecord getAudioRecord() {
                return mockedAudioRecord;
            }
            @Override
            protected Handler getAudioHandler() {
                return mockedHandler;
            }
        };
        ambientSoundDetector.stopListening();
        verify(mockedAudioRecord, times(0)).stop();
    }

    @Test
    public void testStopListeningWhenListeningAndMuted() throws Exception {
        MyShadowAudioManager.setMicrophoneMuted(true);
        ambientSoundDetector = new AmbientSoundDetector(RuntimeEnvironment.application, "id", eventBus, false) {
            @Override
            protected AudioRecord getAudioRecord() {
                return mockedAudioRecord;
            }
            @Override
            protected Handler getAudioHandler() {
                return mockedHandler;
            }
        };
        Assert.assertFalse(MyShadowAudioManager.didChangeMicrophoneSettings());
        Field disabledMuteField = AmbientSoundDetector.class.getDeclaredField("disabledMute");
        disabledMuteField.setAccessible(true);
        Assert.assertFalse(disabledMuteField.getBoolean(ambientSoundDetector));
        Assert.assertTrue(ambientSoundDetector.isMuted());
        ambientSoundDetector.startListening();
        verify(mockedAudioRecord, times(1)).startRecording();
        Assert.assertFalse(ambientSoundDetector.isMuted());
        Assert.assertTrue(MyShadowAudioManager.didChangeMicrophoneSettings());
        Assert.assertTrue(disabledMuteField.getBoolean(ambientSoundDetector));
        when(mockedAudioRecord.getRecordingState()).thenReturn(AudioRecord.RECORDSTATE_RECORDING);
        ambientSoundDetector.stopListening();
        verify(mockedAudioRecord, times(1)).stop();
        Assert.assertFalse(disabledMuteField.getBoolean(ambientSoundDetector));
        Assert.assertTrue(ambientSoundDetector.isMuted());
    }

    @Test
    public void testStopListeningWhenListeningAndNotMuted() throws Exception {
        ambientSoundDetector = new AmbientSoundDetector(RuntimeEnvironment.application, "id", eventBus, false) {
            @Override
            protected AudioRecord getAudioRecord() {
                return mockedAudioRecord;
            }
            @Override
            protected Handler getAudioHandler() {
                return mockedHandler;
            }
        };
        ambientSoundDetector.startListening();
        verify(mockedAudioRecord, times(1)).startRecording();
        when(mockedAudioRecord.getRecordingState()).thenReturn(AudioRecord.RECORDSTATE_RECORDING);
        ambientSoundDetector.stopListening();
        verify(mockedAudioRecord, times(1)).stop();
    }

    @Test
    public void testRecordingStoresFeaturesToFile() throws Exception {
        // TODO: Yelp :o
        ambientSoundDetector = new AmbientSoundDetector(RuntimeEnvironment.application, "id", eventBus, false);
        Assert.assertFalse(savedFile.exists());
        Assert.assertFalse(ShadowAudioRecord.hasReadFromStream());
        Assert.assertTrue(ambientSoundDetector.startListening());
        ShadowLooper.runUiThreadTasks();
        ShadowLooper.idleMainLooper(2500, TimeUnit.MILLISECONDS);
        Assert.assertFalse(savedFile.exists());
        ShadowLooper.idleMainLooper(1000, TimeUnit.MILLISECONDS); // Pushing it abit to ensure it has passed the limit
        Assert.assertTrue(ShadowAudioRecord.hasReadFromStream());
        // TODO: Need it to actually read data and store to file for confirmation!!
//        Assert.assertTrue(savedFile.exists());
    }
}