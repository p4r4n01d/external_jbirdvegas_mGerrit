package com.jbirdvegas.mgerrit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ExpandableListView;

public class ProjectsList extends Activity
{
    ExpandableListView mProjectsListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.projects_list);

        // Action bar Up affordance
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mProjectsListView = (ExpandableListView) findViewById(R.id.projects);
        // TODO: Set adapter for mProjectsListView
        // TODO: Implement onChildClickListener for clicking on rows in mProjectsListView
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
}
