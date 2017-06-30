package com.telenor.possumexample.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.telenor.possumexample.R;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.interfaces.IPossumTrust;
import com.telenor.possumlib.utils.Do;

import java.util.ArrayList;
import java.util.List;

public class CombinedTrustChart extends Fragment implements IPossumTrust {
    private LineChart lineChart;
    private List<Entry> combinedTrustScores = new ArrayList<>();
    private int pos = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_sub_combined, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        lineChart = (LineChart) view.findViewById(R.id.lineChart);
        lineChart.setTouchEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawBorders(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getAxisLeft().setTextSize(15f);
        lineChart.getXAxis().setDrawAxisLine(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setDescription(null);
        lineChart.getAxisLeft().setAxisMaximum(1);
        lineChart.getAxisLeft().setAxisMinimum(0);
        lineChart.getAxisLeft().setDrawLabels(true);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawAxisLine(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawAxisLine(false);
        lineChart.setNoDataText("No trustScores yet");

        AwesomePossum.addTrustListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AwesomePossum.removeTrustListener(this);
    }

    @Override
    public void changeInCombinedTrust(final float combinedTrustScore, final String status) {
        Do.onMain(new Runnable() {
            @Override
            public void run() {
                combinedTrustScores.add(new Entry(pos, combinedTrustScore));
                pos++;
                if (combinedTrustScores.size() > 30) {
                    combinedTrustScores.remove(0);
                }
                LineDataSet dataSet = new LineDataSet(combinedTrustScores, "TrustScore over time");
                List<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(dataSet);
                LineData data = new LineData(dataSets);
                lineChart.setData(data);
                lineChart.invalidate();
            }
        });
    }

    @Override
    public void changeInDetectorTrust(int detectorType, float newTrustScore, String status) {
    }

    @Override
    public void failedToAscertainTrust(Exception exception) {

    }
}