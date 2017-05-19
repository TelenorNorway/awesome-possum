package com.telenor.possumlib.interfaces;

/**
 * Contains information regarding the sensors availability
 */
public interface ISensorStatusUpdate {
    void sensorStatusChanged(int sensorType);
}
