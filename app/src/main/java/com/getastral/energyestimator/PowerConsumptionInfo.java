package com.getastral.energyestimator;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

class PowerConsumptionInfo {
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
