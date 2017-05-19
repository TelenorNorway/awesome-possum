package com.telenor.possumlib.exceptions;

/**
 * Exception thrown if the library isn't initialized
 */
public class NotInitializedException extends Exception {
    public NotInitializedException() {
        super("AwesomePossum is not initialized");
    }
}