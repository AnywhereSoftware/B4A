
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

import java.util.StringTokenizer;

import android.location.Location;
import android.location.LocationManager;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;

/**
 * A Location object holds various information about a specific GPS fix.
 *In most cases you will work with locations that are passed to the GPS LocationChanged event.
 *The location object can also be used to calculate distance and bearing to other locations. 
 */
@ShortName("Location")
public class LocationWrapper extends AbsObjectWrapper<Location>{
	/**
	 * Initializes an empty location object.
	 */
	public void Initialize() {
		Location l = new Location(LocationManager.GPS_PROVIDER);
		setObject(l);
	}
	/**
	 * Initializes the location object with the given Latitude and Longitude.
	 *Values can be formatted in any of the three formats:
	 *Degrees: [+-]DDD.DDDDD
	 *Minutes: [+-]DDD:MM.MMMMM (Minute = 1 / 60 of a degree)
	 *Seconds: [+-]DDD:MM:SS.SSSSS (Second = 1 / 3600 of a degree)
	 *Example:<code>
	 *Dim L1 As Location
	 *L1.Initialize2("45:30:30", "45:20:15")</code>
	 */
	public void Initialize2(String Latitude, String Longitude) {
		Initialize();
		getObject().setLatitude(convert(Latitude));
		getObject().setLongitude(convert(Longitude));
	}
	
	private static double convert(String coordinate) {
        // IllegalArgumentException if bad syntax
        if (coordinate == null) {
            throw new NullPointerException("coordinate");
        }

        boolean negative = false;
        if (coordinate.charAt(0) == '-') {
            coordinate = coordinate.substring(1);
            negative = true;
        }

        StringTokenizer st = new StringTokenizer(coordinate, ":");
        int tokens = st.countTokens();
        if (tokens < 1) {
            throw new IllegalArgumentException("coordinate=" + coordinate);
        }
        try {
            String degrees = st.nextToken();
            double val;
            if (tokens == 1) {
                val = Double.parseDouble(degrees);
                return negative ? -val : val;
            }

            String minutes = st.nextToken();
            int deg = Integer.parseInt(degrees);
            double min;
            double sec = 0.0;

            if (st.hasMoreTokens()) {
                min = Integer.parseInt(minutes);
                String seconds = st.nextToken();
                sec = Double.parseDouble(seconds);
            } else {
                min = Double.parseDouble(minutes);
            }

            boolean isNegative180 = negative && (deg == 180) &&
                (min == 0) && (sec == 0);

            // deg must be in [0, 179] except for the case of -180 degrees
            if ((deg < 0.0) || (deg > 179 && !isNegative180)) {
                throw new IllegalArgumentException("coordinate=" + coordinate);
            }
            if (min < 0 || min >= 60) {
                throw new IllegalArgumentException("coordinate=" +
                        coordinate);
            }
            if (sec < 0 || sec >= 60) {
                throw new IllegalArgumentException("coordinate=" +
                        coordinate);
            }

            val = deg*3600.0 + min*60.0 + sec;
            val /= 3600.0;
            return negative ? -val : val;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("coordinate=" + coordinate);
        }
    }
	/**
	 * Converts the given coordinate to a string formatted with the following format:
	 *[+-]DDD:MM.MMMMM (Minute = 1 / 60 of a degree)
	 */
	public static String ConvertToMinutes(double Coordinate) {
		return Location.convert(Coordinate, Location.FORMAT_MINUTES);
	}
	/**
	 * Converts the given coordinate to a string formatted with the following format:
	 *[+-]DDD:MM:SS.SSSSS (Minute = 1 / 60 of a degree, Second = 1 / 3600 of a degree)
	 */
	public static String ConvertToSeconds(double Coordinate) {
		return Location.convert(Coordinate, Location.FORMAT_SECONDS);
	}
	/**
	 * Gets or sets the fix latitude (degrees from -90 (South) to 90 (North)).
	 */
	public double getLatitude() {
		return getObject().getLatitude();
	}
	public void setLatitude(double value) {
		getObject().setLatitude(value);
	}
	
	/**
	 * Gets or sets the fix time.
	 */
	public long getTime() {
		return getObject().getTime();
	}
	public void setTime(long value) {
		getObject().setTime(value);
	}
	/**
	 * Gets or sets the fix altitude (meters).
	 */
	public double getAltitude() {
		return getObject().getAltitude();
	}
	public void setAltitude(double value) {
		getObject().setAltitude(value);
	}
	/**
	 * Returns true if the fix includes altitude value.
	 */
	public boolean getAltitudeValid() {
		return getObject().hasAltitude();
	}
	/**
	 * Gets or sets the fix longitude (degrees from -180 to 180, positive values represent the eastern hemisphere).
	 */
	public double getLongitude() {
		return getObject().getLongitude();
	}
	public void setLongitude(double value) {
		getObject().setLongitude(value);
	}
	/**
	 * Gets or sets the fix speed (meters / second).
	 */
	public float getSpeed() {
		return getObject().getSpeed();
	}
	public void setSpeed(float value) {
		getObject().setSpeed(value);
	}
	/**
	 * Returns true if the fix includes speed value.
	 */
	public boolean getSpeedValid() {
		return getObject().hasSpeed();
	}
	
	/**
	 * Gets or sets the fix accuracy (meters).
	 */
	public float getAccuracy() {
		return getObject().getAccuracy();
	}
	public void setAccuracy(float value) {
		getObject().setAccuracy(value);
	}
	/**
	 * Returns true if the fix includes accuracy value.
	 */
	public boolean getAccuracyValid() {
		return getObject().hasAccuracy();
	}
	/**
	 * Gets or sets the fix bearing East of true North.
	 */
	public float getBearing() {
		return getObject().getBearing();
	}
	public void setBearing(float value) {
		getObject().setBearing(value);
	}
	/**
	 * Returns true if the fix includes bearing value.
	 */
	public boolean getBearingValid() {
		return getObject().hasBearing();
	}
	/**
	 * Returns the distance to the given location, measured in meters.
	 */
	public float DistanceTo (Location TargetLocation) {
		return getObject().distanceTo(TargetLocation);
	}
	/**
	 * Returns the bearing to the given location.
	 */
	public float BearingTo (Location TargetLocation) {
		return getObject().bearingTo(TargetLocation);
	}
}
