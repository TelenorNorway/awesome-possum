package com.telenor.possumlib.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.telenor.possumlib.constants.DetectorType;

/***
 * Helper class for accessing database and loading/saving/deleting dataSets
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static DBHelper instance;
    private static final String DATABASE_NAME = "AwesomePossum";
    private static final String tag = DBHelper.class.getName();

    // TABLE NAMES
    public static final String TABLE_NETWORK_MATRIX = "networkTraining";
    public static final String TABLE_BLUETOOTH_MATRIX = "bluetoothTraining";

    // Field names
    public static final String FIELD_MAC = "mac";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Training tables
        db.execSQL("CREATE TABLE " + TABLE_NETWORK_MATRIX + " ("+FIELD_MAC + " TEXT PRIMARY KEY," +
                "amount INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_BLUETOOTH_MATRIX + " ("+FIELD_MAC + " TEXT PRIMARY KEY," +
                "amount INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // SQLite does not support ALTER TABLE DROP COLUMN
    }

    public long getNumberOfRows(String tableName) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), tableName);
    }

    public void clearLearningFor(int detectorType) {
        String tableName;
        switch (detectorType) {
            case DetectorType.Wifi:
                tableName = TABLE_NETWORK_MATRIX;
                break;
            case DetectorType.Bluetooth:
                tableName = TABLE_BLUETOOTH_MATRIX;
                break;
            default:
                throw new RuntimeException("Unhandled detector table:" + detectorType);
        }
        getWritableDatabase().delete(tableName, null, null);
    }

    /* Returns true if dataIn is present in the top collectionSize entries in tableName */
    public boolean compareToTrainingData(String tableName, String field, String dataIn, int collectionSize) {
        String sql = "SELECT DISTINCT amount, " + field + " " +
                "FROM " + tableName + " " +
                "ORDER BY amount DESC " +
                "LIMIT " + collectionSize;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String data = cursor.getString(1);
            if (data.equals(dataIn)) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    /**
     * Increments amount corresponding to dataIn by one. If dataIn does not exist in tableName
     * then it is added with amount 1.
     * @param tableName name of table to insert/update
     * @param field field responsible for containing the data we want to check for
     * @param dataIn the data we want to compare/increment against
     */
    public void addToTrainingData(String tableName, String field, String dataIn) {
        getWritableDatabase().execSQL("REPLACE INTO "+tableName+" ("+field+", amount) VALUES (?, IFNULL((SELECT amount FROM "+tableName+" WHERE "+field+"=?),0)+1)", new String[]{dataIn, dataIn});
    }
}