package com.jbirdvegas.mgerrit.cards;

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
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.jbirdvegas.mgerrit.GerritControllerActivity;
import com.jbirdvegas.mgerrit.Prefs;
import com.jbirdvegas.mgerrit.R;
import com.jbirdvegas.mgerrit.database.UserChanges;
import com.jbirdvegas.mgerrit.helpers.GravatarHelper;

public class CommitCardBinder implements SimpleCursorAdapter.ViewBinder {
    private final RequestQueue mRequestQuery;
    private final Context mContext;
    private final int mGreen;
    private final int mRed;

    // Cursor indices
    private Integer useremail_index;
    private Integer userid_index;

    public CommitCardBinder(Context context, RequestQueue requestQueue) {
        this.mRequestQuery = requestQueue;
        this.mContext = context;

        this.mGreen = context.getResources().getColor(R.color.text_green);
        this.mRed = context.getResources().getColor(R.color.text_red);
    }

    @Override
    public boolean setViewValue(View view, final Cursor cursor, final int columnIndex) {
        // These indicies will not change regardless of the view
        if (useremail_index == null) {
            useremail_index = cursor.getColumnIndex(UserChanges.C_EMAIL);
        }
        if (userid_index == null) {
            userid_index = cursor.getColumnIndex(UserChanges.C_USER_ID);
        }

        if (view.getId() == R.id.commit_card_commit_owner) {
            TextView owner = (TextView) view;
            owner.setText(cursor.getString(columnIndex));
            owner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Prefs.setTrackingUser(mContext, cursor.getInt(userid_index));
                }
            });
            GravatarHelper.attachGravatarToTextView(owner,
                    cursor.getString(useremail_index),
                    mRequestQuery);
        } else if (view.getId() == R.id.commit_card_project_name) {
            TextView project = (TextView) view;
            project.setText(cursor.getString(columnIndex));
            project.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Prefs.setCurrentProject(mContext, cursor.getString(columnIndex));
                }
            });
        } else if (view.getId() == R.id.commit_card_commit_status) {
            TextView status = (TextView) view;
            String statusText = cursor.getString(columnIndex);
            status.setText(statusText);
            if (statusText.equals("MERGED")) {
                status.setTextColor(mGreen);
            } else if (statusText.equals("ABANDONED")) {
                status.setTextColor(mRed);
            }
        } else {
            return false;
        }
        return true;
    }

    // When the cursor changes, these may not be valid
    public void onCursorChanged() {
        useremail_index = null;
        userid_index = null;
    }
}
