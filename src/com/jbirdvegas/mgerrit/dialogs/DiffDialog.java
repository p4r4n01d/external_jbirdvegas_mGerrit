package com.jbirdvegas.mgerrit.dialogs;

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

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.jbirdvegas.mgerrit.Prefs;
import com.jbirdvegas.mgerrit.R;
import com.jbirdvegas.mgerrit.tasks.ZipRequest;
import com.jbirdvegas.mgerrit.views.DiffTextView;

import java.util.regex.Pattern;

public class DiffDialog extends AlertDialog.Builder {

    private String mLineSplit = System.getProperty("line.separator");
    private DiffTextView mDiffTextView;
    private DiffFailCallback mDiffFailCallback;

    private final String mFilePath;

    public interface DiffFailCallback {
        public void killDialogAndErrorOut(Exception e);
    }

    public DiffDialog(Context context, Integer changeNumber, Integer patchSetNumber,
                      String filePath) {
        super(context);
        context.setTheme(Prefs.getCurrentThemeID(context));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.diff_dialog, null);
        setView(rootView);

        mFilePath = filePath;
        mDiffTextView = (DiffTextView) rootView.findViewById(R.id.diff_view_diff);

        ZipRequest request = new ZipRequest(context, changeNumber, patchSetNumber, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s != null) setTextView(s);
                else if (mDiffFailCallback != null) {
                    mDiffFailCallback.killDialogAndErrorOut(
                            new NullPointerException("Failed to get diff!"));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDiffTextView.setText("Failed to load diff :(");
            }
        }
        );
        Volley.newRequestQueue(context).add(request);
    }

    public DiffDialog addExceptionCallback(DiffFailCallback failCallback) {
        mDiffFailCallback = failCallback;
        return this;
    }

    private void setTextView(String result) {
        Pattern pattern = Pattern.compile("\\Qdiff --git \\E");
        String[] filesChanged = pattern.split(result);
        StringBuilder builder = new StringBuilder(0);
        for (String change : filesChanged) {
            String concat;
            int index = change.lastIndexOf(mFilePath);
            if (index < 0) continue;

            concat = change.substring(2, index).trim().split(" ", 2)[0];
            if (concat.equals(mFilePath)) {
                change.replaceAll("\n", mLineSplit);
                builder.append(change);
            }
        }
        if (builder.length() == 0) {
            builder.append("Diff not found!");
        }
        // reset text size to default
        mDiffTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_DeviceDefault_Small);
        mDiffTextView.setTypeface(Typeface.MONOSPACE);
        // rebuild text; required to respect the \n
        mDiffTextView.setDiffText(builder.toString());
    }
}
