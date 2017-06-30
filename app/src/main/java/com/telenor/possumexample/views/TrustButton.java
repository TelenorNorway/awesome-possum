package com.telenor.possumexample.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.telenor.possumexample.R;
import com.telenor.possumlib.AwesomePossum;

import java.util.Locale;

public class TrustButton extends RelativeLayout {
    private TrustWheel trustWheel;
    private TextView centerTextView;
    private boolean authenticating;
    private float trustScore;
    private int timePassedInMillis;
    private Handler authHandler = new Handler(Looper.getMainLooper());
    private Runnable authRunnable;

    private static final String tag = TrustButton.class.getName();

    public TrustButton(Context context) {
        super(context);
        init(context);
    }

    public TrustButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TrustButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setRunnableWithId(final String myId) {
        authRunnable = new Runnable() {
            @Override
            public void run() {
                timePassedInMillis += authInterval();
                trustWheel.setProgress(timePassedInMillis);
                if (timePassedInMillis >= authTime()) {
                    timePassedInMillis = 0;
                    AwesomePossum.authenticate(getContext(), myId, getContext().getString(R.string.authenticateUrl), getContext().getString(R.string.apiKey), true);
                }
                authHandler.postDelayed(authRunnable, authInterval());
            }
        };
    }

    private void init(Context context) {
        trustWheel = new TrustWheel(context, authTime());
        addView(trustWheel);
        centerTextView = new TextView(context);
        centerTextView.setTextColor(Color.GRAY);
        centerTextView.setTextSize(32); // TODO: Text size should be size dependent
        centerTextView.setBackgroundColor(Color.TRANSPARENT);
        LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        centerTextView.setLayoutParams(params);
        addView(centerTextView);
        centerTextView.bringToFront();
        setTrustScore(trustScore, null);
    }

    /**
     * Time spent waiting for an authentication
     *
     * @return the time in milliseconds
     */
    private int authTime() {
        return 5000;
    }

    private int authInterval() {
        return 20;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        trustWheel.setEnabled(enabled);
    }

    public void setTrustScore(float score, String overridingStatus) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Invalid trustScore, must be within 0 and 100. Was " + score + ".");
        }
        if (centerTextView == null || trustWheel == null) return;
        if (overridingStatus != null) {
            centerTextView.setTextColor(Color.WHITE);
            centerTextView.setText(overridingStatus);
        } else {
            centerTextView.setText(String.format(Locale.US, "%.0f%%", score));
            centerTextView.setTextColor(Color.BLACK);
        }
        trustScore = score;
        trustWheel.setTrustScore(score);
    }

    public void toggleAuthenticating(String id) {
        authenticating = !authenticating;
        if (authenticating) {
            Log.i(tag, "Authenticating");
            AwesomePossum.authenticate(getContext(), id, getContext().getString(R.string.authenticateUrl), getContext().getString(R.string.apiKey));
            authHandler.postDelayed(authRunnable, authInterval());
        } else {
            AwesomePossum.stopListening(getContext());
            Log.i(tag, "Stopped authenticating");
            timePassedInMillis = 0;
            trustWheel.setProgress(0);
            authHandler.removeCallbacks(authRunnable);
        }
    }
}