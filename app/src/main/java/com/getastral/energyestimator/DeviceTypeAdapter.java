package com.getastral.energyestimator;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class DeviceTypeAdapter extends BaseAdapter {

    private List<DatabaseHelper.ApplianceType> mApplianceTypeList;

    public DeviceTypeAdapter() {
        mApplianceTypeList = DatabaseHelper.getApplianceTypeList();
    }

    @Override
    public int getCount() {
        return mApplianceTypeList.size();
    }

    @Override
    public Object getItem(int position) {
        return mApplianceTypeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mApplianceTypeList.indexOf(getItem(position));
    }


    private void loadApplianceImage(View rootView, DatabaseHelper.ApplianceType applianceType) {
        Context context = ApplicationGlobals.getAppContext();

        int imgId =  context.getResources().getIdentifier(applianceType.imageName, "drawable", context.getPackageName());
        ImageView img = (ImageView) rootView.findViewById(R.id.img_appliance);
        Drawable imgDrawable = context.getResources().getDrawable(imgId);
        img.setImageDrawable(imgDrawable);
    }

    /**
     * Renders the UI for the given device item.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) ApplicationGlobals.getAppContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_appliance_type, null);
        }

        final DatabaseHelper.ApplianceType applianceType = mApplianceTypeList.get(position);

        TextView txtName = (TextView) convertView.findViewById(R.id.txt_appliance_name);
        txtName.setText(applianceType.name);

        loadApplianceImage(convertView, applianceType);

        return convertView;
    }
}
