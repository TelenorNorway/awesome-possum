package com.telenor.possumlib.detectors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEternalEventDetector;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.constants.DetectorType;

import org.joda.time.DateTime;

/**
 * Sensor meant to take in different events regarding the apps events
 */
public class MetaDataDetector extends AbstractEternalEventDetector {
    public static final String BATTERY_CHANGED = "BATTERY_CHANGED";
    public static final String GENERAL_EVENT = "GENERAL_EVENT";
    private static final String tag = MetaDataDetector.class.getName();

    public MetaDataDetector(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) throws IllegalArgumentException {
        super(context, identification, secretKeyHash, eventBus);
    }

    @Override
    public void eventReceived(BasicChangeEvent object) {
        if (object instanceof MetaDataChangeEvent) {
            MetaDataChangeEvent event = (MetaDataChangeEvent)object;
            long timestamp = DateTime.now().getMillis();
            switch (event.eventType()) {
                case GENERAL_EVENT:
                    sessionValues.add(timestamp+" "+event.message());
                    break;
                case BATTERY_CHANGED:
                    sessionValues.add(timestamp+" BATTERY_CHANGED "+event.message());
                    break;
                default:
                    Log.e(tag, "Unhandled meta event:"+ event.eventType());
            }
            super.eventReceived(object);
        }
    }

    @Override
    public int detectorType() {
        return DetectorType.MetaData;
    }
}