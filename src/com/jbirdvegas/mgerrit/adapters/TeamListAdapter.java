package com.jbirdvegas.mgerrit.adapters;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.jbirdvegas.mgerrit.R;
import com.jbirdvegas.mgerrit.objects.GerritDetails;

import java.util.List;

public class TeamListAdapter extends ArrayAdapter<GerritDetails> {

    private LayoutInflater mInflator;
    private int mSelectedPos;
    private ViewHolder gerritPlaceholder;

    List<GerritDetails> data;

    public TeamListAdapter(Context context, List<GerritDetails> objects) {
        super(context, R.layout.gerrit_row, objects);
        mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        data = objects;
    }

    /**
     * @Override
     * Get the data item associated with the specified position in the data set.
     * @param position Position of the item whose data we want within the adapter's data set.
     * @return The data at the specified position, or null if there is no data
     *  bound to the position
     */
    public GerritDetails getItem(int position) {
        // Override this so we don't get out of range exceptions
        if (position < 0 || position > data.size()) {
            return null;
        }
        return data.get(position);
    }

    @Override
    public int getCount() {
        // We want one more view at the end of the list to add another item
        return data.size() + 1;
    }

    public GerritDetails getSelectedGerrit() {
        if (mSelectedPos < data.size()) {
            return data.get(mSelectedPos);
        }
        String name = gerritPlaceholder.gerritEditName.getText().toString();
        String url = gerritPlaceholder.gerritEditUrl.getText().toString();
        return new GerritDetails(name, url);
    }

    public int setSelectedGerrit(String selectedGerritUrl) {
        for (int i = 0; i < data.size(); i++) {
            GerritDetails gerrit = data.get(i);
            if (selectedGerritUrl.equals(gerrit.getGerritUrl())) {
                mSelectedPos = i;
                break;
            }
        }
        return mSelectedPos;
    }

    public int setSelectedGerrit(int position) {
        this.mSelectedPos = position;
        notifyDataSetChanged();
        return mSelectedPos;
    }

    @Override
    public int getItemViewType(int position) {
        return position < data.size() ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2; // Two different view layouts supported
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        String gerritUrl;

        // Row specific handling
        if (getItemViewType(position) == 0) {
            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.gerrit_row, null);
            }

            GerritDetails rowData = getItem(position);

            viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.gerritName = (CheckedTextView) convertView.findViewById(R.id.txtGerritName);
                viewHolder.gerritUrl = (TextView) convertView.findViewById(R.id.txtGerritURL);
                convertView.setTag(viewHolder);
            }

            gerritUrl = rowData.getGerritUrl();
            viewHolder.gerritName.setText(rowData.getGerritName());
            viewHolder.gerritUrl.setText(gerritUrl);

            viewHolder.gerritName.setChecked(position == mSelectedPos);

        } else {
            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.add_team_row, null);
            }

            viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.gerritEditName = (EditText) convertView.findViewById(R.id.add_team_name_edittext);
                viewHolder.gerritEditUrl = (EditText) convertView.findViewById(R.id.add_team_url_edittext);
                viewHolder.gerritIsSelected = (RadioButton) convertView.findViewById(R.id.chkGerritSelected);
                convertView.setTag(viewHolder);
            }
            gerritPlaceholder = viewHolder;
            viewHolder.gerritIsSelected.setChecked(mSelectedPos == position);

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TeamListAdapter.this.setSelectedGerrit(position);
                }
            };

            viewHolder.gerritEditName.setOnClickListener(clickListener);
            viewHolder.gerritEditUrl.setOnClickListener(clickListener);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private static class ViewHolder {
        CheckedTextView gerritName;
        TextView gerritUrl;
        RadioButton gerritIsSelected;
        EditText gerritEditName;
        EditText gerritEditUrl;
    }

}