package com.telenor.possumlib.detectortests;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.detectors.GestureDetector;

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
        gestureDetector = new GestureDetector(RuntimeEnvironment.application, "fakeUnique", "fakeId", eventBus);
    }

    @After
    public void tearDown() throws Exception {
        gestureDetector = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(gestureDetector);
    }
}