package com.telenor.possumlib.services;

import com.telenor.possumlib.abstractservices.AbstractCollectionService;

/**
 * Service handling uploading of data set to server for trustScore
 * analysis
 */
public class AuthenticationService extends AbstractCollectionService {
    @Override
    protected boolean isAuthenticating() {
        return true;
    }

    @Override
    public long timeSpentGathering() {
        return 3000;
    }
}