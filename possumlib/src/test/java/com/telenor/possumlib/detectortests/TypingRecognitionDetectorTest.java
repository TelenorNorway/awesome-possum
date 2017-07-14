package com.telenor.possumlib.detectortests;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.changeevents.TypingChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.TypingRecognitionDetector;
import com.telenor.possumlib.models.PossumBus;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(PossumTestRunner.class)
public class TypingRecognitionDetectorTest {
    private TypingRecognitionDetector typingRecognitionDetector;
    private PossumBus eventBus;
    @Before
    public void setUp() throws Exception {
        eventBus = new PossumBus();
        typingRecognitionDetector = new TypingRecognitionDetector(RuntimeEnvironment.application, eventBus);
    }

    @After
    public void tearDown() throws Exception {
        eventBus = null;
        if (typingRecognitionDetector.storedData().exists()) {
            Assert.assertTrue(typingRecognitionDetector.storedData().delete());
        }
        typingRecognitionDetector = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(typingRecognitionDetector);
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertEquals(0, typingRecognitionDetector.storedData().length());
        Assert.assertTrue(typingRecognitionDetector.isEnabled());
        Assert.assertTrue(typingRecognitionDetector.isAvailable());
        Assert.assertEquals(DetectorType.Keyboard, typingRecognitionDetector.detectorType());
        Assert.assertEquals("Keyboard", typingRecognitionDetector.detectorName());
    }

    private class FakeEvent extends PossumEvent {
        public FakeEvent() {
            super(null, null);
        }
    }

    @Test
    public void testTypingEventNotListening() throws Exception {
        eventBus.post(new TypingChangeEvent("a"));
        Assert.assertEquals(0, typingRecognitionDetector.storedData().length());
    }

    @Test
    public void testWrongEventReceived() throws Exception {
        typingRecognitionDetector.startListening();
        eventBus.post(new FakeEvent());
        Assert.assertEquals(0, typingRecognitionDetector.storedData().length());
    }
    @Test
    public void testTypingEventWhenListening() throws Exception {
        typingRecognitionDetector.startListening();
        eventBus.post(new TypingChangeEvent("a"));
        Assert.assertTrue(typingRecognitionDetector.storedData().length() > 0);
    }
}