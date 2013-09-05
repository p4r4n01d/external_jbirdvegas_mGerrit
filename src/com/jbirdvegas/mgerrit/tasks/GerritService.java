package com.jbirdvegas.mgerrit.tasks;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jbirdvegas.mgerrit.database.DatabaseFactory;
import com.jbirdvegas.mgerrit.message.ErrorDuringConnection;
import com.jbirdvegas.mgerrit.message.Finished;
import com.jbirdvegas.mgerrit.objects.JSONCommit;
import com.jbirdvegas.mgerrit.objects.Project;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Evan on 2/09/13.
 */
public class GerritService extends IntentService {

    public static final String TAG = "GerritService";

    public static final String URL_KEY = "Url";
    public static final String DATA_TYPE_KEY = "Type";

    public static enum DataTypes { Project }

    private String mCurrentUrl;

    public GerritService() {
        super("");
    }

    /**
     * Creates an IntentService.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GerritService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mCurrentUrl = intent.getStringExtra(URL_KEY);

        int dataType = intent.getIntExtra(DATA_TYPE_KEY, 0);
        if (dataType == DataTypes.Project.ordinal()) {
            fetchData(new ProjectInserter(this));
        }
    }

    // IMPORTANT: Do NOT call this on the main thread!
    private void fetchData(final DatabaseInserter dbInserter) {

        // Won't be able to actually get JSON response back as it
        //  is improperly formed (junk at start), but requesting raw text and
        //  trimming it should be fine.

        RequestQueue queue = Volley.newRequestQueue(this);

        /* Not sure whether to create a new request type and have that handle the trimming,
         *  JSON parsing and Database insertion
         */
        StringRequest request = new StringRequest(mCurrentUrl,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                String json = s.substring(5);
                dbInserter.insert(json);
                new Finished(GerritService.this, json, GerritService.this.mCurrentUrl);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                new ErrorDuringConnection(GerritService.this, volleyError,
                        GerritService.this.mCurrentUrl);
            }
        });
        queue.add(request);
    }

    abstract class DatabaseInserter {
        protected final Context mContext;

        DatabaseInserter(Context context) {
            this.mContext = context;
        }

        abstract void insert(String json);
    }


    class ProjectInserter extends DatabaseInserter {
        ProjectInserter(Context context) {
            super(context);
        }

        @Override
        void insert(String json) {
            List<Project> projectList = new ArrayList<Project>();

            JSONObject projectsJson;
            try {
                projectsJson = new JSONObject(json);

                Iterator stringIterator = projectsJson.keys();
                while (stringIterator.hasNext()) {
                    String path = (String) stringIterator.next();
                    JSONObject projJson = projectsJson.getJSONObject(path);
                    String kind = projJson.getString(JSONCommit.KEY_KIND);
                    String id = projJson.getString(JSONCommit.KEY_ID);
                    projectList.add(Project.getInstance(path, kind, id));
                }
            } catch (JSONException e) {
                e.printStackTrace(); // TODO: Send ParseError Message
            }
            Collections.sort(projectList);
            DatabaseFactory.getDatabase(mContext).getProjectsTable().insertProjects(projectList);
        }
    }
}
