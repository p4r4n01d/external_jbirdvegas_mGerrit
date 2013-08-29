package com.jbirdvegas.mgerrit.tasks;

import android.content.Context;
import android.database.Cursor;

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
        mProjectListFetcher.execute(Prefs.getCurrentGerrit(mContext) + "projects/?d");
    }
}
