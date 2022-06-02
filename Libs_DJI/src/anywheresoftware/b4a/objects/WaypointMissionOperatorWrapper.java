package anywheresoftware.b4a.objects;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointExecutionProgress;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.mission.waypoint.WaypointMission.Builder;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("DJIWaypointMissionOperator")
@Events(values={"MissionProgress (ExecutionEvent As Object, TargetWaypointIndex As Int)", "MissionStart", "MissionFinish (Error As String)"}) 
public class WaypointMissionOperatorWrapper extends DJIBaseComponentWrapper<WaypointMissionOperator>{
	
	/**
	 * Initializes the mission operator.
	 */
	public void Initialize(final BA ba, String EventName) {
		setObject(MissionControl.getInstance().getWaypointMissionOperator());
		super.initialize(ba, EventName);
		getObject().addListener(new WaypointMissionOperatorListener() {

			@Override
			public void onDownloadUpdate(
					WaypointMissionDownloadEvent paramWaypointMissionDownloadEvent) {
				
			}

			@Override
			public void onExecutionFinish(DJIError paramDJIError) {
				ba.raiseEventFromUI(getObject(), getEventName() + "_missionfinish", paramDJIError == null ? "" : String.valueOf(paramDJIError));
			}

			@Override
			public void onExecutionStart() {
				ba.raiseEventFromUI(getObject(), getEventName() + "_missionstart");
			}

			@Override
			public void onExecutionUpdate(
					WaypointMissionExecutionEvent event) {
				if (event != null && event.getProgress() != null) {
					WaypointExecutionProgress progress = event.getProgress();
					ba.raiseEventFromUI(getObject(), getEventName() + "_missionprogress", event, progress.targetWaypointIndex);
				}
			}

			@Override
			public void onUploadUpdate(
					WaypointMissionUploadEvent paramWaypointMissionUploadEvent) {
				
			}
			
		});
	}
	/**
	 * Returns the mission state.
	 *One of the following values: UNKNOWN, DISCONNECTED, NOT_SUPPORTED, RECOVERING, READY_TO_UPLOAD, UPLOADING, READY_TO_EXECUTE, EXECUTING, EXECUTION_PAUSED.
	 */
	public String getMissionState() {
		WaypointMissionState state = getObject().getCurrentState();
		if (state != null)
			return state.getName();
		else
			return "UNKNOWN";
	}
	/**
	 * Loads a mission. The next step is to upload it with UploadMission.
	 */
	public void LoadMission(WaypointMissionBuilderWrapper MissionBuilder) {
		DJIError error = getObject().loadMission(MissionBuilder.getObject().build());
		if (error != null)
			throw new RuntimeException(error.toString());
	}
	/**
	 * Uploads a mission. The Result event will be raised.
	 */
	public Object UploadMission() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().uploadMission(cc);
		return cc;
	}
	/**
	 * Starts a mission. The Result event will be raised.
	 */
	public Object StartMission() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().startMission(cc);
		return cc;
	}
	/**
	 * Resumes a mission. The Result event will be raised.
	 */
	public Object ResumeMission() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().resumeMission(cc);
		return cc;
	}
	/**
	 * Pauses a mission. The Result event will be raised.
	 */
	public Object PauseMission() {
		B4ACompletionCallback cc = new B4ACompletionCallback();
		getObject().pauseMission(cc);
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
	 * Mission builder. Note that the AutoFlightSpeed and MaxFlightSpeed must be set.
	 */
	@ShortName("DJIWaypointMissionBuilder")
	public static class WaypointMissionBuilderWrapper extends AbsObjectWrapper<WaypointMission.Builder> {
		public void Initialize() {
			setObject(new Builder());
		}
		/**
		 * Gets or sets the auto flight speed (m/s). This is the default flight speed.
		 *Valid values are between -15 to 15.
		 */
		public float getAutoFlightSpeed() {
			return getObject().getAutoFlightSpeed();
		}
		public void setAutoFlightSpeed(float f) {
			getObject().autoFlightSpeed(f);
		}
		/**
		 * Adds a waypoint.
		 */
		public void AddWaypoint(WaypointWrapper Waypoint) {
			getObject().addWaypoint(Waypoint.getObject());
		}
		/**
		 * Gets or sets the maximum flight speed (m/s). Maximum speed is achieved by pushing the throttle joystick.
		 */
		public float getMaxFlightSpeed() {
			return getObject().getMaxFlightSpeed();
		}
		public void setMaxFlightSpeed(float f) {
			getObject().maxFlightSpeed(f);
		}
		/**
		 * Action that will be executed when the mission finishes.
		 *NO_ACTION - No further action will be taken on completion of mission.
		 *GO_HOME - The aircraft will go home and land.
		 *AUTO_LAND - The aircraft will land at the last waypoint.
		 *GO_FIRST_WAYPOINT - The aircraft will go to the first waypoint.
		 *CONTINUE_UNTIL_END - Mission will not end until StopMission is called.
		 */
		public void SetFinishAction(WaypointMissionFinishedAction Action) {
			getObject().finishedAction(Action);
		}
	}
	@ShortName("DJIWaypoint")
	public static class WaypointWrapper extends AbsObjectWrapper<Waypoint> {
		/**
		 * Initializes the waypoint with the given coordinate and altitude (in meters).
		 */
		public void Initialize(double Latitude, double Longitude, float Altitude) {
			setObject(new Waypoint(Latitude, Longitude, Altitude));
		}
		/**
		 * Adds an action that will be executed when the aircraft reaches the point.
		 *Action - One of the following values:
		 *STAY - Stays at the current point. Parameter = Period in milliseconds.
		 *START_TAKE_PHOTO - Starts to shoot a photo. Parameter is ignored.
		 *START_RECORD - Starts recordsing. Parameter is ignored.
		 *STOP_RECORD - Stops recording. Parameter is ignored.
		 *ROTATE_AIRCRAFT - Rotates the aircraft's yaw. Parameter should be between -180 to 180.
		 *GIMBAL_PITCH - Rotates the gimbal's pitch. Parameter should be between -90 to 0.
		 *
		 */
		public void AddAction(WaypointActionType Action, int Parameter) {
			if (getObject().addAction(new WaypointAction(Action, Parameter)) == false)
				throw new RuntimeException("Cannot add action");
		}
	}
	
	
}
