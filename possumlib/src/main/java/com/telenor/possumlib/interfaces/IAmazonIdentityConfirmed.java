package com.telenor.possumlib.interfaces;

import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Quick and easy interface for giving the amazon client on finding it
 */
public interface IAmazonIdentityConfirmed {
    void foundAmazonIdentity(AmazonS3Client client);
    void failedToFindAmazonIdentity();
}
