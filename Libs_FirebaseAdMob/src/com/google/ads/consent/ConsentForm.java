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

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * A Google rendered form for collecting consent from a user.
 */
public class ConsentForm {

    private final ConsentFormListener listener;
    private final Context context;
    private final boolean personalizedAdsOption;
    private final boolean nonPersonalizedAdsOption;
    private final boolean adFreeOption;
    private final URL appPrivacyPolicyURL;
    private final Dialog dialog;
    private final WebView webView;
    private LoadState loadState;

    private enum LoadState {
        NOT_READY,
        LOADING,
        LOADED
    }

    private ConsentForm(Builder builder) {
        this.context = builder.context;

        if (builder.listener == null) {
            this.listener = new ConsentFormListener() {};
        } else {
            this.listener = builder.listener;
        }

        this.personalizedAdsOption = builder.personalizedAdsOption;
        this.nonPersonalizedAdsOption = builder.nonPersonalizedAdsOption;
        this.adFreeOption = builder.adFreeOption;
        this.appPrivacyPolicyURL = builder.appPrivacyPolicyURL;
        this.dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.loadState = LoadState.NOT_READY;

        this.webView = new WebView(context);
        this.webView.setBackgroundColor(Color.TRANSPARENT);
        this.dialog.setContentView(webView);
        this.dialog.setCancelable(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(
            new WebViewClient() {

                boolean isInternalRedirect;

                private boolean isConsentFormUrl(String url) {
                    return !TextUtils.isEmpty(url) && url.startsWith("consent://");
                }

                private void handleUrl(String url) {
                    if (!isConsentFormUrl(url)) {
                        return;
                    }

                    isInternalRedirect = true;
                    Uri uri = Uri.parse(url);
                    String action = uri.getQueryParameter("action");
                    String status = uri.getQueryParameter("status");
                    String browserUrl = uri.getQueryParameter("url");
                    if (action.equals("load_complete"))
                    	handleLoadComplete(status);
                    else if (action.equals("dismiss")) {
                    	isInternalRedirect = false;
                        handleDismiss(status);
                    } else if (action.equals("browser")){
                    	handleOpenBrowser(browserUrl);
                    }
                }

                @Override
                public void onLoadResource (WebView view, String url) {
                    handleUrl(url);
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (isConsentFormUrl(url)) {
                        handleUrl(url);
                        return true;
                    }
                    return false;
                }

                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (isConsentFormUrl(url)) {
                        handleUrl(url);
                        return true;
                    }
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if (!isInternalRedirect) {
                        updateDialogContent(view);
                    }
                    super.onPageFinished(view, url);
                }

                @Override
                public void onReceivedError(
                    WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    loadState = LoadState.NOT_READY;
                    listener.onConsentFormError(error.toString());
                }
            });
    }

    /**
     * Creates a new {@link Builder} for constructing a {@link ConsentForm}.
     */
    public static class Builder {

        private final Context context;
        private ConsentFormListener listener;
        private boolean personalizedAdsOption;
        private boolean nonPersonalizedAdsOption;
        private boolean adFreeOption;
        private final URL appPrivacyPolicyURL;

        public Builder(Context context, URL appPrivacyPolicyURL) {
            this.context = context;
            this.personalizedAdsOption = false;
            this.nonPersonalizedAdsOption = false;
            this.adFreeOption = false;
            this.appPrivacyPolicyURL = appPrivacyPolicyURL;

            if (this.appPrivacyPolicyURL == null) {
                throw new IllegalArgumentException("Must provide valid app privacy policy url"
                    + " to create a ConsentForm");
            }
        }

        public Builder withListener(ConsentFormListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder withPersonalizedAdsOption() {
            this.personalizedAdsOption = true;
            return this;
        }

        public Builder withNonPersonalizedAdsOption() {
            this.nonPersonalizedAdsOption = true;
            return this;
        }

        public Builder withAdFreeOption() {
            this.adFreeOption = true;
            return this;
        }

        public ConsentForm build() {
            return new ConsentForm(this);
        }
    }

