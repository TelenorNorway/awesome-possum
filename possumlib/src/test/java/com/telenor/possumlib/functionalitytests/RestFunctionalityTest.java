package com.telenor.possumlib.functionalitytests;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.functionality.RestFunctionality;
import com.telenor.possumlib.interfaces.IRestListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PossumTestRunner.class)
public class RestFunctionalityTest {
    private RestFunctionality restFunctionality;
    private IRestListener listener;
    private String url;
    private String kurtId;

    @Before
    public void setUp() throws Exception {
        url = "http://fakeAssUrl.com";
        kurtId = "myFakeKurt";
        listener = new IRestListener() {
            @Override
            public void successfullyPushed() {

            }

            @Override
            public void failedToPush(Exception exception) {

            }
        };
        restFunctionality = new RestFunctionality(listener, url, kurtId);
    }

    @After
    public void tearDown() throws Exception {
        listener = null;
        restFunctionality = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(listener);
        Assert.assertNotNull(restFunctionality);
    }
}