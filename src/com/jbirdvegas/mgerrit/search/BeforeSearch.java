package com.jbirdvegas.mgerrit.search;

/*
 * Copyright (C) 2014 Android Open Kang Project (AOKP)
 *  Author: Evan Conway (P4R4N01D), 2014
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
public class BeforeSearch extends AgeSearch {

    public static final String OP_NAME = "before";

    static {
        registerKeyword(OP_NAME, BeforeSearch.class);
        registerKeyword("until", BeforeSearch.class);
    }

    public BeforeSearch(String param) {
        super(param, "<=");
    }

    public BeforeSearch(long timestamp, String operator) {
        super(timestamp, "<=");
    }

    @Override
    public String toString() {
        return toString(OP_NAME);
    }
}
