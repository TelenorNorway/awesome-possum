package com.telenor.possumlib.changeevents;

/**
 * An event for notifying about a change in typing
 */
public class TypingChangeEvent extends PossumEvent {
    public TypingChangeEvent(String message) {
        super(null, message);
    }
}