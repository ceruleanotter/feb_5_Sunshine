/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

import java.util.HashMap;
import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
         This function gets called before each test is executed to delete the database.  This makes
         sure that we always have a clean test.
    */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherEntry.TABLE_NAME);

        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + LocationEntry.TABLE_NAME + ")", null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        class ColumnProps {
            public ColumnProps( String type, boolean notNull, boolean partOfPrimaryKey ) {
                m_type = type;
                m_notNull = notNull;
                m_partOfPrimaryKey = partOfPrimaryKey;
            }
            final String m_type;
            final boolean m_notNull;
            final boolean m_partOfPrimaryKey;
        };

        final HashMap<String, ColumnProps> locationColumnHashSet = new HashMap<String, ColumnProps>();
        locationColumnHashSet.put(LocationEntry._ID, new ColumnProps("INTEGER", false, true));
        locationColumnHashSet.put(LocationEntry.COLUMN_CITY_NAME, new ColumnProps("TEXT", true, false));
        locationColumnHashSet.put(LocationEntry.COLUMN_COORD_LAT, new ColumnProps("REAL", true, false));
        locationColumnHashSet.put(LocationEntry.COLUMN_COORD_LONG, new ColumnProps("REAL", true, false));
        locationColumnHashSet.put(LocationEntry.COLUMN_LOCATION_SETTING, new ColumnProps("TEXT", true, false));

        int columnNameIndex = c.getColumnIndex("name");
        int columnTypeIndex = c.getColumnIndex("type");
        int columnNotNullIndex = c.getColumnIndex("notnull");
        int columnPrimaryKeyIndex = c.getColumnIndex("pk");

        do {
            String columnName = c.getString(columnNameIndex);
            ColumnProps cp = locationColumnHashSet.get(columnName);
            if ( null != cp ) {
                locationColumnHashSet.remove(columnName);
                String type = c.getString(columnTypeIndex);
                boolean notNull = c.getInt(columnNotNullIndex) == 1;
                boolean primaryKey = c.getInt(columnPrimaryKeyIndex) == 1;
                assertEquals("Error: The type of column '" + columnName + "' is incorrect.", type, cp.m_type);
                assertEquals("Error: The column '" + columnName + "' must " +
                        (cp.m_notNull ? "" : "NOT ") + "be set to use the NOT NULL constraint.", notNull, cp.m_notNull);
                assertEquals("Error: The column '" + columnName + "' must " +
                        (cp.m_partOfPrimaryKey ? "" : "NOT ") + "be set to use the PRIMARY_KEY constraint.", primaryKey, cp.m_partOfPrimaryKey);
            }
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();

        // Let's start over from a clean state.
        deleteTheDatabase();

        // let's see if we have the location UNIQUE constraint set correctly
        db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();

        // insert the North Pole location values once
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Insert of LocationValues failed.", locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // insert the North Pole location values a second time
        long secondLocationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);

        // The second row should abort on insert because we require a unique LocationEntry, which
        // means that secondLocationRowId should be -1.
        assertEquals("Error: Constraint doesn't match. " + LocationEntry.COLUMN_LOCATION_SETTING +
                " must be set to TEXT UNIQUE NOT NULL.", -1,secondLocationRowId);
    }


    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public long testLocationTable() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.

        // First step: Get reference to writable database
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Fifth Step: Validate the location Query
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();

        // Return the row ID of our new location
        return locationRowId;
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {
        // First step: Insert our location and get the row ID.
        long locationRowId = testLocationTable();

        // Make sure we have a potentially valid row ID.
        assertFalse("Error: Location Not Inserted Correctly", locationRowId == -1L);

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.

        // First step: Get reference to writable database
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Weather): Create weather values
        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);

        // Third Step (Weather): Insert ContentValues into database and get a row ID back
        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                WeatherEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertTrue( "Error: No Records returned from location query", weatherCursor.moveToFirst() );

        // Fifth Step: Validate the location Query
        TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                weatherCursor, weatherValues);

        // Sixth Step: Close cursor and database
        weatherCursor.close();
        dbHelper.close();
    }
}
