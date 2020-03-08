
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
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd.OnAppInstallAdLoadedListener;
import com.google.android.gms.ads.formats.NativeContentAd.OnContentAdLoadedListener;

@Permissions(values={"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"})
@ShortName("NativeAds")
@ActivityObject
public class NativeAds {
	@Hide
	AdLoader loader;
	public void Initialize(BA ba, String EventName, String PublisherId) {
	}
	public void test(BA ba) {
		
		AdLoader adLoader = new AdLoader.Builder(ba.context, "ca-app-pub-3940256099942544/2247696110")
	    	.forAppInstallAd(new OnAppInstallAdLoadedListener() {
	        @Override
	        public void onAppInstallAdLoaded(NativeAppInstallAd appInstallAd) {
	            BA.Log("asdasd" + appInstallAd);
	        }
	    })
	    .forContentAd(new OnContentAdLoadedListener() {
	        @Override
	        public void onContentAdLoaded(NativeContentAd contentAd) {
	        	BA.Log("asdasdasd" + contentAd);
	        }
	    })
	    .withAdListener(new AdListener() {
	        @Override
	        public void onAdFailedToLoad(int errorCode) {
	            BA.LogError("error: " + errorCode);
	        }
	    })
	    .withNativeAdOptions(new NativeAdOptions.Builder()
	            // Methods in the NativeAdOptions.Builder class can be
	            // used here to specify individual options settings.
	            .build())
	    .build();
		adLoader.loadAd(new AdRequest.Builder().build());
	}
}
