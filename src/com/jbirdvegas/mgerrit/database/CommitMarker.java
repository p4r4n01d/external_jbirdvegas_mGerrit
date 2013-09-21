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

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.jbirdvegas.mgerrit.helpers.DBParams;
import com.jbirdvegas.mgerrit.objects.CommitterObject;
import com.jbirdvegas.mgerrit.objects.JSONCommit;

import java.util.Collections;
import java.util.List;

public class CommitMarker extends DatabaseTable {
    // Table name
    public static final String TABLE = "_Marker";

    // --- Columns ---
    private static final String C_CHANGE_ID = "change_id";
    private static final String C_UPDATED = "time_modified";

    // The sortkey of the change.
    private static final String C_SORTKEY = "_sortkey";

    private static final String[] PRIMARY_KEY = { C_CHANGE_ID };

    public static final int ITEM_LIST = UriType.CommitMarkerList.ordinal();
    public static final int ITEM_ID = UriType.CommitMarkerID.ordinal();

    public static final Uri CONTENT_URI = Uri.parse(DatabaseFactory.BASE_URI + TABLE);

    public static final String CONTENT_TYPE = DatabaseFactory.BASE_MIME_LIST + TABLE;
    public static final String CONTENT_ITEM_TYPE = DatabaseFactory.BASE_MIME_ITEM + TABLE;

    private static CommitMarker mInstance = null;

    public static CommitMarker getInstance() {
        if (mInstance == null) mInstance = new CommitMarker();
        return mInstance;
    }

    @Override
    public void create(String TAG, SQLiteDatabase db) {
        db.execSQL("create table " + TABLE + " ("
                + C_CHANGE_ID + " text PRIMARY KEY, "
                + C_UPDATED + " text NOT NULL, "
                + C_SORTKEY + " text NOT NULL, "
                + "FOREIGN KEY (" + C_CHANGE_ID + ") REFERENCES "
                    + Users.TABLE + "(" + Changes.C_CHANGE_ID + "), "
                + "FOREIGN KEY (" + C_UPDATED + ") REFERENCES "
                    + Users.TABLE + "(" + Changes.C_UPDATED + "))");
    }

    @SuppressWarnings("unused")
    public static void addURIMatches(UriMatcher _urim) {
        _urim.addURI(DatabaseFactory.AUTHORITY, TABLE, ITEM_LIST);
        _urim.addURI(DatabaseFactory.AUTHORITY, TABLE + "/#", ITEM_ID);
    }

    /**
     * Given a change id, get its corresponding search key
     */
    public static String getSortkeyOfChange(Context context, String changeID) {
        Uri uri = DBParams.fetchOneRow(CONTENT_URI);
        Cursor c = context.getContentResolver().query(uri,
                new String[] { C_SORTKEY },
                C_CHANGE_ID + " = ?",
                new String[] { changeID },
                null);
        if (c.moveToFirst()) return c.getString(0);
        return null;
    }

    /**
     * Records a commit with its last updated time and sortkey for resuming
     *  the query later.
     */
    protected static void markCommit(Context context, JSONCommit commit) {
        ContentValues contentValues = new ContentValues(3);
        contentValues.put(C_CHANGE_ID, commit.getChangeId());
        contentValues.put(C_UPDATED, commit.getLastUpdatedDate());
        contentValues.put(C_SORTKEY, commit.getSortKey());
        context.getContentResolver().insert(CONTENT_URI, contentValues);
    }
}
