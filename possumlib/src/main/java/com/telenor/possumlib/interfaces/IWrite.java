package com.telenor.possumlib.interfaces;

public interface IWrite {
    void bytesWritten(int progress);
    void uploadComplete(Exception e, String message);
}
