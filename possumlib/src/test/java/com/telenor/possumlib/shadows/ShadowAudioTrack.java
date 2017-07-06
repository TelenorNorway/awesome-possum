package com.telenor.possumlib.shadows;

import android.media.AudioTrack;

import com.telenor.possumlib.constants.Constants;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link android.media.AudioTrack}
 */
@Implements(AudioTrack.class)
public class ShadowAudioTrack {
    @Implementation
    public static int getMinBufferSize(int sampleRate, int channelConfig, int audioFormat) {
        return Constants.AUTHENTICATION_TIME; // This is actually retrieved by way of a native method, assumes a standard value
    }
}