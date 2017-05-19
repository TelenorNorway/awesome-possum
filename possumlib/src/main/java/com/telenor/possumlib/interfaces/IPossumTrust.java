package com.telenor.possumlib.interfaces;

/**
 * Interface determining a change in trustscore
 */
public interface IPossumTrust {
    void changeInTrust(int detectorType, float newTrustScore, float combinedTrustScore);
}