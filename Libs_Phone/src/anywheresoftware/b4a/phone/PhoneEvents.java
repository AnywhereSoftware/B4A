
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
 
 package anywheresoftware.b4a.phone;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.IntentWrapper;
import anywheresoftware.b4a.phone.Phone.PhoneId;
/**
 * The Android OS sends all kinds of messages to notify applications of changes in the system.
 *The PhoneEvents object allows you to catch such messages and handle those events in your program.
 *Usually you will want to add this object to a Service module instead of an Activity module in order not to miss events that happen while
 *your activity is paused.
 *Working with this object is quite simple. You should declare this object in Sub Process_Globals and initialize it in Sub Service_Create.
 *From now on your declared sub events will handle the events.
 *The Intent object which was sent by the system is passed as the last parameter.
 *The events supported are:
 *<b>AirplaneModeChanged</b> - Raised when the "airplane mode" state changes.
 *State - True when airplane mode is active.
 *<b>BatteryChanged</b> - Raised when the battery status changes.
 *Level - The current level.
 *Scale - The maximum level.
 *Plugged - Whether the device is plugged to an electricity source.
 *<b>ConnectivityChanged</b> - There was a change in the state of the WIFI network or the MOBILE network (other network).
 *NetworkType - WIFI or MOBILE.
 *State - One of the following values: CONNECTING, CONNECTED, SUSPENDED, DISCONNECTING, DISCONNECTED, UNKNOWN.
 *<b>DeviceStorageLow</b> - The device internal memory condition is low.
 *<b>DeviceStorageOk</b> - The device internal low memory condition no longer exists.
 *<b>PackageAdded</b> - An application was installed.
 *Package - The application package name.
 *<b>PackageRemoved</b> - An application was uninstalled.
 *Package - The application package name.
 *<b>PhoneStateChanged</b> - The phone state has changed.
 *State - One of the three values: IDLE, OFFHOOK, RINGING. OFFHOOK means that there is a call or that the phone is dialing.
 *IncomingCall - Available when the State value is RINGING.
 *<b>ScreenOff</b> - The screen has turned off.
 *<b>ScreenOn</b> - The screen has turned on.
 *<b>Shutdown</b> - The phone is shutting down (turned off not just sleeping).
 *<b>SmsDelivered</b> - An Sms message sent by your application was delivered to the recipient.
 *PhoneNumber - The target phone number.
 *<b>SmsSentStatus</b> - Raised after your application sends an Sms message.
 *Success - Whether the message was sent successfully.
 *ErrorMessage - One of the following values: GENERIC_FAILURE, NO_SERVICE, RADIO_OFF, NULL_PDU or OK.
 *PhoneNumber - The target phone number.
 *<b>TextToSpeechFinish</b> - The Text To Speech engine has finished processing the messages in the queue.
 *<b>UserPresent</b> - The user has unlocked the keyguard screen.
 */
@ShortName("PhoneEvents")
@Events(values={
		"AirplaneModeChanged (State As Boolean, Intent As Intent)",
		"BatteryChanged (Level As Int, Scale As Int, Plugged As Boolean, Intent As Intent)",
		"ConnectivityChanged (NetworkType As String, State As String, Intent As Intent)",
		"DeviceStorageLow (Intent As Intent)",
		"DeviceStorageOk (Intent As Intent)",
		"PackageAdded (Package As String, Intent As Intent)",
		"PackageRemoved (Package As String, Intent As Intent)",
		"PhoneStateChanged (State As String, IncomingNumber As String, Intent As Intent)",
		"ScreenOff (Intent As Intent)",
		"ScreenOn (Intent As Intent)",
		"SmsDelivered (PhoneNumber As String, Intent As Intent)",
		"SmsSentStatus (Success As Boolean, ErrorMessage As String, PhoneNumber As String, Intent As Intent)",
		"Shutdown (Intent As Intent)",
		"TextToSpeechFinish (Intent As Intent)",
		"UserPresent (Intent As Intent)"
})


