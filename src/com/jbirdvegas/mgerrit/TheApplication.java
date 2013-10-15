package com.jbirdvegas.mgerrit;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.jbirdvegas.mgerrit.database.DatabaseFactory;
import com.jbirdvegas.mgerrit.message.StatusSelected;
import com.jbirdvegas.mgerrit.objects.GerritURL;
import com.jbirdvegas.mgerrit.objects.JSONCommit;

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
public class TheApplication extends Application
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mPrefs;

    public static final String PREF_CHANGE_TYPE = "Preference Changed";
    public static final String PREF_CHANGE_KEY = "Preference Key";

    // This should be set to the status corresponding to the initially selected tab
    private static String sLastSelectedStatus = JSONCommit.Status.NEW.toString();

    private static final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sLastSelectedStatus = intent.getStringExtra(StatusSelected.STATUS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mStatusReceiver,
                new IntentFilter(StatusSelected.TYPE));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusReceiver);
    }

    /**
     * Callback to be invoked when the Gerrit instance changes in the
     * sharedPreferences
     * @param newGerrit The URL to the new Gerrit instance
     */
    public void onGerritChanged(String newGerrit)
    {
        GerritURL.setGerrit(newGerrit);
        DatabaseFactory.changeGerrit(this, newGerrit);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Prefs.GERRIT_KEY)) onGerritChanged(Prefs.getCurrentGerrit(this));
        sendPreferenceChangedMessage(key);
    }

    private void sendPreferenceChangedMessage(String key) {
        Intent intent = new Intent(PREF_CHANGE_TYPE);
        intent.putExtra(PREF_CHANGE_KEY, key);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static String getLastSelectedStatus() {
        return sLastSelectedStatus;
    }
}
