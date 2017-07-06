package com.telenor.possumexample.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.telenor.possumexample.R;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IPossumTrust;

import java.util.Locale;

public class IndividualSensorTrustFragment extends Fragment implements IPossumTrust {
    private TextView accelerometer;
    private TextView gyroscope;
    private TextView position;
    private TextView image;
    private TextView network;
    private TextView sound;
    private TextView bluetooth;

    private static final String tag = IndividualSensorTrustFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_sub_individual, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        accelerometer = (TextView) view.findViewById(R.id.accelerometer);
        gyroscope = (TextView) view.findViewById(R.id.gyroscope);
        position = (TextView) view.findViewById(R.id.position);
        image = (TextView) view.findViewById(R.id.image);
        network = (TextView) view.findViewById(R.id.network);
        sound = (TextView) view.findViewById(R.id.sound);
        bluetooth = (TextView) view.findViewById(R.id.bluetooth);
    }

    @Override
    public void onResume() {
        super.onResume();
        AwesomePossum.addTrustListener(getContext(), this);
        updateSensors(AwesomePossum.latestTrustScore());
    }

    @Override
    public void onPause() {
        super.onPause();
        AwesomePossum.removeTrustListener(this);
    }

    public void updateSensors(JsonObject object) {
        changeTrust(DetectorType.Accelerometer, object.get("accelerometer").getAsJsonObject());
        changeTrust(DetectorType.Gyroscope, object.get("gyroscope").getAsJsonObject());
        changeTrust(DetectorType.Wifi, object.get("network").getAsJsonObject());
        changeTrust(DetectorType.Position, object.get("position").getAsJsonObject());
        changeTrust(DetectorType.Image, object.get("image").getAsJsonObject());
        changeTrust(DetectorType.Bluetooth, object.get("bluetooth").getAsJsonObject());
        changeTrust(DetectorType.Audio, object.get("sound").getAsJsonObject());
    }

    private void changeTrust(int detectorType, JsonObject object) {
        changeInDetectorTrust(detectorType, object.get("score").getAsFloat(),
                object.get("status").getAsString());
    }

    @Override
    public void changeInCombinedTrust(float combinedTrustScore, String status) {

    }

    @Override
    public void changeInDetectorTrust(int detectorType, float newTrustScore, String status) {
        TextView textView = textViewForType(detectorType);
        switch (status) {
            case "TRAINING":
                textView.setTextColor(Color.parseColor("#FF6600"));
                break;
            default:
                textView.setTextColor(Color.BLACK);
        }
        textView.setText(String.format(Locale.US, "%s: %.0f%%", detectorNameFromType(detectorType), newTrustScore * 100));
    }

    private TextView textViewForType(int type) {
        switch (type) {
            case DetectorType.Accelerometer:
                return accelerometer;
            case DetectorType.Audio:
                return sound;
            case DetectorType.Bluetooth:
                return bluetooth;
            case DetectorType.Image:
                return image;
            case DetectorType.Gyroscope:
                return gyroscope;
            case DetectorType.Position:
                return position;
            case DetectorType.Wifi:
                return network;
            default:
                throw new IllegalArgumentException("Wrong type of detector:" + type);
        }
    }

    private String detectorNameFromType(int type) {
        switch (type) {
            case DetectorType.Accelerometer:
                return "Accelerometer";
            case DetectorType.Audio:
                return "AmbientSound";
            case DetectorType.Bluetooth:
                return "Bluetooth";
            case DetectorType.Image:
                return "Image";
            case DetectorType.Gyroscope:
                return "Gyroscope";
            case DetectorType.Position:
                return "Position";
            case DetectorType.Wifi:
                return "Network";
            default:
                throw new IllegalArgumentException("Wrong type of detector:" + type);
        }
    }

    @Override
    public void failedToAscertainTrust(Exception exception) {
        Log.e(tag, "Failed trust:", exception);
    }
}