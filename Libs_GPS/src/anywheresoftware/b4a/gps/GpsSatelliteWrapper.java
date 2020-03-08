
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

import android.location.GpsSatellite;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;

/**
 * The GPSSatellite object holds various information about a GPS satellite. A List with the available satellites is passed to the GpsStatus event.
 */
@ShortName("GPSSatellite")
public class GpsSatelliteWrapper extends AbsObjectWrapper<GpsSatellite>{
	/**
	 * Tests whether this satellite was used to calculate the most recent fix.
	 */
	public boolean getUsedInFix() {
		return getObject().usedInFix();
	}
	/**
     * Returns the signal to noise ratio for the satellite.
     */
	public float getSnr() {
		return getObject().getSnr();
	}
	/**
	 * Returns the satellite azimuth in degrees (0 - 360).
	 */
	public float getAzimuth() {
		return getObject().getAzimuth();
	}
	/**
	 * Returns the satellite elevation in degrees (0 - 90).
	 */
	public float getElevation() {
		return getObject().getElevation();
	}
	/**
     * Returns the PRN (pseudo-random number) for the satellite.
     */
	public int getPrn() {
		return getObject().getPrn();
	}
}
