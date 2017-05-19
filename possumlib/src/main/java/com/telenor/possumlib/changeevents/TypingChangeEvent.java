package com.telenor.possumlib.changeevents;

/**
 * An event for notifying about a change in typing
 */
public class TypingChangeEvent extends BasicChangeEvent {
    TypingChangeEvent(String message) {
        super(null, message);
    }
}