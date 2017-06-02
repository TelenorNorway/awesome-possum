package com.telenor.possumlib.detectortests;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.changeevents.TypingChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.TypingRecognitionSensor;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(PossumTestRunner.class)
public class TypingRecognitionSensorTest {
    private TypingRecognitionSensor typingRecognitionSensor;
    private EventBus eventBus;
    @Before
    public void setUp() throws Exception {
        eventBus = new EventBus();
        typingRecognitionSensor = new TypingRecognitionSensor(RuntimeEnvironment.application, "id", "secretKeyHash", eventBus);
    }

    @After
    public void tearDown() throws Exception {
        eventBus = null;
        if (typingRecognitionSensor.storedData().exists()) {
            Assert.assertTrue(typingRecognitionSensor.storedData().delete());
        }
        typingRecognitionSensor = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(typingRecognitionSensor);
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertEquals(0, typingRecognitionSensor.storedData().length());
        Assert.assertTrue(typingRecognitionSensor.isEnabled());
        Assert.assertTrue(typingRecognitionSensor.isAvailable());
        Assert.assertTrue(typingRecognitionSensor.isValidSet());
        Assert.assertEquals(DetectorType.Keyboard, typingRecognitionSensor.detectorType());
        Assert.assertEquals("Keyboard", typingRecognitionSensor.detectorName());
    }

    private class FakeEvent extends BasicChangeEvent {
        public FakeEvent() {
            super(null, null);
        }
    }

    @Test
    public void testTypingEventNotListening() throws Exception {
        eventBus.post(new TypingChangeEvent("a"));
        Assert.assertEquals(0, typingRecognitionSensor.storedData().length());
    }

    @Test
    public void testWrongEventReceived() throws Exception {
        typingRecognitionSensor.startListening();
        eventBus.post(new FakeEvent());
        Assert.assertEquals(0, typingRecognitionSensor.storedData().length());
    }
    @Test
    public void testTypingEventWhenListening() throws Exception {
        typingRecognitionSensor.startListening();
        eventBus.post(new TypingChangeEvent("a"));
        Assert.assertTrue(typingRecognitionSensor.storedData().length() > 0);
    }
}