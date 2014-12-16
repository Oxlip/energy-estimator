package com.getastral.energyestimator;

import android.util.Log;

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

        float totalUnitsPerDay = (totalActiveWatts + totalStandbyWatts) / 1000;
        int TOTAL_DAYS = 60;
        float remainingUnits = totalUnitsPerDay * TOTAL_DAYS;
        List<DatabaseHelper.ElectricityRates> list;

        Log.d("PC", "Total watts per day " + totalUnitsPerDay);
        int i=0;
        list = DatabaseHelper.getElectricityRateList("Tamil Nadu", "Public");
        for(DatabaseHelper.ElectricityRates er: list) {
            float totalWattsPerBillingPeriod = totalUnitsPerDay * er.billingPeriod;
            float cStart = er.conditionUnitsStart, cEnd = er.conditionUnitsEnd;
            float slabUnits, units;
            if (er.endUnit == -1f) {
                slabUnits = remainingUnits;
            } else {
                slabUnits = er.endUnit - er.startUnit;
                if (slabUnits > remainingUnits) {
                    slabUnits = remainingUnits;
                }
            }
            Log.d("PC", "cStart " + cStart + " cEnd " + cEnd + " slabWatts " + slabUnits + " remainingUnits " + remainingUnits );
            if (((cStart == -1f) || (totalWattsPerBillingPeriod > cStart)) &&
                 ((cEnd == -1f)  || (totalWattsPerBillingPeriod < cEnd))) {
                slabNames.add(String.format("%6.0fKw @ Rs. %2.2f", slabUnits, er.rate));
                slabUsageValues.add(new BarEntry(er.rate * slabUnits, i++));
                remainingUnits -= slabUnits;
            }
        }
    }
}
