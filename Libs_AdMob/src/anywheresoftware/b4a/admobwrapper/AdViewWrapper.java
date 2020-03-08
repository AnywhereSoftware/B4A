
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

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.ViewWrapper;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * The AdMob library allows you to add ads served by AdMob to your application.
 *See the <link>AdMob Tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/7300-admob-tutorial-add-ads-your-application.html</link>.
 *This library requires some additional configuration as described in the tutorial.
 *By default the view must be set to a size of 320dip x 50dip. Otherwise it will not display.
 *Tablets support three additional sizes: IAB_BANNER - 468dip x 60dip, IAB_MRECT - 300dip x 250dip and IAB_LEADERBOARD - 728dip x 90dip.
 *The size of AdView must match the selected size.
 */
@Version(2.10f)
@ShortName("AdView")
@Events(values={"ReceiveAd", "FailedToReceiveAd (ErrorCode As String)",
		"AdScreenDismissed", "PresentScreen"})
@ActivityObject
@DontInheritEvents
@Permissions(values={"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"})
@DependsOn(values={"com.google.android.gms:play-services-ads"})
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
	 *PublisherId - The publisher id you received from AdMob.
	 *Size - One of the SIZE constants.
	 */
	public void Initialize2(final BA ba, String EventName, String PublisherId, Object Size) {
		AdView ad = new AdView(ba.activity);
		ad.setAdSize((com.google.android.gms.ads.AdSize)Size);
		ad.setAdUnitId(PublisherId);
		setObject(ad);
		super.Initialize(ba, EventName);
		final String eventName = EventName.toLowerCase(BA.cul);
		getObject().setAdListener(new AdListener() {

			@Override
			public void onAdFailedToLoad(int e){
				ba.raiseEvent(getObject(), eventName + "_failedtoreceivead", String.valueOf(e));
			}
			@Override
			public void onAdLoaded() {
				ba.raiseEvent(getObject(), eventName + "_receivead");
			}
			@Override
			public void onAdClosed() {
				ba.raiseEventFromDifferentThread(getObject(), null, 0, eventName + "_adscreendismissed", false, null);
			}
			@Override
			public void onAdLeftApplication() {
				//
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
}

