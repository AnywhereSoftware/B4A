
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.tech.TagTechnology;
import android.os.Build;
import android.os.Parcelable;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.object.JavaObject;

/**
 * Supports reading and writing NFC tags and supports sending messages between two Android devices.
 */
@Version(2.02f)
@ShortName("NFC")
@Permissions(values={"android.permission.NFC"})
@Events(values={"CreateMessage As List"})
@ActivityObject
@DependsOn(values={"JavaObject"})
public class NFC {
	/**
	 * Tests whether the Intent contains data read from an NDef tag.
	 */
	public boolean IsNdefIntent(Intent Intent) {
		if (Intent == null)
			return false;
		return Intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	}
	/**
	 * Retrieves the NdefRecords stored in the Intent object.
	 */
	public List GetNdefRecords(Intent Intent) {
		List l = new List();
		l.Initialize();
		Parcelable[] rawMsgs = Intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMsgs != null) {
			for (int i = 0; i < rawMsgs.length; i++) {
				NdefMessage nm = (NdefMessage) rawMsgs[i];
				for (NdefRecord r : nm.getRecords()) {
					l.Add( r);
				}
			}
		}
		return l;
	}
	/**
	 * Returns true if an NFC adapter is available.
	 */
	public boolean getIsSupported() {
		return NfcAdapter.getDefaultAdapter(BA.applicationContext) != null;
	}
	/**
	 * Forces all NFC intents to be sent to the current activity. Should be called from Activity_Resume.
	 *DisableForegroundDispatch should called from Activity_Pause.
	 */
	public void EnableForegroundDispatch(BA ba) throws ClassNotFoundException {
		Intent i = new Intent(ba.context, ba.activity.getClass());
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= 31)
			flags |= 33554432; //FLAG_MUTABLE
		PendingIntent pi = PendingIntent.getActivity(ba.context, 0
				, i,
				flags);
		try {
			NfcAdapter.getDefaultAdapter(BA.applicationContext).enableForegroundDispatch(ba.activity, pi, null, null);
		} catch (IllegalStateException ie) {
			BA.LogInfo("Failed to enable foreground dispatch.");
		}
	}
	/**
	 * See EnableForegroundDispatch. 
	 */
	public void DisableForegroundDispatch(BA ba) {
		NfcAdapter.getDefaultAdapter(BA.applicationContext).disableForegroundDispatch(ba.activity);
	}
	/**
	 * Returns an array with the technologies supported by the NFC tag.
	 */
	public String[] GetTechList(Intent Intent) {
		Tag tag = Intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		return tag.getTechList();
	}
	/**
	 * Returns true if NFC is supported and is enabled.
	 *If IsSupported returned True and IsEnabled returned False then you can show the NFC settings page:
	 *<code>
	 *Dim in As Intent
	 *in.Initialize("android.settings.NFC_SETTINGS", "")
	 *StartActivity(in)</code>
	 */
	public boolean getIsEnabled() {
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(BA.applicationContext);
		return adapter != null && adapter.isEnabled();
	}
	/**
	 * Sets the sub that will handle the CreateMessage event.
	 */
	public void PreparePushMessage(final BA ba, final String EventName) {
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(ba.context);
		adapter.setNdefPushMessageCallback(new CreateNdefMessageCallback() {

			@Override
			public NdefMessage createNdefMessage(NfcEvent arg0) {
				final CountDownLatch latch = new CountDownLatch(1);
				final AtomicReference<List> ar = new AtomicReference<List>();
				BA.handler.post(new Runnable() {


					@Override
					public void run() {
						List Records = (List) ba.raiseEvent(NFC.this, EventName.toLowerCase(BA.cul) + 
								"_createmessage");
						ar.set(Records);
						latch.countDown();
					}
				});
				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				List r = ar.get();
				if (r == null || r.IsInitialized() == false)
					return null;
				return new NdefMessage(r.getObject().toArray(new NdefRecord[r.getSize()]));
			}

		},ba.activity, new Activity[0]);
		return;
	}
	
	/**
	 * Creates an NdefMessage with a Uri payload.
	 */
	public Object CreateUriRecord(String Uri) {
		return NdefRecord.createUri(Uri);
	}
	/**
	 * Creates an NdefMessage with the given mime type and data.
	 *Note that this method is only supported by Android 4.1+ (API 16).
	 */
	public Object CreateMimeRecord(String Mime, byte[] Data) {
		return NdefRecord.createMime(Mime, Data);
	}

	@ShortName("NdefRecord")
	public static class NdefRecordWrapper extends AbsObjectWrapper<NdefRecord> {
		/**
		 * Returns the whole payload.
		 */
		public byte[] GetPayload() {
			return getObject().getPayload();
		}

		private static java.util.List<String> UriTypes = Arrays.asList(
				""
				, "http://www."
				, "https://www."
				, "http://"
				, "https://"
				, "tel:"
				, "mailto:"
				, "ftp://anonymous:anonymous@"
				, "ftp://ftp."
				, "ftps://"
				, "sftp://"
				, "smb://"
				, "nfs://"
				, "ftp://"
				, "dav://"
				, "news:"
				, "telnet://"
				, "imap:"
				, "rtsp://"
				, "urn:"
				, "pop:"
				, "sip:"
				, "sips:"
				, "tftp:"
				, "btspp://"
				, "btl2cap://"
				, "btgoep://"
				, "tcpobex://"
				, "irdaobex://"
				, "file://"
				, "urn:epc:id:"
				, "urn:epc:tag:"
				, "urn:epc:pat:"
				, "urn:epc:raw:"
				, "urn:epc:"
				, "urn:nfc:");

		/**
		 * Reads the payload and returns the stored text.
		 */
		public String GetAsTextType() throws UnsupportedEncodingException {

			byte[] payload = getObject().getPayload();
			String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
			int languageCodeLength = payload[0] & 0077;
			@SuppressWarnings("unused")
			String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
			String text =
				new String(payload, languageCodeLength + 1,
						payload.length - languageCodeLength - 1, textEncoding);
			return text;
		}
		/**
		 * Reads the payload and returns the stored Uri.
		 */
		public String GetAsUriType() {
			byte[] payload = getObject().getPayload();
			String prefix = UriTypes.get(payload[0]);
			byte[] prefixBytes = prefix.getBytes(Charset.forName("UTF-8"));

			byte[] fullUri = new byte[prefixBytes.length + payload.length - 1];
			System.arraycopy(prefixBytes, 0, fullUri, 0, prefixBytes.length);
			System.arraycopy(payload, 1, fullUri, prefixBytes.length, payload.length - 1);
			Uri uri = Uri.parse(new String(fullUri, Charset.forName("UTF-8")));
			return uri.toString();
		}

	}
	/**
	 * Provides access to a scanned tag.
	 */
	@ShortName("TagTechnology")
	@Events(values={"Connected (Success As Boolean)", "RunAsync (Flag As Int, Success As Boolean, Result As Object)"})
	public static class TagTechnologyWrapper extends AbsObjectWrapper<TagTechnology> {
		/**
		 * Initializes the object.
		 *EventName - Set the subs that will handle the events.
		 *Tech - The NFC technology that will be used.
		 *Intent - The intent received in Activity_Resume.
		 */
		public void Initialize(String EventName, String Tech, Intent Intent) throws Exception {
			Tag tag = Intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			setObject((TagTechnology) Class.forName(Tech).getMethod("get", Tag.class).invoke(null, tag));
			AbsObjectWrapper.getExtraTags(getObject()).put("event", EventName.toLowerCase(BA.cul));
		}
		private String getEventName() {
			return (String) AbsObjectWrapper.getExtraTags(getObject()).get("event");
		}
		/**
		 * Connects to the tag. The Connected event will be raised.
		 */
		public void Connect(BA ba) {
			BA.runAsync(ba, getObject(), getEventName() + "_connected", new Object[] {false}, new Callable<Object[]>() {

				@Override
				public Object[] call() throws Exception {
					getObject().connect();
					return new Object[] {true};
				}

			});
		}
		/**
		 * Asynchronously runs the given method. This can be used to access native I/O methods.
		 *EventName - The sub that will handle the RunAsync event.
		 *Method - Java method name.
		 *Params - Array of parameters.
		 *Flag - Arbitrary number that will be passed to the RunAsync event.
		 */
		public void RunAsync(BA ba, final String EventName, final String Method,final Object[] Params, final int Flag) {
			BA.runAsync(ba, getObject(), EventName.toLowerCase(BA.cul) + "_runasync" , new Object[] {Flag, false, null}, new Callable<Object[]>() {

				@Override
				public Object[] call() throws Exception {
					JavaObject jo = new JavaObject();
					jo.setObject(getObject());
					Object res = jo.RunMethod(Method, Params);
					return new Object[] {Flag, true, res};
				}

			});
		}
		/**
		 * Closes the connection.
		 */
		public void Close() throws IOException {
			if (IsInitialized())
				getObject().close();
		}
		/**
		 * Returns true if there is an active connection.
		 */
		public boolean getConnected() {
			return getObject().isConnected();
		}
	}
}
