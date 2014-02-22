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
import android.content.Intent;

import com.jbirdvegas.mgerrit.R;
import com.jbirdvegas.mgerrit.database.Changes;
import com.jbirdvegas.mgerrit.database.DatabaseTable;
import com.jbirdvegas.mgerrit.database.SyncTime;
import com.jbirdvegas.mgerrit.database.UserChanges;
import com.jbirdvegas.mgerrit.objects.GerritURL;
import com.jbirdvegas.mgerrit.objects.JSONCommit;

import java.util.Arrays;

class ChangeListProcessor extends SyncProcessor<JSONCommit[]> {

    GerritService.Direction mDirection;

    ChangeListProcessor(Context context, GerritURL url) {
        super(context, url);
    }

    @Override
    void insert(JSONCommit[] commits) {
        if (commits.length > 0) {
            UserChanges.insertCommits(getContext(), Arrays.asList(commits));
        }
    }

    @Override
    boolean isSyncRequired(Context context, Intent intent) {
        String direction = intent.getStringExtra(GerritService.CHANGES_LIST_DIRECTION);
        if (direction != null) {
            mDirection = GerritService.Direction.valueOf(direction);
            if (mDirection == GerritService.Direction.Older) {
                return true;
            }
        } else {
            mDirection = GerritService.Direction.Newer;
        }

        long syncInterval = context.getResources().getInteger(R.integer.changes_sync_interval);
        long lastSync = SyncTime.getValueForQuery(context, SyncTime.CHANGES_LIST_SYNC_TIME, getQuery());
        boolean sync = isInSyncInterval(syncInterval, lastSync);
        if (!sync) return true;

        // Better just make sure that there are changes in the database
        return DatabaseTable.isEmpty(context, Changes.CONTENT_URI);
    }

    @Override
    Class<JSONCommit[]> getType() {
        return JSONCommit[].class;
    }

    @Override
    void doPostProcess(JSONCommit[] data) {
        if (mDirection != GerritService.Direction.Older) {
            SyncTime.setValue(mContext, SyncTime.CHANGES_LIST_SYNC_TIME,
                    System.currentTimeMillis(), getQuery());
        }
    }

    @Override
    int count(JSONCommit[] data) {
        if (data != null) return data.length;
        else return 0;
    }
}
