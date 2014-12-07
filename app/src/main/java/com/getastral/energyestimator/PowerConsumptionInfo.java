package com.getastral.energyestimator;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

class PowerConsumptionInfo {
    List<DatabaseHelper.DeviceInfo> deviceList;
    ArrayList<Entry> applianceUsage;
    ArrayList<String> applianceNames;

    ArrayList<String> slabNames;
    ArrayList<BarEntry> slabUsageValues;

    float totalActiveWatts;
    float totalStandbyWatts;
    float totalActiveHours;
    float totalStandbyHours;

    public PowerConsumptionInfo() {
        deviceList = DatabaseHelper.getDevices();
        applianceUsage = new ArrayList<Entry>();
        applianceNames = new ArrayList<String>();

        slabNames = new ArrayList<String>();
        slabUsageValues = new ArrayList<BarEntry>();

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

            applianceNames.add(deviceInfo.name);
            applianceUsage.add(new Entry(activeWatts + standbyWatts, xIndex));

            xIndex++;
        }

        float totalWatts = totalActiveWatts + totalStandbyWatts;
        float remainingWatts = totalWatts;
        float usageRate[] = {1f, 1.5f, 3.0f};
        int usageLimit[] = {1000, 30000, 100000};

        for(int i=0; i<3 && remainingWatts > 0 ; i++) {
            float used = usageLimit[i];
            if (remainingWatts < used ) {
                used = remainingWatts;
            }
            slabNames.add(String.format("Rs. %2.2f", usageRate[i]));
            slabUsageValues.add(new BarEntry(usageRate[i] * used, i));
            remainingWatts -= used;
        }
    }
}
