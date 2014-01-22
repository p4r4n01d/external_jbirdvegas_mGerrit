package com.jbirdvegas.mgerrit.cards;

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

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jbirdvegas.mgerrit.R;
import com.jbirdvegas.mgerrit.database.UserChanges;
import com.jbirdvegas.mgerrit.helpers.EmoticonSupportHelper;

import org.jetbrains.annotations.NotNull;

public class PatchSetMessageCard implements CardBinder {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private Integer mLastUpdated_index;
    private Integer mMessage_index;

    public PatchSetMessageCard(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View setViewValue(Cursor cursor, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.patchset_message_card, null);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        if (convertView.getTag() == null) {
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        viewHolder.lastUpdate.setText(cursor.getString(mLastUpdated_index));

        String message = cursor.getString(mMessage_index);
        if (message == null || message.isEmpty()) {
            viewHolder.commitMessageTextView.setText(
                    EmoticonSupportHelper.getSmiledText(mContext, message));
        } else {
            viewHolder.commitMessageTextView.setText(
                    mContext.getString(R.string.current_revision_is_draft_message));
        }
        return convertView;
    }

    private void setIndicies(@NotNull Cursor cursor) {
        if (mLastUpdated_index == null) {
            mLastUpdated_index = cursor.getColumnIndex(UserChanges.C_UPDATED);
        }
        if (mMessage_index == null) {
            mMessage_index = cursor.getColumnIndex(UserChanges.C_MESSAGE);
        }
    }


    private static class ViewHolder {
        TextView lastUpdate;
        TextView commitMessageTextView;

        ViewHolder(View view) {
            lastUpdate = (TextView) view.findViewById(R.id.message_card_last_update);
            commitMessageTextView = (TextView) view.findViewById(R.id.message_card_message);
        }
    }
}
