package com.jbirdvegas.mgerrit;

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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jbirdvegas.mgerrit.adapters.TeamListAdapter;
import com.jbirdvegas.mgerrit.helpers.GerritTeamsHelper;
import com.jbirdvegas.mgerrit.objects.GerritDetails;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GerritSwitcher extends DialogFragment {

    public static final String TAG = "GerritSwitcher";

    Context mContext;
    List<GerritDetails> gerritData;
    Dialog mDialog;
    private ListView mListView;
    private TeamListAdapter mAdapter;

    ToastRunnable showToast = new ToastRunnable();
    Handler handler = new Handler();

    Pattern urlPattern = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.add_team_dialog, null))
                .setMessage(R.string.choose_gerrit_instance)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Only dismiss the dialog if a Gerrit has been selected
                        if (onCommitSelection()) {
                            dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void initialiseGerritList(Resources res) {
        final ArrayList <String> teams = new ArrayList<String>();
        Collections.addAll(teams, res.getStringArray(R.array.gerrit_names));

        final ArrayList<String> urls = new ArrayList<String>();
        Collections.addAll(urls, res.getStringArray(R.array.gerrit_webaddresses));

        GerritTeamsHelper teamsHelper = new GerritTeamsHelper();
        teams.addAll(teamsHelper.getGerritNamesList());
        urls.addAll(teamsHelper.getGerritUrlsList());

        int min = Math.min(teams.size(), urls.size());
        for (int i = 0; i < min; i++) {
            gerritData.add(new GerritDetails(teams.get(i), urls.get(i)));
        }

        // Set the current Gerrit as selected
        String currentGerrit = Prefs.getCurrentGerrit(mContext);
        mAdapter.setSelectedGerrit(currentGerrit);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getActivity();
        Resources res = mContext.getResources();
        mDialog = this.getDialog();

        initialiseGerritList(res);

        mListView = (ListView) mDialog.findViewById(R.id.lvGerritTeams);
        mAdapter = new TeamListAdapter(mContext, gerritData);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: This position might be the placeholder
                mAdapter.setSelectedGerrit(position);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // on long click delete the file and refresh the list
                GerritDetails team = gerritData.get(i);
                File target = new File(GerritTeamsHelper.mExternalCacheDir + "/" + team.getGerritName());

                boolean success = target.delete();
                StringBuilder builder = new StringBuilder().append("Attempt to delete: ")
                        .append(target.getAbsolutePath())
                        .append(" was a ");
                if (success) {
                    builder.append("success.");
                } else {
                    builder.append("failure.");
                    Log.v(TAG, "Files present:" + Arrays.toString(GerritTeamsHelper.mExternalCacheDir.list()));
                }

                gerritData.remove(i);
                mAdapter.notifyDataSetChanged();
                return success;
            }
        });
    }

    /**
     * @return Whether the new gerrit was set
     */
    private boolean onCommitSelection() {
        // TODO: If the placeholder was selected, we should make sure it is non-empty
        String gerritUrl = mAdapter.getSelectedGerrit();
        if (isUrlValid(gerritUrl)) {
            Prefs.setCurrentGerrit(mContext, gerritUrl);
            return true;
        } else {
            Toast.makeText(mContext, "The Gerrit URL set was invalid", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Validator for URLs
    private boolean isUrlValid(String url) {
        Matcher m = urlPattern.matcher(url);
        return m.matches();
    }

    private class ToastRunnable {

        private String mString;

        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, mString, Toast.LENGTH_SHORT).show();
            }
        };

        Runnable showToast(String s) {
            mString = s;
            return runnable;
        }
    }
}
