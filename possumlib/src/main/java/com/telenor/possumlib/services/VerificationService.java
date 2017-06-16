package com.telenor.possumlib.services;

import android.util.Log;

import com.amazonaws.services.s3.AmazonS3Client;
import com.telenor.possumlib.abstractservices.AbstractAmazonService;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.interfaces.IOnVerify;
import com.telenor.possumlib.utils.Send;

/**
 * Used to confirm that Awesome Possum still is used. Checks for the existence of a certain file
 * in S3 and verifies it has the correct values. If it is missing or has invalid values - the
 * Awesome Possum is considered terminated and is unauthorized to continue.
 */
public class VerificationService extends AbstractAmazonService implements IOnVerify {
    @Override
    public void foundAmazonIdentity(AmazonS3Client client) {
        amazonFunctionality.getVerificationFile(client, "killswitch/state", this);
    }

    @Override
    public void failedToFindAmazonIdentity() {
        Log.e(tag, "Unable to find amazon identity in verificationService");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "Terminating verification service");
    }

    @Override
    public void allowAwesomePossumToRun() {
        Log.i(tag, "Allow possum to run");
        stopSelf();
    }

    @Override
    public void terminateAllAwesomeness() {
        Log.i(tag, "EXTERMINATE - EXTERMINAATE - EEEXTEEERMINAAAATEEE!!!");
        Send.messageIntent(this, Messaging.POSSUM_TERMINATE, "WithExtremePrejudice");
        stopSelf();
    }
}
