package com.telenor.possumexample.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.telenor.possumexample.R;

public class AllSensorsChartFragment extends Fragment {
    private LineChart lineChart;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_sub_all_sensors_chart, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        lineChart = (LineChart)view.findViewById(R.id.lineChart);
    }
}