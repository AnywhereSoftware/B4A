
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
 
 package anywheresoftware.b4a.gps;

import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus.NmeaListener;
import android.os.Bundle;
import android.util.Log;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;

/**
 * The main object that raises the GPS events.
 *Note that this library requires Android 2.0 or above.
 */
@ShortName("GPS")
@Permissions(values={"android.permission.ACCESS_FINE_LOCATION"})
@Events(values={"LocationChanged (Location1 As Location)",
		"UserEnabled (Enabled As Boolean)",
		"GpsStatus (Satellites As List)",
		"NMEA (TimeStamp As Long, Sentence As String)"})
@Version(1.20f)
public class GPS implements CheckForReinitialize{
	private LocationManager locationManager;
	private String event;
	private LocationListener listener;
	private GpsStatus.Listener glistener;
	private GpsStatus.NmeaListener nmeaListener;
	public void Initialize(String EventName) {
		event = EventName.toLowerCase(BA.cul) + "_";
		locationManager = (LocationManager) BA.applicationContext.getSystemService(Context.LOCATION_SERVICE);
		
	}
	@Override
	public boolean IsInitialized() {
		return event != null;
	}
	/**
	 * The GPS library allows you to get information from the phone's GPS device.
	 *See the <link>GPS tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6592-gps-tutorial.html</link> for more information about this library.
	 */
	public static void LIBRARY_DOC() {
		
	}
	/**
	 * Starts listening for events.
	 *MinimumTime - The shortest period (measured in milliseconds) between events. Pass 0 for highest frequency.
	 *MinimumDistance - The shortest change in distance (measured in meters) for which to raise events. Pass 0 for highest frequency.
	 */
	@RaisesSynchronousEvents
	public void Start(final BA ba, long MinimumTime, float MinimumDistance) {
		listener = new LocationListener() {

			@Override
			public void onLocationChanged(Location arg0) {
				LocationWrapper lw = new LocationWrapper();
				lw.setObject(arg0);
				ba.raiseEvent(null, event + "locationchanged", lw);
			}

			@Override
			public void onProviderDisabled(String arg0) {
				ba.raiseEvent(null, event + "userenabled", false);
			}

			@Override
			public void onProviderEnabled(String arg0) {
				ba.raiseEvent(null, event + "userenabled", true);
			}

			@Override
			public void onStatusChanged(String arg0, int arg1,
					Bundle arg2) {
				
			}
		};
		if (ba.subExists(event + "gpsstatus")) {
			glistener = new GpsStatus.Listener() {
				GpsStatus status;
				List list = new List();
				{
					list.Initialize();
				}
				@Override
				public void onGpsStatusChanged(int arg0) {
					status = locationManager.getGpsStatus(status);
					list.Clear();
					if (status != null && status.getSatellites() != null)
					for (GpsSatellite gs : status.getSatellites()) {
						list.Add(gs);
					}
					ba.raiseEvent(null, event + "gpsstatus", list);
				}
				
			};
			locationManager.addGpsStatusListener(glistener);
		}
		if (ba.subExists(event + "nmea")) {
			nmeaListener = new NmeaListener() {

				@Override
				public void onNmeaReceived(long arg0, String arg1) {
					ba.raiseEvent(null, event + "nmea", arg0, arg1);
				}
				
			};
			locationManager.addNmeaListener(nmeaListener);
		}
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MinimumTime,
				MinimumDistance, listener); 
		if (getGPSEnabled()) //only disabled gets called automatically.
			listener.onProviderEnabled(null);
		
	}
	/**
	 * Stops listening to the GPS. You will usually want to call Stop inside Sub Activity_Pause.
	 */
	public void Stop() {
		if (listener != null)
			locationManager.removeUpdates(listener);
		listener = null;
		if (glistener != null)
			locationManager.removeGpsStatusListener(glistener);
		glistener = null;
		if (nmeaListener != null)
			locationManager.removeNmeaListener(nmeaListener);
		nmeaListener = null;
	}
	/**
	 * Returns the intent that is used to show the global locations settings.
	 *Example:<code>
	 *If GPS1.GPSEnabled = False Then StartActivity(GPS1.LocationSettingsIntent)</code>
	 */
	public Intent getLocationSettingsIntent() {
		return new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	}
	/**
	 * Tests whether the user has enabled the GPS.
	 */
	public boolean getGPSEnabled() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
}
