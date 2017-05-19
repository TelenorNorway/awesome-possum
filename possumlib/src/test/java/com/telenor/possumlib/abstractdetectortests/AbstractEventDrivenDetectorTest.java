package com.telenor.possumlib.abstractdetectortests;

import android.content.Context;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.WifiChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.utils.FileUtil;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;

import java.io.File;

import static com.telenor.possumlib.abstractdetectors.AbstractDetector.MINIMUM_SAMPLES;

@RunWith(PossumTestRunner.class)
public class AbstractEventDrivenDetectorTest {
    private AbstractEventDrivenDetector abstractEventDrivenDetector;
    private boolean storeWithInterval;
    private File fakeFile;
    private boolean isEnabled;
    private EventBus eventBus;

    @Before
    public void setUp() throws Exception {
        storeWithInterval = false;
        isEnabled = true;
        eventBus = new EventBus();
        fakeFile = new File(RuntimeEnvironment.application.getFilesDir() + "/data/fakeFile");
        Context mockedContext = Mockito.mock(Context.class);
        Mockito.when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        abstractEventDrivenDetector = new AbstractEventDrivenDetector(mockedContext, "fakeUnique", "fakeId", eventBus) {
            @Override
            protected boolean storeWithInterval() {
                return storeWithInterval;
            }

            @Override
            public boolean isEnabled() {
                return isEnabled;
            }

            @Override
            public boolean isValidSet() {
                return true;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public File storedData() {
                return fakeFile;
            }

            @Override
            public int detectorType() {
                return DetectorType.MetaData;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        abstractEventDrivenDetector = null;
        FileUtil.clearDirectory(RuntimeEnvironment.application);
    }

    @Test
    public void testObjectChangedWithInterval() throws Exception {
        storeWithInterval = true;
        File dataDir = new File(RuntimeEnvironment.application.getFilesDir() + "/data");
        if (dataDir.exists()) {
            Assert.assertTrue(dataDir.delete());
        }
        Assert.assertTrue(dataDir.mkdir());
        Assert.assertTrue(fakeFile.createNewFile());
        for (int i = 0; i < MINIMUM_SAMPLES; i++) {
//            abstractEventDrivenDetector.sessionValues().add();
            abstractEventDrivenDetector.eventReceived(new WifiChangeEvent("test"));
//            abstractEventDrivenDetector.objectChanged(null);
        }
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertEquals(500, abstractEventDrivenDetector.sessionValues().size());
        abstractEventDrivenDetector.eventReceived(new WifiChangeEvent("test"));
//        abstractEventDrivenDetector.sessionValues().add("test");
        Assert.assertEquals(3006, fakeFile.length());
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());
        abstractEventDrivenDetector.sessionValues().add("test");
//        abstractEventDrivenDetector.objectChanged(null);
        Assert.assertEquals(3006, fakeFile.length());
        Assert.assertEquals(1, abstractEventDrivenDetector.sessionValues().size());
    }

    @Test
    public void testObjectChangedWithoutInterval() throws Exception {
        storeWithInterval = false;
        File dataDir = new File(RuntimeEnvironment.application.getFilesDir() + "/data");
        if (dataDir.exists()) {
            Assert.assertTrue(dataDir.delete());
        }
        Assert.assertTrue(dataDir.mkdir());
        Assert.assertTrue(fakeFile.createNewFile());
        for (int i = 0; i < (MINIMUM_SAMPLES/2); i++) {
            abstractEventDrivenDetector.sessionValues().add("test");
//            abstractEventDrivenDetector.objectChanged(null);
        }
        Assert.assertTrue(fakeFile.length() > 0);
        Assert.assertEquals(0, abstractEventDrivenDetector.sessionValues().size());
    }

    @Test
    public void testFillerListChanged() throws Exception {
        ShadowLog.setupLogging();
//        abstractEventDrivenDetector.listChanged();
        Assert.assertTrue(ShadowLog.getLogs().size() == 1);
    }
}