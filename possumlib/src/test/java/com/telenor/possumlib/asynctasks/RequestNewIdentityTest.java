package com.telenor.possumlib.asynctasks;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.telenor.possumlib.PossumTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowApplication;

import java.lang.reflect.Field;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(PossumTestRunner.class)
public class RequestNewIdentityTest {
    private RequestNewIdentity newIdentity;
    @Mock
    private CognitoCachingCredentialsProvider mockedProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        newIdentity = new RequestNewIdentity(mockedProvider);
    }

    @After
    public void tearDown() throws Exception {
        newIdentity.cancel(true);
        newIdentity = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(newIdentity);
        Field providerField = RequestNewIdentity.class.getDeclaredField("provider");
        providerField.setAccessible(true);
        Assert.assertEquals(mockedProvider, providerField.get(newIdentity));
    }

    @Test
    public void testBackgroundTaskRequestsIdentityId() throws Exception {
        newIdentity.execute((Void)null);
        ShadowApplication.runBackgroundTasks();
        verify(mockedProvider, atLeastOnce()).getIdentityId();
    }

}