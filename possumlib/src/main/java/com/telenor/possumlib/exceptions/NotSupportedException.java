package com.telenor.possumlib.exceptions;

/**
 * Exception for actions that are not supported due to reasons
 */
public class NotSupportedException extends Exception {
    public NotSupportedException(String message) {
        super(message);
    }
}
