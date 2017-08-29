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

    /**
     * Method for sending data between apps, handy if you need to see what the image taken looks
     * like
     * @param dataReceived byte array of the image taken
     */
    void possumFaceFound(byte[] dataReceived);

    void possumImageSnapped(byte[] dataReceived);
    /**
     * The spots on given image for
     * @param xCoords the x-coordinates on the image where a face landmark is found
     * @param yCoords the y-coordinates on the image where a face landmark is found
     */
    void possumFaceCoordsReceived(int[] xCoords, int[] yCoords);
}