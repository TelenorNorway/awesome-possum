package com.telenor.possumlib.detectortests;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.detectors.AmbientSoundDetector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AmbientSoundDetectorTest {
    private AmbientSoundDetector ambientSoundDetector;
    private EventBus eventBus;
    @Before
    public void setUp() throws Exception {
        eventBus = new EventBus();
        ambientSoundDetector = new AmbientSoundDetector(RuntimeEnvironment.application, "id", "hash", eventBus);
    }

    @After
    public void tearDown() throws Exception {
        eventBus = null;
        ambientSoundDetector = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(ambientSoundDetector);
    }
}