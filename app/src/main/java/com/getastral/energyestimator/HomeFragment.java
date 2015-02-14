package com.getastral.energyestimator;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        List<DatabaseHelper.DeviceInfo> deviceList = DatabaseHelper.getDevices();
        final DeviceListAdapter deviceListAdapter = DeviceListAdapter.getInstance(getActivity(), deviceList);
        final SwipeListView listView = (SwipeListView) getView().findViewById(R.id.device_list);
        final TextView txtUtilityProvider = (TextView) getView().findViewById(R.id.txt_home_utility_provider);
        final TextView txtState = (TextView) getView().findViewById(R.id.txt_home_state_name);

        String stateName = getState();
        txtState.setText(stateName);
        txtUtilityProvider.setText("BESCOM");

        listView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d("swipe", String.format("onStartClose %d", position));
            }

            @Override
            public void onClickFrontView(int position) {
                Log.d("swipe", String.format("onClickFrontView %d", position));
            }

            @Override
            public void onClickBackView(int position) {
                Log.d("swipe", String.format("onClickBackView %d", position));
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    //data.remove(position);
                    Log.d("swipe", "deleting " + position);
                    DatabaseHelper.DeviceInfo deviceInfo = (DatabaseHelper.DeviceInfo)deviceListAdapter.getItem(position);
                    DatabaseHelper.deleteDeviceInfo(deviceInfo);

                }
                deviceListAdapter.notifyDataSetChanged();
            }

        });
        listView.setAdapter(deviceListAdapter);
        deviceListAdapter.setListView(listView);

        final FloatingActionsMenu fam = (FloatingActionsMenu) getView().findViewById(R.id.fab_menu);

        FloatingActionButton fab_add = (FloatingActionButton)  getView().findViewById(R.id.fab_add_appliance);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm =  getActivity().getSupportFragmentManager();
                NewApplianceDialog newApplianceDialog = NewApplianceDialog.newInstance("Select Appliance");
                newApplianceDialog.show(fm, "fragment_new_appliance");
                fam.collapse();
            }
        });

        FloatingActionButton fab_report = (FloatingActionButton)  getView().findViewById(R.id.fab_show_report);
        fab_report.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReportActivity.class);
                startActivity(intent);
                fam.collapse();
            }
        });
    }

    private String getState() {
        LocationManager lm = (LocationManager)  ApplicationGlobals.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location location = null;

        for (int i=providers.size()-1; i>=0; i--) {
            location = lm.getLastKnownLocation(providers.get(i));
            if (location != null) {
                break;
            }
        }

        if (location == null) {
            Log.d("GPS", "location is null");
            return null;
        }

        Geocoder geocoder;
        List<Address> addresses;
        try
        {
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String country = addresses.get(0).getCountryName();
            if (country.compareTo("India") == 0) {
                return addresses.get(0).getAdminArea();
            } else {
                return "";
            }
        } catch (IOException e) {
            Log.d("GPS", "Unable to get location" + e);
            return null;
        }
    }
}
