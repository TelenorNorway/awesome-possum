package com.telenor.possumlib.changeevents;

/**
 * An event for requesting either a single image or a continuous stream
 */
public class ImageChangeEvent extends PossumEvent {
    ImageChangeEvent(String eventType) {
        super(eventType, null);
    }
}