    private static String getApplicationName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    private static String getAppIconURIString(Context context) {
        Drawable iconDrawable = context.getPackageManager().getApplicationIcon(context
            .getApplicationInfo());
        Bitmap bitmap = Bitmap.createBitmap(iconDrawable.getIntrinsicWidth(),
            iconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        iconDrawable.draw(canvas);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private static String createJavascriptCommand(String command, String argumentsJSON) {
        HashMap <String, Object> args = new HashMap <String, Object > ();
        args.put("info", argumentsJSON);
        HashMap <String, Object> wrappedArgs = new HashMap <String, Object > ();
        wrappedArgs.put("args", args);
        return String.format("javascript:%s(%s)", command, new Gson().toJson(wrappedArgs));
    }

    private void updateDialogContent(WebView webView) {
        HashMap <String, Object> formInfo = new HashMap<String, Object > ();
        formInfo.put("app_name", getApplicationName(context));
        formInfo.put("app_icon", getAppIconURIString(context));
        formInfo.put("offer_personalized", this.personalizedAdsOption);
        formInfo.put("offer_non_personalized", this.nonPersonalizedAdsOption);
        formInfo.put("offer_ad_free", this.adFreeOption);
        formInfo.put("is_request_in_eea_or_unknown",
            ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown());
        formInfo.put("app_privacy_url", this.appPrivacyPolicyURL);
        ConsentData consentData = ConsentInformation.getInstance(context).loadConsentData();

        formInfo.put("plat", consentData.getSDKPlatformString());
        formInfo.put("consent_info", consentData);

        String argumentsJSON = new Gson().toJson(formInfo);
        String javascriptCommand = createJavascriptCommand("setUpConsentDialog",
            argumentsJSON);
        webView.loadUrl(javascriptCommand);
    }

    public void load() {
        if (this.loadState == LoadState.LOADING) {
            listener.onConsentFormError("Cannot simultaneously load multiple consent forms.");
            return;
        }

        if (this.loadState == LoadState.LOADED) {
            listener.onConsentFormLoaded(this);
            return;
        }

        this.loadState = LoadState.LOADING;
        this.webView.loadUrl("file:///android_asset/consentform.html");
    }

    private void handleLoadComplete(String status) {
        if (TextUtils.isEmpty(status)) {
            this.loadState = LoadState.NOT_READY;
            listener.onConsentFormError("No information");
        } else if (status.contains("Error")) {
            this.loadState = LoadState.NOT_READY;
            listener.onConsentFormError(status);
        } else {
            this.loadState = LoadState.LOADED;
            listener.onConsentFormLoaded(this);
        }
    }

    private void handleOpenBrowser(String urlString) {
        if (TextUtils.isEmpty(urlString)) {
            listener.onConsentFormError("No valid URL for browser navigation.");
            return;
        }

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            context.startActivity(browserIntent);
        } catch (ActivityNotFoundException exception) {
            listener.onConsentFormError("No Activity found to handle browser intent.");
        }
    }

    private void handleDismiss(String status) {
        this.loadState = LoadState.NOT_READY;
        dialog.dismiss();

        if (TextUtils.isEmpty(status)) {
            listener.onConsentFormError("No information provided.");
            return;
        }

        if (status.contains("Error")) {
            listener.onConsentFormError(status);
            return;
        }

        boolean userPrefersAdFree = false;
        ConsentStatus consentStatus;
         if (status.equals("personalized"))
        	consentStatus = ConsentStatus.PERSONALIZED;
        else if (status.equals("non_personalized"))
        		consentStatus = ConsentStatus.NON_PERSONALIZED;
        else if (status.equals("ad_free")) {
        	userPrefersAdFree = true;
            consentStatus = ConsentStatus.UNKNOWN;
        }
        else
        	consentStatus = ConsentStatus.UNKNOWN;

        ConsentInformation.getInstance(context).setConsentStatus(consentStatus, "form");
        listener.onConsentFormClosed(consentStatus, userPrefersAdFree);
    }

    public void show() {
        if (this.loadState != LoadState.LOADED) {
            listener.onConsentFormError("Consent form is not ready to be displayed.");
            return;
        }

        if (ConsentInformation.getInstance(context).isTaggedForUnderAgeOfConsent()) {
            listener.onConsentFormError("Error: tagged for under age of consent");
            return;
        }

        this.dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        this.dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                listener.onConsentFormOpened();
            }
        });

        this.dialog.show();

        if (!this.dialog.isShowing()) {
            listener.onConsentFormError("Consent form could not be displayed.");
        }
    }

    public boolean isShowing() {
        return this.dialog.isShowing();
    }
}
