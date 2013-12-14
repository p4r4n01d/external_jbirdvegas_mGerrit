package com.jbirdvegas.mgerrit;

/*
 * Copyright (C) 2013 Android Open Kang Project (AOKP)
 *  Author: Jon Stanford (JBirdVegas), 2013
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

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jbirdvegas.mgerrit.adapters.GooFileArrayAdapter;
import com.jbirdvegas.mgerrit.objects.GooFileObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class AOKPChangelog extends Activity {
    private static final String TAG = AOKPChangelog.class.getSimpleName();
    public static final String KEY_CHANGELOG_START = "changelog_start";
    public static final String KEY_CHANGELOG_STOP = "changelog_stop";
    private RequestQueue mRequestQueue;
    private String query = "http://goo.im/json2&path=/devs/aokp/" + Build.DEVICE + "/nightly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(Prefs.getCurrentThemeID(this));
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.aokp_changelog);

        // Action bar Up affordance
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);
        Prefs.setCurrentGerrit(this, getResources().getStringArray(R.array.gerrit_webaddresses)[0]);
        findDates();
    }

    private void findDates() {
        // use Volley to get our packages list
        Log.d(TAG, "Calling: " + query);
        setProgressBarIndeterminateVisibility(true);

        mRequestQueue.add(
                new JsonObjectRequest(
                        Request.Method.GET,
                        query,
                        null,
                        new gooImResponseListener(),
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.e(TAG, "Failed to get recent upload dates from goo.im!", volleyError);
                                setProgressBarIndeterminateVisibility(false);
                            }
                        }));
        mRequestQueue.start();
    }

    private void setupListView(List<GooFileObject> gooFilesList) {
        ListView updatesList = (ListView) findViewById(R.id.changelog);
        final GooFileArrayAdapter gooAdapter = new GooFileArrayAdapter(this,
                R.layout.goo_files_list_item,
                gooFilesList);
        updatesList.setAdapter(gooAdapter);
        updatesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent changelog = new Intent(AOKPChangelog.this, GerritControllerActivity.class);
                changelog.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                        | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                changelog.putExtra(KEY_CHANGELOG_START, gooAdapter.getGooFilesList().get(i + 1));
                changelog.putExtra(KEY_CHANGELOG_STOP, gooAdapter.getGooFilesList().get(i));
                startActivity(changelog);
            }
        });
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

    class gooImResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            setProgressBarIndeterminateVisibility(false);

            Toast.makeText(AOKPChangelog.this,
                    R.string.please_select_update_for_range,
                    Toast.LENGTH_LONG).show();
            try {
                JSONArray result = response.getJSONArray("list");
                int resultsSize = result.length();
                List<GooFileObject> filesList = new LinkedList<>();

                for (int i = 0; resultsSize > i; i++) {
                    filesList.add(GooFileObject.getInstance(result.getJSONObject(i)));
                }
                setupListView(filesList);
            } catch (Exception e) {
                Log.e("ErrorListener", e.getLocalizedMessage());
            }
        }
    }
}
