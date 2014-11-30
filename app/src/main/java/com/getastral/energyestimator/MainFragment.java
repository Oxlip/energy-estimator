package com.getastral.energyestimator;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainFragment() {
    }

    private void setupImageView(View parentView, int imageId, int drawableId, int color, View.OnClickListener onClickListener) {
        Drawable myIcon = getResources().getDrawable(drawableId);
        myIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ImageView imgView = (ImageView)parentView.findViewById(imageId);
        imgView.setImageDrawable(myIcon);
        if (onClickListener != null) {
            imgView.setOnClickListener(onClickListener);
        }
    }

    private View.OnClickListener summaryImgOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        View listViewHeader = getActivity().getLayoutInflater().inflate(R.layout.header_appliance_list, null);
        getListView(view).addHeaderView(listViewHeader);

        ImageView imgElectricityBill = (ImageView)view.findViewById(R.id.img_electricity_bill);

        setupImageView(view, R.id.img_electricity_bill, R.drawable.bill, Color.WHITE, summaryImgOnClickListener);
        setupImageView(view, R.id.img_electricity_saving, R.drawable.piggy, Color.GREEN, summaryImgOnClickListener);
        setupImageView(view, R.id.img_electricity_consumed, R.drawable.energy, Color.WHITE, null);
        setupChart(view);
        drawChart(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    protected class PowerConsumptionInfo {
        List<DatabaseHelper.DeviceInfo> deviceList;
        ArrayList<Entry> usageChartYVals;
        ArrayList<String> usageChartXVals;
        float totalActiveWatts;
        float totalStandbyWatts;
        float totalActiveHours;
        float totalStandbyHours;

        public PowerConsumptionInfo() {
            deviceList = DatabaseHelper.getDevices();
            usageChartYVals = new ArrayList<Entry>();
            usageChartXVals = new ArrayList<String>();
            totalActiveWatts = totalStandbyWatts = 0;
            totalActiveHours = totalStandbyHours = 0;

            int xIndex = 0;
            for (DatabaseHelper.DeviceInfo deviceInfo : deviceList) {
                float activeWatts = deviceInfo.activeWatts * deviceInfo.activeHours;
                float standbyWatts = deviceInfo.standbyWatts * deviceInfo.standbyHours;

                totalActiveWatts += activeWatts;
                totalStandbyWatts += standbyWatts;

                totalActiveHours += deviceInfo.activeHours;
                totalStandbyHours += deviceInfo.standbyHours;

                usageChartXVals.add(deviceInfo.name);
                usageChartYVals.add(new Entry(activeWatts + standbyWatts, xIndex));

                xIndex++;
            }
        }
    }

    private void setChartData(PieChart chart, PowerConsumptionInfo powerConsumptionInfo) {
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


    private void setupChart(View view) {
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

        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);

        // display percentage values
        chart.setUsePercentValues(true);

        chart.setCenterText("Electricity\nExpense");
        chart.setDrawCenterText(true);

    }

    private void drawChart(View view) {
        PowerConsumptionInfo powerConsumptionInfo = new PowerConsumptionInfo();
        PieChart chart = (PieChart) view.findViewById(R.id.chart);
        setChartData(chart, powerConsumptionInfo);

        chart.setDrawLegend(true);
        Legend l = chart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);

        chart.animateXY(500, 500);

        updateSummaryValue(view, powerConsumptionInfo);
    }

    private void updateSummaryValue(View view, PowerConsumptionInfo powerConsumptionInfo) {
        float bill, savable, consumed;
        float rate = 2.8f;
        int yearMultiplier = 365;

        consumed = (powerConsumptionInfo.totalActiveWatts + powerConsumptionInfo.totalStandbyWatts) / 1000;
        bill = consumed * rate;
        savable = (bill / 100 * 23) + (powerConsumptionInfo.totalStandbyWatts / 1000 * rate);

        consumed *= yearMultiplier;
        bill *= yearMultiplier;
        savable *= yearMultiplier;

        TextView txtElectricityBill = (TextView)view.findViewById(R.id.txt_electricity_bill);
        TextView txtElectricitySavable = (TextView)view.findViewById(R.id.txt_electricity_saving);
        TextView txtElectricityConsumed = (TextView)view.findViewById(R.id.txt_electricity_consumed);

        txtElectricityBill.setText(String.format("%.0f", bill));
        txtElectricitySavable.setText(String.format("%.0f", savable));
        txtElectricityConsumed.setText(String.format("%.0f", consumed));
    }

    private ListView getListView(View view) {
        if (view == null) {
            view = getView();
        }

        if (view == null) {
            Log.d("MainFragment", "Empty view");
            return null;
        }
        return (ListView) view.findViewById(R.id.device_list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        final MainActivity activity = (MainActivity)getActivity();

        List<DatabaseHelper.DeviceInfo> deviceList = DatabaseHelper.getDevices();
        DeviceListAdapter deviceListAdapter = DeviceListAdapter.getInstance(activity, deviceList);
        deviceListAdapter.setOnSeekStopListener(new DeviceListAdapter.OnSeekStopListener() {
            @Override
            public void onSeekStop(DatabaseHelper.DeviceInfo deviceInfo, float value) {
                drawChart(getView());
            }
        });
        final ListView listView = getListView(null);
        listView.setAdapter(deviceListAdapter);
        deviceListAdapter.setListView(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridLayout layout = (GridLayout)view.findViewById(R.id.layout_details);

                // Creating the expand animation for the item
                ExpandAnimation expandAni = new ExpandAnimation(layout, 500);

                // Start the animation on the toolbar
                layout.startAnimation(expandAni);

                mCallbacks.onItemSelected(Integer.toString(position));

            }
        });
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        FloatingActionButton fab = (FloatingActionButton)  getView().findViewById(R.id.fab);
        //fab.attachToListView(listview);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                NewApplianceDialog newApplianceDialog = NewApplianceDialog.newInstance("Select Appliance");
                newApplianceDialog.show(fm, "fragment_new_appliance");
            }
        });
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        ListView listview = getListView(null);
        listview.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        ListView listview = getListView(null);
        if (position == ListView.INVALID_POSITION) {
            listview.setItemChecked(mActivatedPosition, false);
        } else {
            listview.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