public class PhoneEvents {
	private BroadcastReceiver br;
	private BA ba;
	private String ev;
	private HashMap<String, ActionHandler> map = new HashMap<String, ActionHandler>();
	public PhoneEvents() {
		map.put(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED, new ActionHandler() {
			{event = "_texttospeechfinish";}
			@Override
			public void handle(Intent intent) {
				send(intent, null);
			}
		});
		map.put(ConnectivityManager.CONNECTIVITY_ACTION, new ActionHandler() {
			{event = "_connectivitychanged";}
			@Override
			public void handle(Intent intent) {
				NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				String type = ni.getTypeName();
				String state = ni.getState().toString();
				send(intent, new Object[] {type, state});
			}
		});


		map.put(Intent.ACTION_USER_PRESENT, new ActionHandler() {
			{event = "_userpresent";}
			@Override
			public void handle(Intent intent) {
				send(intent, null);
			}
		});
		map.put(Intent.ACTION_SHUTDOWN, new ActionHandler() {
			{event = "_shutdown";}
			@Override
			public void handle(Intent intent) {
				send(intent, null);
			}
		});
		map.put(Intent.ACTION_SCREEN_ON, new ActionHandler() {
			{event = "_screenon";}
			@Override
			public void handle(Intent intent) {
				send(intent, null);
			}
		});
		map.put(Intent.ACTION_SCREEN_OFF, new ActionHandler() {
			{event = "_screenoff";}
			@Override
			public void handle(Intent intent) {
				send(intent, null);
			}
		});
		map.put(Intent.ACTION_PACKAGE_REMOVED, new ActionHandler() {
			{event = "_packageremoved";}
			@Override
			public void handle(Intent intent) {
				send(intent, new Object[] {intent.getDataString()});
			}
		});
		map.put(Intent.ACTION_PACKAGE_ADDED, new ActionHandler() {
			{event = "_packageadded";}
			@Override
			public void handle(Intent intent) {
				send(intent, new Object[] {intent.getDataString()});
			}
		});

		map.put(Intent.ACTION_DEVICE_STORAGE_LOW, new ActionHandler() {
			{event = "_devicestoragelow";}
			@Override
			public void handle(Intent intent) {
				send(intent, null);
			}
		});
		map.put("b4a.smssent", new ActionHandler() {
			{event = "_smssentstatus";}
			@Override
			public void handle(Intent intent) {
				String msg = "";
				switch (resultCode) {
				case Activity.RESULT_OK:
					msg = "OK";
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					msg = "GENERIC_FAILURE";
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					msg="NO_SERVICE";
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					msg="NULL_PDU";
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					msg="RADIO_OFF";
					break;

				}
				send(intent, new Object[]{resultCode == Activity.RESULT_OK, msg, intent.getStringExtra("phone")});
			}
		});
		map.put("b4a.smsdelivered", new ActionHandler() {
			{event = "_smsdelivered";}
			@Override
			public void handle(Intent intent) {
				send(intent, new Object[] {intent.getStringExtra("phone")});
			}
		});
		map.put(Intent.ACTION_DEVICE_STORAGE_OK, new ActionHandler() {
			{event = "_devicestorageok";}
			@Override
			public void handle(Intent intent) {
				send(intent, null);
			}
		});
		map.put(Intent.ACTION_BATTERY_CHANGED, new ActionHandler() {
			{event = "_batterychanged";}
			@Override
			public void handle(Intent intent) {
				int level = intent.getIntExtra("level", 0);
				int scale = intent.getIntExtra("scale", 1);
				boolean plugged = intent.getIntExtra("plugged", 0) > 0;
				send(intent, new Object[] {level, scale, plugged});
			}
		});
		map.put(Intent.ACTION_AIRPLANE_MODE_CHANGED, new ActionHandler() {
			{event = "_airplanemodechanged";}
			@Override
			public void handle(Intent intent) {
				boolean state = intent.getBooleanExtra("state", false);
				send(intent, new Object[] {state});
			}
		});

		for (Entry<String, ActionHandler> e : map.entrySet()) {
			e.getValue().action = e.getKey();
		}
	}
	/**
	 * Initializes the object and starts listening for events.
	 *The PhoneStateEvent will also be handled.
	 *Example:<code>
	 *Dim PhoneId As PhoneId
	 *Dim PE As PhoneEvents
	 *PE.InitializeWithPhoneState("PE", PhoneId)</code>
	 */
	public void InitializeWithPhoneState(final BA ba, String EventName, PhoneId PhoneId) {
		map.put(TelephonyManager.ACTION_PHONE_STATE_CHANGED, new ActionHandler() {
			{event = "_phonestatechanged";}
			@Override
			public void handle(Intent intent) {
				String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
				String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
				if (incomingNumber == null)
					incomingNumber = "";
				send(intent, new Object[] {state, incomingNumber});
			}
		});
		map.get(TelephonyManager.ACTION_PHONE_STATE_CHANGED).action = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
		Initialize(ba, EventName);
	}
	/**
	 * Initializes the object and starts listening for events.
	 *The PhoneStateEvent will not be raised. Use InitializeWithPhoneState instead if it is needed.
	 */
	public void Initialize(final BA ba, String EventName) {
		this.ba = ba;
		this.ev = EventName.toLowerCase(BA.cul);
		StopListening();
		br = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() == null)
					return;
				ActionHandler ah = map.get(intent.getAction());
				if (ah != null) {
					ah.resultCode = getResultCode();
					ah.handle(intent);
				}
			}
		};
		IntentFilter f1 = new IntentFilter();
		IntentFilter f2 = null;
		for (ActionHandler ah : map.values()) {
			if (ba.subExists(ev + ah.event)) {
				if (ah.action == Intent.ACTION_PACKAGE_ADDED || ah.action == Intent.ACTION_PACKAGE_REMOVED) {
					if (f2 == null) {
						f2 = new IntentFilter();
						f2.addDataScheme("package");
					}
					f2.addAction(ah.action);
				}
				f1.addAction(ah.action);
			}
		}
		registerReceiver(br, f1, true);
		if (f2 != null) {
			registerReceiver(br, f2, true);
		}

	}
	
	@Hide
	public static void registerReceiver(BroadcastReceiver br, IntentFilter fi, boolean exported) {
		if (Build.VERSION.SDK_INT >= 33) {
			BA.applicationContext.registerReceiver(br, fi, exported ? Context.RECEIVER_EXPORTED : Context.RECEIVER_NOT_EXPORTED);
		} else {
			BA.applicationContext.registerReceiver(br, fi);
		}
	}
	
	/**
	 * Stops listening for events. You can later call Initialize to start listening for events again.
	 */
	public void StopListening() {
		if (br != null)
			BA.applicationContext.unregisterReceiver(br);
		br = null;
	}

	private abstract class ActionHandler {
		public String event;
		public String action;
		public int resultCode;
		public abstract void handle(Intent intent);
		protected void send(Intent intent, Object[] args) {
			final Object[] o;
			if (args == null)
				o = new Object[1];
			else {
				o = new Object[args.length + 1];
				System.arraycopy(args, 0, o, 0, args.length);
			}
			o[o.length - 1] = AbsObjectWrapper.ConvertToWrapper(new IntentWrapper(), intent);
			if (BA.debugMode) {
				BA.handler.post(new BA.B4ARunnable() {
					@Override
					public void run() {
						ba.raiseEvent(this, ev + event, o);
					}
				});
			}
			else {
				ba.raiseEvent(this, ev + event, o);
			}
		}
	}
	/**
	 * Listens for incoming SMS messages.
	 *The MessageReceived event is raised when a new message arrives.
	 *Returning True from the MessageReceived event will cause the broadcasted message to be aborted.
	 *This can be used to prevent the message from reaching the standard SMS application.
	 *However in order for your application to receive the message before other applications you should use Initialize2 and set the priority value to a value larger than 0. It should be 999 according to the Android documentation.
	 */
	@ShortName("SmsInterceptor")
	@Permissions(values={"android.permission.RECEIVE_SMS"})
	@Events(values={"MessageReceived (From As String, Body As String) As Boolean",
			"MessageSent (MessageId As Int)"})
	public static class SMSInterceptor {
		private String eventName;
		private BroadcastReceiver br;
		private BA ba;
		/**
		 * Initializes the object and starts listening for new messages.
		 */
		public void Initialize(String EventName,final BA ba)
		{
			Initialize2(EventName, ba, 0);
		}
		/**
		 * Listens to outgoing messages. MessageSent event will be raised when a message is sent.
		 *You can call SmsMessages.GetByMessageId to retrieve the message.
		 */
		public void ListenToOutgoingMessages() {
			final Uri content = Uri.parse("content://sms");
			ContentObserver co = new ContentObserver(new Handler()) {
				@Override
			    public void onChange(boolean selfChange) {
			        super.onChange(selfChange);
			        Cursor cursor = BA.applicationContext.getContentResolver().query(
							content,null, null, null, null);
					if (cursor.moveToNext()) {
						String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
						int type = cursor.getInt(cursor.getColumnIndex("type"));
						if (protocol != null || type != 2) {
							return;
						}
				        ba.raiseEvent(null, eventName + "_messagesent", cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
						cursor.close();
					}
			    }
			};
			ContentResolver contentResolver = BA.applicationContext.getContentResolver();
			contentResolver.registerContentObserver(content, true, co);
		}
		/**
		 * Initializes the object and starts listening for new messages.
		 *The last parameter defines the application priority compared to other applications that listen to incoming messages.
		 *You should set it to 999 according to the official Android documentation in order to receive the message first.
		 *It is however possible that a third party application has used a higher value.
		 */
		public void Initialize2(String EventName,final BA ba, int Priority) {
			this.ba = ba;
			eventName = EventName.toLowerCase(BA.cul);
			br = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") == false)
						return;
					Bundle bundle = intent.getExtras();
					if (bundle != null) {
						Object[] pduObj = (Object[]) bundle.get("pdus");
						for (int i = 0; i < pduObj.length; i++) {
							SmsMessage sm = SmsMessage.createFromPdu((byte[]) pduObj[i]);
							Boolean res = (Boolean)ba.raiseEvent(SMSInterceptor.this, eventName + "_messagereceived", sm.getOriginatingAddress(), sm.getMessageBody());
							if (res != null && res == true)
								abortBroadcast();
						}
					}
				}
			};
			IntentFilter fil = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
			fil.setPriority(Priority);
			registerReceiver(br, fil, true);
		}
		
		/**
		 * Stops listening for events. You can later call Initialize to start listening again.
		 */
		public void StopListening() {
			if (br != null)
				BA.applicationContext.unregisterReceiver(br);
			br = null;
		}
	}
}
