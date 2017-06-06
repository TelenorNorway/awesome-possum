package com.telenor.possumexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;
import com.telenor.possumlib.interfaces.IPossumTrust;

/**
 * Placeholder activity that uses the library
 */
public class MainActivity extends AppCompatActivity implements IPossumTrust {
    private static final String tag = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
    }

    public void toggleListen(View view) {
        if (AwesomePossum.isListening(this)) {
            AwesomePossum.stopListening(this);
            ((Button)view).setText(R.string.listenOn);
        } else {
            try {
                AwesomePossum.startListening(this, "superSecretEncryptedKurt");
                ((Button)view).setText(R.string.listenOff);
            } catch (GatheringNotAuthorizedException e) {
                AwesomePossum.getAuthorizeDialog(this, "superSecretEncryptedKurt", getString(R.string.bucket), "Join the Awesome Possum Project", "By clicking ok you accept that you are 18 years of age and that you allow Telenor to gather anonymous data about your phone", "Ok", "Cancel").show();
            }
        }
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
        AwesomePossum.startUpload(this, "superSecretEncryptedKurt", getString(R.string.bucket));
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