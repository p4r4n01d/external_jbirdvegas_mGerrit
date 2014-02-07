package com.jbirdvegas.mgerrit.objects;

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

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.jbirdvegas.mgerrit.R;
import com.jbirdvegas.mgerrit.cards.PatchSetChangesCard;

public class DiffActionBar implements ActionMode.Callback {

    private final boolean mShowViewer;
    private ActionMode mActionMode;
    private final Context mContext;

    public DiffActionBar(Context context, boolean diffSupported) {
        mContext = context;
        mShowViewer = diffSupported;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.changed_file_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        final MenuItem viewerItem = menu.findItem(R.id.menu_diff_internal);
        final boolean enabled = viewerItem.isEnabled();
        viewerItem.setEnabled(mShowViewer);
        return enabled != mShowViewer;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        TagHolder holder = (TagHolder) mode.getTag();

        switch (item.getItemId()) {
            case R.id.menu_diff_internal:
                PatchSetChangesCard.launchDiffViewer(mContext, holder.changeNumber,
                        holder.patchset, holder.filePath);
                break;
            case R.id.menu_diff_external:
                PatchSetChangesCard.launchDiffInBrowser(mContext, holder.changeNumber,
                        holder.patchset, holder.filePath);
                break;
            default:
                return false;
        }
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
    }

    public void setActionMode(ActionMode mActionMode) {
        this.mActionMode = mActionMode;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public static class TagHolder {
        public final int position;
        public final Integer changeNumber;
        public final String filePath;
        public final Integer patchset;

        public TagHolder(View view, int pos) {
            position = pos;
            changeNumber = (Integer) view.getTag(R.id.changeNumber);
            filePath = (String) view.getTag(R.id.filePath);
            patchset = (Integer) view.getTag(R.id.patchSetNumber);
        }
    }
}
