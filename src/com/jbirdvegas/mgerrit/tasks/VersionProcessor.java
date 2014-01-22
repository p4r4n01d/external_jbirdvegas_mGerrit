package com.jbirdvegas.mgerrit.tasks;

import android.content.Context;
import android.net.Uri;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jbirdvegas.mgerrit.Prefs;
import com.jbirdvegas.mgerrit.database.Config;
import com.jbirdvegas.mgerrit.message.ErrorDuringConnection;
import com.jbirdvegas.mgerrit.message.StartingRequest;
import com.jbirdvegas.mgerrit.objects.GerritURL;

import java.net.URL;

/*
 * Copyright (C) 2014 Android Open Kang Project (AOKP)
 *  Author: Evan Conway (P4R4N01D), 2014
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
public class VersionProcessor extends SyncProcessor<String> {

    private final String mUrl;

    VersionProcessor(Context context) {
        super(context);
        String url = Prefs.getCurrentGerrit(context);
        mUrl = url + "/config/server/version";
    }

    @Override
    void insert(String data) {
        // Trim off the junk beginning and the quotes around the version number
        data = data.substring(6, data.length() - 1);
        Config.setValue(getContext(), Config.KEY_VERSION, data);
    }

    @Override
    boolean isSyncRequired() {
        return false;
    }

    @Override
    Class<String> getType() {
        return String.class;
    }

    @Override
    protected void fetchData() {

        Response.Listener<String> listener = getListener(mUrl);

        StringRequest request = new StringRequest(mUrl,
                getListener(mUrl), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // Need to make sure this is a 404 error
                if (volleyError.networkResponse.statusCode == 404) {
                    // Pretend we got a response
                    getListener(mUrl).onResponse(Config.VERSION_DEFAULT);
                }
            }
        });

        this.fetchData(mUrl, request);

        // Won't be able to actually get JSON response back as it
        //  is improperly formed (junk at start), but requesting raw text and
        //  trimming it should be fine.

        RequestQueue queue = Volley.newRequestQueue(mContext);
        new StartingRequest(mContext, mUrl).sendUpdateMessage();


        queue.add(request);
    }
}