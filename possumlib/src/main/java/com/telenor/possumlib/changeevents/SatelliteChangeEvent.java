package com.telenor.possumlib.changeevents;

/**
 * An event for notifying a change in the gps satellites
 */
public class SatelliteChangeEvent extends BasicChangeEvent {
    SatelliteChangeEvent(String message) {
        super(null, message);
    }
}