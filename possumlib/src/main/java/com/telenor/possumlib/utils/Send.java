package com.telenor.possumlib.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.telenor.possumlib.constants.Messaging;

/**
 * Utility class for sending messages
 */
public class Send {
    /**
     * Sends intent to AwesomePossum library where it can be handled
     *
     * @param type    the type of the message, should always be a constant from Messaging
     * @param message the actual message to be sent
     */
    public static void messageIntent(@NonNull Context context, String type, String message) {
        Intent intent = new Intent(Messaging.POSSUM_MESSAGE);
        intent.putExtra(Messaging.POSSUM_MESSAGE_TYPE, type);
        intent.putExtra(Messaging.POSSUM_MESSAGE, message);
        context.sendBroadcast(intent);
    }

}
