package com.jbirdvegas.mgerrit.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.jbirdvegas.mgerrit.objects.Project;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectsTable
{
    // Table name
    public static final String TABLE = "Symbols";

    // Columns
    public static final String C_ROOT = "base";
    public static final String C_SUBPROJECT = "project";
    public static final String C_DESCRIPTION = "desc";

    // Don't bother storing the kind - it is constant

    // Sort by condition for querying results.
    public static final String SORT_BY = C_ROOT + " ASC, " + C_SUBPROJECT + " ASC";

    public SQLiteDatabase mDb;

    protected ProjectsTable(SQLiteDatabase db) {
        this.mDb = db;
    }

    // We could make the sql text static and execute it from DBHelper
    protected void create() {
        mDb.execSQL("create table " + TABLE + " ("
                + C_ROOT + " text NOT NULL, "
                + C_SUBPROJECT + " text NOT NULL, "
                + C_DESCRIPTION + " text, " // This will probably be null
                + " PRIMARY KEY (" + C_ROOT + ", " + C_SUBPROJECT + "))");
    }

    /** Insert the list of projects into the database **/
    public boolean insertProjects(List<Project> projects) {

        ContentValues projectValues = new ContentValues(2);
        mDb.beginTransaction();

        for (Project project : projects) {
            projectValues.clear();
            Pair<String, String> proj = splitPath(project.getmPath());
            projectValues.put(C_ROOT, proj.first);
            projectValues.put(C_ROOT, proj.second);

            // We are only inserting PK columns so we should use the IGNORE resolution algorithm.
            mDb.insertWithOnConflict(TABLE, null, projectValues, SQLiteDatabase.CONFLICT_IGNORE);
        }

        mDb.endTransaction();
        return true;
    }

    public Cursor getProjects() {
        return mDb.query(TABLE, new String[] { C_ROOT, C_SUBPROJECT},
                null, null, null, null, SORT_BY);
    }

    // Return a cursor containing all of the root (base) projects
    public Cursor getRootProjects(String rootProject) {
        String[] projection = new String[] { "_id", C_ROOT};
        return mDb.query(TABLE, projection,
                null, null, null, null, SORT_BY);
    }

    // Return a cursor containing all of the subprojects under one root project
    public Cursor getSubprojects(String rootProject) {
        String[] projection = new String[] { "_id", C_SUBPROJECT};
        return mDb.query(TABLE, projection,
                C_ROOT + " = ?", new String[] {rootProject},
                null, null, SORT_BY);
    }

    // Split a project's path (name) into its root and subproject
    private Pair<String, String> splitPath(String projectPath)
    {
        // TODO: We want 2 parts, check limit parameter and confirm value
        String p[] = projectPath.split("/", 1);
        return new Pair<String, String>(p[0], p[1]);
    }
}
