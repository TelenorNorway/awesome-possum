package com.telenor.possumlib.exceptions;

/**
 * Exception thrown if user hasn't authorized the app to gather data
 */
public class GatheringNotAuthorizedException extends Exception {
    public GatheringNotAuthorizedException() {
        super("Gathering is not allowed by user");
    }
}
