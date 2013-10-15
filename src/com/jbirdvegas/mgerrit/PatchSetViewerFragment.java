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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fima.cardsui.views.CardUI;
import com.jbirdvegas.mgerrit.cards.PatchSetChangesCard;
import com.jbirdvegas.mgerrit.cards.PatchSetCommentsCard;
import com.jbirdvegas.mgerrit.cards.PatchSetMessageCard;
import com.jbirdvegas.mgerrit.cards.PatchSetPropertiesCard;
import com.jbirdvegas.mgerrit.cards.PatchSetReviewersCard;
import com.jbirdvegas.mgerrit.database.SelectedChange;
import com.jbirdvegas.mgerrit.message.StatusSelected;
import com.jbirdvegas.mgerrit.objects.CommitterObject;
import com.jbirdvegas.mgerrit.objects.GerritURL;
import com.jbirdvegas.mgerrit.objects.JSONCommit;
import com.jbirdvegas.mgerrit.tasks.GerritTask;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Class handles populating the screen with several
 * cards each giving more information about the patchset
 * <p/>
 * All cards are located at jbirdvegas.mgerrit.cards.*
 */
public class PatchSetViewerFragment extends Fragment {
    private static final String TAG = PatchSetViewerFragment.class.getSimpleName();

    private CardUI mCardsUI;
    private RequestQueue mRequestQueue;
    private Activity mParent;
    private Context mContext;

    private View mCurrentFragment;
    private GerritURL mUrl;
    private String mSelectedChange;
    private String mStatus;

    public static final String CHANGE_ID = "changeID";
    public static final String STATUS = "queryStatus";

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mStatus = intent.getStringExtra(StatusSelected.STATUS);
            loadChange();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.commit_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParent = this.getActivity();
        mContext = mParent.getApplicationContext();

        if (getArguments() != null) {
            mSelectedChange = getArguments().getString(CHANGE_ID);
            mStatus = getArguments().getString(STATUS);
            SelectedChange.setSelectedChange(mContext, mSelectedChange, mStatus);
        } else {
            mStatus = TheApplication.getLastSelectedStatus();
        }
    }

    private void init()
    {
        mCurrentFragment = this.getView();

        mCardsUI = (CardUI) mCurrentFragment.findViewById(R.id.commit_cards);
        mRequestQueue = Volley.newRequestQueue(mParent);

        mUrl = new GerritURL();
        loadChange();
    }

    private void executeGerritTask(final String query) {
        new GerritTask(mParent) {
            @Override
            public void onJSONResult(String s) {
                try {
                    addCards(mCardsUI,
                            new JSONCommit(
                                    new JSONArray(s).getJSONObject(0),
                                    mContext));
                } catch (JSONException e) {
                    Log.d(TAG, "Response from "
                            + query + " could not be parsed into cards :(", e);
                }
            }
        }.execute(query);
    }

    private void addCards(CardUI ui, JSONCommit jsonCommit) {
        // Properties card
        Log.d(TAG, "Loading Properties Card...");
        ui.addCard(new PatchSetPropertiesCard(jsonCommit, this, mRequestQueue, mParent), true);

        // Message card
        Log.d(TAG, "Loading Message Card...");
        ui.addCard(new PatchSetMessageCard(jsonCommit), true);

        // Changed files card
        if (jsonCommit.getChangedFiles() != null
                && !jsonCommit.getChangedFiles().isEmpty()) {
            Log.d(TAG, "Loading Changes Card...");
            ui.addCard(new PatchSetChangesCard(jsonCommit, mParent), true);
        }

        // Code reviewers card
        if (jsonCommit.getCodeReviewers() != null
                && !jsonCommit.getCodeReviewers().isEmpty()) {
            Log.d(TAG, "Loading Reviewers Card...");
            ui.addCard(new PatchSetReviewersCard(jsonCommit, mRequestQueue, mParent), true);
        } else {
            Log.d(TAG, "No reviewers found! Not adding reviewers card");
        }

        // Comments Card
        if (jsonCommit.getMessagesList() != null
                && !jsonCommit.getMessagesList().isEmpty()) {
            Log.d(TAG, "Loading Comments Card...");
            ui.addCard(new PatchSetCommentsCard(jsonCommit, this, mRequestQueue), true);
        } else {
            Log.d(TAG, "No commit comments found! Not adding comments card");
        }
    }

    public void setSelectedChange(String changeID) {
        if (changeID == null || changeID.length() < 0) {
            return; // Invalid changeID
        } else if (changeID == mSelectedChange) {
            return; // Same change selected, no need to do anything.
        }

        SelectedChange.setSelectedChange(mContext, changeID);
        loadChange(changeID);
    }

    private void loadChange(String changeID) {
        this.mSelectedChange = changeID;
        mUrl.setChangeID(mSelectedChange);
        mUrl.requestChangeDetail(true);
        mCardsUI.clearCards();
        executeGerritTask(mUrl.toString());
    }

    private void loadChange() {
        if (mStatus == null) {
            // Without the status we cannot find a changeid to load data for
            return;
        }

        String changeID = SelectedChange.getSelectedChange(mContext, mStatus);
        if (changeID == null) {
            // No previously selected change id for this status, cannot load any data
            return;
        }

        loadChange(changeID);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mParent).registerReceiver(mStatusReceiver,
                new IntentFilter(StatusSelected.TYPE));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mParent).unregisterReceiver(mStatusReceiver);
    }

    /*
    Possible cards

    --Patch Set--
    Select patchset number to display in these cards
    -------------

    --Times Card--
    Original upload time
    Most recent update
    --------------

    --Inline comments Card?--
    Show all comments inlined on code view pages
    **may be kind of pointless without context of surrounding code**
    * maybe a webview for each if possible? *
    -------------------------

     */

    private CommitterObject committerObject = null;

    public void registerViewForContextMenu(View view) {
        registerForContextMenu(view);
    }

    private static final int OWNER = 0;
    private static final int REVIEWER = 1;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        committerObject = (CommitterObject) v.getTag();
        menu.setHeaderTitle(R.string.developers_role);
        menu.add(0, v.getId(), OWNER, v.getContext().getString(R.string.context_menu_owner));
        menu.add(0, v.getId(), REVIEWER, v.getContext().getString(R.string.context_menu_reviewer));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String tab = null;
        switch (item.getOrder()) {
            case OWNER:
                tab = CardsFragment.KEY_OWNER;
                break;
            case REVIEWER:
                tab = CardsFragment.KEY_REVIEWER;
        }
        committerObject.setState(tab);
        Intent intent = new Intent(mParent, ReviewTab.class);
        intent.putExtra(CardsFragment.KEY_DEVELOPER, committerObject);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        return true;
    }
}