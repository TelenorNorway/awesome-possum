package com.telenor.possumlib.services;

import com.telenor.possumlib.abstractservices.AbstractCollectionService;

/**
 * Service handling general gathering of data for the purpose of later uploading it
 */
public class DataCollectionService extends AbstractCollectionService {
    @Override
    protected boolean isAuthenticating() {
        return false;
    }

    @Override
    public long timeSpentGathering() {
        return 0;
    }
}