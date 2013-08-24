package com.jbirdvegas.mgerrit;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.jbirdvegas.mgerrit.database.DatabaseFactory;
import com.jbirdvegas.mgerrit.database.ProjectsTable;
import com.jbirdvegas.mgerrit.objects.JSONCommit;
import com.jbirdvegas.mgerrit.objects.Project;
import com.jbirdvegas.mgerrit.tasks.GerritTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ProjectsList extends Activity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    ExpandableListView mProjectsListView;
    ProjectsTable mProjectsTable;
    ProjectsListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.projects_list);

        // Action bar Up affordance
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mProjectsListView = (ExpandableListView) findViewById(R.id.projects);

        // Set the adapter for the list view
        mListAdapter = new ProjectsListAdapter(this,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { ProjectsTable.C_ROOT }, // Name for group layouts
                new int[] { android.R.id.text1 },
                android.R.layout.simple_expandable_list_item_1,
                new String[] { ProjectsTable.C_SUBPROJECT }, // Name for child layouts
                new int[] { android.R.id.text1 });
        mProjectsListView.setAdapter(mListAdapter);

        mProjectsListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id)
            {
                TextView tv = (TextView) v;
                String subgroup = tv.getText().toString();
                String root = ""; // TODO get root project
                String project = root + "/" + subgroup;
                Prefs.setCurrentProject(ProjectsList.this, project);
                return true;
            }
        });

        mProjectsTable = DatabaseFactory.getDatabase(this).getProjectsTable();

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new ProjectsListLoader(this, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mListAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mListAdapter.changeCursor(null);
    }


    class ProjectsListLoader extends AsyncTaskLoader<Cursor> {

        GerritTask mProjectListFetcher;

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
            Cursor c = mProjectsTable.getProjects();

            /* This needs to block as this will query database, find there are
             *  no results, fetch some and re-query the database before the results
             *  are inserted. */
            if (c.isAfterLast())
            {
                fetchProjects();

                // Hope the Status is PENDING initially.
                while (mProjectListFetcher.getStatus() != AsyncTask.Status.FINISHED) {
                    try { Thread.sleep(100); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
            else return c;

            return mProjectsTable.getProjects();
        }

        private void fetchProjects() {
            mProjectListFetcher = new GerritTask(ProjectsList.this)
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
                        mProjectsTable.insertProjects(projects);

                        /* TODO: Wake up the main thread of this AsyncTaskLoader as the
                         *  results are in */

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            mProjectListFetcher.execute(Prefs.getCurrentGerrit(ProjectsList.this) + "projects/?d");
        }
    }


    class ProjectsListAdapter extends SimpleCursorTreeAdapter {
        // Note that the constructor does not take a Cursor. This is done to avoid
        // querying the database on the main thread.
        public ProjectsListAdapter(Context context,
                                   int groupLayout,
                                   String[] groupFrom,
                                   int[] groupTo,
                                   int childLayout,
                                   String[] childFrom,
                                   int[] childTo) {
            super(context, null, groupLayout, groupFrom, groupTo, childLayout,
                    childFrom, childTo);
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor)
        {
            /* (TODO) Note: It is your responsibility to manage this Cursor through the Activity lifecycle.
             * It is a good idea to use Activity.managedQuery which will handle this for you.
             * In some situations, the adapter will deactivate the Cursor on its own, but this will
             *  not always be the case, so please ensure the Cursor is properly managed.
             */

            // TODO: The column index will always be constant here
            String root = groupCursor.getString(groupCursor.getColumnIndex(ProjectsTable.C_ROOT));
            return mProjectsTable.getSubprojects(root);
        }
    }
}
