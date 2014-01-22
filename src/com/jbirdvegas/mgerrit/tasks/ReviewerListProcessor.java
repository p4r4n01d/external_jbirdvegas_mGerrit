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

import com.jbirdvegas.mgerrit.database.Reviewers;
import com.jbirdvegas.mgerrit.objects.GerritURL;
import com.jbirdvegas.mgerrit.objects.Reviewer;

class ReviewerListProcessor extends SyncProcessor<Reviewer[]> {

    private final String mUrl;
    private final String mChangeid;

    ReviewerListProcessor(Context context, GerritURL url, String changeid) {
        super(context, url);
        mUrl = url.toString();
        mChangeid = changeid;
    }

    @Override
    void insert(Reviewer[] data) {
        Reviewers.insertReviewers(getContext(), mChangeid, data);
    }

    @Override
    boolean isSyncRequired() {
        return true;
    }

    @Override
    Class<Reviewer[]> getType() {
        return Reviewer[].class;
    }
}