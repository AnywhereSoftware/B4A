package anywheresoftware.b4a.objects;

import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("DJIFlightControllerCurrentState")
public class DJIFlightControllerCurrentStateWrapper extends AbsObjectWrapper<FlightControllerState>{
	/**
	 * Returns true if the motors are on.
	 */
	public boolean getAreMotorsOn() {
		return getObject().areMotorsOn();
	}
	/**
	 * Returns the aircraft location.
	 */
	public DJILocation3DWrapper getAircraftLocation() {
		return (DJILocation3DWrapper)AbsObjectWrapper.ConvertToWrapper(new DJILocation3DWrapper(), getObject().getAircraftLocation());
	}
	/**
	 * Returns the aircraft attitude (pitch, roll and yaw).
	 */
	public DJIAttitudeWrapper getAttitude() {
		return (DJIAttitudeWrapper)AbsObjectWrapper.ConvertToWrapper(new DJIAttitudeWrapper(), getObject().getAttitude());
	}
	/**
	 * Returns the current flight mode.
	 *Possible values:  Manual, Atti, AttiCourseLock, AttiHover, Hover, GPSBlake, GPSAtti, GPSCourseLock, GPSHomeLock, GPSHotPoint, 
	 *AssistedTakeOff, AutoTakeOff, AutoLanding, AttiLanding, GPSWaypoint, GoHome, ClickGo, Joystick, AttiLimited,
	 *GPSAttiLimited, GPSFollowMe, Tracking, Pointing, PANO, Farming, FPV, GPSSport, GPSNovice, 
	 *ConfirmLanding, TerrainTracking, NaviAdvGoHome, NaviAdvLanding, TripodGPS, TrackHeadLock, MotorsJustStarted, GPSGentle, Unknown
	 */
	public String getFlightMode() {
		return DJIFlightControllerWrapper.EnumToString(getObject().getFlightMode());
	}
	/**
	 * Returns the home location.
	 */
	public DJILocation2DWrapper getHomeLocation() {
		return (DJILocation2DWrapper)AbsObjectWrapper.ConvertToWrapper(new DJILocation2DWrapper(), getObject().getHomeLocation());
	}
	/**
	 *Returns 255 if there is no signal. Otherwise returns a value between 0 (weak signal) to 5 (strong signal).
	 */
	public int getGPSStatus() {
		return getObject().getGPSSignalLevel() == null ? 255 : getObject().getGPSSignalLevel().value();
	}
	
	/**
	 * Returns the number of GPS satellites found.
	 */
	public double getSatelliteCount() {
		return getObject().getSatelliteCount();
	}
	/**
	 * Returns the x-axis velocity measured in meters per second.
	 */
	public double getVelocityX() {
		return getObject().getVelocityX();
	}
	/**
	 * Returns the y-axis velocity measured in meters per second.
	 */
	public double getVelocityY() {
		return getObject().getVelocityY();
	}
	/**
	 * Returns the z-axis velocity measured in meters per second.
	 */
	public double getVelocityZ() {
		return getObject().getVelocityZ();
	}
	/**
	 * Returns True if the aircraft is flying.
	 */
	public boolean getFlying() {
		return getObject().isFlying();
	}
	/**
	 * Returns True if the aircraft is going home.
	 */
	public boolean getGoingHome() {
		return getObject().isGoingHome();
	}
	/**
	 * Returns True if the IMU is preheating.
	 */
	public boolean getIMUPreheating() {
		return getObject().isIMUPreheating();
	}
	/**
	 * Returns true if the home location was set.
	 */
	public boolean getHomeLocationSet() {
		return getObject().isHomeLocationSet();
	}
	/**
	 * Returns the go home height in meters.
	 */
	public float getGoHomeHeight() {
		return getObject().getGoHomeHeight();
	}
	@ShortName("DJILocation3D")
	public static class DJILocation3DWrapper extends AbsObjectWrapper<LocationCoordinate3D> {
		/**
		 * Returns the altitude measured by barometer in meters (relative to the take off location).
		 */
		public float getAltitude() {
			return getObject().getAltitude();
		}
		public double getLatitude() {
			return getObject().getLatitude();
		}
		public double getLongitude() {
			return getObject().getLongitude();
		}
	}
	@ShortName("DJILocation2D")
	public static class DJILocation2DWrapper extends AbsObjectWrapper<LocationCoordinate2D> {
		
		public double getLatitude() {
			return getObject().getLatitude();
		}
		public double getLongitude() {
			return getObject().getLongitude();
		}
	}
	@ShortName("DJIAttitude")
	public static class DJIAttitudeWrapper extends AbsObjectWrapper<Attitude> {
		public double getPitch() {
			return getObject().pitch;
		}
		public double getRoll() {
			return getObject().roll;
		}
		public double getYaw() {
			return getObject().yaw;
		}
	}
}
