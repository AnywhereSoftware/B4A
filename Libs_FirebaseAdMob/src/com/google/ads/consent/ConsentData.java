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

import com.google.gson.annotations.SerializedName;
import java.util.HashSet;

class ConsentData {

    private static final String SDK_PLATFORM = "android";

    private static final String SDK_VERSION = "1.0.7";

    @SerializedName("providers")
    private HashSet<AdProvider> adProviders;

    @SerializedName("is_request_in_eea_or_unknown")
    private boolean isRequestLocationInEeaOrUnknown;

    @SerializedName("consented_providers")
    private HashSet<AdProvider> consentedAdProviders;

    @SerializedName("tag_for_under_age_of_consent")
    private Boolean underAgeOfConsent;

    @SerializedName("consent_state")
    private ConsentStatus consentStatus;

    @SerializedName("pub_ids")
    private HashSet<String> publisherIds;

    @SerializedName("has_any_npa_pub_id")
    private boolean hasNonPersonalizedPublisherId;

    @SerializedName("consent_source")
    private String consentSource;

    @SerializedName("version")
    private final String sdkVersionString;

    @SerializedName("plat")
    private final String sdkPlatformString;

    @SerializedName("raw_response")
    private String rawResponse;

    ConsentData() {
        this.adProviders = new HashSet<AdProvider>();
        this.consentedAdProviders = new HashSet<AdProvider>();
        this.publisherIds = new HashSet<String>();
        this.underAgeOfConsent = false;
        this.consentStatus = ConsentStatus.UNKNOWN;
        this.isRequestLocationInEeaOrUnknown = false;
        this.hasNonPersonalizedPublisherId = false;
        this.sdkVersionString = SDK_VERSION;
        this.sdkPlatformString = SDK_PLATFORM;
        this.rawResponse = "";
    }

    boolean isTaggedForUnderAgeOfConsent() {
        return underAgeOfConsent;
    }

    void tagForUnderAgeOfConsent(boolean underAgeOfConsent) {
        this.underAgeOfConsent = underAgeOfConsent;
    }

    HashSet<AdProvider> getAdProviders() {
        return adProviders;
    }

    void setAdProviders(HashSet<AdProvider> adProviders) {
        this.adProviders = adProviders;
    }

    ConsentStatus getConsentStatus() {
        return consentStatus;
    }

    void setConsentStatus(ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
    }

    HashSet<String> getPublisherIds() {
        return publisherIds;
    }

    void setPublisherIds(HashSet<String> publisherIds) {
        this.publisherIds = publisherIds;
    }

    boolean isRequestLocationInEeaOrUnknown() {
        return isRequestLocationInEeaOrUnknown;
    }

    void setRequestLocationInEeaOrUnknown(boolean eeaRequestLocationOrUnknown) {
        isRequestLocationInEeaOrUnknown = eeaRequestLocationOrUnknown;
    }

    HashSet<AdProvider> getConsentedAdProviders() {
        return consentedAdProviders;
    }

    void setConsentedAdProviders(HashSet<AdProvider> consentedAdProviders) {
        this.consentedAdProviders = consentedAdProviders;
    }

    boolean hasNonPersonalizedPublisherId() {
        return hasNonPersonalizedPublisherId;
    }

    void setHasNonPersonalizedPublisherId(boolean hasNonPersonalizedPublisherId) {
        this.hasNonPersonalizedPublisherId = hasNonPersonalizedPublisherId;
    }

    public String getSDKVersionString() {
        return this.sdkVersionString;
    }

    public String getSDKPlatformString() {
        return this.sdkPlatformString;
    }

    public String getConsentSource() {
        return consentSource;
    }

    public void setConsentSource(String consentSource) {
        this.consentSource = consentSource;
    }

    String getRawResponse() {
        return rawResponse;
    }

    void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}
