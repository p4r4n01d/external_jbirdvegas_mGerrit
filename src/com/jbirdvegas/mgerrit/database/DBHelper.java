package com.jbirdvegas.mgerrit.database;

/*
 * Copyright (C) 2013 Android Open Kang Project (AOKP)
 *  Author: Evan Conway (P4R4N01D), 2013
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBHelper extends SQLiteOpenHelper
{
    static final String TAG = "DbHelper";
    static final int DB_VERSION = 1;
    private static DBHelper mInstance;
    private static Context sContext;
    private static String sDbName;
    private static DatabaseFactory sFactory;

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private DBHelper(DatabaseFactory databaseFactory, Context context, String dbName) {
        super(context, dbName, null, DB_VERSION);
        sContext = context;
        sDbName = dbName;
        sFactory = databaseFactory;
    }

    // This method prevents the existing static values from being overwritten when unnecessary
    public static DBHelper getInstance(DatabaseFactory databaseFactory, Context ctx, String dbName) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DBHelper(databaseFactory, ctx, dbName);
        } else if (sDbName.equals(dbName)) {
            //this.close();
            mInstance = new DBHelper(databaseFactory, ctx, dbName);
        }
        return mInstance;
    }

    // Called only once, first time the DB is created. Create all the tables here
    @Override
    public void onCreate(SQLiteDatabase db) {
        new ProjectsTable(db, sFactory, sContext).create();
    }

    // Called whenever newVersion > oldVersion. Can do some version number checking
    //  in here to avoid completely emptying the database, although it may be a good
    //  idea to query the lot again.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + ProjectsTable.TABLE);

        Log.d(TAG, "Database Updated.");
        onCreate(db); // run onCreate to get new database
    }

    // Sanitise the names of the Gerrit instances so we do not create odd file names
    public static String getDatabaseName(String gerritURL) {
        String[] parts = gerritURL.split("\\.");
        String name;
        if (parts.length < 2)  name = parts[parts.length - 1];
        else name = parts[parts.length - 2];

        // Conservative naming scheme - allow only letters. digits and underscores.
        return name.toLowerCase().replaceAll("[^a-z0-9_]+", "");
    }

    protected void shutdown()
    {
        this.close();
        mInstance = null;
        sContext = null;
        sDbName = null;
        sFactory = null;
    }
}
