
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

import java.text.ParseException;
import java.util.Date;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipAudioCall.Listener;
import android.os.Build;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
/**
 * Provides access to Voip / Sip services. Only Android 2.3 (API 9) and above are supported.
 *A tutorial is available <link>here|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/13088-android-sip-voip-tutorial.html</link>.
 */
@ShortName("Sip")
@Permissions(values={"android.permission.USE_SIP", "android.permission.INTERNET",
		"android.permission.RECORD_AUDIO", "android.permission.ACCESS_WIFI_STATE",
		"android.permission.WAKE_LOCK", "android.permission.MODIFY_AUDIO_SETTINGS"})
		@Events(values={
				"Registering",
				"RegistrationDone (ExpiryTime As Long)",
				"RegistrationFailed (ErrorCode As Int, ErrorMessage As String)",
				"CallEstablished",
				"CallEnded",
				"Calling",
				"CallBusy",
				"CallError (ErrorCode As Int, ErrorMessage As String)",
				"CallRinging (IncomingCall As SipAudioCall)"
		})
		@Version(1.02f)
		public class SIP {


	SipManager manager;
	SipProfile me;
	SipProfile.Builder builder;
	private boolean alreadyRegistered;
	private String eventName;
	private BroadcastReceiver br;
	/**
	 * Tests whether Sip API is supported on the device.
	 */
	public boolean getIsSipSupported() { 
		return SipManager.isApiSupported(BA.applicationContext);
	}
	/**
	 * Tests whether Voip is supported on this device.
	 */
	public boolean getIsVoipSupported() {
		return SipManager.isVoipSupported(BA.applicationContext);
	}
	/**
	 * Initializes the object.
	 *EventName - Sets the subs that will handle the events.
	 *Uri - The profile Uri. For example: sip:zzz@iptel.org
	 *Password - Account password.
	 */
	public void Initialize2(String EventName, String Uri, String Password, BA ba) throws ParseException {
		builder = new SipProfile.Builder(Uri);
		shared(EventName, Password, ba);
		
	}
	/**
	 * Initializes the object.
	 *EventName - Sets the subs that will handle the events.
	 *User - User name.
	 *Host - Host name or IP address.
	 *Password - Account password.
	 */
	public void Initialize(String EventName, String User, String Host, String Password, BA ba) throws ParseException {
		builder = new SipProfile.Builder(User, Host);
		shared(EventName, Password, ba);
	}
	private void shared(String EventName, String password, final BA ba) {
		this.eventName = EventName.toLowerCase(BA.cul);
		manager = SipManager.newInstance(BA.applicationContext);
		alreadyRegistered = false;
		builder.setPassword(password);
		br = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent intent) {
	            try {
					SipAudioCall call = manager.takeAudioCall(intent, null);
					ba.raiseEvent(SIP.this, eventName + "_callringing", AbsObjectWrapper.ConvertToWrapper(new SipAudioCallWrapper(), call));
					call.setListener(new AudioListener(ba, eventName), true);
				} catch (SipException e) {
					throw new RuntimeException(e);
				}
			}
			
		};
		IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        BA.applicationContext.registerReceiver(br, filter);
	}
	/**
	 * Sets the outbound proxy address.
	 */
	public void setOutboundProxy(String v) {
		builder.setOutboundProxy(v);
	}
	/**
	 * Sets whether keep-alive messages will be sent automatically.
	 */
	public void setSendKeepAlive(boolean b) {
		builder.setSendKeepAlive(b);
	}
	/**
	 * Sets whether the Sip manager will register automatically if needed.
	 */
	public void setAutoRegistration(boolean b) {
		builder.setAutoRegistration(b);
	}
	/**
	 * Sets the connection port.
	 */
	public void setPort(int p) {
		builder.setPort(p);
	}
	/**
	 * Sets the user display name.
	 */
	public void setDisplayName(String s) {
		builder.setDisplayName(s);
	}
	/**
	 * Sets the user defined profile name.
	 */
	public void setProfileName(String s) {
		builder.setProfileName(s);
	}
	/**
	 * Sets the protocol. Either "TCP" or "UDP".
	 */
	public void setProtocol(String s) {
		builder.setProtocol(s);
	}
	/**
	 * Tests whether the object was initialized.
	 */
	public boolean getIsInitialized() {
		return manager != null;
	}
	/**
	 * Sends a registration request to the server.
	 *The following events will be raised: Registering and RegistrationDone or RegistrationFail.
	 */
	public void Register(final BA ba) throws SipException {
		me = builder.build();
		Intent in = new Intent();
		in.setAction("android.SipDemo.INCOMING_CALL");
		int flags = Intent.FILL_IN_DATA;
		if (Build.VERSION.SDK_INT >= 31)
			flags |= 0x2000000; //FLAG_MUTABLE
		PendingIntent pi = PendingIntent.getBroadcast(BA.applicationContext, 0, in, flags);
		manager.open(me, pi, null);	
		manager.setRegistrationListener(me.getUriString(), new SipRegistrationListener() {
			public void onRegistering(String localProfileUri) {
				if (manager == null)
					return;
				ba.raiseEventFromDifferentThread(SIP.this, null, 0, eventName + "_registering", false, null);
			}

			public void onRegistrationDone(String localProfileUri, long expiryTime) {
				if (manager == null || alreadyRegistered)
					return;
				alreadyRegistered = true;
				ba.raiseEventFromDifferentThread(SIP.this, null, 0, eventName + "_registrationdone", false, new Object[]{expiryTime});

			}

			public void onRegistrationFailed(String localProfileUri, int errorCode,
					String errorMessage) {
				if (manager == null)
					return;
				ba.raiseEventFromDifferentThread(SIP.this, null, 0, eventName + "_registrationfailed", false, new Object[]{errorCode, errorMessage});
			}
		});
	}
	/**
	 * Makes an audio call. This method should only be called after registering.
	 *TargetUri - The target Uri.
	 *TimeoutSeconds - The timeout measured in seconds.
	 */
	public SipAudioCallWrapper MakeCall(final BA ba, String TargetUri, int TimeoutSeconds) throws ParseException, SipException {
		SipAudioCall c = manager.makeAudioCall(me.getUriString() ,TargetUri , new AudioListener(ba, eventName), TimeoutSeconds);
		return (SipAudioCallWrapper) AbsObjectWrapper.ConvertToWrapper(new SipAudioCallWrapper(), c);
	}
	static class AudioListener extends Listener {
		private final BA ba;
		private final String eventName;
		public AudioListener(BA ba, String eventName) {
			this.ba = ba;
			this.eventName = eventName;
		}
		@Override
		public void onCallEstablished(SipAudioCall call) {
			ba.raiseEventFromDifferentThread(call
					, null, 0, eventName + "_callestablished", false, null);
		}

		@Override
		public void onCallEnded(SipAudioCall call) {
			ba.raiseEventFromDifferentThread(call
					, null, 0, eventName + "_callended", false, null);
		}
		@Override
		public void onCallBusy(SipAudioCall call) {
			ba.raiseEventFromDifferentThread(call
					, null, 0, eventName + "_callbusy", false, null);
		}
		@Override
		public void onCalling(SipAudioCall call) {
			ba.raiseEventFromDifferentThread(call
					, null, 0, eventName + "_calling", false, null);
		}
		@Override
		public void onError(SipAudioCall call, int errorCode, String errorMessage) {
			ba.raiseEventFromDifferentThread(call
					, null, 0, eventName + "_callerror", false, new Object[] {errorCode, errorMessage});
		}
	}
	/**
	 * Closes the connection.
	 */
	public void Close() throws SipException {
		if (!getIsInitialized())
			return;
		if (me != null) {
			SipManager m = manager;
			manager = null;
			m.close(me.getUriString());
		}
		if (br != null)
			BA.applicationContext.unregisterReceiver(br);
	}
	/**
	 * Represents an audio call.
	 *This object is created by calling Sip.MakeCall or from the CallRinging event.
	 */
	@ShortName("SipAudioCall")
	public static class SipAudioCallWrapper extends AbsObjectWrapper<SipAudioCall>{
		/**
		 * Ends the current call.
		 */
		public void EndCall() throws SipException {
			getObject().endCall();
		}
		/**
		 * Starts the audio for the call. Should be called in CallEstablished event.
		 */
		public void StartAudio() {
			getObject().startAudio();
		}
		/**
		 * Sets the speaker mode.
		 */
		public void setSpeakerMode(boolean b) {
			getObject().setSpeakerMode(b);
		}
		/**
		 * Sends a Dtmf tone. Values can be 0-15, where 0-9 are the digits, 10 is '*', 11 is '# and 12-15 are 'A'-'D'.
		 */
		public void SendDtmf(int Code) {
			getObject().sendDtmf(Code);
		}
		/**
		 * Tests whether the microphone is muted.
		 */
		public boolean getIsMuted() {
			return getObject().isMuted();
		}
		/**
		 * Tests whether the call was established.
		 */
		public boolean getIsInCall() {
			return getObject().isInCall();
		}
		/**
		 * Toggles the microphone mute.
		 */
		public void ToggleMute() {
			getObject().toggleMute();
		}
		/**
		 * Answers an incoming call.
		 *TimeoutSeconds - Allowed time for the call to be established.
		 */
		public void AnswerCall(int TimeoutSeconds) throws SipException {
			getObject().answerCall(TimeoutSeconds);
		}
		/**
		 * Gets the peer Uri.
		 */
		public String getPeerUri() {
			SipProfile sp = getObject().getPeerProfile();
			if (sp == null)
				return "";
			else
				return sp.getUriString();
		}
	}
	

}
