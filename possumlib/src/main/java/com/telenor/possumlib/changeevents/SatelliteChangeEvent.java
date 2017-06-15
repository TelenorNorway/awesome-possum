package com.telenor.possumlib.changeevents;

/**
 * An event for notifying a change in the gps satellites
 */
public class SatelliteChangeEvent extends PossumEvent {
    public SatelliteChangeEvent(String message) {
        super(null, message);
    }
}