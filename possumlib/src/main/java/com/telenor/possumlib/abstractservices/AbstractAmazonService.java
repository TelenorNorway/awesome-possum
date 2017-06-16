package com.telenor.possumlib.abstractservices;

import android.content.Intent;

import com.telenor.possumlib.functionality.AmazonFunctionality;
import com.telenor.possumlib.interfaces.IAmazonIdentityConfirmed;

/**
 * Service dealing with amazon's S3 service
 */
public abstract class AbstractAmazonService extends AbstractBasicService implements IAmazonIdentityConfirmed {
    protected AmazonFunctionality amazonFunctionality;

    @Override
    public void onCreate() {
        super.onCreate();
        amazonFunctionality = new AmazonFunctionality(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String identityPoolId = intent.getStringExtra("identityPoolId");
        if (identityPoolId == null) throw new RuntimeException("Missing identityPoolId on Amazon Service start");
        amazonFunctionality.setCognitoProviderWithIdentityPoolId(identityPoolId);
        return super.onStartCommand(intent, flags, startId);
    }
}