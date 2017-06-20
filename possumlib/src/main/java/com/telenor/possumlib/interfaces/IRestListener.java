package com.telenor.possumlib.interfaces;

public interface IRestListener {
    void successfullyPushed(String message);
    void failedToPush(Exception exception);
}