package com.jbirdvegas.mgerrit.tasks;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.jbirdvegas.mgerrit.database.Reviewers;
import com.jbirdvegas.mgerrit.objects.CommitterObject;
import com.jbirdvegas.mgerrit.objects.JSONCommit;
import com.jbirdvegas.mgerrit.objects.Projects;
import com.jbirdvegas.mgerrit.objects.Reviewer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public final class Deserializers {

    // Custom deserializer as we are separating the one object into two (the committer is not nested)
    private static final JsonDeserializer<Reviewer> reviewerDeserializer = new JsonDeserializer<Reviewer>() {

        @Override
        public Reviewer deserialize(JsonElement jsonElement,
                                    Type type,
                                    JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {

                Reviewer reviewer = new Gson().fromJson(jsonElement, type);
                reviewer.setCommitter(new Gson().fromJson(jsonElement, CommitterObject.class));
                return reviewer;
            }
        };

    // Custom deserializer to get all the reviewers and their associated labels
    private static final JsonDeserializer<Reviewer[]> reviewersDeserializer = new JsonDeserializer<Reviewer[]>() {

        @Override
        public Reviewer[] deserialize(JsonElement jsonElement,
                                    Type type,
                                    JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {

            ArrayList<Reviewer> reviewers = new ArrayList<Reviewer>();

            JsonObject labels = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> labelParent : labels.entrySet()) {
                JsonObject label = labelParent.getValue().getAsJsonObject();
                if (label.has("all")) {
                    Reviewer[] rs = new Gson().fromJson(label.getAsJsonArray("all"), Reviewer[].class);
                    for (Reviewer r : rs) {
                        r.setLabel(label.getAsString()); // Set the label the value corresponds to
                    }
                    reviewers.addAll(Arrays.asList(rs));
                }
            }
            Reviewer[] reviewersArray = new Reviewer[reviewers.size()];
            return reviewers.toArray(reviewersArray);
        }
    };

    private static final JsonDeserializer<JSONCommit> commitDeserializer = new JsonDeserializer<JSONCommit>() {

        @Override
        public JSONCommit deserialize(JsonElement jsonElement,
                                    Type type,
                                    JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {

            JSONCommit commit = new Gson().fromJson(jsonElement, type);
            return commit;
        }
    };

    // Register all of the custom deserializers here
    protected static void addDeserializers(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(Projects.class, new Projects());
        gsonBuilder.registerTypeAdapter(Reviewer.class, reviewerDeserializer);
        //gsonBuilder.registerTypeAdapter(JSONCommit.class, commitDeserializer); NOT CURRENTLY USED
    }
}
