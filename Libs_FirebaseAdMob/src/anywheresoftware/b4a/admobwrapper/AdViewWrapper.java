
/*
 * Copyright 2010 - 2021 Anywhere Software (www.b4x.com)
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

import android.os.Bundle;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.ViewWrapper;
import anywheresoftware.b4a.objects.collections.List;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

@Version(3.00f)
@ShortName("AdView")
@Events(values={"ReceiveAd", "FailedToReceiveAd (ErrorCode As String)",
		"AdScreenDismissed", "PresentScreen"})
	@ActivityObject
	@DontInheritEvents
	@Permissions(values={"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "com.google.android.gms.permission.AD_ID"})
	@DependsOn(values={"com.google.firebase:firebase-ads", "gson-2.8.5", "GoogleConsent.aar"})
	public class AdViewWrapper extends ViewWrapper<AdView> {
	/**
	 * 320dip x 50dip (default size)
	 */
	public static Object SIZE_BANNER = AdSize.BANNER;
	/**
	 * 468dip x 60dip - tablet only
	 */
	public static Object SIZE_IAB_BANNER = AdSize.FULL_BANNER;
	/**
	 * 728dip x 90dip - tablet only
	 */
	public static Object SIZE_IAB_LEADERBOARD = AdSize.LEADERBOARD;
	/**
	 * 300dip x 250dip - tablet only
	 */
	public static Object SIZE_IAB_MRECT = AdSize.MEDIUM_RECTANGLE;
	/**
	 * Ad will use the full available width automatically.
	 *You can use this code to add such an ad to the bottom of the screen:
	 *<code>
	 *Adview1.Initialize2("Ad", "xxxxxxxx", AdView1.SIZE_SMART_BANNER)
	 *Dim height As Int
	 *If GetDeviceLayoutValues.ApproximateScreenSize < 6 Then
	 *    'phones
	 *    If 100%x > 100%y Then height = 32dip Else height = 50dip
	 *Else
	 *    'tablets
	 *    height = 90dip
	 *End If
	 *Activity.AddView(AdView1, 0dip, 100%y - height, 100%x, height)</code>
	 */
	public static Object SIZE_SMART_BANNER = AdSize.SMART_BANNER;
	/**
	 * Initializes the AdView using the default 320dip x 50dip size.
	 *EventName - Name of Subs that will handle the events.
	 *PublisherId - The publisher id you received from AdMob.
	 */
	public void Initialize(final BA ba, String EventName, String PublisherId) {
		Initialize2(ba, EventName, PublisherId, AdSize.BANNER);
	}
	/**
	 * Initializes the AdView.
	 *EventName - Name of Subs that will handle the events.
	 *AdUnitId - The Ad unit id received from AdMob.
	 *Size - One of the SIZE constants.
	 */
	public void Initialize2(final BA ba, String EventName, String AdUnitId, Object Size) {
		AdView ad = new AdView(ba.activity);
		ad.setAdSize((com.google.android.gms.ads.AdSize)Size);
		ad.setAdUnitId(AdUnitId);
		setObject(ad);
		super.Initialize(ba, EventName);
		final String eventName = EventName.toLowerCase(BA.cul);
		getObject().setAdListener(new AdListener() {

			@Override
			public void onAdFailedToLoad(LoadAdError e){
				ba.raiseEventFromDifferentThread(getObject(), null, 0, eventName + "_failedtoreceivead",false, 
						new Object[] { String.valueOf(e)});
			}
			@Override
			public void onAdLoaded() {
				ba.raiseEventFromDifferentThread(getObject(), null, 0, eventName + "_receivead", false, null);
			}
			@Override
			public void onAdClosed() {
				ba.raiseEventFromDifferentThread(getObject(), null, 0, eventName + "_adscreendismissed", false, null);
			}
			
			@Override
			public void onAdOpened() {
				ba.raiseEventFromDifferentThread(getObject(), null, 0, eventName + "_presentscreen", false, null);

			}
		});
	}

	/**
	 * Sends a request to AdMob, requesting an ad.
	 */
	public void LoadAd() {
		com.google.android.gms.ads.AdRequest req = new com.google.android.gms.ads.AdRequest.Builder().build();
		getObject().loadAd(req);
	}
	/**
	 * Requests an ad configured with AdRequestBuilder.
	 */
	public void LoadAdWithBuilder(AdRequestBuilderWrapper Builder) {
		getObject().loadAd(Builder.getObject().build());
	}

	/**
	 *Should be called from Activity_Pause. 
	 */
	public void Pause() {
		getObject().pause();
	}
	/**
	 *Should be called from Activity_Resume.
	 */
	public void Resume() {
		getObject().resume();
	}
	
	
	
	@ShortName("InterstitialAd")
	@Events(values={"ReceiveAd", "FailedToReceiveAd (ErrorCode As String)",
			"AdClosed", "AdOpened"})
	@ActivityObject
	public static class InterstitialAdWrapper {
		@Hide
		public InterstitialAd loadedAd;
		private String AdUnitId;
		private String EventName;
		/**
		 * Initializes the object.
		 *EventName - Set the subs that will handle the events.
		 *AdUnitId - The ad unit id. Test id: <code>ca-app-pub-3940256099942544/1033173712</code>
		 */
		public void Initialize(final BA ba, String EventName, String AdUnitId) {
			this.EventName = EventName.toLowerCase(BA.cul);
			this.AdUnitId = AdUnitId;
			
		}
		public boolean IsInitialized() {
			return this.AdUnitId != null;
		}
		/**
		 * Requests an ad. The AdLoaded event will be raised.
		 */
		public void LoadAd(BA ba) {
			LoadAdWithBuilder(ba, new AdRequest.Builder());
		}
		/**
		 * Requests an ad configured with AdRequestBuilder.
		 */
		public void LoadAdWithBuilder(final BA ba, AdRequest.Builder AdRequestBuilder) {
			this.loadedAd = null;
			InterstitialAd.load(ba.context, AdUnitId, AdRequestBuilder.build(), new InterstitialAdLoadCallback() {
				 @Override
			      public void onAdLoaded(InterstitialAd interstitialAd) {
					 loadedAd = interstitialAd;
					 ba.raiseEventFromDifferentThread(InterstitialAdWrapper.this, null, 0, EventName + "_receivead", false, null);
				 }
				 @Override
			      public void onAdFailedToLoad(LoadAdError loadAdError) {
					 ba.raiseEventFromDifferentThread(InterstitialAdWrapper.this, null, 0, EventName + "_failedtoreceivead", false, new Object[] {String.valueOf(loadAdError)});
				 }
			});
		}
		/**
		 * Tests whether there is an ad ready to be shown.
		 */
		public boolean getReady() {
			return loadedAd != null;
		}
		/**
		 * Shows the loaded ad.
		 */
		public void Show(final BA ba) {
			loadedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
				@Override
				  public void onAdDismissedFullScreenContent() {
					ba.raiseEventFromDifferentThread(InterstitialAdWrapper.this, null, 0, EventName + "_adclosed", false, null);
				  }

				  @Override
				  public void onAdFailedToShowFullScreenContent(AdError adError) {
				    BA.Log("The ad failed to show: " + adError);
				  }

				  @Override
				  public void onAdShowedFullScreenContent() {
				    loadedAd = null;
				    ba.raiseEventFromDifferentThread(InterstitialAdWrapper.this, null, 0, EventName + "_adopened", false, null);
				  }

			});
			loadedAd.show(ba.activity);
		}
	}
	
	@ShortName("MobileAds")
	public static class MobileAdsWrapper {
		/**
		 * Should be called before requesting an ad. Can be used with Wait For like this:
		 *<code>Wait For (MobileAds.Initialize) MobileAds_Ready</code>
		 */
		public Object Initialize(final BA ba) {
			MobileAds.initialize(BA.applicationContext, new OnInitializationCompleteListener() {
				
				@Override
				public void onInitializationComplete(InitializationStatus arg0) {
					ba.raiseEventFromUI(MobileAdsWrapper.this, "mobileads_ready");
				}
			});
			return this;
		}
		public Object CreateRequestConfigurationBuilder(List TestDeviceIds) {
			RequestConfiguration.Builder builder = new RequestConfiguration.Builder();
			builder.setTestDeviceIds((java.util.List)TestDeviceIds.getObject());
			return builder;
		}
		public void SetConfiguration (Object ConfigurationBuilder) {
			MobileAds.setRequestConfiguration(((RequestConfiguration.Builder)ConfigurationBuilder).build());
		}
	}

	@ShortName("AdRequestBuilder")
	public static class AdRequestBuilderWrapper extends AbsObjectWrapper<AdRequest.Builder> {
		
		public AdRequestBuilderWrapper Initialize() {
			setObject(new AdRequest.Builder());
			return this;
		}
		/**
		 * Request non-personalized ads.
		 */
		public AdRequestBuilderWrapper NonPersonalizedAds() {
			Bundle extras = new Bundle();
			extras.putString("npa", "1");
			getObject().addNetworkExtrasBundle(AdMobAdapter.class, extras);
			return this;
		}
		
	}
}

