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

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Utility methods for collecting consent from users.
 */
public class ConsentInformation {
    private static class ConsentInfoUpdateResponse {
        boolean success;
        String responseInfo;

        ConsentInfoUpdateResponse(boolean success, String responseInfo) {
            this.success = success;
            this.responseInfo = responseInfo;
        }
    }
    private static final String MOBILE_ADS_SERVER_URL =
            "https://adservice.google.com/getconfig/pubvendors";
    private static final String TAG = "ConsentInformation";
    private static final String PREFERENCES_FILE_KEY = "mobileads_consent";
    private static final String CONSENT_DATA_KEY = "consent_string";
    private static ConsentInformation instance;

    private final Context context;
    private List<String> testDevices;
    private String hashedDeviceId;
    private DebugGeography debugGeography;

    private ConsentInformation(Context context) {
        this.context = context.getApplicationContext();
        this.debugGeography = DebugGeography.DEBUG_GEOGRAPHY_DISABLED;
        this.testDevices = new ArrayList<String>();
        this.hashedDeviceId = getHashedDeviceId();
    }

    public static synchronized ConsentInformation getInstance(Context context) {
        if (instance == null) {
            instance = new ConsentInformation(context);
        }
        return instance;
    }

  protected String getHashedDeviceId() {
        ContentResolver contentResolver = context.getContentResolver();
        String androidId =
            contentResolver == null
                ? null
                : Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        return md5(((androidId == null) || isEmulator()) ? "emulator" : androidId);
    }

