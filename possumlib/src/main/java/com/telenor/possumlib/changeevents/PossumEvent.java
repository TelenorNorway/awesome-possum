package com.telenor.possumlib.changeevents;

public class PossumEvent {
    private String eventType;
    private String message;
    public PossumEvent(String eventType, String message) {
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
