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

import com.jbirdvegas.mgerrit.database.UserChanges;

import org.joda.time.DurationFieldType;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgeSearch extends SearchKeyword {

    public static final String OP_NAME = "age";

    // We may only need one of these
    private Period period;
    private Instant instant;

    private static final HashMap<String, DurationFieldType> replacers;
    static {
        replacers = new HashMap<>();
        replacers.put("s", DurationFieldType.seconds());
        replacers.put("sec", DurationFieldType.seconds());
        replacers.put("secs", DurationFieldType.seconds());
        replacers.put("second", DurationFieldType.seconds());
        replacers.put("seconds", DurationFieldType.seconds());

        replacers.put("m", DurationFieldType.minutes());
        replacers.put("min", DurationFieldType.minutes());
        replacers.put("mins", DurationFieldType.minutes());
        replacers.put("minute", DurationFieldType.minutes());
        replacers.put("minutes", DurationFieldType.minutes());

        replacers.put("h", DurationFieldType.hours());
        replacers.put("hr", DurationFieldType.hours());
        replacers.put("hrs", DurationFieldType.hours());
        replacers.put("hour", DurationFieldType.hours());
        replacers.put("hours", DurationFieldType.hours());

        replacers.put("d", DurationFieldType.days());
        replacers.put("day", DurationFieldType.days());
        replacers.put("days", DurationFieldType.days());

        replacers.put("w", DurationFieldType.weeks());
        replacers.put("week", DurationFieldType.weeks());
        replacers.put("weeks", DurationFieldType.weeks());

        replacers.put("mon", DurationFieldType.months());
        replacers.put("mon", DurationFieldType.months());
        replacers.put("mth", DurationFieldType.months());
        replacers.put("mths", DurationFieldType.months());
        replacers.put("month", DurationFieldType.months());
        replacers.put("months", DurationFieldType.months());

        replacers.put("y", DurationFieldType.years());
        replacers.put("yr", DurationFieldType.years());
        replacers.put("yrs", DurationFieldType.years());
        replacers.put("year", DurationFieldType.years());
        replacers.put("years", DurationFieldType.years());
    }

    /** Used for serialising the period into a string and must be output
     *   in a format that can be re-parsed later */
    private static PeriodFormatter periodParser = new PeriodFormatterBuilder()
            .appendYears().appendSuffix(" years ")
            .appendMonths().appendSuffix(" months ")
            .appendWeeks().appendSuffix(" weeks ")
            .appendDays().appendSuffix(" days ")
            .appendHours().appendSuffix(" hours ")
            .appendMinutes().appendSuffix(" minutes ")
            .appendSeconds().appendSuffix(" seconds ")
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
        String operator = getOperator();
        if (operator.equals("=")) {
            return UserChanges.C_UPDATED + " BETWEEN ? AND ?";
        } else {
            return "? " + operator + " " + UserChanges.C_UPDATED;
        }
    }

    @Override
    public String[] getEscapeArgument() {
        String operator = getOperator();
        if (operator.equals("=")) {
            Instant earlier = Instant.now().minus(period.toStandardDuration());
            Instant later = Instant.now().plus(period.toStandardDuration());
            return new String[] {
                    String.valueOf(earlier.getMillis()),
                    String.valueOf(later.getMillis()),
            };
        } else {
            return new String[] { String.valueOf(instant.getMillis()) };
        }
    }

    private static String extractParameter(String param) {
        return param.replaceFirst("[=<>]+", "");
    }

    private void parseDate(String param) {
        instant = new Instant();
        period = new Period();

        try {
            instant = Instant.parse(param, ISODateTimeFormat.localDateOptionalTimeParser());
            period = new Period(instant, Instant.now());
        } catch (IllegalArgumentException e) {
            period = toPeriod(param);
            instant = Instant.now().minus(period.toStandardDuration());
        }
    }

    @Override
    public String toString() {
        return OP_NAME + ":\"" + periodParser.print(period) + "\"";
    }

    /**
     * Parses a string into a Period object according to the replacers
     *  mapping. This allows for duplicate fields (e.g. seconds being
     *  declared twice as in "2s 3 sec") with the duplicate fields being
     *  added together (the above example would be the same as "5 seconds").
     * The order of the fields is not important.
     *
     * @param dateOffset The parameter without the operator. If the operator
     *                   is passed in it will be ignored
     * @return A period corresponding to the parsed input string
     */
    private Period toPeriod(final String dateOffset) {
        String regexp = "(\\d+) *([a-zA-z]+)";
        Period period = new Period();

        if (dateOffset == null || dateOffset.isEmpty())
            return period;

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(dateOffset);
        while (matcher.find()) {
            String svalue = matcher.toMatchResult().group(1);
            DurationFieldType fieldType = replacers.get(matcher.toMatchResult().group(2));
            if (fieldType != null) {
                // Note that both these methods do not modify their objects
                period = period.withFieldAdded(fieldType, Integer.parseInt(svalue));
            }
        }

        return period;
    }
}
