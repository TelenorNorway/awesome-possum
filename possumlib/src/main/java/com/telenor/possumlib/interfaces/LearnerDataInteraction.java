package com.telenor.possumlib.interfaces;

/**
 * Interface for implementing the learners data retrieval both so it can easier be mocked
 * but also so that it will be easier to switch out the data retrieval source with another
 * type
 */
public interface LearnerDataInteraction {
    /**
     * Retrieves all rows of data
     */
    int numberOfDataRows();

    /**
     * Adds a line of data to the trainingData
     * @param data string with data
     */
    void addToTrainingData(String data);

    /**
     * Compares a row of data with the collectionSize and returns whether you found it or not
     * @param data string to compare
     * @param collectionSize the size you are comparing
     * @return true if match, false if not
     */
    boolean compareTrainingDataWithSize(String data, int collectionSize);
}