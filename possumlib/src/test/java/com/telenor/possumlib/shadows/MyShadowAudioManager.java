package com.telenor.possumlib.shadows;

import android.media.AudioManager;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(AudioManager.class)
public class MyShadowAudioManager {
    private static boolean isMuted;
    private static boolean changedMuted;
    @Implementation
    public boolean isMicrophoneMute() {
        return isMuted;
    }

    @Implementation
    public void setMicrophoneMute(boolean muted) {
        changedMuted = true;
        isMuted = muted;
    }
    public static void setMicrophoneMuted(boolean muted) {
        isMuted = muted;
    }

    public static boolean didChangeMicrophoneSettings() {
        return changedMuted;
    }

    @Resetter
    public static void reset() {
        isMuted = false;
        changedMuted = false;
    }
}