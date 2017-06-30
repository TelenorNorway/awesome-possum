package com.telenor.possumlib.interfaces;

/**
 * Interface for when tensorFlow is loaded
 */
public interface ITensorLoadComplete {
    void tensorFlowLoaded();
    void tensorFlowFailedLoad();
}