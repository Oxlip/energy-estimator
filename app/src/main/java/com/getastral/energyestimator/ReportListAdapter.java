package com.getastral.energyestimator;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend;

import java.util.ArrayList;

/**
 * List Adapter to hold all the reports and statistics.
 * This is a singleton class(because only one list is enough for the whole application).
 */
public class ReportListAdapter extends BaseAdapter {

    private static ReportListAdapter mInstance = null;

    private int TOTAL_REPORTS = 3;

    protected ReportListAdapter() {
        // Exists only to defeat instantiation.
    }

    /**
     * Returns the current instance or creates new instance.
     * @return ReportListAdapter instance.
     */
    public static ReportListAdapter getInstance() {
        if(mInstance == null) {
            mInstance = new ReportListAdapter();
        }
        return mInstance;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return TOTAL_REPORTS;
    }

    /**
     * Renders the UI for the given report.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) ApplicationGlobals.getAppContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_piechart, null);
        }
        setupPieChart(convertView);
        drawPieChart(convertView);
        return convertView;
    }

    private void setPieChartData(PieChart chart, PowerConsumptionInfo powerConsumptionInfo) {
        PieDataSet pieDataSet = new PieDataSet(powerConsumptionInfo.usageChartYVals, "");
        pieDataSet.setSliceSpace(3f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        pieDataSet.setColors(colors);

        PieData data = new PieData(powerConsumptionInfo.usageChartXVals, pieDataSet);
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }


    private void setupPieChart(View view) {
        PieChart chart = (PieChart) view.findViewById(R.id.chart);
        // change the color of the center-hole
        chart.setHoleColor(Color.rgb(235, 235, 235));
        chart.setHoleRadius(60f);
        chart.setDrawHoleEnabled(true);

        chart.setDescription("");

        // draws the corresponding description value into the slice
        chart.setDrawXValues(false);
        chart.setDrawYValues(false);

        chart.setRotationAngle(0);

        chart.setRotationEnabled(false);

        // display percentage values
        chart.setUsePercentValues(true);

        chart.setCenterText("Electricity\nExpense");
        chart.setDrawCenterText(true);

    }

    private void drawPieChart(View view) {
        PowerConsumptionInfo powerConsumptionInfo = new PowerConsumptionInfo();
        PieChart chart = (PieChart) view.findViewById(R.id.chart);
        setPieChartData(chart, powerConsumptionInfo);

        chart.setDrawLegend(true);
        Legend l = chart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);

        chart.animateXY(500, 500);
    }

}