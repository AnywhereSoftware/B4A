/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.vending.licensing;

/**
 * Policy used by {@link LicenseChecker} to determine whether a user should
 * have access to the application.
 */
public interface Policy {

    /**
     * Result of a license check.
     */
    public enum LicenseResponse {
        /**
         * User is licensed to use the app.
         */
        LICENSED,
        /**
         * User is not licensed to use the app.
         */
        NOT_LICENSED,
        /**
         * Retryable error. e.g. no network or application is over request
         * quota.
         */
        RETRY,
    }

    /**
     * Provide results from contact with the license server. Retry counts are incremented if
     * the current value of response is RETRY. Results will be used for any future policy
     * decisions.
     *
     * @param response the result from validating the server response
     * @param rawData the raw server response data, can be null for RETRY
     */
    void processServerResponse(LicenseResponse response, ResponseData rawData);

    /**
     * Check if the user should be allowed access to the application.
     */
    boolean allowAccess();
}
