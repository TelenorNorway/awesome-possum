package com.telenor.possumexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;
import com.telenor.possumlib.interfaces.IPossumTrust;

/**
 * Placeholder activity that uses the library
 */
public class MainActivity extends AppCompatActivity implements IPossumTrust {
    private static final String tag = MainActivity.class.getName();
    private EditText uniqueKurt;
    //private BarChart barChart;
    private SharedPreferences preferences;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        //barChart = (BarChart)findViewById(R.id.barChart);
        preferences = getSharedPreferences("dummyPrefs", MODE_PRIVATE);
        uniqueKurt = (EditText)findViewById(R.id.uniqueKurt);
        uniqueKurt.setText(myKurt());
        uniqueKurt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePreferences();
            }
        });
    }

    private String myKurt() {
        return preferences.getString("storedKurt", "myKurtId");
    }

    private void updatePreferences() {
        String suggestedKurt = uniqueKurt.getText().toString();
        if (suggestedKurt.length() < 3) {
            uniqueKurt.setText(myKurt());
        } else {
            preferences.edit().putString("storedKurt", suggestedKurt).apply();
        }
    }

    public void toggleListen(View view) {
        if (AwesomePossum.isListening()) {
            AwesomePossum.stopListening(this);
            ((Button)view).setText(R.string.listenOn);
        } else {
            try {
                if (AwesomePossum.isAuthorized(this) && AwesomePossum.hasMissingPermissions(this)) {
                    Log.i(tag, "Has missing permissions and/or is not authorized");
                    AwesomePossum.requestNeededPermissions(this);
                } else {
                    Log.i(tag, "Starting to listen");
                    AwesomePossum.startListening(this, myKurt());
                    ((Button)view).setText(R.string.listenOff);
                }
            } catch (GatheringNotAuthorizedException e) {
                AwesomePossum.getAuthorizeDialog(this, myKurt(), getString(R.string.bucket), "Join the Awesome Possum Project", "By clicking ok you accept that you are 18 years of age and that you allow Telenor to gather anonymous data about your phone", "Ok", "Cancel").show();
            }
        }
        Log.i(tag, "Clicked toggleListening - listening status now:"+AwesomePossum.isListening());
    }

    public void toggleLearning(View view) {
        AwesomePossum.setLearning(this, !AwesomePossum.isLearning());
        if (AwesomePossum.isLearning()) {
            ((Button)view).setText(R.string.learningOff);
        } else {
            ((Button)view).setText(R.string.learningOn);
        }
    }

    public void toggleAuth(View view) {
        if (AwesomePossum.isAuthenticating()) {
            AwesomePossum.removeTrustListener(this);
            ((Button)view).setText(R.string.authOn);
        } else {
            AwesomePossum.addTrustListener(this);
            ((Button)view).setText(R.string.authOff);
        }
    }

    public void upload(View view) {
        AwesomePossum.startUpload(this, myKurt(), getString(R.string.bucket));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void changeInTrust(int detectorType, float newTrustScore, float combinedTrustScore) {
        Log.i(tag, "Trust change:"+detectorType+", "+newTrustScore+", "+combinedTrustScore);
    }
}