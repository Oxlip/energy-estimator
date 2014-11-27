package com.getastral.energyestimator;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
    private List<DatabaseHelper.DeviceInfo> mDeviceInfoList;
    private static DeviceListAdapter mInstance = null;
    private OnSeekStopListener mOnSeekStopListener = null;
    private ListView mListview = null; //listview using this adapter.

    private static final String LOG_TAG_DEVICE_LIST_ADAPTER = "DeviceListAdapter";

    /**
     * Interface definition for a callback to be invoked when seekbar seek is finished
     */
    public interface OnSeekStopListener {

        /**
         * Callback method to be invoked when seekbar seek is finished.
         * <p>
         * @param deviceInfo The deviceInfo whose value was changed.
         * @param value New seeked value.
         */
        void onSeekStop(DatabaseHelper.DeviceInfo deviceInfo, int value);
    }

    public void setOnSeekStopListener(OnSeekStopListener onSeekStopListener) {
        mOnSeekStopListener = onSeekStopListener;
    }

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
     * @param deviceInfoList
     * @return DeviceListAdapter instance.
     */
    public static DeviceListAdapter getInstance(Context context, List<DatabaseHelper.DeviceInfo> deviceInfoList) {
        if(mInstance == null) {
            mInstance = new DeviceListAdapter();
            mInstance.mContext = context;
            mInstance.mDeviceInfoList = deviceInfoList;
            mInstance.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    int oldCount = mInstance.getCount();
                    List<DatabaseHelper.DeviceInfo> list = DatabaseHelper.getDevices();
                    DeviceListAdapter.setDeviceInfoList(list);
                    if (mInstance.mListview != null && oldCount != 0) {
                        mInstance.mListview.setSelection(list.size() - 1);
                    }
                }
            });

        }
        return mInstance;
    }

    /**
     * Sets new device list. (usually called when database is updated).
     */
    public static void setDeviceInfoList(List<DatabaseHelper.DeviceInfo> deviceInfoList) {
        mInstance.mDeviceInfoList = deviceInfoList;
    }

    @Override
    public int getCount() {
        return mDeviceInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDeviceInfoList.indexOf(getItem(position));
    }

    private void loadApplianceImage(View rootView, String applianceTypeName) {
        Context context = ApplicationGlobals.getAppContext();
        DatabaseHelper.ApplianceType applianceType = DatabaseHelper.getApplianceTypeByName(applianceTypeName);

        int imgId =  context.getResources().getIdentifier(applianceType.imageName, "drawable", context.getPackageName());
        ImageView img = (ImageView) rootView.findViewById(R.id.dl_image);
        Drawable imgDrawable = context.getResources().getDrawable(imgId);
        img.setImageDrawable(imgDrawable);
    }

    public void setListView(ListView listView) {
        mListview = listView;
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

        final DatabaseHelper.DeviceInfo deviceInfo = mDeviceInfoList.get(position);

        loadApplianceImage(convertView, deviceInfo.applianceType);

        TextView txtName = (TextView) convertView.findViewById(R.id.dl_name);
        txtName.setText(deviceInfo.name);

        SeekBar seekBar = (SeekBar) convertView.findViewById(R.id.dl_active_hours);
        seekBar.setMax(0);
        seekBar.setMax(24);
        seekBar.setProgress((int) deviceInfo.activeHours);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int activeHours = seekBar.getProgress();
                deviceInfo.activeHours = activeHours;
                DatabaseHelper.saveDeviceInfo(deviceInfo);

                if (mOnSeekStopListener != null) {
                    mOnSeekStopListener.onSeekStop(deviceInfo, activeHours);
                }

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
