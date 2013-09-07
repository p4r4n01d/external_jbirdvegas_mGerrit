package com.jbirdvegas.mgerrit.tasks;

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
import android.database.Cursor;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.jbirdvegas.mgerrit.Prefs;
import com.jbirdvegas.mgerrit.database.ProjectsTable;
import com.jbirdvegas.mgerrit.objects.GerritURL;

public class ProjectsListLoader extends SQLiteCursorLoader
{
    ProjectsTable mProjTbl;

    String mRawQuery = null;
    String[] mArgs = null;
    GerritURL mUrl;

    /* This will monitor for changes to the data, and report them through new calls to the
     *  onLoadFinished callback */
    /* The Loader will release the data once it knows the application is no longer
     *  using it (should be managed by the superclass SQLiteCursorLoader) */

    public ProjectsListLoader(Context context,
                              ProjectsTable projectsTable,
                              String rawQuery,
                              String[] args) {
        super(context,
                projectsTable.getFactory().getDatabaseHelper(), rawQuery, args);
        mRawQuery = rawQuery;
        mArgs = args;
        mProjTbl = projectsTable;
        mUrl = new GerritURL();

        // This is just a precaution in case it has not been set yet
        GerritURL.setGerrit(Prefs.getCurrentGerrit(getContext()));
    }

    private Cursor query() {
        return mProjTbl.getFactory().getWritableDatabase().rawQuery(mRawQuery, mArgs);
    }

    @Override
    protected Cursor buildCursor() {
        return query();
    }
}
