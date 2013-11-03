package com.jbirdvegas.mgerrit.objects;

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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

public class CommitComment implements Parcelable {
    private static final String KEY_REVISION_NUMBER = "revision_number";
    private static final boolean DEBUG = false;
    private static final String TAG = CommitComment.class.getSimpleName();

    @SerializedName(CommitComment.KEY_REVISION_NUMBER)
    private int mRevisionNumber;

    @SerializedName(JSONCommit.KEY_MESSAGE)
    private String mMessage;

    @SerializedName(JSONCommit.KEY_DATE)
    private String mDate;

    @SerializedName(JSONCommit.KEY_AUTHOR)
    private CommitterObject mAuthorObject;

    @SerializedName("SONCommit.KEY_ID")
    private String mId;

    public CommitComment(JSONObject jsonObject) {
        new Gson().fromJson(jsonObject.toString(), this.getClass());
    }

    public static CommitComment getInstance(JSONObject jsonObject) {
        if (DEBUG) {
            try {
                Log.d(TAG, "CommitComment RawJSON: " + jsonObject.toString(4));
            } catch (JSONException e) {
                Log.e(TAG, "DEBUG FAILED!", e);
            }
        }
        return new CommitComment(jsonObject);
    }

    public int getRevisionNumber() {
        return mRevisionNumber;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getDate() {
        return mDate;
    }

    public CommitterObject getAuthorObject() {
        return mAuthorObject;
    }

    public String getId() {
        return mId;
    }

    public CommitComment(Parcel parcel) {
        mId = parcel.readString();
        mAuthorObject = parcel.readParcelable(CommitterObject.class.getClassLoader());
        mDate = parcel.readString();
        mMessage = parcel.readString();
        mRevisionNumber = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeParcelable(mAuthorObject, 0);
        parcel.writeString(mDate);
        parcel.writeString(mMessage);
        parcel.writeInt(mRevisionNumber);
    }
}
