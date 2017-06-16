package com.telenor.possumlib.interfaces;

public interface IRestListener {
    void successfullyPushed();
    void failedToPush(Exception exception);
}