    /** Return the MD5 hash of a string. */
    private String md5(String string) {
        // Old devices have a bug where OpenSSL can leave MessageDigest in a bad state, but trying
        // multiple times seems to clear it.
        for (int i = 0; i < 3 /** max attempts */; ++i) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(string.getBytes());
        return String.format("%032X", new BigInteger(1, md5.digest()));
            } catch (NoSuchAlgorithmException e) {
                // Try again.
            } catch (ArithmeticException ex) {
                return null;
            }
        }
        return null;
    }

    protected void setHashedDeviceId(String hashedDeviceId) {
        this.hashedDeviceId = hashedDeviceId;
    }

    private boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk".equals(Build.PRODUCT);
    }

    /** Returns if the current device is a designated debug device. */
    public boolean isTestDevice() {
        return isEmulator() || this.testDevices.contains(this.hashedDeviceId);
    }

    /**
     * Registers a device as a test device. Test devices will respect debug geography settings to
     * enable easier testing. Test devices must be added individually so that debug geography
     * settings won't accidentally get released to all users.
     * <p>You can access the hashedDeviceId from logcat once your app calls
     * requestConsentInfoUpdate.</p>
     *
     * @param hashedDeviceId The hashed device id that should be considered a debug device.
     */
    public void addTestDevice(String hashedDeviceId) {
        this.testDevices.add(hashedDeviceId);
    }

    /** Returns the location that's set for testing purposes. */
    public DebugGeography getDebugGeography() {
        return this.debugGeography;
    }

    /**
     * Sets the location of the device for testing purposes.
     *
     * @param debugGeography The location to be used for testing purposes.
     */
    public void setDebugGeography(DebugGeography debugGeography) {
        this.debugGeography = debugGeography;
    }

    private static class AdNetworkLookupResponse {
        @SerializedName("ad_network_id")
        private String id;

        @SerializedName("company_ids")
        private List<String> companyIds;

        @SerializedName("lookup_failed")
        private boolean lookupFailed;

        @SerializedName("not_found")
        private boolean notFound;

        @SerializedName("is_npa")
        private boolean isNPA;
    }

    /**
     * Describes a consent update server response.
     */
    protected static class ServerResponse {
        List<AdProvider> companies;

        @SerializedName("ad_network_ids")
        List<AdNetworkLookupResponse> adNetworkLookupResponses;

        @SerializedName("is_request_in_eea_or_unknown")
        Boolean isRequestLocationInEeaOrUnknown;
    }

    private static class ConsentInfoUpdateTask extends
        AsyncTask<Void, Void, ConsentInfoUpdateResponse> {

        private static final String UPDATE_SUCCESS = "Consent update successful.";

        private final String url;
        private final ConsentInformation consentInformation;
        private final List<String> publisherIds;
        private final ConsentInfoUpdateListener listener;

        ConsentInfoUpdateTask(
            String url,
            ConsentInformation consentInformation,
            List<String> publisherIds,
            ConsentInfoUpdateListener listener) {
            this.url = url;
            this.listener = listener;
            this.publisherIds = publisherIds;
            this.consentInformation = consentInformation;
        }

        private String readStream(InputStream inputStream) {
            byte[] contents = new byte[1024];
            int bytesRead;
            StringBuilder strFileContents = new StringBuilder();

            InputStream stream = new BufferedInputStream(inputStream);
            try {
                while ((bytesRead = stream.read(contents)) != -1) {
                    strFileContents.append(new String(contents, 0, bytesRead));
                }
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
                return null;
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }

            return strFileContents.toString();
        }

        private ConsentInfoUpdateResponse makeConsentLookupRequest(String urlString) {
            try {
                URL url = new URL(urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String responseString = readStream(urlConnection.getInputStream());
                    urlConnection.disconnect();
                    consentInformation.updateConsentData(responseString, publisherIds);
                    return new ConsentInfoUpdateResponse(true, UPDATE_SUCCESS);
                } else {
                    return new ConsentInfoUpdateResponse(
                        false, urlConnection.getResponseMessage());
                }
            } catch (Exception e) {
                return new ConsentInfoUpdateResponse(false, e.getLocalizedMessage());
            }
        }

        @Override
        public ConsentInfoUpdateResponse doInBackground(Void... unused) {
            String publisherIdsString = TextUtils.join(",", this.publisherIds);
            ConsentData consentData = consentInformation.loadConsentData();
            Uri.Builder uriBuilder =
                Uri.parse(url)
                    .buildUpon()
                    .appendQueryParameter("pubs", publisherIdsString)
                    .appendQueryParameter("es", "2")
                    .appendQueryParameter("plat", consentData.getSDKPlatformString())
                    .appendQueryParameter("v", consentData.getSDKVersionString());
            if (consentInformation.isTestDevice()
                && consentInformation.getDebugGeography()
                   != DebugGeography.DEBUG_GEOGRAPHY_DISABLED) {
                uriBuilder =
                    uriBuilder.appendQueryParameter(
                        "debug_geo",
                        consentInformation.getDebugGeography().getCode().toString());
            }
            return makeConsentLookupRequest(uriBuilder.build().toString());
        }

        @Override
        protected void onPostExecute(ConsentInfoUpdateResponse result) {
            if (result.success) {
                this.listener.onConsentInfoUpdated(consentInformation.getConsentStatus());
            } else {
                this.listener.onFailedToUpdateConsentInfo(result.responseInfo);
            }
        }
    }

    public synchronized void setTagForUnderAgeOfConsent(boolean underAgeOfConsent) {
        ConsentData consentData = this.loadConsentData();
        consentData.tagForUnderAgeOfConsent(underAgeOfConsent);
        saveConsentData(consentData);
    }

    public synchronized boolean isTaggedForUnderAgeOfConsent() {
        return this.loadConsentData().isTaggedForUnderAgeOfConsent();
    }

    public synchronized void reset() {
        SharedPreferences.Editor editor = context.getSharedPreferences(
            PREFERENCES_FILE_KEY, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        this.testDevices = new ArrayList<String>();
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    public void requestConsentInfoUpdate(String[] publisherIds,
                                         ConsentInfoUpdateListener listener) {
        requestConsentInfoUpdate(publisherIds, MOBILE_ADS_SERVER_URL, listener);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    protected void requestConsentInfoUpdate(String[] publisherIds, String url,
                                            ConsentInfoUpdateListener listener) {
        if (isTestDevice()) {
            Log.i(TAG, "This request is sent from a test device.");
        } else {
            Log.i(TAG, "Use ConsentInformation.getInstance(context).addTestDevice(\""
                      + getHashedDeviceId()
                      + "\") to get test ads on this device.");
        }
        new ConsentInfoUpdateTask(url, this, Arrays.asList(publisherIds), listener)
              .execute();
    }

    private void validatePublisherIds(final ServerResponse response) throws Exception {

        if (response.isRequestLocationInEeaOrUnknown == null) {
            throw new Exception("Could not parse Event FE preflight response.");
        }

        if (response.companies == null && response.isRequestLocationInEeaOrUnknown) {
            throw new Exception("Could not parse Event FE preflight response.");
        }

        if (!response.isRequestLocationInEeaOrUnknown) {
            return;
        }

        HashSet<String> lookupFailedPublisherIds = new HashSet<String>();
        HashSet<String> notFoundPublisherIds = new HashSet<String>();

        for (AdNetworkLookupResponse adNetworkLookupResponse : response.adNetworkLookupResponses) {
            if (adNetworkLookupResponse.lookupFailed) {
                lookupFailedPublisherIds.add(adNetworkLookupResponse.id);
            }

            if (adNetworkLookupResponse.notFound) {
                notFoundPublisherIds.add(adNetworkLookupResponse.id);
            }
        }

        if (lookupFailedPublisherIds.isEmpty() && notFoundPublisherIds.isEmpty()) {
            return;
        }

        StringBuilder errorString = new StringBuilder("Response error.");

        if (!lookupFailedPublisherIds.isEmpty()) {
            String lookupFailedPublisherIdsString = TextUtils.join(",", lookupFailedPublisherIds);
            errorString.append(
                String.format(" Lookup failure for: %s.", lookupFailedPublisherIdsString));
        }

        if (!notFoundPublisherIds.isEmpty()) {
            String notFoundPublisherIdsString = TextUtils.join(",", notFoundPublisherIds);
            errorString.append(
                String.format(" Publisher Ids not found: %s", notFoundPublisherIdsString));
        }

        throw new Exception(errorString.toString());
    }

    private HashSet<AdProvider> getNonPersonalizedAdProviders(List<AdProvider> adProviders,
                                          HashSet<String> nonPersonalizedAdProviderIds) {
        List<AdProvider> nonPersonalizedAdProviders = new ArrayList<AdProvider>();
        for (AdProvider adProvider : adProviders) {
            if (nonPersonalizedAdProviderIds.contains(adProvider.getId())) {
                nonPersonalizedAdProviders.add(adProvider);
            }
        }

        return new HashSet<AdProvider>(nonPersonalizedAdProviders);
    }

    private synchronized void updateConsentData(String responseString,
                                                List<String> publisherIds) throws Exception {
        ServerResponse response = new Gson().fromJson(responseString,
            ServerResponse.class);

        validatePublisherIds(response);

        boolean hasNonPersonalizedPublisherId = false;
        HashSet<String> nonPersonalizedAdProvidersIds = new HashSet<String>();

        if (response.adNetworkLookupResponses != null) {
            for (AdNetworkLookupResponse adNetworkLookupResponse :
                response.adNetworkLookupResponses) {
                if (!adNetworkLookupResponse.isNPA) {
                    continue;
                }

                hasNonPersonalizedPublisherId = true;
                List<String> companyIds = adNetworkLookupResponse.companyIds;
                if (companyIds != null) {
                    nonPersonalizedAdProvidersIds.addAll(companyIds);
                }
            }
        }

        HashSet<AdProvider> newAdProviderSet;
        if (response.companies == null) {
            newAdProviderSet = new HashSet<AdProvider>();
        } else if (hasNonPersonalizedPublisherId) {
            newAdProviderSet =
                getNonPersonalizedAdProviders(response.companies, nonPersonalizedAdProvidersIds);
        } else {
            newAdProviderSet = new HashSet<AdProvider>(response.companies);
        }

        ConsentData consentData = this.loadConsentData();

        boolean hasNonPersonalizedPublisherIdChanged =
            consentData.hasNonPersonalizedPublisherId() != hasNonPersonalizedPublisherId;

        consentData.setHasNonPersonalizedPublisherId(hasNonPersonalizedPublisherId);
        consentData.setRawResponse(responseString);
        consentData.setPublisherIds(new HashSet<String>(publisherIds));
        consentData.setAdProviders(newAdProviderSet);
        consentData.setRequestLocationInEeaOrUnknown(response.isRequestLocationInEeaOrUnknown);

        if (!response.isRequestLocationInEeaOrUnknown) {
            saveConsentData(consentData);
            return;
        }

        if (!consentData.getAdProviders().equals(consentData.getConsentedAdProviders())
            || hasNonPersonalizedPublisherIdChanged) {
            consentData.setConsentSource("sdk");
            consentData.setConsentStatus(ConsentStatus.UNKNOWN);
            consentData.setConsentedAdProviders(new HashSet<AdProvider>());
        }
        saveConsentData(consentData);
    }

    public synchronized List<AdProvider> getAdProviders() {
        ConsentData consentData = this.loadConsentData();
        return new ArrayList<AdProvider>(consentData.getAdProviders());
    }

    protected ConsentData loadConsentData() {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE_KEY,
            Context.MODE_PRIVATE);
        String consentDataString = sharedPref.getString(CONSENT_DATA_KEY, "");

        if (TextUtils.isEmpty(consentDataString)) {
            return new ConsentData();
        } else {
            return new Gson().fromJson(consentDataString, ConsentData.class);
        }
    }

    private void saveConsentData(ConsentData consentData) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE_KEY,
            Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String consentDataString = new Gson().toJson(consentData);
        editor.putString(CONSENT_DATA_KEY, consentDataString);
        editor.apply();
    }

    public boolean isRequestLocationInEeaOrUnknown() {
        ConsentData consentData = this.loadConsentData();
        return consentData.isRequestLocationInEeaOrUnknown();
    }

    public void setConsentStatus(ConsentStatus consentStatus) {
        this.setConsentStatus(consentStatus, "programmatic");
    }

    protected synchronized void setConsentStatus(ConsentStatus consentStatus, String source) {
        ConsentData consentData = this.loadConsentData();
        if (consentStatus == ConsentStatus.UNKNOWN) {
            consentData.setConsentedAdProviders(new HashSet<AdProvider>());
        } else {
            consentData.setConsentedAdProviders(consentData.getAdProviders());
        }

        consentData.setConsentSource(source);
        consentData.setConsentStatus(consentStatus);
        this.saveConsentData(consentData);
    }

    public synchronized ConsentStatus getConsentStatus() {
        ConsentData consentData = loadConsentData();
        return consentData.getConsentStatus();
    }
}
