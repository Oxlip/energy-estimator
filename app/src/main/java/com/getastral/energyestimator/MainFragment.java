package com.getastral.energyestimator;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

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

    private void setChartData(PieChart chart) {
        List<DatabaseHelper.DeviceInfo> deviceList = DatabaseHelper.getDevices();

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        int xIndex = 0;
        for (DatabaseHelper.DeviceInfo deviceInfo : deviceList) {
            float activeKw = deviceInfo.activeWatts * deviceInfo.activeHours / 1000;
            float standbyKw = deviceInfo.standbyWatts * deviceInfo.standbyHours / 1000;
            float totalKw = activeKw + standbyKw;

            xVals.add(deviceInfo.name);
            yVals.add(new Entry(totalKw, xIndex));

            xIndex++;
        }

        PieDataSet set1 = new PieDataSet(yVals, "Appliance usage");
        set1.setSliceSpace(3f);

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

        set1.setColors(colors);

        PieData data = new PieData(xVals, set1);
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
        chart.setDrawYValues(true);

        chart.setRotationAngle(0);

        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);

        // display percentage values
        chart.setUsePercentValues(true);

        chart.setCenterText("Monthly\nExpense");
        chart.setDrawCenterText(true);

    }

    private void drawChart(View view) {
        PieChart chart = (PieChart) view.findViewById(R.id.chart);
        setChartData(chart);

        chart.setDrawLegend(true);
        Legend l = chart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);

        chart.animateXY(500, 500);
    }

    private ListView getListView() {
        View view = getView();
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
            public void onSeekStop(DatabaseHelper.DeviceInfo deviceInfo, int value) {
                drawChart(getView());
            }
        });
        ListView listview = getListView();
        listview.setAdapter(deviceListAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onItemSelected(Integer.toString(position));
            }
        });

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
        ListView listview = getListView();
        listview.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        ListView listview = getListView();
        if (position == ListView.INVALID_POSITION) {
            listview.setItemChecked(mActivatedPosition, false);
        } else {
            listview.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
