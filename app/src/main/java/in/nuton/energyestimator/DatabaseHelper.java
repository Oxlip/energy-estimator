package in.nuton.energyestimator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database helper is used to manage the creation and upgrading of your database.
 * This class also provides the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static DatabaseHelper mInstance = null;

    private static final String LOG_TAG_DATABASE_HELPER = "DatabaseHelper";

    // name of the database file
    private static final String DATABASE_NAME = "energy.db";
    private static final int DATABASE_VERSION = 1;

    // the DAO objects for various tables
    private Dao<DeviceInfo, String> deviceInfoDao = null;
    private Dao<ApplianceType, String> applianceTypeDao = null;
    private Dao<ApplianceMake, String> applianceMakeDao = null;
    private Dao<ElectricityProvider, String> electricityProviderDao = null;
    private Dao<ElectricityRates, String> electricityRatesDao = null;

    // cached copy of appliance type and make
    private static List<ApplianceType> applianceTypeList = null;
    private static List<ApplianceMake> applianceMakeList = null;

    // Hash table for faster lookup
    private static Map<String, ApplianceType> applianceTypeMap = new HashMap<String, ApplianceType>();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Returns the existing database helper instance or creates new one if required.
     * @return DatabaseHelper instance.
     */
    public static DatabaseHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ApplicationGlobals.getAppContext());
        }
        return mInstance;
    }

    private void populateTables() {
        try {
            Dao<ApplianceMake, String> applianceMakeDao = getApplianceMakeDao();
            Dao<ApplianceType, String> applianceTypeDao = getApplianceTypeDao();

            InputStream is = ApplicationGlobals.getAppContext().getResources().openRawResource(R.raw.populate_db);
            DataInputStream in = new DataInputStream(is);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                strLine = strLine.trim();
                if (strLine.compareTo("") == 0) {
                    continue;
                }
                while (!strLine.endsWith(";")) {
                    String newLine = br.readLine();
                    if (newLine == null) {
                        throw new Exception("Database statement without semicolon");
                    }
                    strLine += newLine.trim();
                }
                Log.d("Database", "Executing " + strLine);
                applianceMakeDao.updateRaw(strLine);
            }
            in.close();
        } catch (Exception e) {
            Log.d(LOG_TAG_DATABASE_HELPER, "Can't populate database");
            e.printStackTrace();
        }
    }

    /**
     * Called when the database is first created.
     * Creates required tables.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, DeviceInfo.class);
            TableUtils.createTable(connectionSource, ApplianceMake.class);
            TableUtils.createTable(connectionSource, ApplianceType.class);
            TableUtils.createTable(connectionSource, ElectricityProvider.class);
            TableUtils.createTable(connectionSource, ElectricityRates.class);
            populateTables();
        } catch (SQLException e) {
            Log.e(LOG_TAG_DATABASE_HELPER, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Called when the application is upgraded and it has a higher version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, DeviceInfo.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our DeviceInfo class. It will create it or just give the cached
     * value.
     */
    public Dao<DeviceInfo, String> getDeviceInfoDao() throws SQLException {
        if (deviceInfoDao == null) {
            deviceInfoDao = getDao(DeviceInfo.class);
        }
        return deviceInfoDao;
    }

    /**
     * Returns the Database Access Object (DAO) for ApplianceType.
     * It will create it or just give the cached value.
     */
    public Dao<ApplianceType, String> getApplianceTypeDao() throws SQLException {
        if (applianceTypeDao == null) {
            applianceTypeDao = getDao(ApplianceType.class);
        }
        return applianceTypeDao;
    }

    /**
     * Returns the Database Access Object (DAO) for ApplianceMake.
     * It will create it or just give the cached value.
     */
    public Dao<ApplianceMake, String> getApplianceMakeDao() throws SQLException {
        if (applianceMakeDao == null) {
            applianceMakeDao = getDao(ApplianceMake.class);
        }
        return applianceMakeDao;
    }

    /**
     * Returns the Database Access Object (DAO) for ElectricityRates.
     * It will create it or just give the cached value.
     */
    public Dao<ElectricityRates, String> getElectricityRatesDao() throws SQLException {
        if (electricityRatesDao == null) {
            electricityRatesDao = getDao(ElectricityRates.class);
        }
        return electricityRatesDao;
    }

    /**
     * Returns the Database Access Object (DAO) for ElectricityProvider.
     * It will create it or just give the cached value.
     */
    public Dao<ElectricityProvider, String> getElectricityProviderDao() throws SQLException {
        if (electricityProviderDao == null) {
            electricityProviderDao = getDao(ElectricityProvider.class);
        }
        return electricityProviderDao;
    }

    /**
     * Returns DeviceInfo for a given deviceAddress
     */
    public static DeviceInfo getDeviceInfo(int id) {
        try {
            return getInstance().getDeviceInfoDao().queryForId(id + "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates or updates given deviceinfo into database.
     */
    public static void saveDeviceInfo(DeviceInfo deviceInfo) {
        try {
            getInstance().getDeviceInfoDao().createOrUpdate(deviceInfo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes given DeviceInfo from database.
     */
    public static void deleteDeviceInfo(DeviceInfo deviceInfo) {
        try {
            getInstance().getDeviceInfoDao().delete(deviceInfo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Returns all the devices that are registered to the user.
     *
     * @return List of devices.
     */
    public static List<DeviceInfo> getDevices() {
        try {
            return getInstance().getDeviceInfoDao().queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Returns list of appliance types.
     *
     * @return List of appliance types.
     */
    public static List<ApplianceType> getApplianceTypeList() {
        if (applianceTypeList != null) {
            return applianceTypeList;
        }
        try {
            applianceTypeList = getInstance().getApplianceTypeDao().queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return applianceTypeList;
    }

    /**
     * Returns Appliance Type associated with the given name.
     * @param name - Name of the appliance
     * @return Appliance Type.
     */
    public static ApplianceType getApplianceTypeByName(String name) {
        if (applianceTypeMap.size() == 0) {
            for (DatabaseHelper.ApplianceType applianceType: getApplianceTypeList()) {
                applianceTypeMap.put(applianceType.name, applianceType);
            }
        }

        return applianceTypeMap.get(name);
    }

    /**
     * Returns list of appliance makes.
     *
     * @return List of appliance makes.
     */
    public static List<ApplianceMake> getApplianceMakeList() {
        if (applianceMakeList != null) {
            return applianceMakeList;
        }
        applianceMakeList = new ArrayList<ApplianceMake>();
        try {
            applianceMakeList = getInstance().getApplianceMakeDao().queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return applianceMakeList;
    }


    /**
     * Returns list of electricity providers.
     *
     */
    public static List<ElectricityProvider> getElectricityProviders(String stateName) {
        Dao<ElectricityProvider, String> dao;
        try {
            dao = getInstance().getElectricityProviderDao();
            if (stateName == null) {
                return dao.queryForAll();
            } else {
                return dao.queryBuilder().where().eq("stateName", stateName).query();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns specified electricity provider.
     *
     */
    public static ElectricityProvider getElectricityProvider(String stateName, String name) {
        try {
            Dao<ElectricityProvider, String> dao = getInstance().getElectricityProviderDao();
            List<ElectricityProvider> providerList = dao.queryBuilder().where().eq("stateName", stateName).and().eq("name", name).query();
            if (providerList == null) {
                return null;
            }
            if (providerList.size() != 1) {
                return null;
            }
            return providerList.get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns electricity rates for the given state/city.
     *
     * @return List of Electricity Rates.
     */
    public static List<ElectricityRates> getElectricityRateList(String stateName, String name) {
        try {
            ElectricityProvider provider = getElectricityProvider(stateName, name);
            return getInstance().getElectricityRatesDao().queryBuilder().where().eq("providerId", provider.id).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        deviceInfoDao = null;
    }

    /**
     * Appliance Make
     */
    @DatabaseTable(tableName = "ApplianceMake")
    static class ApplianceMake {
        /**
         * Unique Appliance Manufacturers name.
         */
        @DatabaseField(id = true)
        String name;

        /**
         * Icon resource name.
         */
        @DatabaseField(canBeNull = true)
        String imageName;

        ApplianceMake() {
            // needed by ormlite
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Appliance Type.
     */
    @DatabaseTable(tableName = "ApplianceType")
    static class ApplianceType {
        /**
         * Unique type name - Light, Fan, TV etc.
         */
        @DatabaseField(id = true)
        String name;

        /**
         * True if this appliance is dimmable.
         */
        @DatabaseField(canBeNull = true)
        boolean isDimmable;

        /**
         * Icon resource name.
         */
        @DatabaseField(canBeNull = true)
        String imageName;

        /**
         * Average - How many watts the device consumes when in active mode.
         */
        @DatabaseField
        float activeWatts;

        /**
         * Average - How many watts the device consumes when in standby mode.
         */
        @DatabaseField
        float standbyWatts;


        ApplianceType() {
            // needed by ormlite
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Information such as Name, Type etc that is associated with a device.
     */
    @DatabaseTable(tableName = "DeviceInfo")
    public static class DeviceInfo {
        /**
         * Unique address of the device(BLE MAC address).
         */
        @DatabaseField(generatedId = true)
        int id;

        /**
         * Custom name given by the user.
         */
        @DatabaseField
        String name;

        /**
         * Type of the connected appliance - Light, Fan, TV etc
         */
        @DatabaseField
        String applianceType;

        /**
         * Appliance manufacturer.
         */
        @DatabaseField
        String applianceMake;

        /**
         * Appliance manufacturer's model number.
         */
        @DatabaseField
        String applianceModel;

        /**
         * When this appliance was bought.
         */
        @DatabaseField
        Date purchaseDate;

        /**
         * How many hours the device is actively used per day.
         */
        @DatabaseField
        float activeHours;

        /**
         * How many hours the device is in standby mode per day.
         */
        @DatabaseField
        float standbyHours;

        /**
         * How many watts the device consumes when in active mode.
         */
        @DatabaseField
        float activeWatts;

        /**
         * How many watts the device consumes when in standby mode.
         */
        @DatabaseField
        float standbyWatts;

        DeviceInfo() {
            // needed by ormlite
        }
    }

    /**
     * Electricity Provider.
     */
    @DatabaseTable(tableName = "ElectricityProvider")
    public static class ElectricityProvider {
        /**
         * Unique id.
         */
        @DatabaseField(generatedId = true)
        int id;

        /**
         * State Name.
         */
        @DatabaseField
        String stateName;

        /**
         * Provider Name.
         */
        @DatabaseField
        String name;

        ElectricityProvider() {
            // needed by ormlite
        }
    }

    /**
     * Electricity Rates.
     */
    @DatabaseTable(tableName = "ElectricityRates")
    public static class ElectricityRates {
        @DatabaseField(generatedId = true)
        int id;

        @DatabaseField(foreign = true, columnName="providerId")
        ElectricityProvider electricityProvider;

        /**
         * Billing period in days.
         */
        @DatabaseField
        int billingPeriod;

        /**
         * Condition for units consumed in the billing cycle.
         */
        @DatabaseField
        int conditionUnitsStart;

        @DatabaseField
        int conditionUnitsEnd;

        /**
         * Condition max load connected.
         */
        @DatabaseField
        int conditionLoadStart;

        @DatabaseField
        int conditionLoadEnd;

        /**
         * Single phase or 3 phase.
         */
        @DatabaseField
        boolean conditionSinglePhase;

        /**
         * Unit range start.
         */
        @DatabaseField
        int startUnit;

        /**
         * Unit range end.
         */
        @DatabaseField
        int endUnit;

        /**
         * Rate in rupees.
         */
        @DatabaseField
        float rate;

        ElectricityRates() {
            // needed by ormlite
        }

    }
}
