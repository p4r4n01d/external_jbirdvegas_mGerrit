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
import android.os.Looper;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;

import com.jbirdvegas.mgerrit.Prefs;
import com.jbirdvegas.mgerrit.database.ProjectsTable;
import com.jbirdvegas.mgerrit.objects.JSONCommit;
import com.jbirdvegas.mgerrit.objects.Project;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ProjectsListLoader extends SQLiteCursorLoader
{
    GerritTask mProjectListFetcher;
    Context mContext;
    ProjectsTable mProjTbl;

    String mRawQuery = null;
    String[] mArgs = null;

    /* This will monitor for changes to the data, and report them through new calls to the
     *  onLoadFinished callback */
    /* The Loader will release the data once it knows the application is no longer
     *  using it (should be managed by the superclass SQLiteCursorLoader) */

    public ProjectsListLoader(Context context, ProjectsTable projectsTable, String rawQuery, String[] args) {
        super(context,
                projectsTable.getFactory().getDatabaseHelper(), rawQuery, args);
        mContext = context;
        mRawQuery = rawQuery;
        mArgs = args;
        mProjTbl = projectsTable;
    }

    @Override
    protected Cursor buildCursor() {
        Cursor c = mProjTbl.getFactory().getWritableDatabase().rawQuery(mRawQuery, mArgs);
        if (c.isAfterLast()) {
            fetchProjects();

            // Hopefully mProjectListFetcher has been started at this point
            try {
                mProjectListFetcher.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Go around and query again.
        return buildCursor();
    }

    private void fetchProjects() {
        mProjectListFetcher = new GerritTask(mContext)
        {
            @Override
            public void onJSONResult(String jsonString)
            {
                try {
                    JSONObject projectsJson = new JSONObject(jsonString);
                    Iterator stringIterator = projectsJson.keys();

                    List<Project> projects = new ArrayList<Project>();
                    while (stringIterator.hasNext()) {
                        String path = (String) stringIterator.next();
                        JSONObject projJson = projectsJson.getJSONObject(path);
                        String kind = projJson.getString(JSONCommit.KEY_KIND);
                        String id = projJson.getString(JSONCommit.KEY_ID);
                        projects.add(Project.getInstance(path, kind, id));
                    }
                    Collections.sort(projects);

                    // Insert projects into database
                    mProjTbl.insertProjects(projects);

                    /* Wake up the main thread of this Loader as the results are in */
                    this.notify();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Looper.prepare();
        mProjectListFetcher.execute(Prefs.getCurrentGerrit(mContext) + "projects/?d");
    }
}
