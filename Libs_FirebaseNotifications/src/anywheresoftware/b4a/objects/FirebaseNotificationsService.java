
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

import java.util.Map.Entry;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.Map;

@Hide
public class FirebaseNotificationsService extends FirebaseMessagingService{
	private static boolean firstMessage = true;
	private static Handler handler;
	private static Class<?> ServiceClass;
	private static Class<?> ReceiverClass;
	@Override
	public void onCreate() {
		super.onCreate();
		try {
			handler = new Handler(Looper.getMainLooper());
			ServiceClass = Class.forName(getPackageName() + ".firebasemessaging");
			if (BroadcastReceiver.class.isAssignableFrom(ServiceClass)) {
				ReceiverClass = ServiceClass;
			} else {
				ReceiverClass = Class.forName(getPackageName() + ".firebasemessaging$firebasemessaging_BR");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			BA.LogError("FirebaseMessaging not found.");
		}
	}
	public static Intent createIntent(Service me, String event) {
		Intent i;
		i = new Intent(me, ReceiverClass);
		i.setAction("b4a_firebasemessaging");
		i.putExtra("event", event);
		return i;
	}
	@Override
	public void onNewToken(String token) {
		sendBroadcast(createIntent(this, "b4a_tokenrefresh"));
	}
	@Override
	public void onMessageReceived(final RemoteMessage remoteMessage) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				final Intent i = createIntent(FirebaseNotificationsService.this, "b4a_messagereceived");
				i.putExtra("message", remoteMessage);
				try {
					if (firstMessage || Common.IsPaused(null, ServiceClass) == false) {
						firstMessage = false;
						sendBroadcast(i);
					} else {
						BA.handler.postDelayed(new Runnable() {

							@Override
							public void run() {
								sendBroadcast(i);
							}

						}, 500);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}});
	}

	@DependsOn(values={"com.google.firebase:firebase-messaging", "com.google.firebase:firebase-core"})
	@Version(3.10f)
	@ShortName("FirebaseMessaging")
	@Events(values={"TokenRefresh (Token As String)", "MessageArrived (Message As RemoteMessage)"})
	public static class FirebaseMessageWrapper extends AbsObjectWrapper<FirebaseMessaging> {
		private BA ba;
		private String eventName;
		private String token = "";
		public void Initialize(BA ba, String EventName) {
			setObject(FirebaseMessaging.getInstance());
			this.ba = ba;
			this.eventName = EventName.toLowerCase(BA.cul);
			updateToken();
		}
		private void updateToken() {
			FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {

				@Override
				public void onSuccess(String arg0) {
					token = arg0;
					ba.raiseEventFromUI(this, eventName + "_tokenrefresh", token);
				}
			});
		}
		/**
		 * Should be called from Service_Start. Returns true if the intent was handled.
		 */
		@SuppressWarnings("deprecation")
		public boolean HandleIntent(Intent Intent) {
			if (Intent == null || "b4a_firebasemessaging".equals(Intent.getAction()) == false)
				return false;
			Intent.setExtrasClassLoader(RemoteMessage.class.getClassLoader());
			String event = Intent.getStringExtra("event");
			if (event.equals("b4a_tokenrefresh")) 
				updateToken();
			else if (event.equals("b4a_messagereceived")) {
				RemoteMessage rm = Intent.getParcelableExtra("message");
				ba.raiseEventFromUI(this, eventName + "_messagearrived", AbsObjectWrapper.ConvertToWrapper(new RemoteMessageWrapper(), rm));
			}
			return true;
		}
		/**
		 * Returns the device token. The token can change from time to time. The TokenRefresh event is raised when the token changes.
		 *The token is only needed when sending messages to a specific device.
		 */
		public String getToken() {
			return BA.returnString(token);
		}
		/**
		 * Subscribes to the specified topic.
		 *Example:<code>
		 *fm.SubscribeToTopic("general")</code>
		 */
		public void SubscribeToTopic(String Topic) {
			getObject().subscribeToTopic(Topic);
		}
		/**
		 * Unsubscribes from a topic.
		 */
		public void UnsubscribeFromTopic(String Topic) {
			getObject().unsubscribeFromTopic(Topic);
		}

	}
	/**
	 * Holds the push message data.
	 */
	@ShortName("RemoteMessage")
	public static class RemoteMessageWrapper extends AbsObjectWrapper<RemoteMessage> {
		/**
		 * Gets the collapse key (if set).
		 */
		public String getCollapseKey() {
			return BA.returnString(getObject().getCollapseKey());
		}
		/**
		 * Returns the sender id or the topic name. In the later case the value will start with /topics/
		 */
		public String getFrom() {
			return BA.returnString(getObject().getFrom());
		}
		/**
		 * Gets the message id.
		 */
		public String getMessageId() {
			return getObject().getMessageId();
		}
		/**
		 * Returns the time the message was sent.
		 */
		public long getSentTime() {
			return getObject().getSentTime();
		}
		/**
		 * Returns a Map with the key / values set as the message data.
		 */
		public Map GetData() {
			Map m = new Map();
			m.Initialize();
			if (getObject().getData() != null) {
				for (Entry<String, String> e : getObject().getData().entrySet()) {
					m.Put(e.getKey(), e.getValue());
				}
			}
			return m;
		}

	}

}
