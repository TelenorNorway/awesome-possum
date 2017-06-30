package com.telenor.possumlib.interfaces;

/**
 * Interface determining a change in trustScore
 */
public interface IPossumTrust {
    void changeInCombinedTrust(float combinedTrustScore, String status);
    void changeInDetectorTrust(int detectorType, float newTrustScore, String status);
    void failedToAscertainTrust(Exception exception);
}