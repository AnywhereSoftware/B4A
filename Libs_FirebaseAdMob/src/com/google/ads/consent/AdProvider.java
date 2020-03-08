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

/**
 * Ad provider information.
 */
public class AdProvider {

    @SerializedName("company_id")
    private String id;

    @SerializedName("company_name")
    private String name;

    @SerializedName("policy_url")
    private String privacyPolicyUrlString;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrivacyPolicyUrlString() {
        return privacyPolicyUrlString;
    }

    public void setPrivacyPolicyUrlString(String privacyPolicyUrlString) {
        this.privacyPolicyUrlString = privacyPolicyUrlString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AdProvider that = (AdProvider) o;

        return id.equals(that.id) && name.equals(that.name)
            && privacyPolicyUrlString.equals(that.privacyPolicyUrlString);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + privacyPolicyUrlString.hashCode();
        return result;
    }
}
