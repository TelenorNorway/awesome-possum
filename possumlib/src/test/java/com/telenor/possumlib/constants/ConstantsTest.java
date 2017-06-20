package com.telenor.possumlib.constants;

import com.telenor.possumlib.PossumTestRunner;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PossumTestRunner.class)
public class ConstantsTest {
    @Test
    public void testConstantsValues() throws Exception {
        Assert.assertEquals("encryptedKurt", Constants.ENCRYPTED_KURT);
        Assert.assertEquals("AwesomePossumPrefs", Constants.SHARED_PREFERENCES);
        Assert.assertEquals("tempEncryptedKurt", Constants.ENCRYPTED_TEMP_KURT);
        Assert.assertEquals("isLearning", Constants.IS_LEARNING);
        Assert.assertEquals("startTime", Constants.START_TIME);
    }
}