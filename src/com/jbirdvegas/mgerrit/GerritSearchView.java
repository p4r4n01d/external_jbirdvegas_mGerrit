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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import com.jbirdvegas.mgerrit.search.OwnerSearch;
import com.jbirdvegas.mgerrit.search.ProjectSearch;
import com.jbirdvegas.mgerrit.search.SearchKeyword;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GerritSearchView extends SearchView
        implements SearchView.OnQueryTextListener {

    private static final String TAG = "GerrritSearchView";
    Context mContext;

    public GerritSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnQueryTextListener(this);
        setupCancelButton();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Set<SearchKeyword> tokens = constructTokens(query);
        if (tokens == null) {
            Log.w(TAG, "Could not process query: " + query);
        } else {
            // If there is no project keyword in the query, it should be cleared
            if (SearchKeyword.findKeyword(tokens, ProjectSearch.class) < 0 &&
                    !Prefs.getCurrentProject(mContext).isEmpty()) {
                Prefs.setCurrentProject(mContext, null);
            }

            // If there is no owner keyword in the query, it should be cleared
            if (SearchKeyword.findKeyword(tokens, OwnerSearch.class) < 0 &&
                    Prefs.getTrackingUser(mContext) != null) {
                Prefs.clearTrackingUser(mContext);
            }

            // Pass this on to the current CardsFragment instance
            if (!processTokens(tokens)) {
                Log.w(TAG, "Could not process query: " + query);
            }
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Handled when the search is submitted instead.
        if (newText.isEmpty()) {
            onSearchQueryCleared();
            // This should be called last and should always be processable
            processTokens(null);
        } else setVisibility(VISIBLE);
        return false;
    }

    /**
     * Set the search query. This will construct the SQL query and restart
     *  the loader to perform the query
     * @param query The search query text
     */
    public Set<SearchKeyword> constructTokens(String query) {
        // Clear any previous searches that where made
        if (query == null || query.isEmpty()) {
            return new HashSet<>();
        }

        return SearchKeyword.constructTokens(query);
    }

    // Additional logic to be run when the search query is empty
    private void onSearchQueryCleared() {
        Prefs.setCurrentProject(mContext, null);
        Prefs.clearTrackingUser(mContext);
    }

    public boolean processTokens(Set<SearchKeyword> tokens) {
        Bundle bundle = new Bundle();

        if (tokens != null && !tokens.isEmpty()) {
            String where = SearchKeyword.constructDbSearchQuery(tokens);
            if (where != null && !where.isEmpty()) {
                ArrayList<String> bindArgs = new ArrayList<>();
                for (SearchKeyword token : tokens) {
                    bindArgs.addAll(Arrays.asList(token.getEscapeArgument()));
                }

                bundle.putString("WHERE", where);
                bundle.putStringArrayList("BIND_ARGS", bindArgs);
            } else {
                return false;
            }
        }
        Intent intent = new Intent(CardsFragment.SEARCH_QUERY);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        return true;
    }

    private void setupCancelButton() {
        try {
            Field searchField = SearchView.class.getDeclaredField("mCloseButton");
            searchField.setAccessible(true);
            ImageView closeBtn = (ImageView) searchField.get(this);
            closeBtn.setVisibility(VISIBLE);
            closeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleVisibility();
                }
            });
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void toggleVisibility() {
        int visibility = getVisibility();
        if (visibility == View.GONE) {
            setVisibility(View.VISIBLE);
            requestFocus();
        }
        else setVisibility(View.GONE);
    }

    @Override
    public void setVisibility(int visibility) {
        String query = getQuery().toString();
        if (!query.isEmpty() && visibility == GONE) setQuery("", true);
        super.setVisibility(visibility);
        setIconified(visibility == GONE);
    }
}
