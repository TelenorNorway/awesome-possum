package com.telenor.possumlib.functionalitytests;

import com.amazonaws.services.s3.AmazonS3Client;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.functionality.AmazonFunctionality;
import com.telenor.possumlib.interfaces.IAmazonIdentityConfirmed;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(PossumTestRunner.class)
public class AmazonFunctionalityTest {
    private AmazonFunctionality amazonFunctionality;
    private IAmazonIdentityConfirmed listener;

    @Before
    public void setUp() throws Exception {
        listener = new IAmazonIdentityConfirmed() {
            @Override
            public void foundAmazonIdentity(AmazonS3Client client) {

            }

            @Override
            public void failedToFindAmazonIdentity() {

            }
        };
        amazonFunctionality = new AmazonFunctionality(RuntimeEnvironment.application, listener);
    }

    @After
    public void tearDown() throws Exception {
        listener = null;
        amazonFunctionality = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(listener);
        Assert.assertNotNull(amazonFunctionality);
    }
}