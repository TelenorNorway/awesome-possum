package com.telenor.possumlib;

import android.content.ContentValues;
import android.database.Cursor;

import com.telenor.possumlib.utils.DBHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSQLiteConnection;

@RunWith(PossumTestRunner.class)
public class DBHelperTest {
    private DBHelper dbHelper;
    @Before
    public void setUp() throws Exception {
        ShadowSQLiteConnection.setUseInMemoryDatabase(true);
        dbHelper = new DBHelper(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        ShadowSQLiteConnection.reset();
        dbHelper = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(dbHelper);
        // TODO: Confirm synchronization or waste of time? Also, running it causes multiple connection causing major problems. F**k it!
//        Assert.assertNotNull(DBHelper.getInstance(RuntimeEnvironment.application));

        Assert.assertEquals("AwesomePossum", dbHelper.getDatabaseName());
        Assert.assertEquals(1, dbHelper.getReadableDatabase().getVersion());
    }

    @Test
    public void testOnCreateDB() throws Exception {
        // Remove all tables and run instance gain. Then confirm existance of tables
        clearDatabase(dbHelper);
        dbHelper = new DBHelper(RuntimeEnvironment.application);
        Assert.assertTrue(dbHelper.getReadableDatabase().isDatabaseIntegrityOk());
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.FIELD_MAC, "test");
        cv.put("amount", 10);
        dbHelper.getWritableDatabase().insert(DBHelper.TABLE_NETWORK_MATRIX, null, cv);

        Cursor cursor = dbHelper.getReadableDatabase().query(DBHelper.TABLE_NETWORK_MATRIX, new String[]{"amount"}, null, null, null, null, null);
        Assert.assertTrue(cursor.moveToNext());
        Assert.assertEquals(1, cursor.getCount());
        int amountFromDb = cursor.getInt(0);
        Assert.assertEquals(10, amountFromDb);
        cursor.close();
    }

//    @Test
//    public void testUpgradeDB() throws Exception {
//
//    }
//
//    @Test
//    public void testSomething() throws Exception {
//    }

    private void clearDatabase(DBHelper helper) {
        helper.getWritableDatabase().execSQL("DROP TABLE "+DBHelper.TABLE_NETWORK_MATRIX);
        helper.getWritableDatabase().execSQL("DROP TABLE "+DBHelper.TABLE_BLUETOOTH_MATRIX);
    }
}