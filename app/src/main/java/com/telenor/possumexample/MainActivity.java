package com.telenor.possumexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;

import java.util.ArrayList;
import java.util.List;

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

    public void upload(View view) {
        AwesomePossum.startUpload(this, "kurtNilsærn", getString(R.string.bucketKey), true);
    }

    public void startListen(View view) {
        try {
            List<String> unwanted = new ArrayList<>();
            unwanted.add("Position");
            AwesomePossum.setUnwantedDetectors(unwanted);
            AwesomePossum.listen(this);
        } catch (GatheringNotAuthorizedException e) {
            AwesomePossum.getAuthorizeDialog(this, "kurtNilsærn", "Join the Awesome Possum Project", "By clicking ok you accept that you are 18 years of age and that you allow Telenor to gather anonymous data about your phone", "Ok", "Cancel").show();
            Log.i(tag, "Need authorization from user");
        }
    }

    public void stopListen(View view) {
        AwesomePossum.stopListening(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}