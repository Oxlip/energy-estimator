package com.getastral.energyestimator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.List;

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
        DeviceListAdapter deviceListAdapter = DeviceListAdapter.getInstance(getActivity(), deviceList);
        final ListView listView = (ListView) getView().findViewById(R.id.device_list);
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
}
