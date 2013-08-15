package com.jbirdvegas.mgerrit.objects;

import com.jbirdvegas.mgerrit.StaticWebAddress;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A class that helps to deconstruct Gerrit queries and assemble them
 *  when necessary. This allows for setting individual parts of a query
 *  without knowing other query parameters.
 */
public class GerritURL
{
    private static String mGerritBase;
    private String mProject;
    private String mStatus;
    private String mEmail;
    private String mCommitterState;
    private boolean mRequestDetailedAccounts = false;

    public void setProject(String project) {
        mProject = project;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public void setCommitterState(String committerState) {
        mCommitterState = committerState;
    }

    public static void setGerrit(String mGerritBase) {
        GerritURL.mGerritBase = mGerritBase;
    }

    public void setRequestDetailedAccounts(boolean requestDetailedAccounts) {
        mRequestDetailedAccounts = requestDetailedAccounts;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(0)
            .append(mGerritBase)
            .append(StaticWebAddress.getStatusQuery())
            .append("(");

        if (!"".equals(mCommitterState) && !"".equals(mEmail))
        {
            builder.append(mCommitterState)
                .append(':')
                .append(mEmail);
        }

        if (!"".equals(mStatus))
        {
            builder.append('+')
                .append(JSONCommit.KEY_STATUS)
                .append(":")
                .append(mStatus);
        }

        try {
            if (!"".equals(mProject))
            {
                    builder.append('+')
                        .append(JSONCommit.KEY_PROJECT)
                        .append(":")
                        .append(URLEncoder.encode(mProject, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        builder.append(")");

        if (mRequestDetailedAccounts) {
            builder.append(JSONCommit.DETAILED_ACCOUNTS_ARG);
        }

        return builder.toString();
    }
}
