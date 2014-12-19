package com.getastral.energyestimator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getastral.energyestimator.DatabaseHelper.ElectricityRates;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend;

import java.util.ArrayList;
import java.util.List;

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
        switch (position) {
            case 0:
                return getPieChart(convertView, parent);
            case 1:
                return getElectricitySlabTable(convertView, parent);
            case 2:
                return getBarChart(convertView, parent);
        }
        return null;
    }

    private View getPieChart(View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) ApplicationGlobals.getAppContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_report_appliance_usage_graph, null);
        }
        setupPieChart(convertView);
        return convertView;
    }

    private View getBarChart(View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) ApplicationGlobals.getAppContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_report_electricity_slab_graph, null);
        }
        setupBarChart(convertView);
        return convertView;
    }

    private View getElectricitySlabTable(View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) ApplicationGlobals.getAppContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_report_electricity_slab_table, null);
        }
        setupElectricitySlabTable(convertView);
        return convertView;
    }

    private void addRowDividerToGrid(GridLayout gridLayout, int row, int columnCount) {
        Context context = ApplicationGlobals.getAppContext();
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.rightMargin = 5;
        param.topMargin = 5;
        param.rowSpec = GridLayout.spec(row);
        param.columnSpec = GridLayout.spec(0, columnCount);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setMinimumHeight(2);
        linearLayout.setBackgroundColor(Color.DKGRAY);
        linearLayout.setLayoutParams(param);

        gridLayout.addView(linearLayout);
    }

    private void addHeaderToGrid(GridLayout gridLayout, int column, String title, int colWidth) {
        int MARGIN = 5;
        Context context = ApplicationGlobals.getAppContext();
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.rightMargin = MARGIN;
        param.topMargin = MARGIN;
        param.rowSpec = GridLayout.spec(0);
        param.columnSpec = GridLayout.spec(column);
        param.setGravity(Gravity.FILL_HORIZONTAL);

        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setText(title);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setLayoutParams(param);

        textView.setWidth(colWidth - (MARGIN * 2));

        gridLayout.addView(textView);
    }

    private void addContentToGrid(GridLayout gridLayout, int row, int column, String text, boolean highlight) {
        int MARGIN = 5;
        Context context = ApplicationGlobals.getAppContext();
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.rightMargin = MARGIN;
        param.topMargin = MARGIN;
        param.rowSpec = GridLayout.spec(row);
        param.columnSpec = GridLayout.spec(column);
        param.setGravity(Gravity.FILL_HORIZONTAL);

        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setLayoutParams(param);
        if (highlight) {
            textView.setBackgroundColor(Color.BLUE);
            textView.setTextColor(Color.WHITE);
        } else {
            textView.setTextColor(Color.BLACK);
        }

        gridLayout.addView(textView);
    }


    private void setupElectricitySlabTable(View convertView) {
        int COLUMN_COUNT = 5, HEADER_ROW_COUNT = 1, MARGIN = 5;
        int rowCount;
        int colWidth;

        WindowManager wm = (WindowManager) ApplicationGlobals.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        colWidth = (size.x / COLUMN_COUNT) - MARGIN;

        GridLayout gridLayout = (GridLayout)convertView.findViewById(R.id.tab_report_electricity_slab_table);
        gridLayout.removeAllViews();

        List<ElectricityRates> list = DatabaseHelper.getElectricityRateList("Tamil Nadu", "Public");

        gridLayout.setColumnCount(COLUMN_COUNT);
        rowCount = (list.size() + HEADER_ROW_COUNT) * 2;
        gridLayout.setRowCount(rowCount);

        addHeaderToGrid(gridLayout, 0, "From", colWidth);
        addHeaderToGrid(gridLayout, 1, "To", colWidth);
        addHeaderToGrid(gridLayout, 2, "", colWidth);
        addHeaderToGrid(gridLayout, 3, "",  colWidth);
        addHeaderToGrid(gridLayout, 4, "Cost", colWidth);

        int row = 1;
        for(ElectricityRates electricityRate: list) {
            boolean highlight = false;
            addContentToGrid(gridLayout, row, 0, "" + electricityRate.conditionUnitsStart, highlight);
            addContentToGrid(gridLayout, row, 1, "" + electricityRate.conditionUnitsEnd, highlight);
            addContentToGrid(gridLayout, row, 2, "" + electricityRate.startUnit, highlight);
            addContentToGrid(gridLayout, row, 3, "" + electricityRate.endUnit, highlight);
            addContentToGrid(gridLayout, row, 4, "" + electricityRate.rate, highlight);
            addRowDividerToGrid(gridLayout, row++, COLUMN_COUNT);
        }
    }

    private void setPieChartData(PieChart chart, PowerConsumptionInfo powerConsumptionInfo) {
        PieDataSet pieDataSet = new PieDataSet(powerConsumptionInfo.applianceUsage, "");
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

        PieData data = new PieData(powerConsumptionInfo.applianceNames, pieDataSet);
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }


    private void setupPieChart(View view) {
        PieChart chart = (PieChart) view.findViewById(R.id.pie_chart);
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

        setPieChartData(chart, new PowerConsumptionInfo());

        chart.setDrawLegend(true);
        Legend l = chart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);

        chart.animateXY(500, 500);
    }


    private void setBarChartData(BarChart chart, PowerConsumptionInfo powerConsumptionInfo) {
        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());

        BarDataSet barSlabRates = new BarDataSet(powerConsumptionInfo.slabCosts, "Watts");
        barSlabRates.setColors(colors);
        BarDataSet barSlabUnits = new BarDataSet(powerConsumptionInfo.slabUnitsUsage, "Cost");
        barSlabUnits.setColors(colors);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barSlabUnits);
        dataSets.add(barSlabRates);

        BarData data = new BarData(powerConsumptionInfo.slabNames, dataSets);
        data.setGroupSpace(110f);
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }

    private void setupBarChart(View view) {
        BarChart chart = (BarChart) view.findViewById(R.id.bar_chart);

        setBarChartData(chart, new PowerConsumptionInfo());
        chart.set3DEnabled(true);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.setDrawVerticalGrid(false);
        chart.setDrawHorizontalGrid(false);
        chart.setDrawGridBackground(false);
        chart.animateY(1500);
    }
}