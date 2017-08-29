package com.telenor.possumexample;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.JsonArray;
import com.telenor.possumexample.dialogs.DefineIdDialog;
import com.telenor.possumexample.fragments.MainFragment;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String tag = MainActivity.class.getName();
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        AwesomePossum.init(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferences = getSharedPreferences("dummyPrefs", MODE_PRIVATE);
        showFragment(MainFragment.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AwesomePossum.terminate(getApplicationContext());
    }

    private void showFragment(Class<? extends Fragment> fragmentClass) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        try {
            transaction.replace(R.id.mainFragment, fragmentClass.newInstance());
        } catch (Exception e) {
            Log.e(tag, "Failed to instantiate Fragment:", e);
        }
        transaction.commitAllowingStateLoss();
    }

    public SharedPreferences preferences() {
        return preferences;
    }

    public String myId() {
        return preferences.getString("storedId", "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void defineIdDialog(MenuItem item) {
        DefineIdDialog dialog = new DefineIdDialog();
        dialog.show(getSupportFragmentManager(), DefineIdDialog.class.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.define_id:
                defineIdDialog(menuItem);
                return true;
            case R.id.listening:
                if (validId(myId())) {
                    try {
                        AwesomePossum.startListening(this, myId(), getString(R.string.identityPoolId));
                        invalidateOptionsMenu();
                    } catch (GatheringNotAuthorizedException e) {
                        AwesomePossum.getAuthorizeDialog(this, myId(), getString(R.string.identityPoolId), "Authorize AwesomePossum", "We need permission from you", "Granted", "Denied").show();
                        }
                } else {
                    showInvalidIdDialog();
                }
                return true;
            case R.id.upload:
                if (validId(myId())) {
                    AwesomePossum.startUpload(this, myId(), getString(R.string.identityPoolId));
                }
                return true;
            case R.id.resetData:
                if (validId(myId())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Deletion of training data");
                    builder.setMessage(String.format(Locale.US, "Do you want to delete all training data related to the id '%s' ?", myId()));
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JsonArray detectors = new JsonArray();
                            detectors.add("all");
                            AwesomePossum.resetMyData(myId(), getString(R.string.resetUrl), getString(R.string.apiKey), detectors);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
                return true;
        }
        return false;
    }

    public void showInvalidIdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invalid id");
        builder.setMessage("Need a valid id to proceed");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public boolean validId(String uniqueId) {
        return uniqueId != null && uniqueId.length() > 2;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem listeningItem = menu.findItem(R.id.listening);
        if (listeningItem != null) {
            listeningItem.setTitle(AwesomePossum.isListening() ? getString(R.string.listenOff) : getString(R.string.listenOn));
        }
        return super.onPrepareOptionsMenu(menu);
    }
}