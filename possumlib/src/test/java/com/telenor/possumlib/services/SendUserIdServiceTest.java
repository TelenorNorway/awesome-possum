package com.telenor.possumlib.services;

import com.telenor.possumlib.PossumTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.util.ServiceController;

@RunWith(PossumTestRunner.class)
public class SendUserIdServiceTest {
    private ServiceController<SendUserIdService> serviceController;

    @Before
    public void setUp() throws Exception {
        serviceController = Robolectric.buildService(SendUserIdService.class).attach();
    }

    @After
    public void tearDown() throws Exception {
        serviceController.destroy();
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(serviceController.get());
    }
}