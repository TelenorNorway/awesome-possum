package com.telenor.possumlib.interfaces;

import com.telenor.possumlib.changeevents.PossumEvent;

public interface IPossumEventListener {
    void eventReceived(PossumEvent object);
}