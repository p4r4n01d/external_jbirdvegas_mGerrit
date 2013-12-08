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

import android.util.TimeFormatException;

import com.jbirdvegas.mgerrit.database.UserChanges;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.Instant;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class AgeSearch extends SearchKeyword {

    public static final String OP_NAME = "age";

    // We may only need one of these
    private Period period;
    private Instant instant;

    private static final HashMap<String, String> replacers;
    static {
        replacers = new HashMap<>();
        replacers.put("s", "seconds");
        replacers.put("sec", "seconds");
        replacers.put("secs", "seconds");
        replacers.put("second", "seconds");
        replacers.put("seconds", "seconds");

        replacers.put("m", "minutes");
        replacers.put("min", "minutes");
        replacers.put("mins", "minutes");
        replacers.put("minute", "minutes");
        replacers.put("minutes", "minutes");

        replacers.put("h", "hours");
        replacers.put("hr", "hours");
        replacers.put("hrs", "hours");
        replacers.put("hour", "hours");
        replacers.put("hours", "hours");

        replacers.put("d", "days");
        replacers.put("day", "days");
        replacers.put("days", "days");

        replacers.put("w", "weeks");
        replacers.put("week", "weeks");
        replacers.put("weeks", "weeks");

        replacers.put("mon", "months");
        replacers.put("mon", "months");
        replacers.put("mth", "months");
        replacers.put("mths", "months");
        replacers.put("month", "months");
        replacers.put("months", "months");

        replacers.put("y", "years");
        replacers.put("yr", "years");
        replacers.put("yrs", "years");
        replacers.put("year", "years");
        replacers.put("years", "years");
    }

    /** A parser corresponding to the format of the output string
     *  in the standardize method. */
    PeriodFormatter periodParser = new PeriodFormatterBuilder()
            .appendSeconds().appendSuffix(" seconds ")
            .appendMinutes().appendSuffix(" minutes ")
            .appendHours().appendSuffix(" hours ")
            .appendDays().appendSuffix(" days ")
            .appendWeeks().appendSuffix(" weeks ")
            .appendMonths().appendSuffix(" months ")
            .appendYears().appendSuffix(" years ")
            .toFormatter();


    static {
        registerKeyword(OP_NAME, AgeSearch.class);
    }

    public AgeSearch(String param, String operator) {
        super(OP_NAME, operator, param);

        parseDate(param);
    }

    public AgeSearch(String param) {
        // We need to extract the operator and the parameter from the string
        this(extractParameter(param), extractOperator(param));
    }

    @Override
    public String buildSearch() {
        /* Use the operator to determine what to do here -
         * <, > : we can just match against the instant
         * = : we have to use the period and extract all the > 0 components
         */
        return UserChanges.C_UPDATED + " " + getOperator() + " ?";
    }

    @Override
    public String[] getEscapeArgument() {
        return new String[] { String.valueOf(instant.getMillis()) };
    }

    private static String extractParameter(String param) {
        return param.replaceFirst("[=<>]+", "");
    }

    private void parseDate(String param) {
        instant = new Instant();
        period = new Period();

        try {
            // TODO find what exception this raises on failure
            instant = Instant.parse(param, ISODateTimeFormat.localDateOptionalTimeParser());
            period = new Period(instant, Instant.now());
        } catch (TimeFormatException e) {
            period = periodParser.parsePeriod(standardize(param));
            instant = Instant.now().plus(period.toStandardDuration());
        }
    }

    /**
     * Standardises the units in dateOffset according to those specified
     *  in the replacers map. This allows a parser to be able to parse
     *  strings such as "1day", "1d" and "1 d"
     *
     * @param dateOffset The parameter without the operator
     * @return A standardised period string with the same meaning as the
     *  original.
     */
    private String standardize(final String dateOffset) {
        String prefix = "(\\d+) *";
        Pattern pattern;
        String newFormat = dateOffset, newString = "";

        if (dateOffset == null || dateOffset.isEmpty())
            return newFormat;

        for (Map.Entry<String, String> entry : replacers.entrySet()) {
            pattern = Pattern.compile(prefix + entry.getKey());
            Matcher matcher = pattern.matcher(newFormat);

            if (matcher.find()) {
                String svalue = matcher.toMatchResult().group(1);
                newString += svalue + " " + entry.getValue() + " ";

                // Remove what we have just processed from the input string
                newFormat = matcher.replaceFirst("");
                newFormat = newFormat.trim();
                if (newFormat.isEmpty()) break;
                else continue;
            }
        }

        return newString;
    }
}
