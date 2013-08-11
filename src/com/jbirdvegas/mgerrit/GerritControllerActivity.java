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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jbirdvegas.mgerrit.helpers.GerritTeamsHelper;
import com.jbirdvegas.mgerrit.listeners.MyTabListener;
import com.jbirdvegas.mgerrit.objects.CommitterObject;
import com.jbirdvegas.mgerrit.objects.GooFileObject;
import com.jbirdvegas.mgerrit.objects.JSONCommit;
import com.jbirdvegas.mgerrit.objects.Project;
import com.jbirdvegas.mgerrit.tasks.GerritTask;
import com.jbirdvegas.mgerrit.widgets.AddTeamView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GerritControllerActivity extends FragmentActivity {

    private static final String TAG = GerritControllerActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private CommitterObject mCommitterObject;
    private String mProject;
    private GooFileObject mChangeLogStart;
    private GooFileObject mChangeLogStop;
    private ActionBar mActionBar;

    private ReviewTab mReviewTab;
    private MergedTab mMergedTab;
    private AbandonedTab mAbandonedTab;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (!CardsFragment.mSkipStalking) {
            try {
                mCommitterObject = getIntent()
                        .getExtras()
                        .getParcelable(CardsFragment.KEY_DEVELOPER);
            } catch (NullPointerException npe) {
                // non author specific view
                // use default
            }
        }

        try {
            mProject = getIntent().getStringExtra(JSONCommit.KEY_PROJECT);
        } catch (NullPointerException npe) {
            // not following one project
        }

        try {
            mChangeLogStart = getIntent()
                    .getExtras()
                    .getParcelable(AOKPChangelog.KEY_CHANGELOG_START);
            mChangeLogStop = getIntent()
                    .getExtras()
                    .getParcelable(AOKPChangelog.KEY_CHANGELOG_STOP);
        } catch (NullPointerException npe) {
            Log.d(TAG, "Changelog was null");
        }
        // Setup tabs //
        setupTabs();
    }

    private void createTabs()
    {
        mReviewTab = new ReviewTab();
        mMergedTab = new MergedTab();
        mAbandonedTab = new AbandonedTab();
    }

    /** MUST BE CALLED ON MAIN THREAD */
    private void setupTabs()
    {
        createTabs();

        // setup action bar for tabs
        mActionBar = getActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setDisplayShowTitleEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        /** The {@link ViewPager} that will host the section contents. */
        ViewPager viewPager;
        viewPager = (ViewPager) findViewById(R.id.tabs);
        viewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        viewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mActionBar.setSelectedNavigationItem(position);
                    }
                });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            mActionBar.addTab(mActionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(new MyTabListener(viewPager, this)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gerrit_instances_menu, menu);
        return true;
    }

    private AlertDialog alertDialog = null;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                Intent intent = new Intent(this, Prefs.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                return true;
            case R.id.menu_help:
                Builder builder = new Builder(this);
                builder.setTitle(R.string.menu_help);
                LayoutInflater layoutInflater = this.getLayoutInflater();
                View dialog = layoutInflater.inflate(R.layout.dialog_help, null);
                builder.setView(dialog);
                builder.create();
                builder.show();
                return true;
            case R.id.menu_refresh:
                refreshTab();
                return true;
            case R.id.menu_team_instance:
                showGerritDialog();
                return true;
            case R.id.menu_projects:
                getProjectsList();
                return true;
            case R.id.menu_changelog:
                Intent changelog = new Intent(this,
                        AOKPChangelog.class);
                changelog.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(changelog);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getProjectsList() {
        new GerritTask(this) {
            @Override
            public void onJSONResult(String jsonString) {
                try {
                    JSONObject projectsJson = new JSONObject(jsonString);
                    Iterator stringIterator = projectsJson.keys();
                    List<Project> projectLinkedList
                            = new LinkedList<Project>();
                    while (stringIterator.hasNext()) {
                        String path = (String) stringIterator.next();
                        JSONObject projJson = projectsJson.getJSONObject(path);
                        String kind = projJson.getString(JSONCommit.KEY_KIND);
                        String id = projJson.getString(JSONCommit.KEY_ID);
                        projectLinkedList.add(Project.getInstance(path, kind, id));
                    }
                    Collections.sort(projectLinkedList);
                    ListView projectsList = new ListView(getThis());
                    projectsList.setAdapter(new ArrayAdapter<Object>(getThis(),
                            android.R.layout.simple_list_item_single_choice,
                            projectLinkedList.toArray()));
                    projectsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Project project = (Project) adapterView.getItemAtPosition(i);
                            mProject = project.getmPath();
                            if (alertDialog != null) {
                                alertDialog.dismiss();
                                alertDialog = null;
                            }
                            refreshTab();
                        }
                    });
                    Builder projectsBuilder = new Builder(getThis());
                    projectsBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    projectsBuilder.setView(projectsList);
                    AlertDialog alertDialog1 = projectsBuilder.create();
                    alertDialog = alertDialog1;
                    alertDialog1.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(Prefs.getCurrentGerrit(this) + "projects/?d");
    }

    // TODO: Refresh the tabs on a change
    private void showGerritDialog() {
        final Builder teamBuilder = new Builder(this);
        ListView instances = new ListView(this);
        Resources res = getResources();

        final ArrayList <String> teams = new ArrayList<String>(0);
        String[] gerritNames = res.getStringArray(R.array.gerrit_names);
        Collections.addAll(teams, gerritNames);

        final ArrayList<String> urls = new ArrayList<String>(0);
        String[] gerritWeb = res.getStringArray(R.array.gerrit_webaddresses);
        Collections.addAll(urls, gerritWeb);

        GerritTeamsHelper teamsHelper = new GerritTeamsHelper();
        teams.addAll(teamsHelper.getGerritNamesList());
        urls.addAll(teamsHelper.getGerritUrlsList());

        final ArrayAdapter <String> instanceAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                teams);
        instances.setAdapter(instanceAdapter);
        instances.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Prefs.setCurrentGerrit(view.getContext(), urls.get(i));
                    // refresh the screen and go straight to default "Review tab"
                    refreshTab();
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                        alertDialog = null;
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {

                }
            }
        });
        instances.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // on long click delete the file and refresh the list
                File target = new File(GerritTeamsHelper.mExternalCacheDir + "/" + teams.get(i));
                boolean success = target.delete();
                Log.v(TAG, "Attempt to delete: " + target.getAbsolutePath()
                        + " was " + success);
                if (!success) {
                    Log.v(TAG, "Files present:" + Arrays.toString(GerritTeamsHelper.mExternalCacheDir.list()));
                }
                teams.remove(i);
                urls.remove(i);
                instanceAdapter.notifyDataSetChanged();
                return success;
            }
        });
        teamBuilder.setView(instances);
        teamBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        teamBuilder.setPositiveButton(R.string.add_gerrit_team,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog addTeamDialog = new Builder(teamBuilder.getContext())
                        .setTitle(R.string.add_gerrit_team)
                        .setIcon(android.R.drawable.ic_input_add)
                        .create();
                AddTeamView.RefreshCallback callback =
                        new AddTeamView.RefreshCallback() {
                    @Override
                    public void refreshScreenCallback() {
                        refreshTab();
                    }
                };
                AddTeamView addTeamView = new AddTeamView(
                        teamBuilder.getContext(),
                        addTeamDialog);
                addTeamView.addRefreshScreenCallback(callback);
                addTeamDialog.setView(addTeamView.getView());
                addTeamDialog.show();
            }
        });
        this.alertDialog = teamBuilder.create();
        this.alertDialog.show();
    }

    private Activity getThis() {
        return this;
    }

    private void refreshTab() {
        mSectionsPagerAdapter.getCurrentFragment().refresh();
    }

    public CommitterObject getCommitterObject() { return mCommitterObject; }
    public void clearCommitterObject() { mCommitterObject = null; }

    public String getProject() { return mProject; }
    public void clearProject() { mProject = ""; }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding
     * to one of the sections/tabs/pages.
     *
     * Class not used.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        int mPosition;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        /** Called to instantiate the fragment for the given page. */
        public Fragment getItem(int position) {

            mPosition = position;

            switch (position)
            {
                case 0: return mReviewTab;
                case 1: return mMergedTab;
                case 2: return mAbandonedTab;
                default: return null;
            }
        }

        @Override
        /** Return the number of views available. */
        public int getCount() { return 3; }

        public int getPosition() { return mPosition; }

        public CardsFragment getCurrentFragment() {
            return (CardsFragment)getItem(mPosition);
        }

        @Override
        /** Called by the ViewPager to obtain a title string to describe
         *  the specified page. */
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.reviewable);
                case 1: return getString(R.string.merged);
                case 2: return getString(R.string.abandoned);
            }
            return null;
        }
    }
}