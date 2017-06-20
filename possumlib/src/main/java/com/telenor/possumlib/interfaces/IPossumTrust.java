package com.telenor.possumlib.interfaces;

/**
 * Interface determining a change in trustScore
 */
public interface IPossumTrust {
    void changeInTrust(int detectorType, float newTrustScore, float combinedTrustScore);
    void failedToAscertainTrust(Exception exception);
}