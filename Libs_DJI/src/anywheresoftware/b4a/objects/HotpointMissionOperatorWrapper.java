package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import dji.common.error.DJIError;
import dji.common.mission.hotpoint.HotpointHeading;
import dji.common.mission.hotpoint.HotpointMission;
import dji.common.mission.hotpoint.HotpointMissionEvent;
import dji.common.mission.hotpoint.HotpointMissionState;
import dji.common.mission.hotpoint.HotpointStartPoint;
import dji.common.model.LocationCoordinate2D;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.hotpoint.HotpointMissionOperator;
import dji.sdk.mission.hotpoint.HotpointMissionOperatorListener;

@ShortName("DJIHotpointMissionOperator")
@Events(values={"HotpointMissionProgress (ExecutionEvent As Object)", "HotpointMissionStart", "HotpointMissionFinish (Error As String)"}) 
public class HotpointMissionOperatorWrapper extends DJIBaseComponentWrapper<HotpointMissionOperator>{
	
	/**
	 * Initializes the mission operator.
	 */
	public void Initialize(final BA ba, String EventName) {
		setObject(MissionControl.getInstance().getHotpointMissionOperator());
		super.initialize(ba, EventName);
		getObject().addListener(new HotpointMissionOperatorListener() {
			

			@Override
			public void onExecutionFinish(DJIError paramDJIError) {
				ba.raiseEventFromUI(getObject(), getEventName() + "_hotpointmissionfinish", paramDJIError == null ? "" : String.valueOf(paramDJIError));
			}

			@Override
			public void onExecutionStart() {
				ba.raiseEventFromUI(getObject(), getEventName() + "_hotpointmissionstart");
			}

			@Override
			public void onExecutionUpdate(
					HotpointMissionEvent event) {
				if (event != null) {
					ba.raiseEventFromUI(getObject(), getEventName() + "_hotpointmissionprogress", event);
				}
			}
		});
	}
	/**
	 * Returns the mission state.
	 *One of the following values: UNKNOWN, DISCONNECTED, NOT_SUPPORTED, RECOVERING, READY_TO_EXECUTE, EXECUTING, EXECUTION_PAUSED, INITIAL_PHASE.
	 */
	public String getMissionState() {
		HotpointMissionState state = getObject().getCurrentState();
		if (state != null)
			return state.getName();
		else
			return "UNKNOWN";
	}

	/**
	 * Starts a mission. The Result event will be raised.
	 */
	public Object StartMission(HotpointMission Mission) {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().startMission(Mission, cc);
		return cc;
	}
	/**
	 * Resumes a mission. The Result event will be raised.
	 */
	public Object ResumeMission() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().resume(cc);
		return cc;
	}
	/**
	 * Pauses a mission. The Result event will be raised.
	 */
	public Object PauseMission() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().pause(cc);
		return cc;
	}
	/**
	 * Stops a mission. The Result event will be raised.
	 */
	public Object StopMission() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().stop(cc);
		return cc;
	}
	
	/**
	 * Configures a hotpoint mission.
	 */
	@ShortName("DJIHotpointMission")
	public static class HotpointMissionWrapper extends AbsObjectWrapper<HotpointMission> {
		/**
		 * Initializes the hotpoint mission configuration.
		 *Latitude / Longitude - Hotpoint center.
		 *Altitude - Orbit altitude in meters.
		 *Radius - Orbit radius in meters.
		 *AngularVelocity - Angular velocity in degrees / second.
		 *Heading - Aircraft heading. One of the following values: ALONG_CIRCLE_LOOKING_FORWARDS, ALONG_CIRCLE_LOOKING_BACKWARDS, TOWARDS_HOT_POINT, AWAY_FROM_HOT_POINT, CONTROLLED_BY_REMOTE_CONTROLLER, USING_INITIAL_HEADING.
		 */
		public void Initialize(double Latitude, double Longitude, double Altitute, double Radius, float AngularVelocity, HotpointHeading Heading) {
			setObject(new HotpointMission(new LocationCoordinate2D(Latitude, Longitude), Altitute, Radius, AngularVelocity, true, HotpointStartPoint.NEAREST, Heading));
		}
		
		
		
	}
	
	
	
}
