package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.DJIBaseComponentWrapper.B4ACompletionCallback;
import anywheresoftware.b4a.objects.DJIFlightControllerCurrentStateWrapper.DJILocation2DWrapper;
import dji.common.error.DJIError;
import dji.common.mission.followme.FollowMeHeading;
import dji.common.mission.followme.FollowMeMission;
import dji.common.mission.followme.FollowMeMissionEvent;
import dji.common.mission.followme.FollowMeMissionState;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.followme.FollowMeMissionOperator;
import dji.sdk.mission.followme.FollowMeMissionOperatorListener;

@ShortName("DJIFollowMeMissionOperator")
@Events(values={"FollowMeMissionProgress (ExecutionEvent As Object)", "FollowMeMissionStart", "FollowMeMissionFinish (Error As String)"}) 
public class DJIFollowMeMissionOperatorWrapper extends DJIBaseComponentWrapper<FollowMeMissionOperator>{
	
	/**
	 * Initializes the mission operator.
	 */
	public void Initialize(final BA ba, String EventName) {
		setObject(MissionControl.getInstance().getFollowMeMissionOperator());
		super.initialize(ba, EventName);
		getObject().addListener(new FollowMeMissionOperatorListener() {
			
			@Override
			public void onExecutionUpdate(FollowMeMissionEvent event) {
				if (event != null) {
					ba.raiseEventFromUI(getObject(), getEventName() + "_followmemissionprogress", event);
				}
			}
			
			@Override
			public void onExecutionStart() {
				ba.raiseEventFromUI(getObject(), getEventName() + "_followmemissionstart");
			}
			
			@Override
			public void onExecutionFinish(DJIError arg0) {
				ba.raiseEventFromUI(getObject(), getEventName() + "_followmemissionfinish", arg0 == null ? "" : String.valueOf(arg0));
				
			}
		});

	}
	/**
	 * Returns the mission state.
	 *One of the following values: UNKNOWN, DISCONNECTED, NOT_SUPPORTED, RECOVERING, NOT_READY, READY_TO_EXECUTE, EXECUTING, GOT_STUCK
	 */
	public String getMissionState() {
		FollowMeMissionState state = getObject().getCurrentState();
		if (state != null)
			return state.getName();
		else
			return "UNKNOWN";
	}

	/**
	 * Starts a mission. The Result event will be raised.
	 */
	public Object StartMission(FollowMeMission Mission) {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().startMission(Mission, cc);
		return cc;
	}
	
	
	/**
	 * Stops a mission. The Result event will be raised.
	 */
	public Object StopMission() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().stopMission(cc);
		return cc;
	}
	/**
	 * Update the Follow Me mission's target location to follow. The Result event will be raised.
	 */
	public Object UpdateFollowingTarget (DJILocation2DWrapper Target) {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().updateFollowingTarget(Target.getObject(), cc);
		return cc;
	}
	
	/**
	 * Configures a hotpoint mission.
	 */
	@ShortName("DJIFollowMeMission")
	public static class FollowMeMissionWrapper extends AbsObjectWrapper<FollowMeMission> {
		/**
		 * Initializes the FollowMe mission configuration.
		 *Latitude / Longitude - Initial position.
		 *Altitude - Altitude in meters.
		 *Heading - Aircraft heading. One of the following values: TOWARD_FOLLOW_POSITION, CONTROLLED_BY_REMOTE_CONTROLLER
		 */
		public void Initialize(double Latitude, double Longitude, float Altitute, FollowMeHeading Heading) {
			setObject(new FollowMeMission(Heading, Latitude, Longitude, Altitute));
		}
		
		
		
	}
	
	
	
}
