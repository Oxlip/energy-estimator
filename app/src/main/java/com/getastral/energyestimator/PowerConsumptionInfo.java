package com.getastral.energyestimator;

import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

class PowerConsumptionInfo {
    static int TOTAL_DAYS = 60;

    List<DatabaseHelper.DeviceInfo> deviceList;
    ArrayList<Entry> applianceUsage;
    ArrayList<String> applianceNames;

    ArrayList<String> slabNames;
    ArrayList<BarEntry> slabCosts;
    ArrayList<BarEntry> slabUnitsUsage;

    float totalActiveWatts;
    float totalStandbyWatts;
    float totalActiveHours;
    float totalStandbyHours;

    float totalUnitsPerDay;
    float totalUnits;
    float totalCost;

    float highestRate;
    float savable;

    public PowerConsumptionInfo() {
        deviceList = DatabaseHelper.getDevices();
        applianceUsage = new ArrayList<>();
        applianceNames = new ArrayList<>();

        slabNames = new ArrayList<>();
        slabCosts = new ArrayList<>();
        slabUnitsUsage = new ArrayList<>();

        totalActiveWatts = totalStandbyWatts = 0;
        totalActiveHours = totalStandbyHours = 0;

        totalUnitsPerDay = 0;

        int xIndex = 0;
        for (DatabaseHelper.DeviceInfo deviceInfo : deviceList) {
            float activeWatts = deviceInfo.activeWatts * deviceInfo.activeHours;
            float standbyWatts = deviceInfo.standbyWatts * deviceInfo.standbyHours;

            totalActiveWatts += activeWatts;
            totalStandbyWatts += standbyWatts;

            Log.d("PC", deviceInfo.name +  " hours " + deviceInfo.activeHours + " watts " + deviceInfo.activeWatts + " " + deviceInfo.standbyWatts + " " + deviceInfo.standbyHours);

            totalActiveHours += deviceInfo.activeHours;
            totalStandbyHours += deviceInfo.standbyHours;

            applianceNames.add(deviceInfo.name);
            applianceUsage.add(new Entry(activeWatts + standbyWatts, xIndex));

            xIndex++;
        }

        // main business logic
        totalUnitsPerDay = (totalActiveWatts + totalStandbyWatts) / 1000;
        totalUnits = totalUnitsPerDay * TOTAL_DAYS;
        totalCost = 0;
        highestRate = 0;

        float remainingUnits = totalUnits;
        List<DatabaseHelper.ElectricityRates> list;

        Log.d("PC", "Total watts per day " + totalUnitsPerDay);
        int i=0;
        list = DatabaseHelper.getElectricityRateList("Tamil Nadu", "Public");
        for(DatabaseHelper.ElectricityRates er: list) {
            float totalWattsPerBillingPeriod = totalUnitsPerDay * er.billingPeriod;
            float cStart = er.conditionUnitsStart, cEnd = er.conditionUnitsEnd;
            float slabUnits;
            if (er.endUnit == -1f) {
                slabUnits = remainingUnits;
            } else {
                slabUnits = er.endUnit - er.startUnit;
                if (slabUnits > remainingUnits) {
                    slabUnits = remainingUnits;
                }
            }
            Log.d("PC", "cStart " + cStart + " cEnd " + cEnd + " slabUnits " + slabUnits + " remainingUnits " + remainingUnits );
            if (((cStart == -1f) || (totalWattsPerBillingPeriod > cStart)) &&
                 ((cEnd == -1f)  || (totalWattsPerBillingPeriod < cEnd))) {
                float cost = er.rate * slabUnits;

                if (er.rate > highestRate) {
                    highestRate = er.rate;
                }
                slabNames.add(String.format("%6.0fKw @ Rs. %2.2f", slabUnits, er.rate));
                slabCosts.add(new BarEntry(cost, i));
                slabUnitsUsage.add(new BarEntry(slabUnits, i));
                remainingUnits -= slabUnits;

                totalCost += cost;
                i++;
            }
        }
        savable = (totalCost / 100 * 18) + (totalStandbyWatts / 1000 * highestRate);
    }
}
