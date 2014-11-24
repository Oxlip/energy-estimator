package com.getastral.energyesitmator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.AsyncTask;
import android.os.ConditionVariable;
import android.util.Log;

import java.util.Date;
import java.util.UUID;

public class Device {
    /** Device specific information populated from database.*/
    public int id;
    public String name;
    public String applianceType;
    public String applianceMake;
    public String applianceModel;
    public long purchaseDate;
    public long purchasePrice;
    public float activeWatts;
    public float standbyWatts;
    public float activeHours;
    public float standbyHours;

    /** Set to true if the device is stored in database. */
    private boolean isSaved;

    /** Application context. */
    private final Context mContext;

    private static final String LOG_TAG_DEVICE = "Device";

    /**
     * Construct a new device.
     * @param context Application context for opening the database.
     */
    public Device(Context context) {
        mContext = context;
    }

    /**
     * Saves the device into database.
     */
    public void save() {
        DatabaseHelper db = new DatabaseHelper(this.mContext);
        db.saveDevice(this);
        this.isSaved = true;
    }

    /**
     * Deletes the given device from database.
     */
    public void delete() {
        DatabaseHelper db = new DatabaseHelper(this.mContext);
        db.removeDevice(this);
        this.isSaved = false;
    }

    /**
     * Returns device storage state.
     * @return True if device is saved.
     */
    public boolean isRegistered() {
        return this.isSaved;
    }
}
