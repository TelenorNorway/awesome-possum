package com.telenor.possumlib.abstractdetectortests;

import android.content.Context;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IPossumEventListener;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.FileUtil;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class AbstractEventDrivenDetectorTest {
    private AbstractEventDrivenDetector abstractEventDrivenDetector;
    private boolean storeWithInterval;
    private File fakeFile;
    private boolean isEnabled;
    private PossumBus eventBus;
    @Mock
    private Context mockedContext;
    @Mock
    private PossumBus mockedEventBus;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        isEnabled = true;
        eventBus = new PossumBus();
        fakeFile = new File(RuntimeEnvironment.application.getFilesDir() + "/fakeFile");
        Assert.assertTrue(fakeFile.createNewFile());
        when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        abstractEventDrivenDetector = getDetectorWithEventBus(RuntimeEnvironment.application, eventBus);
    }

    @After
    public void tearDown() throws Exception {
        abstractEventDrivenDetector = null;
        if (fakeFile.exists()) Assert.assertTrue(fakeFile.delete());
        FileUtil.clearDirectory(RuntimeEnvironment.application);
    }


    private AbstractEventDrivenDetector getDetectorWithEventBus(Context context, PossumBus eventBus) {
        return new AbstractEventDrivenDetector(context, "fakeUnique", eventBus, false) {
            @Override
            protected boolean storeWithInterval() {
                return storeWithInterval;
            }

            @Override
            public boolean isEnabled() {
                return isEnabled;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public String requiredPermission() {
                return null;
            }

            @Override
            public File storedData() {
                return fakeFile;
            }

            @Override
            public int detectorType() {
                return DetectorType.MetaData;
            }

            @Override
            public String detectorName() {
                return "MetaData";
            }
        };
    }

    @Test
    public void testStartListeningWhenAvailable() throws Exception {
        abstractEventDrivenDetector = getDetectorWithEventBus(mockedContext, mockedEventBus);
        verify(mockedEventBus, never()).register(any(IPossumEventListener.class));
        Assert.assertTrue(abstractEventDrivenDetector.startListening());
        verify(mockedEventBus, atLeastOnce()).register(any(IPossumEventListener.class));
    }

    @Test
    public void startListeningWhenNotAvailable() throws Exception {
        isEnabled = false;
        abstractEventDrivenDetector = getDetectorWithEventBus(mockedContext, mockedEventBus);
        verify(mockedEventBus, never()).register(any(IPossumEventListener.class));
        Assert.assertFalse(abstractEventDrivenDetector.startListening());
        verify(mockedEventBus, never()).register(any(IPossumEventListener.class));
    }

    @Test
    public void testStopListeningWhenAvailable() throws Exception {
        abstractEventDrivenDetector = getDetectorWithEventBus(mockedContext, mockedEventBus);
        Assert.assertTrue(abstractEventDrivenDetector.startListening());
        verify(mockedEventBus, never()).unregister(any(IPossumEventListener.class));
        abstractEventDrivenDetector.stopListening();
        verify(mockedEventBus, atLeastOnce()).unregister(any(IPossumEventListener.class));
    }

    @Test
    public void testStopListeningWhenNotAvailable() throws Exception {
        isEnabled = false;
        abstractEventDrivenDetector = getDetectorWithEventBus(mockedContext, mockedEventBus);
        Assert.assertFalse(abstractEventDrivenDetector.startListening());
        verify(mockedEventBus, never()).unregister(any(IPossumEventListener.class));
        abstractEventDrivenDetector.stopListening();
        verify(mockedEventBus, never()).unregister(any(IPossumEventListener.class));
    }

    @Test
    public void testEventReceivedWithInterval() throws Exception {
        storeWithInterval = true;
        abstractEventDrivenDetector.startListening();
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());
        eventBus.post(new TestChangeEvent(null));
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());
        for (int i = 0; i < AbstractEventDrivenDetector.MINIMUM_SAMPLES; i++) {
            eventBus.post(new TestChangeEvent("message"));
        }
        Assert.assertEquals(AbstractEventDrivenDetector.MINIMUM_SAMPLES, abstractEventDrivenDetector.sessionValues().size());
        Assert.assertEquals("message", abstractEventDrivenDetector.sessionValues().get(abstractEventDrivenDetector.sessionValues().size()-1));
        eventBus.post(new TestChangeEvent("message"));
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());

    }

    @Test
    public void testEventReceivedWithoutInterval() throws Exception {
        storeWithInterval = false;
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());
        Assert.assertEquals(0, abstractEventDrivenDetector.storedData().length());
        abstractEventDrivenDetector.startListening();
        eventBus.post(new TestChangeEvent("test"));
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());
        Assert.assertTrue(abstractEventDrivenDetector.storedData().length() > 0);
    }

    @Test
    public void testNothingArrivesWhenNotListening() throws Exception {
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());
        Assert.assertEquals(0, abstractEventDrivenDetector.storedData().length());
        eventBus.post(new TestChangeEvent("test"));
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());
        Assert.assertEquals(0, abstractEventDrivenDetector.storedData().length());
    }

    class TestChangeEvent extends PossumEvent {
        TestChangeEvent(String message) {
            super(null, message);
        }
    }
}