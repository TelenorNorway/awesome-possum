package com.telenor.possumlib.functionality;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.IdentityChangedListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.telenor.possumlib.asynctasks.AsyncAmazonVerification;
import com.telenor.possumlib.constants.Constants;
import com.telenor.possumlib.interfaces.IAmazonIdentityConfirmed;
import com.telenor.possumlib.interfaces.IOnVerify;

/**
 * Functionality to handle all interaction with Amazons S3, specifically retrieving the
 * cognito provider and ensuring that the id is confirmed
 */
public class AmazonFunctionality extends AsyncTask<Void, Void, String> implements IdentityChangedListener {
    private CognitoCachingCredentialsProvider cognitoProvider;
    private final Context context;
    private IAmazonIdentityConfirmed listener;
    private static final String tag = AmazonFunctionality.class.getName();

    public AmazonFunctionality(@NonNull Context context, @NonNull IAmazonIdentityConfirmed listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCognitoProviderWithIdentityPoolId(@NonNull String identityPoolId) {
        cognitoProvider = new CognitoCachingCredentialsProvider(
                context,
                identityPoolId,
                Regions.US_EAST_1 // Region
        );
        if (cognitoProvider.getCachedIdentityId() != null) {
            listener.foundAmazonIdentity(new AmazonS3Client(cognitoProvider));
        } else {
            cognitoProvider.registerIdentityChangedListener(this);
            execute((Void) null);
        }
    }

    @Override
    public void identityChanged(String oldIdentityId, String newIdentityId) {
        cognitoProvider.unregisterIdentityChangedListener(this);
        if (cognitoProvider.getCachedIdentityId() == null) {
            listener.failedToFindAmazonIdentity();
        } else {
            listener.foundAmazonIdentity(new AmazonS3Client(cognitoProvider));
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        return cognitoProvider.getIdentityId();
    }

    public void getVerificationFile(@NonNull AmazonS3Client client, @NonNull String bucketKey, @NonNull IOnVerify verifyListener) {
        Log.i(tag, "Getting verification file:" + Constants.BUCKET+" - "+bucketKey);
        AsyncAmazonVerification verification = new AsyncAmazonVerification(bucketKey, verifyListener);
        verification.execute(client);
    }
}