package com.jbirdvegas.mgerrit.tasks;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import com.jbirdvegas.mgerrit.Prefs;
import com.jbirdvegas.mgerrit.database.DatabaseFactory;
import com.jbirdvegas.mgerrit.database.ProjectsTable;
import com.jbirdvegas.mgerrit.objects.JSONCommit;
import com.jbirdvegas.mgerrit.objects.Project;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ProjectsListLoader extends AsyncTaskLoader<Cursor>
{

    GerritTask mProjectListFetcher;
    Context mContext;
    ProjectsTable mProjTbl;
    Cursor mCursor;

    /* TODO: The Loader will monitor for changes to the data, and report them through
     *  new calls to ProjectsList.onLoadFinished. */
    /* TODO: The Loader will release the data once it knows the application is no longer
     *  using it. */

    public ProjectsListLoader(Context context, Bundle args) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        /* Try querying the database, and if there are no results, fetch the
         *  list from the server and re-query the database. */

        // Avoid referencing the parent activity directly to avoid context leaks.
        mProjTbl = DatabaseFactory.getDatabase(mContext).getProjectsTable();
        Cursor mCursor = mProjTbl.getProjects();

        /* This needs to block as this will query database, find there are
         *  no results, fetch some and re-query the database before the results
         *  are inserted. */
        if (mCursor.isAfterLast())
        {
            fetchProjects();

            // Hope the Status is PENDING initially.
            while (mProjectListFetcher.getStatus() != AsyncTask.Status.FINISHED) {
                try { Thread.sleep(100); }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
        else mCursor = mProjTbl.getProjects();
        return mCursor;
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

                    /* TODO: Wake up the main thread of this AsyncTaskLoader as the
                     *  results are in */

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        mProjectListFetcher.execute(Prefs.getCurrentGerrit(mContext) + "projects/?d");
    }

    private void releaseResources() {
        mCursor.close();
    }
}
