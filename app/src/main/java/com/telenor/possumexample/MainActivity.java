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
//    private Button learnButton;
    private Button authenticateButton;
    private Button listenButton;
    private Button uploadButton;
    private SharedPreferences preferences;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        //barChart = (BarChart)findViewById(R.id.barChart);
        preferences = getSharedPreferences("dummyPrefs", MODE_PRIVATE);
//        learnButton = (Button)findViewById(R.id.learnButton);
        authenticateButton = (Button)findViewById(R.id.authenticateButton);
        listenButton = (Button)findViewById(R.id.listenButton);
        uploadButton = (Button)findViewById(R.id.uploadButton);
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
        preferences.edit().putString("storedKurt", suggestedKurt).apply();
        boolean isEnabled = myKurt().length() > 0;
        listenButton.setEnabled(isEnabled);
        uploadButton.setEnabled(isEnabled);
        authenticateButton.setEnabled(isEnabled);
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
                    AwesomePossum.startListening(this, myKurt(), getString(R.string.identityPoolId));
                    ((Button)view).setText(R.string.listenOff);
                }
            } catch (GatheringNotAuthorizedException e) {
                AwesomePossum.getAuthorizeDialog(this, myKurt(), getString(R.string.identityPoolId), "Join the Awesome Possum Project", "By clicking ok you accept that you are 18 years of age and that you allow Telenor to gather anonymous data about your phone", "Ok", "Cancel").show();
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
        if (!AwesomePossum.isAuthenticating()) {
            AwesomePossum.addTrustListener(this);
            AwesomePossum.authenticate(this, myKurt(), true);
            authenticateButton.setText(R.string.authOff);
            authenticateButton.setEnabled(false);
        }
    }

    public void upload(View view) {
        AwesomePossum.startUpload(this, myKurt(), getString(R.string.identityPoolId));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void changeInTrust(int detectorType, float newTrustScore, float combinedTrustScore) {
        Log.i(tag, "Trust change:"+detectorType+", "+newTrustScore+", "+combinedTrustScore);
        authenticateButton.setText(R.string.authOn);
        authenticateButton.setEnabled(myKurt().length() > 0);
        AwesomePossum.removeTrustListener(this);
    }

    @Override
    public void failedToAscertainTrust(Exception exception) {
        Log.e(tag, "Failed to ascertain trust:",exception);
        authenticateButton.setText(R.string.authOn);
        authenticateButton.setEnabled(myKurt().length() > 0);
        AwesomePossum.removeTrustListener(this);
    }
}