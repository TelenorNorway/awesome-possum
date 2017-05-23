package com.telenor.possumlib.changeevents;

/**
 * Class for handling basic events with google's eventBus library
 */
public abstract class BasicChangeEvent {
    private String eventType;
    private String message;
    public BasicChangeEvent(String eventType, String message) {
        this.eventType = eventType;
        this.message = message;
    }

    public String eventType() {
        return eventType;
    }
    public String message() {
        return message;
    }
}
