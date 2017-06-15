package com.telenor.possumlib.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.IdentityChangedListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.telenor.possumlib.asynctasks.RequestNewIdentity;
import com.telenor.possumlib.interfaces.IAmazonIdentityConfirmed;

/**
 * Functionality to handle all interaction with Amazons S3, specifically retrieving the
 * cognito provider and ensuring that the id is confirmed
 */
public class AmazonFunctionality implements IdentityChangedListener {
    private CognitoCachingCredentialsProvider cognitoProvider;
    private final Context context;
    private IAmazonIdentityConfirmed listener;

    public AmazonFunctionality(@NonNull Context context, @NonNull IAmazonIdentityConfirmed listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCognitoProviderWithBucket(@NonNull String bucket) {
        cognitoProvider = new CognitoCachingCredentialsProvider(
                context,
                bucket, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        if (cognitoProvider.getCachedIdentityId() != null) {
            listener.foundAmazonIdentity();
        } else {
            cognitoProvider.registerIdentityChangedListener(this);
            new RequestNewIdentity(cognitoProvider).execute((Void) null);
        }
    }

    public AmazonS3Client amazonClient() {
        return cognitoProvider.getCachedIdentityId() != null?new AmazonS3Client(cognitoProvider):null;
    }

    @Override
    public void identityChanged(String oldIdentityId, String newIdentityId) {
        cognitoProvider.unregisterIdentityChangedListener(this);
        if (cognitoProvider.getCachedIdentityId() == null) {
            listener.failedToFindAmazonIdentity();
        } else {
            listener.foundAmazonIdentity();
        }
    }
}