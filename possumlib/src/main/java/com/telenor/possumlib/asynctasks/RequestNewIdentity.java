package com.telenor.possumlib.asynctasks;

import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

/**
 * Attempts to get a new cognito identity
 */
public class RequestNewIdentity extends AsyncTask<Void, Void, String> {
    private CognitoCachingCredentialsProvider provider;
    public RequestNewIdentity(CognitoCachingCredentialsProvider provider) {
        this.provider = provider;
    }
    @Override
    protected String doInBackground(Void... params) {
        return provider.getIdentityId();
    }
}