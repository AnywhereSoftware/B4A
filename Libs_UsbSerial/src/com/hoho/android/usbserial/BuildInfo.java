
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package com.hoho.android.usbserial;

/**
 * Static container of information about this library.
 */
public final class BuildInfo {

    /**
     * The current version of this library. Values are of the form
     * "major.minor.micro[-suffix]". A suffix of "-pre" indicates a pre-release
     * of the version preceeding it.
     */
    public static final String VERSION = "0.2.0-pre";

    private BuildInfo() {
        throw new IllegalStateException("Non-instantiable class.");
    }

}
