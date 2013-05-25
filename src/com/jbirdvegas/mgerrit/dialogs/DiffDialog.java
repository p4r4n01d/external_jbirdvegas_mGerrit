package com.jbirdvegas.mgerrit.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jbirdvegas.mgerrit.R;
import com.jbirdvegas.mgerrit.helpers.Base64Coder;
import com.jbirdvegas.mgerrit.objects.ChangedFile;
import com.jbirdvegas.mgerrit.objects.Diff;

import java.util.regex.Pattern;

/**
 * Created by jbird on 5/16/13.
 */
public class DiffDialog extends AlertDialog.Builder {
    private static final String TAG = DiffDialog.class.getSimpleName();
    private static final String DIFF = "\n\nDIFF\n\n";
    private static final boolean DIFF_DEBUG = false;
    private final String mUrl;
    private final RequestQueue mRequestQueue;
    private View mRootView;
    private final ChangedFile mChangedFile;
    private String mLineSplit = System.getProperty("line.separator");
    private LayoutInflater mInflater;
    private TextView mDiffTextView;

    public DiffDialog(Context context, String website, ChangedFile changedFile) {
        super(context);
        mRequestQueue = Volley.newRequestQueue(context);
        mUrl = website;
        mChangedFile = changedFile;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = mInflater.inflate(R.layout.diff_dialog, null);
        setView(mRootView);
        mDiffTextView = (TextView) mRootView.findViewById(R.id.diff_view_diff);
        Log.d(TAG, "Calling url: " + mUrl); //+ "/b");
        if (DIFF_DEBUG) {
            debugRestDiffApi(context, mUrl, mChangedFile);
        }
        // we can use volley here because we return
        // does not contain the magic number on the
        // first line. return is just the Base64 formatted
        // return
        mRequestQueue.add(getBase64gRequest(mUrl, "/b"));
        mRequestQueue.start();
    }

    private void debugRestDiffApi(Context context, String mUrl, ChangedFile mChangedFile) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        Log.d(TAG, "Targeting changed file: " + mChangedFile);
        requestQueue.add(getDebugRequest(mUrl, "/a"));
        requestQueue.add(getDebugRequest(mUrl, "/b"));
        requestQueue.add(getDebugRequest(mUrl, "/ab"));
        requestQueue.add(getDebugRequest(mUrl, "/"));
        requestQueue.start();
    }

    private Request getDebugRequest(String url, String arg) {
        // seems a bug prevents the args from being respected???
        // See here:
        // https://groups.google.com/forum/?fromgroups#!topic/repo-discuss/xmFCHbD4Z0Q
        String limiter = "&o=context:2";

        final String args = arg + limiter;
        final String weburl = url + args;
        Request debugRequest =
                new StringRequest(weburl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                Log.d(TAG, "[DEBUG-MODE]\n" +
                                        "Decoded Response for args {" + args + '}'
                                        + "\n"
                                        + "url: " + weburl
                                        + "\n==================================="
                                        + Base64Coder.decodeString(s)
                                        + "===================================="
                                );
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.e(TAG, "[DEBUG-MODE]\n"
                                        + "Volley Failed!!!", volleyError);
                            }
                        }
                );
        return debugRequest;
    }

    private Request getBase64gRequest(String url, String arg) {
        // seems a bug prevents the args from being respected???
        // See here:
        // https://groups.google.com/forum/?fromgroups#!topic/repo-discuss/xmFCHbD4Z0Q
        String limitContextLines = "&o=context:2";
        final String args = arg + limitContextLines;
        final String weburl = url + args;
        Request debugRequest =
                new StringRequest(weburl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                if (DIFF_DEBUG) {
                                    Log.d(TAG, "[DEBUG-MODE]\n" +
                                            "Decoded Response for args {" + args + '}'
                                            + "\n"
                                            + "url: " + weburl
                                            + "\n==================================="
                                            + Base64Coder.decodeString(s)
                                            + "===================================="
                                    );
                                }
                                setTextView(Base64Coder.decodeString(s));
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.e(TAG, "Failed to download the diff", volleyError);
                            }
                        }
                );
        return debugRequest;
    }

    private void setTextView(String result) {
        //textView.setText(result);
        // return;

        Pattern pattern = Pattern.compile("\\Qdiff --git \\E");
        String[] filesChanged = pattern.split(result);
        StringBuilder builder = new StringBuilder(0);
        Diff currentDiff = null;
        for (String change : filesChanged) {
            String concat;
            try {
                concat = change.substring(2, change.lastIndexOf(mChangedFile.getPath())).trim();
                concat = concat.split(" ")[0];
            } catch (StringIndexOutOfBoundsException notFound) {
                Log.d(TAG, notFound.getMessage());
                continue;
            }
            if (concat.equals(mChangedFile.getPath())) {
                builder.append(DIFF);
                change.replaceAll("\n", mLineSplit);
                currentDiff = new Diff(getContext(), change);
                builder.append(change);
            }
        }
        if (builder.length() == 0) {
            builder.append("Diff not found!");
        }
        mDiffTextView.setTypeface(Typeface.MONOSPACE);
        // rebuild text; required to respect the \n
        SpannableString spannableString = currentDiff.getColorizedSpan();
        if (spannableString != null) {
            mDiffTextView.setText(currentDiff.getColorizedSpan(), TextView.BufferType.SPANNABLE);
        } else {
            mDiffTextView.setText("Failed to load diff :(");
        }

    }
}
