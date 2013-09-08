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

import java.util.List;

public class ProjectsTable
{
    // Table name
    public static final String TABLE = "Projects";

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

    public static final String SEPERATOR = "/";

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
            projectValues.put(C_SUBPROJECT, proj.second);

            // We are only inserting PK columns so we should use the IGNORE resolution algorithm.
            mDb.insertWithOnConflict(TABLE, null, projectValues, SQLiteDatabase.CONFLICT_IGNORE);
        }

        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        return true;
    }

    public ProjectsListLoader getProjects(String baseproject, String subproject) {

        StringBuilder where = new StringBuilder(0);

        where.append("SELECT rowid as _id, ").append(C_ROOT)
                .append(" FROM ").append(TABLE)
                .append(" WHERE ").append(C_ROOT).append(" <> ''");

        String s = appendLikeQueries(new String[] { C_ROOT, C_SUBPROJECT},
                new String[] { baseproject, subproject});
        if (!s.equals("()")) where.append(" AND ").append(s);

        where.append(" GROUP BY ").append(C_ROOT)
                .append(" ORDER BY ").append(SORT_BY);

        return new ProjectsListLoader(mContext, this, where.toString(), null);
    }

    // Return a cursor containing all of the subprojects under one root project
    public Cursor getSubprojects(String rootProject, String subproject) {

        StringBuilder where = new StringBuilder(0);
        where.append(C_ROOT).append(" = ? AND ").append(C_SUBPROJECT).append(" <> ''");

        String s = appendLikeQueries(new String[] { C_SUBPROJECT},
                new String[] { subproject});
        if (!s.equals("()")) where.append(" AND ").append(s);

        String[] projection = new String[] { "rowid as _id", C_SUBPROJECT};
        return mDb.query(TABLE, projection,
                where.toString(),
                new String[] {rootProject},
                null, null, SORT_BY);
    }

    // Split a project's path (name) into its root and subproject
    private Pair<String, String> splitPath(String projectPath)
    {
        String p[] = projectPath.split(SEPERATOR, 2);
        if (p.length < 2) return new Pair<String, String>(p[0], "");
        else return new Pair<String, String>(p[0], p[1]);
    }

    private String appendLikeQueries(String[] fields, String[] queries) {
        boolean lastSucessful = false;
        StringBuilder builder = new StringBuilder(0).append("(");

        for (int i = 0; i < fields.length; i++) {
            String query = queries[i];
            if (query != null && query.length() > 0) {
                if (lastSucessful) builder.append(" OR ");
                builder.append(fields[i]).append(" LIKE '%").append(query).append("%'");
                lastSucessful = true;
            }
        }
        return builder.append(")").toString();
    }

    public DatabaseFactory getFactory() {
        return mFactory;
    }
}
