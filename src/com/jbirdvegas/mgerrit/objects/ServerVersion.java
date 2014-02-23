package com.jbirdvegas.mgerrit.objects;

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

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerVersion implements Comparator<ServerVersion> {

    String mVersion;

    public ServerVersion(String mVersion) {
        this.mVersion = mVersion;
    }

    /**
     * @param currentVersion The current version of the server
     * @param baseVersion The version which added the feature where support is being tested
     * @return Whether the currentVersion supports the feature added in baseVersion. I.e.
     *  currentVersion >= baseVersion
     */
    public boolean isGreaterVersion(String baseVersion) {
        return this.compare(this, new ServerVersion(baseVersion)) >= 0;
    }

    @Override
    /**
     * Compares the contents of a and b and returns a value indicating which has a
     *  higher version code.
     * @return -1 if version a precedes version b (a < b); 1 if a proceeds b (a > b);
     *  0 if they are the same version (a == b).
     * @throws IllegalArgumentException If either a or b are not valid version codes.
     */
    public int compare(ServerVersion lhs, ServerVersion rhs) {
        String s = "^(\\d+\\.)+\\d*|^\\d+";
        Pattern p = Pattern.compile(s);

        Matcher m1 = p.matcher(lhs.mVersion);
        Matcher m2 = p.matcher(rhs.mVersion);

        String versionA = null, versionB = null;
        if (m1.find()) versionA = m1.group(0);
        if (m2.find()) versionB = m2.group(0);

        if (versionA == null || versionB == null) {
            throw new IllegalArgumentException("One of the version numbers was not valid");
        }

        String[] aNums = versionA.split(".");
        String[] bNums = versionB.split(".");

        for (int i = 0; i < Math.min(aNums.length, bNums.length); i++) {
            if (Integer.parseInt(aNums[i]) < Integer.parseInt(bNums[i]))
                return -1;
            else if (Integer.parseInt(aNums[i]) > Integer.parseInt(bNums[i]))
                return 1;
        }
        return 0;
    }
}
