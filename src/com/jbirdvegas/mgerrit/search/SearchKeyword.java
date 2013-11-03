package com.jbirdvegas.mgerrit.search;

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

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class SearchKeyword {

    public static final String TAG = "SearchKeyword";

    private final String mOpName;
    private final String mOpParam;
    private String mOperator;

    // Initialise the map of search keywords supported
    private static final Set<Class<? extends SearchKeyword>> _CLASSES;
    private static Map<String, Class<? extends SearchKeyword>> _KEYWORDS;
    static {
        _KEYWORDS = new HashMap<String, Class<? extends SearchKeyword>>();
        _CLASSES = new HashSet<Class<? extends SearchKeyword>>();

        // Add each search keyword here
        _CLASSES.add(ChangeSearch.class);
        _CLASSES.add(SubjectSearch.class);
        _CLASSES.add(ProjectSearch.class);
        _CLASSES.add(OwnerSearch.class);
        _CLASSES.add(TopicSearch.class);

        // This will load the class calling the class's static block
        for (Class<? extends SearchKeyword> clazz : _CLASSES) {
            try {
                Class.forName(clazz.getName());
            } catch (ClassNotFoundException e) {
                Log.e(TAG, String.format("Could not load class '%s'", clazz.getSimpleName()));
            }
        }
    }

    public SearchKeyword(String name, String param) {
        this.mOpName = name;
        this.mOpParam = param;
    }

    public SearchKeyword(String name, String operator, String param) {
        mOpName = name;
        mOperator = operator;
        mOpParam = param;
    }


    protected static void registerKeyword(String opName, Class<? extends SearchKeyword> clazz) {
        _KEYWORDS.put(opName, clazz);
    }

    public String getName() { return mOpName; }
    public String getParam() { return mOpParam; }
    public String getOperator() { return mOperator; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(mOpName).append(":\"");
        if (mOperator != null) builder.append(mOperator);
        builder.append(mOpParam).append("\"");
        return builder.toString();
    }

    /**
     * Build a search keyword given a name and its parameter
     * @param name
     * @param param
     * @return
     */
    private static SearchKeyword buildToken(String name, String param) {
        Class<? extends SearchKeyword> keyword = getSearchKeywordType(name);
        if (keyword == null) {
            return null;
        } else {
            Constructor<? extends SearchKeyword> constructor;
            try {
                constructor = keyword.getDeclaredConstructor(String.class, String.class);
                return constructor.newInstance(name, param);
            } catch (Exception e) {
                Log.e(TAG, "Could not call constructor for " + name, e);
            }
        }
        return null;
    }

    private static Class<? extends SearchKeyword> getSearchKeywordType(String name) {
        for (Entry<String, Class<? extends SearchKeyword>> entry : _KEYWORDS.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    protected static SearchKeyword buildToken(Class<? extends SearchKeyword> clazz, String tokenStr) {
        for (Entry<String, Class<? extends SearchKeyword>> entry : _KEYWORDS.entrySet()) {
            if (clazz.equals(entry.getValue())) {
                buildToken(entry.getKey() + ":" + tokenStr);
            }
        }
        return null;
    }

    protected static SearchKeyword buildToken(String tokenStr) {
        String[] s = tokenStr.split(":", 2);
        if (s.length == 2) return buildToken(s[0], s[1]);
        else return null;
    }

    /**
     * Given a raw search query, this will attempt to process it
     *  into the categories to search for (keywords) and their
     *  arguments.
     * @param query A raw search query in the form that Gerrit uses
     * @return A set of SearchKeywords that can be used to construct
     *  the database query
     */
    public static Set<SearchKeyword> constructTokens(String query) {
        Set<SearchKeyword> set = new HashSet<SearchKeyword>();
        String currentToken = "";

        // Characters that have special meaning that can be escaped
        String specialcharacters = "\"{} \t:";

        for (int i = 0, n = query.length(); i < n; i++) {
            char c = query.charAt(i);
            if (c == ':') {
                // Determine what keyword we are constructing
                Class<? extends SearchKeyword> clazz = getSearchKeywordType(currentToken);
                StringBuilder builder = new StringBuilder(query.substring(i + 1));
                SearchKeyword keyword;
                if (clazz == null) {
                    continue;
                } else {
                    try {
                        Method method = clazz.getMethod("parse", StringBuilder.class);
                        keyword = (SearchKeyword) method.invoke(null, clazz, builder);
                    } catch (Exception e) {
                        keyword = parse(clazz, builder);
                    }

                    addToSetIfNotNull(keyword, set);
                    // Continue with the un-parsed string
                    i = 0;
                    query = builder.toString();
                    currentToken = "";
                }
            } else if (c == '\\') {
                if (i + 1 < n) {
                    if (specialcharacters.indexOf(query.charAt(i+1)) >= 0) {
                        // We have escaped a character, ignore the '\'
                        currentToken += query.charAt(i+1);
                        i++;
                        continue;
                    }
                }
                // Treat as literal '\' (i.e. as if we had "\\")
                currentToken += c;
            } else {
                currentToken += c;
            }
        }

        // Have to check if a token was terminated by end of string
        if (currentToken.length() > 0) {
            addToSetIfNotNull(buildToken(currentToken), set);
        }
        return set;
    }

    public static String getQuery(Set<SearchKeyword> tokens) {
        String query = "";
        for (SearchKeyword token : tokens) {
            query += token.toString() + " ";
        }
        return query.trim();
    }

    private static void addToSetIfNotNull(SearchKeyword token, Set<SearchKeyword> set) {
        if (token != null) {
            set.add(token);
        }
    }

    public static String constructDbSearchQuery(Set<SearchKeyword> tokens) {
        StringBuilder whereQuery = new StringBuilder();
        Iterator<SearchKeyword> it = tokens.iterator();
        while (it.hasNext()) {
            SearchKeyword token = it.next();
            if (token == null) continue;
            whereQuery.append(token.buildSearch());
            if (it.hasNext()) whereQuery.append(" AND ");
        }
        return whereQuery.toString();
    }

    public abstract String buildSearch();

    /**
     * Formats the bind argument for query binding.
     * May be overriden to include wildcards in the parameter for like queries
     */
    public String[] getEscapeArgument() {
        return new String[] { getParam() };
    }

    public static String replaceKeyword(String query, SearchKeyword keyword) {
        Set<SearchKeyword> tokens = SearchKeyword.constructTokens(query);
        for (SearchKeyword token : tokens) {
            if (token instanceof ProjectSearch) {
                tokens.remove(token);
            }
        }

        if (!keyword.getParam().equals("")) {
            tokens.add(keyword);
        }
        return SearchKeyword.getQuery(tokens);
    }

    public static int findKeyword(Set<SearchKeyword> tokens, Class<? extends SearchKeyword> clazz) {
        int i = 0;
        for (SearchKeyword token : tokens) {
            if (token.getClass().equals(clazz)) return i;
            else i++;
        }
        return -1;
    }

    /**
     * Parse the parameter for a given SearchKeyword and construct an instance of it.
     *  Note: The characters in the builder that have been parsed MUST be removed otherwise
     *  they will be re-parsed invalidating the search query
     * @param clazz The SearchKeyword to be constructed
     * @param builder The un-parsed parameter as a StringBuilder
     * @return An instance of the given SearchKeyword class
     */
    protected static SearchKeyword parse(Class<? extends SearchKeyword> clazz, StringBuilder builder) {
        String currentToken = "";
        String param = builder.toString();

        // Characters that have special meaning that can be escaped
        String specialcharacters = "\"{} \t:";

        int index;
        for (index = 0; index < param.length(); index++) {
            char c = param.charAt(index);
            if (Character.isWhitespace(c)) {
                break;
            } else if (c == '"') {
                int index2 = param.indexOf('"', index + 1);
                // We don't want to store the quotation marks
                currentToken += param.substring(index + 1, index2);
                index = index2 + 1; // We have processed this many characters
            } else if (c == '{') {
                int index2 = param.indexOf('}', index + 1);
                // We don't want to store these braces
                currentToken += param.substring(index + 1, index2);
                index = index2 + 1; // We have processed this many characters
            } else if (c == '\\') {
                if (index + 1 < param.length()) {
                    if (specialcharacters.indexOf(param.charAt(index+1)) >= 0) {
                        // We have escaped a character, ignore the '\'
                        currentToken += param.charAt(index+1);
                        index++;
                        continue;
                    }
                }
                // Treat as literal '\' (i.e. as if we had "\\")
                currentToken += c;
            } else {
                currentToken += c;
            }
        }

        // Have to check if a token was terminated by end of string
        if (currentToken.length() > 0) {
            // Remove the characters that we have already parsed so they are not re-parsed
            builder.delete(0, index);
            return SearchKeyword.buildToken(clazz, currentToken);
        }
        throw new InvalidParameterException("Could not parse parameter '" + param + "'");
    }
}
