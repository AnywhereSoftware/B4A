package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import dji.common.battery.BatteryState;
import dji.sdk.products.Aircraft;

@ShortName("DJIAircraft")
@Events(values={"BatteryState (Battery As Object, RemainingPercentage As Int)"})
public class DJIAircraftWrapper extends DJIBaseComponentWrapper<Aircraft>{
	/**
	 * Initializes the object.
	 *EventName - Sets the subs that will handle the events.
	 *AircraftData - The parameter passed to DJISDKManager ProductChanged event.
	 */
	public void Initialize(final BA ba, final String EventName, Object AircraftData) {
		setObject((Aircraft)AircraftData);
		super.initialize(ba, EventName);
	}
	/**
	 * Returns the model.
	 */
	public String getModel() {
		if (getObject().getModel() != null)
			return getObject().getModel().getDisplayName();
		return "";

	}

	@Override
	public boolean getConnected() {
		return getObject().isConnected();
	}
	/**
	 * Returns true if the camera is ready.
	 */
	public boolean getCameraReady() {
		return getObject().getCamera() != null && getObject().getCamera().isConnected();
	}
	/**
	 * Returns true if the battery is ready.
	 */
	public boolean getBatteryReady() {
		return getObject().getBattery() != null && getObject().getBattery().isConnected();
	}
	/**
	 * Registers the battery state listener.
	 */
	public void RegisterBatteryStateEvent() {
		getObject().getBattery().setStateCallback(new BatteryState.Callback() {

			@Override
			public void onUpdate(BatteryState paramBatteryState) {
				getBA().raiseEventFromUI(getObject(), getEventName() + "_batterystate", paramBatteryState, paramBatteryState.getChargeRemainingInPercent());
			}

		});
	}

	/**
	 * Gets the aircraft name. The ResultWithValue event will be raised.
	 *Returns an object that can be used as the sender filter parameter.
	 *Example:<code>
	 *Wait for (aircraft.GetName) Aircraft_ResultWithValue (Success As Boolean, ErrorMessage As String, Value As Object)
	 *If Success Then
	 *	aircraftName = Value
	 *End If</code>
	 */
	@SuppressWarnings("unchecked")
	public Object GetName(final BA ba) {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().getName(cc);
		return cc;
	}




}
