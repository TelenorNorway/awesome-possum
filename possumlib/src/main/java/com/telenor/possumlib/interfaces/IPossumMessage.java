package com.telenor.possumlib.interfaces;

/**
 * Basic messaging interface, hook on to it to receive messages sent from Collector or Uploader
 * regarding the status
 */
public interface IPossumMessage {
    /**
     * Basic message receiver
     * @param msgType the type of message. All should be constants in the Messaging class
     * @param message the message received. Only interesting if errors/failures usually.
     */
    void possumMessageReceived(String msgType, String message);
}