package com.getastral.energyestimator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ReportFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReportFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        updateSummaryValue(view, new PowerConsumptionInfo());
        return view;
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
            Log.d("Report Fragment", "Empty view");
            return null;
        }
        return (ListView) view.findViewById(R.id.report_list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        ReportListAdapter reportListAdapter = ReportListAdapter.getInstance();
        final ListView listView = getListView(null);
        listView.setAdapter(reportListAdapter);

    }
}