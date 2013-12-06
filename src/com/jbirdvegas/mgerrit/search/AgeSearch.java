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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgeSearch extends SearchKeyword {

    public static final String OP_NAME = "age";

    private static final HashMap<String, Integer> unitMap;
    static {
        unitMap = new HashMap<>();
        unitMap.put("s", Calendar.SECOND);
        unitMap.put("sec", Calendar.SECOND);
        unitMap.put("secs", Calendar.SECOND);
        unitMap.put("second", Calendar.SECOND);
        unitMap.put("seconds", Calendar.SECOND);

        unitMap.put("m", Calendar.MINUTE);
        unitMap.put("min", Calendar.MINUTE);
        unitMap.put("mins", Calendar.MINUTE);
        unitMap.put("minute", Calendar.MINUTE);
        unitMap.put("minutes", Calendar.MINUTE);

        unitMap.put("h", Calendar.HOUR);
        unitMap.put("hr", Calendar.HOUR);
        unitMap.put("hrs", Calendar.HOUR);
        unitMap.put("hour", Calendar.HOUR);
        unitMap.put("hours", Calendar.HOUR);

        unitMap.put("d", Calendar.DAY_OF_MONTH);
        unitMap.put("day", Calendar.DAY_OF_MONTH);
        unitMap.put("days", Calendar.DAY_OF_MONTH);

        unitMap.put("w", Calendar.WEEK_OF_YEAR);
        unitMap.put("week", Calendar.WEEK_OF_YEAR);
        unitMap.put("weeks", Calendar.WEEK_OF_YEAR);

        unitMap.put("mon", Calendar.MONTH);
        unitMap.put("mon", Calendar.MONTH);
        unitMap.put("mth", Calendar.MONTH);
        unitMap.put("mths", Calendar.MONTH);
        unitMap.put("month", Calendar.MONTH);
        unitMap.put("months", Calendar.MONTH);

        unitMap.put("y", Calendar.YEAR);
        unitMap.put("yr", Calendar.YEAR);
        unitMap.put("yrs", Calendar.YEAR);
        unitMap.put("year", Calendar.YEAR);
        unitMap.put("years", Calendar.YEAR);
    }

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
            try {
                time.parse3339(param);
            } catch (TimeFormatException e2) {
                time.set(parseDate(param).getTimeInMillis());
            }
        }

        return new String[] { String.valueOf(time.normalize(false)) };
    }

    private static String extractParameter(String param) {
        return param.replaceFirst("[=<>]+", "");
    }

    /**
     * Parse a date offset string into an actual date.
     *  For example "1week 5 days" would return a calendar object with
     *   the time 1 week and 5 days (12 days) behind the current time.
     * @param dateOffset The parameter without the operator
     * @return A calendar with an absolute time. Its time will be offset
     *  from now by the amount specified in the offset. If there is a
     *  problem parsing a part of the string only the left-most successfully
     *  parsed portions will be used. The default is the current time.
     */
    private Calendar parseDate(String dateOffset) {
        String prefix = "(\\d+) *";
        Pattern pattern;
        Calendar newTime = Calendar.getInstance();

        if (dateOffset == null || dateOffset.isEmpty())
            return newTime;

        for (Map.Entry<String, Integer> entry : unitMap.entrySet()) {
            pattern = Pattern.compile(prefix + entry.getKey());
            Matcher matcher = pattern.matcher(dateOffset);

            if (matcher.find()) {
                String svalue = matcher.toMatchResult().group(1);
                int unit = Integer.parseInt(svalue);
                newTime.add(entry.getValue(), unit);

                // Skip ahead in the parameter, we may have more modifiers
                int startc = matcher.toMatchResult().start(1);
                int endc = svalue.length();
                // Set param to after what we have just processed
                dateOffset = dateOffset.substring(startc + endc);
                // If it isn't empty we can remove the separator
                if (!dateOffset.isEmpty()) dateOffset = dateOffset.substring(1);

                continue;
            }
        }

        return newTime;
    }
}
