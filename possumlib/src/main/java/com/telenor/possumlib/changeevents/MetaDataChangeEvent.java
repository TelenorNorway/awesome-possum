package com.telenor.possumlib.changeevents;

/**
 * An event for storing meta data about whats happening
 */
public class MetaDataChangeEvent extends BasicChangeEvent {
    public MetaDataChangeEvent(String messageType, String message) {
        super(messageType, message);
    }
}
