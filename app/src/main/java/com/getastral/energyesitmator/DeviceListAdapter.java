package com.getastral.energyesitmator;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * List Adapter to hold discovered devices and render them in UI.
 * This is a singleton class(because only one list is enough for the whole application).
 */
public class DeviceListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Device> mDeviceList;
    private static DeviceListAdapter mInstance = null;

    private static final String LOG_TAG_DEVICE_LIST_ADAPTER = "DeviceListAdapter";

    protected DeviceListAdapter() {
        // Exists only to defeat instantiation.
    }

    /**
     * Returns the current instance.
     * @return DeviceListAdapter instance.
     */
    public static DeviceListAdapter getInstance() {
        return mInstance;
    }

    /**
     * Creates new instance if required.
     * @param context
     * @param deviceList
     * @return DeviceListAdapter instance.
     */
    public static DeviceListAdapter getInstance(Context context, List<Device> deviceList) {
        if(mInstance == null) {
            mInstance = new DeviceListAdapter();
            mInstance.mContext = context;
            mInstance.mDeviceList = deviceList;
        }
        return mInstance;
    }


    /**
     * Add newly discovered device.
     * @param device - New device.
     */
    public void addDevice(Device device) {
        mDeviceList.add(device);
        this.notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDeviceList.indexOf(getItem(position));
    }

    /**
     * Renders the UI for the given device item.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_device_list, null);
        }

        final Device device = mDeviceList.get(position);

        TextView txtName = (TextView) convertView.findViewById(R.id.dl_name);
        txtName.setText(device.name);

        SeekBar seekBar = (SeekBar) convertView.findViewById(R.id.dl_active_hours);
        seekBar.setMax(0);
        seekBar.setMax(24);
        seekBar.setProgress((int) device.activeHours);
        seekBar.setTag(device);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int activeHours = seekBar.getProgress();
                device.activeHours = activeHours;
                device.save();
                Toast.makeText(mContext, String.valueOf(activeHours), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });
        return convertView;
    }
}
