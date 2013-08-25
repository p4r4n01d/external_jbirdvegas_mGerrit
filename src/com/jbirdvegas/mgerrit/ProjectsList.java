package com.jbirdvegas.mgerrit;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.jbirdvegas.mgerrit.adapters.ProjectsListAdapter;
import com.jbirdvegas.mgerrit.database.DatabaseFactory;
import com.jbirdvegas.mgerrit.database.ProjectsTable;
import com.jbirdvegas.mgerrit.tasks.ProjectsListLoader;

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
                String root = mListAdapter.getGroupName(groupPosition);
                String project = root + "/" + subgroup;
                Prefs.setCurrentProject(ProjectsList.this, project);
                ProjectsList.this.finish();
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

    /* We load the data on a seperate thread (AsyncTaskLoader) but what to do
     *  on the main thread. Probably best to block (with a alert dialog) like
     *  the old implementation did, then unblock once all the data has been
     *  downloaded and we can start binding data to views.
     */

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new ProjectsListLoader(this, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mListAdapter.changeCursor(cursor);
        // TODO: Find the current project in the list and scroll to it.
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mListAdapter.changeCursor(null);
    }
}
