package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.DJIFlightControllerCurrentStateWrapper.DJILocation2DWrapper;
import dji.common.flightcontroller.imu.IMUState;
import dji.sdk.flightcontroller.FlightController;

@ShortName("DJIFlightController")
@Events(values={"IMUStateChanged (State As DJIIMUState)"})
public class DJIFlightControllerWrapper extends DJIBaseComponentWrapper<FlightController>{
	
	/**
	 * Initializes the flight controller.
	 */
	public void Initialize(final BA ba, String EventName, DJIAircraftWrapper Aircraft) {
		setObject(Aircraft.getObject().getFlightController());
		super.initialize(ba, EventName);
		getObject().setIMUStateCallback(new IMUState.Callback() {

			@Override
			public void onUpdate(IMUState paramIMUState) {
				ba.raiseEventFromUI(getObject(), getEventName() + "_imustatechanged", AbsObjectWrapper.ConvertToWrapper(new DJIIMUStateWrapper(), paramIMUState));
				
			}
			
		});
	}
	/**
	 * Starts a takeoff action. The engine will start automatically.
	 *The Result event will be raised.
	 *Returns an object that can be used as the sender filter parameter.
	 */
	public Object TakeOff() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().startTakeoff(cc);
		return cc;
	}
	/**
	 * Starts an auto-landing action. The Result event will be raised.
	 *Returns an object that can be used as the sender filter parameter.
	 */
	public Object AutoLanding() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().startLanding(cc);
		return cc;
	}
	/**
	 * Returns true if the simulator has started.
	 */
	public boolean getSimulatorStarted() {
		try {
			return getObject().getSimulator() == null ? false : getObject().getSimulator().isSimulatorActive();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
//	/**
//	 * Gets the LEDs status. The ResultWithValue event will be raised.
//	  *Returns an object that can be used as the sender filter parameter.
//	  *Example:<code>
//		 *Wait For(controller.GetLEDsEnabled) Controller_ResultWithValue (Success As Boolean, ErrorMessage As String, Value As Object)
//		 *If Success Then
//		 *	Log($"LED status: " ${Value}"$)
//		 *End If</code>
//	 */
//	public Object GetLEDsEnabled() {
//		B4ACompletionCallback cc = new B4ACompletionCallback();
//		getObject().getLEDsEnabledSettings(cc);
//		return cc;
//	}
//	/**
//	 * Sets the LEDS status. The Result event will be raised.
//	 *Returns an object that can be used as the sender filter parameter.
//	 *Example: <code>
//		 *Wait For (controller.SetLEDsEnabled(True)) Controller_Result (Success As Boolean, ErrorMessage As String)
//		 *Log($"LED state changed. Success = ${Success}"$)</code>
//	 */
//	public Object SetLEDsEnabled(boolean Enabled) {
//		B4ACompletionCallback cc = new B4ACompletionCallback();
//		getObject().setLEDsEnabledSettings(Enabled, cc);
//		return cc;
//	}
	/**
	 * Returns the aircraft heading.
	 */
	public double getHeading() {
		return getObject().getCompass().getHeading();
	}
	/**
	 * Returns the current state.
	 */
	public DJIFlightControllerCurrentStateWrapper getCurrentState() {
		return (DJIFlightControllerCurrentStateWrapper)AbsObjectWrapper.ConvertToWrapper(new DJIFlightControllerCurrentStateWrapper(), getObject().getState());
	}
	/**
	 * Sets the home location based on the current aircraft location.
	 *The Result event will be raised.
	 *Example:<code>
	 *Dim sf As Object = controller.SetHomeLocationUsingAircraftCurrentLocation
	 *Wait For (sf) Controller_Result (Success As Boolean, ErrorMessage As String)
	 *If Success Then
	 *	Log("Home location set.")
	 *Else
	 *	Log("Error: " & ErrorMessage)
	 *End If</code>
	 */
	public Object SetHomeLocationUsingAircraftCurrentLocation() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().setHomeLocationUsingAircraftCurrentLocation(cc);
		return cc;
	}
	/**
	 * Sets the go home flight height. Should be between 20 to 500.
	 *The Result event will be raised.
	 */
	public Object SetGoHomeHeightInMeters(int Height) {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().setGoHomeHeightInMeters(Height, cc);
		return cc;
	}
	/**
	 * Starts a go home action.
	 *The Result event will be raised.
	 */
	public Object GoHomeStart() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().startGoHome(cc);
		return cc;
	}
	/**
	 * Cancels a go home action.
	 * The Result event will be raised.
	 */
	public Object GoHomeCancel() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().cancelGoHome(cc);
		return cc;
	}
	
	static String EnumToString(Enum<?> e) {
		return e == null ? "" : e.toString();
	}


	@ShortName("DJIIMUState")
	public static class DJIIMUStateWrapper extends AbsObjectWrapper<IMUState> {
		/**
		 * Returns one of the following values:
		 *UNKNOWN, DISCONNECTED, CALIBRATING,  CALIBRATION_FAILED, DATA_EXCEPTION, WARMING_UP, IN_MOTION, NORMAL_BIAS, MEDIUM_BIAS, LARGE_BIAS.
		 */
		public String getAccelerometerStatus() {
			return EnumToString(getObject().getAccelerometerState());
		}
		/**
		 * Returns one of the following values:
		 *NONE, CALIBRATING, SUCCESSFUL, FAILED, UNKNOWN.
		 */
		public String getCalibrationStatus() {
			return EnumToString(getObject().getCalibrationState());
		}
		/**
		 * Returns one of the following values:
		 *UNKNOWN, DISCONNECTED, CALIBRATING,  CALIBRATION_FAILED, DATA_EXCEPTION, WARMING_UP, IN_MOTION, NORMAL_BIAS, MEDIUM_BIAS, LARGE_BIAS.
		 */
		public String getGyroscopeState() {
			return EnumToString(getObject().getGyroscopeState());
		}
	}
}
