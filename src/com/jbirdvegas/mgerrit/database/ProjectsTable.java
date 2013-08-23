package com.jbirdvegas.mgerrit.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ProjectsTable
{
    // Table name
    public static final String TABLE = "Symbols";

    // Columns
    public static final String C_ROOT = "base";
    public static final String C_SUBPROJECT = "project";
    public static final String C_DESCRIPTION = "desc";

    // Sort by condition for querying results.
    public static final String SORT_BY = C_ROOT + " ASC, " + C_SUBPROJECT + " ASC";

    public SQLiteDatabase mDb;

    protected ProjectsTable(SQLiteDatabase db) {
        this.mDb = db;
    }

    // We could make the sql text static and execute it from DBHelper
    protected void create() {
        mDb.execSQL("create table " + TABLE + " ("
                + C_ROOT + " text, "
                + C_SUBPROJECT + " text, "
                + C_DESCRIPTION + " text, "
                + " PRIMARY KEY (" + C_ROOT + ", " + C_SUBPROJECT + "))");
    }

    /** Insert the list of projects into the database **/
    //public void insertProjects()

    public Cursor getProjects() {
        return mDb.query(TABLE, new String[] { C_ROOT, C_SUBPROJECT, C_DESCRIPTION},
                null, null, null, null, SORT_BY);
    }
}
