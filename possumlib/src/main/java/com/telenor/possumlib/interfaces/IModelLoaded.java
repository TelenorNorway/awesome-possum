package com.telenor.possumlib.interfaces;

/**
 * Interface for when a model is loaded successfully
 */
public interface IModelLoaded {
    void modelLoaded(int detectorType, Object model);
}