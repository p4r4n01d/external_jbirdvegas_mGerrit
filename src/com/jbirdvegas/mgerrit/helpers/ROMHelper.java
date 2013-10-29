package com.jbirdvegas.mgerrit.helpers;

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

/**
 * Created by Evan on 30/10/13.
 */
public class ROMHelper {

    public static final String CYANOGENMOD = "CyanogenMod";
    public static final String AOKP = "AOKP";

    /**
     * Given a build string, determine what ROM the user is running and
     *  ignore things such as the version information.
     */
    public static String determineRom(String buildVersion) {

        if (buildVersion.contains("cm") || buildVersion.contains("cyanogenmod")) {
            return CYANOGENMOD;
        } else if (buildVersion.contains("aokp")) {
            return AOKP;
        }
        return buildVersion;
    }

}
