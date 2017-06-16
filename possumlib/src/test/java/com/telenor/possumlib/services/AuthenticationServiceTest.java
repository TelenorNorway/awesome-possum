package com.telenor.possumlib.services;

import com.telenor.possumlib.abstractservices.AbstractCollectionService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ServiceController;

import java.lang.reflect.Field;

@RunWith(RobolectricTestRunner.class)
public class AuthenticationServiceTest {
    private ServiceController<AuthenticationService> serviceController;
    @Before
    public void setUp() throws Exception {
        serviceController = Robolectric.buildService(AuthenticationService.class);
    }
    @After
    public void tearDown() throws Exception {

    }
    @Test
    public void testDefaults() throws Exception {
        Field receiverField = AbstractCollectionService.class.getDeclaredField("receiver");
        receiverField.setAccessible(true);
        Assert.assertNull(receiverField.get(serviceController.get()));
        AuthenticationService service = serviceController.create().get();
        Assert.assertNotNull(receiverField.get(service));
    }
}