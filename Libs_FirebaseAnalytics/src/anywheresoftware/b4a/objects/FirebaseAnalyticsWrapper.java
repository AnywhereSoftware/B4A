
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
 
 package anywheresoftware.b4a.objects;

import java.io.Serializable;
import java.util.Map.Entry;

import android.os.Bundle;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;

@ShortName("FirebaseAnalytics")
@Version(2.00f)
@DependsOn(values={"com.google.firebase:firebase-crash", "com.google.firebase:firebase-analytics", 
					"com.google.firebase:firebase-core"})
public class FirebaseAnalyticsWrapper extends AbsObjectWrapper<FirebaseAnalytics>{
	/**
	 * Initializes the object. FirebaseAnalytics should be a process global variable in the Starter service. It should be initialized in Service_Create sub.
	 */
	public void Initialize() {
		setObject(FirebaseAnalytics.getInstance(BA.applicationContext));
	}
	/**
	 * Sends an event to the analytics service.
	 *EventName - Event name.
	 *Parameters - Map of parameters. Pass Null if not needed.
	 */
	public void SendEvent(String EventName, Map Parameters) {
		Bundle bundle = new Bundle();
		if (Parameters != null && Parameters.IsInitialized()) {
			for (Entry<Object, Object> e : Parameters.getObject().entrySet()) {
				bundle.putSerializable(String.valueOf(e.getKey()), (Serializable)e.getValue());
			}
		}
		getObject().logEvent(EventName, bundle);
	}
	/**
	 * Tests whether Google Play Services are available on the device.
	 */
	public boolean getIsGooglePlayServicesAvailable() {
		return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(BA.applicationContext) == ConnectionResult.SUCCESS;
	}
}
