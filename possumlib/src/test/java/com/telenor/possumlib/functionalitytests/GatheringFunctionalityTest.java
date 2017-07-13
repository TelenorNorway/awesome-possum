package com.telenor.possumlib.functionalitytests;

import android.content.Context;
import android.support.annotation.NonNull;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.functionality.GatheringFunctionality;
import com.telenor.possumlib.models.PossumBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunWith(PossumTestRunner.class)
public class GatheringFunctionalityTest {
    private GatheringFunctionality gatheringFunctionality;
    private boolean methodCalled;
    private boolean detectorsAdded;
    @Before
    public void setUp() throws Exception {
        methodCalled = false;
        detectorsAdded = false;
        gatheringFunctionality = new GatheringFunctionality();
    }

    @After
    public void tearDown() throws Exception {
        gatheringFunctionality = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(gatheringFunctionality);
        Field eventBusField = GatheringFunctionality.class.getDeclaredField("eventBus");
        eventBusField.setAccessible(true);
        Object eventBus = eventBusField.get(gatheringFunctionality);
        Assert.assertNotNull(eventBus);
        Assert.assertTrue(eventBus instanceof PossumBus);

        Field detectorsField = GatheringFunctionality.class.getDeclaredField("detectors");
        detectorsField.setAccessible(true);
        Object detectors = detectorsField.get(gatheringFunctionality);
        Assert.assertNotNull(detectors);
        Assert.assertTrue(detectors instanceof ConcurrentLinkedQueue);
    }

    @Test
    public void testSettingKurtIdStopsGathering() throws Exception {
        gatheringFunctionality = new GatheringFunctionality(){
            @Override
            public void stopGathering() {
                methodCalled = true;
            }
            @Override
            public void addDetectors(@NonNull Context context, @NonNull String uniqueUserId, boolean isAuthenticating) {
                detectorsAdded = true;
            }
        };
        Assert.assertFalse(methodCalled);
        Assert.assertFalse(detectorsAdded);
        gatheringFunctionality.setDetectors(RuntimeEnvironment.application, "uniqueUserId", false, null);
        Assert.assertTrue(methodCalled);
        Assert.assertTrue(detectorsAdded);
    }
}