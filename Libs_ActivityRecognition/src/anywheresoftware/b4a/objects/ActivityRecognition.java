
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

import java.util.ArrayList;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

@ShortName("ActivityRecognition")
@DependsOn(values={"google-play-services"})
@Permissions(values={"com.google.android.gms.permission.ACTIVITY_RECOGNITION"})
@Version(2.20f)
@Events(values={"Connected (Success As Boolean)"})
public class ActivityRecognition implements ConnectionCallbacks, OnConnectionFailedListener {
	private String eventName;
	private BA ba;
	@Hide public ActivityRecognitionApi client;
	@Hide public GoogleApiClient gac;
	private int detectionInterval;
	/**
	 * Initializes the object.
	 */
	public void Initialize(String EventName, BA ba) {
		this.eventName = EventName.toLowerCase(BA.cul);
		this.ba = ba;
		int res = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(BA.applicationContext);
		if (res == ConnectionResult.SUCCESS) {
			gac = new GoogleApiClient.Builder(ba.context).addApi(com.google.android.gms.location.ActivityRecognition.API)
				.addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
			
		}
	}
	@Hide
	@Override
	public void onConnectionSuspended(int arg0) {
		BA.Log("onConnectionSuspended: " + arg0);
		
	}
	/**
	 * Connects to the activity recognition services. Connected event will be raised when the connection is established (or failed).
	 *DetectionIntervalMilliseconds - Detection interval.
	 */
	public void Connect(int DetectionIntervalMilliseconds) {
		this.detectionInterval = DetectionIntervalMilliseconds;
		if (gac == null)
			ba.raiseEvent(this, eventName + "_connected", false);
		else
			gac.connect();

	}
	/**
	 * Stops detection.
	 */
	public void Stop() {
		Intent intent = new Intent(ba.context, RecognitionService.class);
		PendingIntent callbackIntent = PendingIntent.getService(ba.context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(gac, callbackIntent);
	}
	@Hide
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		BA.LogError("failed to connect: " + arg0.toString());
		ba.raiseEvent(this, eventName + "_connected", false);
	}
	@Hide
	@Override
	public void onConnected(Bundle arg0) {
		Intent intent = new Intent(ba.context, RecognitionService.class);
		PendingIntent callbackIntent = PendingIntent.getService(ba.context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(gac, detectionInterval, callbackIntent);
		ba.raiseEvent(this, eventName + "_connected", true);

	}
	
	@Hide
	public static class RecognitionService extends IntentService {
		public RecognitionService() {
			super("RecognitionService");
		}
		@Override
		public void onCreate() {
			super.onCreate();
			try {
				Class.forName(getPackageName() + ".recognitionservice");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				BA.LogError("RecognitionService not found.");
			}
		}


		@Override
		protected void onHandleIntent(Intent arg0) {
			try {
				
				Intent i = new Intent(this, Class.forName(getPackageName() + ".recognitionservice"));
				i.setAction("activity_recognition");
				if (ActivityRecognitionResult.hasResult(arg0) == false)
					return;
				ActivityRecognitionResult arr = ActivityRecognitionResult.extractResult(arg0);
				ArrayList<Object> types = new ArrayList<Object>();
				ArrayList<Object> confs = new ArrayList<Object>();
				for (DetectedActivity da : arr.getProbableActivities()) {
					types.add(getNameFromType(da.getType()));
					confs.add(da.getConfidence());
				}
				i.putExtra("types", types);
				i.putExtra("confidence_values", confs);
				startService(i);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		private String getNameFromType(int activityType) {
			switch(activityType) {
			case DetectedActivity.IN_VEHICLE:
				return "in_vehicle";
			case DetectedActivity.ON_BICYCLE:
				return "on_bicycle";
			case DetectedActivity.ON_FOOT:
				return "on_foot";
			case DetectedActivity.STILL:
				return "still";
			case DetectedActivity.UNKNOWN:
				return "unknown";
			case DetectedActivity.TILTING:
				return "tilting";
			case DetectedActivity.RUNNING:
				return "running";
			case DetectedActivity.WALKING:
				return "walking";
				
			}
			return "unknown-" + activityType;
		}

	}
	
}
