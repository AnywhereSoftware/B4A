
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
 
 package anywheresoftware.b4a.admobwrapper;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;

@Events(values={"InfoUpdated (Success As Boolean)", "FormResult (Success As Boolean, UserPrefersAdFreeOption As Boolean)"})
@ShortName("ConsentManager")
public class ConsentManager {
	private BA ba;
	private String eventName;
	@Hide
	public ConsentInformation ci;
	public final String STATE_PERSONALIZED = ConsentStatus.PERSONALIZED.toString();
	public final String STATE_NON_PERSONALIZED = ConsentStatus.NON_PERSONALIZED.toString();
	public final String STATE_UNKNOWN = ConsentStatus.UNKNOWN.toString();
	private String ConsentState = "";
	public void Initialize(BA ba, String EventName) {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		ci = ConsentInformation.getInstance(ba.context);
	}
	/**
	 * Checks for the current consent state. Raises the InfoUpdated event.
	 *Example:<code>
	 *consent.RequestInfoUpdate(Array("pub-12633333333"))
	 *Wait For consent_InfoUpdated (Success As Boolean)
	 *If Success = False Then
	 *	Log($"Error getting consent state: ${LastException}"$)
	 *End If</code>
	 *
	 */
	public void RequestInfoUpdate(List PublisherIds) {
		ci.requestConsentInfoUpdate( PublisherIds.getObject().toArray(new String[PublisherIds.getSize()]), new ConsentInfoUpdateListener() {
			
			@Override
			public void onFailedToUpdateConsentInfo(String reason) {
				ba.setLastException(new Exception(reason));
				ConsentState = ci.getConsentStatus().toString();
				ba.raiseEventFromUI(ConsentManager.this, eventName + "_infoupdated", false);
				
			}
			@Override
			public void onConsentInfoUpdated(ConsentStatus consentStatus) {
				ConsentState = consentStatus.toString();
				ba.raiseEventFromUI(ConsentManager.this, eventName + "_infoupdated", true);
				
			}
		});
	}
	/**
	 * Returns true if the user is located in the European Economic Area or if the location is unknown.
	 */
	public boolean getIsRequestLocationInEeaOrUnknown() {
		return ci.isRequestLocationInEeaOrUnknown();
	}
	/**
	 * Gets or sets the current consent state. Value will be one of the STATE constants.
	 */
	public String getConsentState() {
		return ConsentState;
	}
	public void setConsentState(String s) {
		ConsentState = s;
		ci.setConsentStatus(ConsentStatus.valueOf(s));
	}
	/**
	 * Adds a test device id. You can see the current device id in the unfiltered logs.
	 *This allows setting the location with SetDebugGeography.
	 */
	public void AddTestDevice(String DeviceId) {
		ci.addTestDevice(DeviceId);
	}
	/**
	 * Sets the geographic location for the test devices.
	 */
	public void SetDebugGeography(boolean InEea) {
		ci.setDebugGeography(InEea ? DebugGeography.DEBUG_GEOGRAPHY_EEA : DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA);
	}
	/**
	 * Shows the consent form. Must be called from an activity.
	 *This method should be called when needed, after the current consent state becomes available.
	 *Raises the FormResult event.
	 * PrivacyURL - A link to your privacy URL page.
	 * PersonalizedOption - Whether to show the personalized option.
	 * NonPersonalizedOption - Whether to show the non-personalized option.
	 * AdFreeOption - Whether to show the ad free option.
	 */
	public void ShowConsentForm(final BA ba, String PrivacyURL, boolean PersonalizedOption, boolean NonPersonalizedOption, boolean AdFreeOption) throws MalformedURLException {
		ConsentForm.Builder builder = new ConsentForm.Builder(ba.activity == null ? ba.sharedProcessBA.activityBA.get().activity : ba.activity, new URL(PrivacyURL));
		if (PersonalizedOption)
			builder.withPersonalizedAdsOption();
		if (NonPersonalizedOption)
			builder.withNonPersonalizedAdsOption();
		if (AdFreeOption)
			builder.withAdFreeOption();
		builder.withListener(new ConsentFormListener() {
			@Override
			public void onConsentFormLoaded(ConsentForm form) {
				if (form.isShowing() == false && ba.isActivityPaused() == false)
					form.show();
			}
			@Override
			public void onConsentFormError(String reason) {
				ba.setLastException(new Exception(reason));
				ba.raiseEventFromUI(ConsentManager.this, eventName + "_formresult", false, false);
			}
		    @Override
			public void onConsentFormOpened() {}
		    @Override
			public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
		    	ConsentState = consentStatus.toString();
		    	ba.raiseEventFromUI(ConsentManager.this, eventName + "_formresult", true, Boolean.TRUE.equals(userPrefersAdFree));
		    }
		}).build().load();
	}
	
}
