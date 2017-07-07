package com.telenor.possumexample.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.telenor.possumexample.R;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IPossumTrust;
import com.telenor.possumlib.utils.Do;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllSensorsChartFragment extends Fragment implements IPossumTrust {
    private LineChart lineChart;
    private Map<Integer, LineDataSet> detectorLineDataSets;
    private Map<Integer, Integer> posMap;
    private List<ILineDataSet> dataSets;
    private LineData primaryLineData;
    private static final String tag = AllSensorsChartFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_sub_all_sensors_chart, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        detectorLineDataSets = new HashMap<>();
        posMap = new HashMap<>();
        LineData lineData = new LineData();
        lineChart = (LineChart) view.findViewById(R.id.lineChart);
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
        lineChart.setData(lineData);
        AwesomePossum.addTrustListener(getContext(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AwesomePossum.removeTrustListener(this);
    }

    @Override
    public void changeInCombinedTrust(float combinedTrustScore, String status) {
    }

    @Override
    public void changeInDetectorTrust(final int detectorType, final float newTrustScore, final String status) {
        Do.onMain(new Runnable() {
            @Override
            public void run() {
                if (detectorType == DetectorType.Accelerometer || !"TRAINING".equals(status)) {
                    addEntry(detectorType, newTrustScore);
                }
            }
        });
    }

    @Override
    public void failedToAscertainTrust(Exception exception) {

    }

    private LineDataSet createSet(int detectorType) {
        LineDataSet set = new LineDataSet(null, AwesomePossum.detectorNameByType(detectorType));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setCircleColor(Color.WHITE);
        switch (detectorType) {
            case DetectorType.Accelerometer:
                set.setColor(ColorTemplate.rgb("FF0000"));
                break;
            case DetectorType.Image:
                set.setColor(ColorTemplate.rgb("00FF00"));
                break;
            case DetectorType.Wifi:
                set.setColor(ColorTemplate.rgb("0000FF"));
                break;
            default:
                Log.i(tag, "Unhandled color for:"+detectorType);
        }
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setLineWidth(2f);
        set.setCircleRadius(0f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }

    private void addEntry(int detectorType, float value) {
        LineData data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByLabel(AwesomePossum.detectorNameByType(detectorType), true);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet(detectorType);
                data.addDataSet(set);
            }
            int dataSetIndex = data.getIndexOfDataSet(set);
            data.addEntry(new Entry(set.getEntryCount(), value), dataSetIndex);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }
}