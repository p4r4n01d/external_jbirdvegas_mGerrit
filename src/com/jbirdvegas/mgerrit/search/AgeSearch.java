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

import android.text.format.Time;
import android.util.TimeFormatException;

import com.jbirdvegas.mgerrit.database.UserChanges;

public class AgeSearch extends SearchKeyword {

    public static final String OP_NAME = "age";

    /** Supported searching operators - these are used directly
     *  in the SQL query */
    final static String[] operators =
            { "=", "<", ">", "<=", ">=" };

    static {
        registerKeyword(OP_NAME, AgeSearch.class);
    }

    public AgeSearch(String param, String operator) {
        super(OP_NAME, operator, param);
    }

    public AgeSearch(String param) {
        // We need to extract the operator and the parameter from the string
        this(extractParameter(param), extractOperator(param));
    }

    @Override
    public String buildSearch() {
        return UserChanges.C_UPDATED + " " + getOperator() + " ?";
    }

    @Override
    public String[] getEscapeArgument() {
        String param = getParam();
        Time time = new Time();
        try {
            time.parse(param);
        } catch (TimeFormatException e) {
            time.parse3339(param);
        }

        return new String[] { String.valueOf(time.normalize(false)) };
    }

    private static String extractOperator(String param) {
        String op = "=";
        for (String operator : operators) {
            if (param.startsWith(operator)) op = operator;
        }
        // '==' also refers to '='
        if (param.startsWith("==")) op = "=";
        return op;
    }

    private static String extractParameter(String param) {
        return param.replaceFirst("[=<>]+", "");
    }
}
