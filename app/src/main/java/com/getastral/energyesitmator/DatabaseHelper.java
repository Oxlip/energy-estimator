package com.getastral.energyesitmator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Helper class to access the Astral Database.
 *
 *  The database contains all the devices(Plug/Switch/Touch) that are registered by the user.
 *  Basically it is cached version of CloudServer's user specific data.
 **/
class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Astral";

    // Devices table Name
    private static final String TABLE_DEVICES = "Devices";

    // Devices table's column names
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_APPLIANCE_TYPE = "appliance_type";
    private static final String FIELD_APPLIANCE_MAKE = "appliance_make";
    private static final String FIELD_APPLIANCE_MODEL = "appliance_model";
    private static final String FIELD_PURCHASE_DATE = "purchase_date";
    private static final String FIELD_PURCHASE_PRICE = "purchase_price";
    private static final String FIELD_ACTIVE_WATTS = "active_watts";
    private static final String FIELD_STANDBY_WATTS = "standby_watts";
    private static final String FIELD_ACTIVE_HOURS = "active_hours";
    private static final String FIELD_STANDBY_HOURS = "standby_hours";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_DEVICES + "(" +
                FIELD_ID + " INTEGER PRIMARY KEY," +
                FIELD_NAME + " TEXT NOT NULL," +
                FIELD_APPLIANCE_TYPE + " TEXT," +
                FIELD_APPLIANCE_MAKE + " TEXT," +
                FIELD_APPLIANCE_MODEL + " TEXT," +
                FIELD_PURCHASE_DATE + " LONG," +
                FIELD_PURCHASE_PRICE + " INT," +
                FIELD_ACTIVE_WATTS + " FLOAT," +
                FIELD_STANDBY_WATTS + " FLOAT," +
                FIELD_ACTIVE_HOURS + " FLOAT," +
                FIELD_STANDBY_HOURS + " FLOAT" +
                ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);

        // Create tables again
        onCreate(db);
    }

    /**
     * Saves the device information to database.
     * This should be happening only once(first time when connect button is clicked).
     *
     * @param device Device needs to be saved.
     */
    void saveDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (device.id != 0) {
            values.put(FIELD_ID, device.id);
        }
        values.put(FIELD_NAME, device.name);
        values.put(FIELD_APPLIANCE_TYPE, device.applianceType);
        values.put(FIELD_APPLIANCE_MAKE, device.applianceMake);
        values.put(FIELD_APPLIANCE_MODEL, device.applianceModel);
        values.put(FIELD_PURCHASE_DATE, device.purchaseDate);
        values.put(FIELD_PURCHASE_PRICE, device.purchasePrice);
        values.put(FIELD_ACTIVE_WATTS, device.activeWatts);
        values.put(FIELD_STANDBY_WATTS, device.standbyHours);
        values.put(FIELD_ACTIVE_HOURS, device.activeHours);
        values.put(FIELD_STANDBY_HOURS, device.standbyHours);


        db.insert(TABLE_DEVICES, null, values);
        db.close();
    }

    /**
     * Check whether the given device exists in the database.
     */
    Boolean isRegistered(int id) {
        String countQuery = "SELECT  * FROM " + TABLE_DEVICES + "WHERE " + FIELD_ID + "==" + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount() > 0;
    }

    /**
     * Returns all the devices that are registered to the user.
     *
     * @param context Application context needs to be passed to Device() constructor.
     * @return List of devices.
     */
    public List<Device> getDevices(Context context) {
        List<Device> deviceList = new ArrayList<Device>();
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Device device = new Device(context);
                device.id = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_ID));
                device.name = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_NAME));
                device.applianceType = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_APPLIANCE_TYPE));
                device.applianceMake = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_APPLIANCE_MAKE));
                device.applianceModel = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_APPLIANCE_MODEL));
                device.purchaseDate = cursor.getLong(cursor.getColumnIndexOrThrow(FIELD_PURCHASE_DATE));
                device.purchasePrice = cursor.getLong(cursor.getColumnIndexOrThrow(FIELD_PURCHASE_PRICE));
                device.activeWatts = cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_ACTIVE_WATTS));
                device.standbyWatts = cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_STANDBY_WATTS));
                device.activeHours = cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_ACTIVE_HOURS));
                device.standbyHours = cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_STANDBY_HOURS));

                deviceList.add(device);
            } while (cursor.moveToNext());
        }

        return deviceList;
    }

    /**
     * Delete device from the database.
     * @param device Device needs to be removed.
     */
    public void removeDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DEVICES, FIELD_ID + " = ?",
                new String[] { String.valueOf(device.id) });
        db.close();
    }
}
