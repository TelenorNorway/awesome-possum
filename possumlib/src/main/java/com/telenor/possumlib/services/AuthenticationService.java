package com.telenor.possumlib.services;

import android.util.Log;

import com.telenor.possumlib.abstractservices.AbstractCollectionService;

/**
 * Service handling uploading of data set to server for trustScore
 * analysis
 */
public class AuthenticationService extends AbstractCollectionService {
    private static final String tag = AuthenticationService.class.getName();

    @Override
    protected boolean isAuthenticating() {
        return true;
    }

    @Override
    public void performPostAction() {
        // TODO: Perform upload
        Log.i(tag, "Performing upload");
    }

    @Override
    public long timeSpentGathering() {
        return 3000;
    }
}