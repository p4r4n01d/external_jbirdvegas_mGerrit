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

import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class FileInfo extends DatabaseTable {

    // Table name
    public static final String TABLE = "FileInfo";

    // --- Columns ---
    // The Change-Id of the change.
    public static final String C_CHANGE_ID = "change_id";

    public static final String C_FILE_NAME = "filename";

    /* The status of the file ("A"=Added, "D"=Deleted, "R"=Renamed, "C"=Copied, "W"=Rewritten).
     * Not set if the file was Modified ("M"). optional. */
    public static final String C_STATUS = "status";

    // Whether the file is binary.
    public static final String C_ISBINARY = "binary";

    // The old file path. Only set if the file was renamed or copied.
    public static final String C_OLDPATH = "old_path";

    // Number of inserted lines. Not set for binary files or if no lines were inserted.
    public static final String C_LINES_INSERTED = "lines_inserted";

    // Number of deleted lines. Not set for binary files or if no lines were deleted.
    public static final String C_LINES_DELETED = "lines_deleted";

    public static final String[] PRIMARY_KEY = { C_CHANGE_ID, C_FILE_NAME };

    public static final int ITEM_LIST = UriType.FileInfoList.ordinal();
    public static final int ITEM_ID = UriType.FileInfoID.ordinal();

    public static final Uri CONTENT_URI = Uri.parse(DatabaseFactory.BASE_URI + TABLE);

    public static final String CONTENT_TYPE = DatabaseFactory.BASE_MIME_LIST + TABLE;
    public static final String CONTENT_ITEM_TYPE = DatabaseFactory.BASE_MIME_ITEM + TABLE;

    // Sort by condition for querying results.
    public static final String SORT_BY = C_FILE_NAME + " ASC";

    private static FileInfo mInstance = null;

    public static FileInfo getInstance() {
        if (mInstance == null) mInstance = new FileInfo();
        return mInstance;
    }

    @Override
    public void create(String TAG, SQLiteDatabase db) {
        // Specify a conflict algorithm here so we don't have to worry about it later
        db.execSQL("create table " + TABLE + " ("
                + C_CHANGE_ID + " text NOT NULL, "
                + C_FILE_NAME + " text NOT NULL, "
                + C_ISBINARY + " INTEGER DEFAULT 0 NOT NULL, "
                + C_OLDPATH + " text, "
                + C_LINES_INSERTED + " INTEGER, "
                + C_LINES_DELETED + " INTEGER, "
                + C_STATUS + " text NOT NULL, "
                + "PRIMARY KEY (" + C_CHANGE_ID + ", " + C_FILE_NAME + ") ON CONFLICT REPLACE, "
                + "FOREIGN KEY (" + C_CHANGE_ID + ") REFERENCES "
                + Changes.TABLE + "(" + Changes.C_CHANGE_ID + "))");
    }

    public static void addURIMatches(UriMatcher _urim)
    {
        _urim.addURI(DatabaseFactory.AUTHORITY, TABLE, ITEM_LIST);
        _urim.addURI(DatabaseFactory.AUTHORITY, TABLE + "/#", ITEM_ID);
    }
}
