/*
 * Copyright 2018 Google LLC
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
package com.google.ads.consent;

/**
 * A listener to be provided to the {@link ConsentForm} for receiving notifications when a consent
 * form has been loaded, opened, closed, or encounters an error.
 */
public abstract class ConsentFormListener {
    public void onConsentFormLoaded(ConsentForm form) {}
    public void onConsentFormError(String reason) {}
    public void onConsentFormOpened() {}
    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {}
}
