package com.telenor.possumexample.views;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class TrustTimerAnimation extends Animation {
    private float newTrustScore;
    private TrustWheel trustWheel;
    private float oldTrustScore;
    public TrustTimerAnimation(TrustWheel trustWheel, float newTrustScore) {
        this.trustWheel = trustWheel;
        this.oldTrustScore = trustWheel.trustScore();
        this.newTrustScore = newTrustScore;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float trustScore = oldTrustScore + ((newTrustScore - oldTrustScore) * interpolatedTime);
        trustWheel.setTrustScore(trustScore);
        trustWheel.requestLayout();
//        circle.setAngle(angle);
//        circle.requestLayout();
    }
}