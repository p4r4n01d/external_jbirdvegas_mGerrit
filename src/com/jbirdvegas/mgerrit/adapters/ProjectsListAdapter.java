package com.jbirdvegas.mgerrit.adapters;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorTreeAdapter;

import com.jbirdvegas.mgerrit.database.DatabaseFactory;
import com.jbirdvegas.mgerrit.database.ProjectsTable;

public class ProjectsListAdapter extends SimpleCursorTreeAdapter
{
    private Context mContext;

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
        mContext = context;
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
        return DatabaseFactory.getDatabase(mContext).getProjectsTable().getSubprojects(root);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true; // All children (projects) are selectable
    }

    public String getGroupName(int groupPosition) {
        Cursor c = getGroup(groupPosition);
        return c.getString(c.getColumnIndex(ProjectsTable.C_ROOT));
    }
}
