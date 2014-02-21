package com.jbirdvegas.mgerrit.objects;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.jbirdvegas.mgerrit.Prefs;
import com.jbirdvegas.mgerrit.database.Config;
import com.jbirdvegas.mgerrit.search.SearchKeyword;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A class that helps to deconstruct Gerrit queries and assemble them
 *  when necessary. This allows for setting individual parts of a query
 *  without knowing other query parameters.
 */
public class GerritURL implements Parcelable
{
    private static Context sContext;
    private String mStatus = "";
    private boolean mRequestDetailedAccounts = false;
    private int mChangeNo = 0;

    private Set<SearchKeyword> mSearchKeywords;

    private enum ChangeDetailLevels {
        DISABLED, // Do not fetch change details
        LEGACY,   // Fetch change details and use legacy URL (Gerrit 2.7 or lower)
        ENABLED   // Fetch change details and use new change details endpoint (Gerrit 2.8+)
    } private ChangeDetailLevels mRequestChangeDetail = ChangeDetailLevels.DISABLED;

    public static final String DETAILED_ACCOUNTS_ARG = "&o=DETAILED_ACCOUNTS";
    // used to query commit message
    public static final String CURRENT_PATCHSET_ARGS = new StringBuilder(0)
            .append("?o=CURRENT_REVISION")
            .append("&o=CURRENT_COMMIT")
            .append("&o=CURRENT_FILES")
            .toString();
    public static final String OLD_CHANGE_DETAIL_ARGS = new StringBuilder(0)
            .append("&o=CURRENT_REVISION")
            .append("&o=CURRENT_COMMIT")
            .append("&o=CURRENT_FILES")
            .append("&o=DETAILED_LABELS")
            .append("&o=MESSAGES")
            .toString();


    // Default constructor to facilitate instantiation
    public GerritURL() { }

    public GerritURL(GerritURL url) {
        mStatus = url.mStatus;
        mRequestDetailedAccounts = url.mRequestDetailedAccounts;
        mChangeNo = url.mChangeNo;
        mRequestChangeDetail = url.mRequestChangeDetail;
    }

    public static void setContext(Context context) {
        GerritURL.sContext = context;
    }

    public void addSearchKeyword(SearchKeyword keyword) {
        if (mSearchKeywords == null) {
            mSearchKeywords = new HashSet<>();
        }
        mSearchKeywords.add(keyword);
    }

    public void addSearchKeywords(Set<SearchKeyword> keywords) {
        for (SearchKeyword keyword : keywords) addSearchKeyword(keyword);
    }

    public void setStatus(String status) {
        if (status == null) status = "";
        mStatus = status;
    }

    /**
     * DETAILED_ACCOUNTS: include _account_id and email fields when referencing accounts.
     * @param requestDetailedAccounts true if to include the additional fields in the response
     */
    public void setRequestDetailedAccounts(boolean requestDetailedAccounts) {
        mRequestDetailedAccounts = requestDetailedAccounts;
    }

    public void requestChangeDetail(boolean request, Boolean useLegacyUrl) {
        if (!request) {
            mRequestChangeDetail = ChangeDetailLevels.DISABLED;
        } else if (!useLegacyUrl) {
            mRequestChangeDetail = ChangeDetailLevels.ENABLED;
            mRequestDetailedAccounts = false;
        } else {
            mRequestChangeDetail = ChangeDetailLevels.LEGACY;
            mRequestDetailedAccounts = true;
        }
    }

    public void setChangeNumber(int changeNumber) {
        mChangeNo = changeNumber;
    }

    @Override
    @Nullable
    public String toString()
    {
        boolean addSeperator;

        StringBuilder builder = new StringBuilder(0).append(Prefs.getCurrentGerrit(sContext));
        builder.append("changes/");

        if (mRequestChangeDetail == ChangeDetailLevels.ENABLED) {
            if (mChangeNo > 0) {
                builder.append(mChangeNo).append("/detail/")
                        .append(GerritURL.CURRENT_PATCHSET_ARGS);
            }
            // Cannot request change detail without a change number.
            else return "";
        } else {
            builder.append("?q=");
            addSeperator = appendStatus(builder, false);
            appendSearchKeywords(builder, addSeperator);
        }

        appendArgs(builder);
        return builder.toString();
    }

    public String getStatus() {
        return mStatus;
    }

    @Nullable
    public String getQuery() {
        if (mStatus == null) return null;
        else {
            return JSONCommit.KEY_STATUS + ":" + mStatus;
        }
    }

    public boolean equals(String str) {
        return str != null && str.equals(this.toString());
    }

    private boolean appendStatus(StringBuilder builder, boolean addSeperator) {
        if (mStatus != null && !mStatus.isEmpty()) {
            if (addSeperator) builder.append('+');
            builder.append(JSONCommit.KEY_STATUS)
                    .append(":")
                    .append(mStatus);
            return true;
        }
        return false;
    }

    private boolean appendSearchKeywords(StringBuilder builder, boolean addSeperator) {
        String version = Config.getValue(sContext, Config.KEY_VERSION);
        if (mSearchKeywords != null && !mSearchKeywords.isEmpty()) {
            if (addSeperator) {
                builder.append('+');
                addSeperator = false;
            }
            for (SearchKeyword keyword : mSearchKeywords) {

                if (addSeperator) {
                    builder.append('+');
                    addSeperator = false;
                }

                String operator = keyword.getGerritQuery(version);
                if (operator != null && !operator.isEmpty()) {
                    builder.append(operator);
                    addSeperator = true;
                }
            }
        }
        return addSeperator;
    }

    private void appendArgs(StringBuilder builder) {
        if (mRequestChangeDetail == ChangeDetailLevels.LEGACY) {
            builder.append(GerritURL.OLD_CHANGE_DETAIL_ARGS);
        }
        if (mRequestDetailedAccounts) {
            builder.append(GerritURL.DETAILED_ACCOUNTS_ARG);
        }
    }


    // --- Parcelable stuff so we can send this object through intents ---
    public static final Creator<GerritURL> CREATOR
            = new Creator<GerritURL>() {
        public GerritURL createFromParcel(Parcel in) {
            return new GerritURL(in);
        }

        @Override
        public GerritURL[] newArray(int size) {
            return new GerritURL[0];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mChangeNo);
        dest.writeString(mStatus);
        dest.writeInt(mRequestDetailedAccounts ? 1 : 0);
        dest.writeString(mRequestChangeDetail.name());

        int size = mSearchKeywords.size();
        dest.writeInt(size);
        SearchKeyword[] keywords = new SearchKeyword[size];
        dest.writeTypedArray(mSearchKeywords.toArray(keywords), flags);
    }

    public GerritURL(Parcel in) {
        mChangeNo = in.readInt();
        mStatus = in.readString();
        mRequestDetailedAccounts = in.readInt() == 1;
        mRequestChangeDetail = ChangeDetailLevels.valueOf(in.readString());

        SearchKeyword[] keywords = new SearchKeyword[in.readInt()];
        in.readTypedArray(keywords, SearchKeyword.CREATOR);
        mSearchKeywords = new HashSet<>(Arrays.asList(keywords));
    }
}
