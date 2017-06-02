package com.telenor.possumlib.detectortests;

import android.view.MotionEvent;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.GestureDetector;

import net.danlew.android.joda.JodaTimeAndroid;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(PossumTestRunner.class)
public class GestureDetectorTest {
    private GestureDetector gestureDetector;
    private EventBus eventBus;

    @Before
    public void setUp() throws Exception {
        eventBus  = new EventBus();
        JodaTimeAndroid.init(RuntimeEnvironment.application);
        gestureDetector = new GestureDetector(RuntimeEnvironment.application, "fakeUnique", "fakeId", eventBus);
        if (gestureDetector.storedData().exists()) {
            Assert.assertTrue(gestureDetector.storedData().delete());
        }
    }

    @After
    public void tearDown() throws Exception {
        gestureDetector = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(gestureDetector);
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertTrue(gestureDetector.isEnabled());
        Assert.assertTrue(gestureDetector.isValidSet());
        Assert.assertTrue(gestureDetector.isAvailable());
        Assert.assertEquals(DetectorType.Gesture, gestureDetector.detectorType());
        Assert.assertEquals("Gesture", gestureDetector.detectorName());
    }

    @Test
    public void testOnTouch() throws Exception {
        Assert.assertEquals(0, gestureDetector.sessionValues().size());
        Assert.assertTrue(gestureDetector.storedData().length() == 0);
        gestureDetector.onTouch(null, MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1, null, null, 0, 0, 0, 0, 0, 0, 0, 0));
        Assert.assertEquals(0, gestureDetector.sessionValues().size());
        Assert.assertTrue(gestureDetector.storedData().length() > 0);
    }
}