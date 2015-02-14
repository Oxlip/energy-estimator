package in.nuton.energyestimator;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class NewApplianceDialog extends DialogFragment {
    private EditText mEditText;

    public NewApplianceDialog() {
        // Empty constructor required for DialogFragment
    }

    public static NewApplianceDialog newInstance(String title) {
        NewApplianceDialog frag = new NewApplianceDialog();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_appliance, container);
        ListView listview = (ListView) view.findViewById(R.id.lst_appliance_types);
        listview.setAdapter(new DeviceTypeAdapter());
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Add new appliance based on the selected type
                DatabaseHelper.DeviceInfo deviceInfo = new DatabaseHelper.DeviceInfo();
                DatabaseHelper.ApplianceType applianceType = DatabaseHelper.getApplianceTypeList().get(position);
                deviceInfo.name = applianceType.name;
                deviceInfo.applianceType = applianceType.name;
                deviceInfo.activeHours = 0;
                deviceInfo.activeWatts = applianceType.activeWatts;
                deviceInfo.standbyWatts = applianceType.standbyWatts;
                DatabaseHelper.saveDeviceInfo(deviceInfo);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        DeviceListAdapter.getInstance().notifyDataSetChanged();
                    }
                });
                //close the dialog
                dismiss();

            }
        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
