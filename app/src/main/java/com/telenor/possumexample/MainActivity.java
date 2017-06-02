package com.telenor.possumexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;

/**
 * Placeholder activity that uses the library
 */
public class MainActivity extends AppCompatActivity {
    private static final String tag = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
    }

    public void startListen(View view) {
        try {
            AwesomePossum.startListening(this, "superSecretEncryptedKurt");
        } catch (GatheringNotAuthorizedException e) {
            AwesomePossum.getAuthorizeDialog(this, "superSecretEncryptedKurt", getString(R.string.bucket), "Join the Awesome Possum Project", "By clicking ok you accept that you are 18 years of age and that you allow Telenor to gather anonymous data about your phone", "Ok", "Cancel").show();
            Log.i(tag, "Need authorization from user");
        }
    }

    public void stopListen(View view) {
        AwesomePossum.stopListening(this);
        AwesomePossum.startUpload(this, "superSecretEncryptedKurt", getString(R.string.bucket));
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}