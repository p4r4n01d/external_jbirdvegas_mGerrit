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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.jbirdvegas.mgerrit.objects.Project;
import com.jbirdvegas.mgerrit.tasks.ProjectsListLoader;

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
    DatabaseFactory mFactory;
    Context mContext;

    protected ProjectsTable(SQLiteDatabase db, DatabaseFactory factory, Context context) {
        this.mDb = db;
        this.mFactory = factory;
        this.mContext = context;
    }

    // We could make the sql text static and execute it from DBHelper
    protected void create() {
        mDb.execSQL("create table " + TABLE + " ("
                + C_ROOT + " text NOT NULL, "
                + C_SUBPROJECT + " text NOT NULL, "
                + C_DESCRIPTION + " text, " // This will probably be null
                + " PRIMARY KEY (" + C_ROOT + ", " + C_SUBPROJECT + "))");
    }

    /* Note: We may have to implement our own method of monitoring and reporting changes
     *  if Cursor.registerContentObserver only works with ContentProviders
     */

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

    public ProjectsListLoader getProjects() {

        String search = "SELECT " + C_ROOT + ", " + C_SUBPROJECT + " FROM " + TABLE
                + " ORDER BY " + SORT_BY;

        return new ProjectsListLoader(mContext, this, search, null);
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
        String p[] = projectPath.split("/", 2);
        return new Pair<String, String>(p[0], p[1]);
    }

    public DatabaseFactory getFactory() {
        return mFactory;
    }
}
