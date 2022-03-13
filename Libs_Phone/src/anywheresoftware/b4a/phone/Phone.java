
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.IOnActivityResult;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.ActivityWrapper;
import anywheresoftware.b4a.objects.collections.List;

@ShortName("Phone")
@Version(2.52f)
public class Phone {
	
	/**
	 * <b>The Phone library changed and several methods belonging to the Phone object have moved to PhoneId and PhoneVibrate.</b>
	 * The reason for this change is to avoid adding unnecessary permissions.
	 * The Phone library contains all kinds of features related to the Android phone.
	 *<b>Phone</b> object includes information about the device and also other general features.
	 *<b>PhoneAccelerometer</b> and PhoneOrientation objects are now replaced with PhoneSensors which support other sensors as well.
	 *<b>PhoneEvents</b> allows you to handle all kinds of system events.
	 *<b>PhoneId</b> gives access to the the specific phone values.
	 *<b>PhoneSms</b> supports sending Sms messages.
	 *<b>PhoneVibrate</b> vibrates the phone.
	 *<b>SmsMessages</b> together with Sms support fetching messages from the phone database.
	 *<b>SmsInterceptor</b> intercepts incoming Sms messages.
	 *<b>PhoneIntents</b> and PhoneCalls include several useful intents.
	 *<b>Email</b> helps with building an Intent that sends an email.
	 *<b>PhoneWakeState</b> allows you to force the screen and power to keep on.
	 *<b>Contact</b> and Contacts give access to the stored contacts.
	 *<b>CallLog</b> and CallItem give access to the phone calls log.
	 *<b>ContentChooser</b> allows the user to choose content from other applications. For example the user can choose an image from the Gallery application.
	 *<b>VoiceRecognition</b> converts speech to text.
	 *<b>LogCat</b> tracks the internal phone logs.
	 *<b>PackageManager</b> allows you to retrieve information about the installed applications.
	 */
	public static void LIBRARY_DOC() {
		//
	}
	/**
	 * Runs a native shell command. Many commands are inaccessible because of OS security restrictions.
	 *Calling Shell will block the calling thread until the other process completes.
	 *Command - Command to run.
	 *Args - Additional arguments. Can be Null if not needed.
	 *StdOut - A StringBuilder that will hold the standard output value. Can be Null if not needed.
	 *StdErr - A StringBuilder that will hold the standard error value. Can be Null if not needed.
	 *Returns the process exit value.
	 *Example:<code>
	 *Dim p As Phone
	 *Dim sb As StringBuilder
	 *sb.Initialize
	 *p.Shell("df", Null, sb, Null)
	 *Msgbox(sb.ToString, "Free space:")</code>
	 */
	public static int Shell(String Command, String[] Args, StringBuilder StdOut, StringBuilder StdErr) throws InterruptedException, IOException {
		Process process;
		if (Args == null || Args.length == 0)
			process = Runtime.getRuntime().exec(Command);
		else {
			String[] a = new String[1 + Args.length];
			a[0] = Command;
			System.arraycopy(Args, 0, a, 1, Args.length);
			process = Runtime.getRuntime().exec(a);
		}
		byte[] buffer = new byte[4096];
		int count;
		InputStream in;
		if (StdOut != null) {
			in = process.getInputStream();
			while ((count = in.read(buffer)) != -1) {
				StdOut.append(new String(buffer, 0, count, "UTF8"));
			}
		}
		if (StdErr != null) {
			in = process.getErrorStream();
			while ((count = in.read(buffer)) != -1) {
				StdErr.append(new String(buffer, 0, count, "UTF8"));
			}
		}
		process.waitFor();
		return process.exitValue();
	}
	/**
	 * Asynchronous version of Shell. Should be used with Wait For:
	 * <code>
	 *Dim p As Phone
	 *Wait For (p.ShellAsync("ping", Array As String("-c", "1", "b4x.com"))) Complete (Success As Boolean, ExitValue As Int, StdOut As String, StdErr As String)
	 *If Success Then
	 *	Log(ExitValue)
	 *	Log("Out: " & StdOut)
	 *	Log("Err: "&  StdErr)
	 *Else
	 *	Log("Error: " & LastException)
	 *End If</code>
	 */
	public static Object ShellAsync (final BA ba, final String Command, final String[] Args) {
		final StringBuilder out = new StringBuilder();
		final StringBuilder err = new StringBuilder();
		final Object sender = new Object();
		BA.runAsync(ba, sender, "complete", new Object[] {false, -1, out.toString(), err.toString()}, new Callable<Object[]>() {
			
			@Override
			public Object[] call() throws Exception {
				int res = Shell(Command, Args, out, err);
				return new Object[] {true, res, out.toString(), err.toString()};
			}
		});
		return sender;
	}
	/**
	 * Returns an internal drawable object.
	 *See this <link>page|http://developer.android.com/intl/fr/reference/android/R.drawable.html</link> for a list of available resources.
	 *Example:<code>
	 *Dim p As Phone
	 *Dim bd As BitmapDrawable
	 *bd = p.GetResourceDrawable(17301618)
	 *Activity.AddMenuItem2("Menu1", "Menu1", bd.Bitmap)</code>
	 */
	public Drawable GetResourceDrawable(int ResourceId) {
		return BA.applicationContext.getResources().getDrawable(ResourceId);
	}
	/**
	 * Tests whether the phone "airplane mode" is on.
	 */
	public static boolean IsAirplaneModeOn(){
		return Settings.System.getInt(BA.applicationContext.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	/**
	 * Returns the value of the phone settings based on the given key.
	 *The possible keys are listed <link>here|http://developer.android.com/intl/fr/reference/android/provider/Settings.Secure.html</link>.
	 *The keys are lower cased.
	 *Example:<code>
	 *Dim p As Phone
	 *Log(p.GetSettings("android_id"))</code>
	 */
	public static String GetSettings(String Settings) {
		String s = Secure.getString(BA.applicationContext.getContentResolver(), Settings);
		if (s == null) {
			s = android.provider.Settings.System.getString(BA.applicationContext.getContentResolver(), Settings);
			if (s == null)
				s = "";
		}
		return s;
	}
	/**
	 * Sends an intent to all BroadcastReceivers that listen to this type of intents.
	 *Example of asking the media scanner to rescan a file:<code>
	 *Dim i As Intent
	 *i.Initialize("android.intent.action.MEDIA_SCANNER_SCAN_FILE", _
	 *	"file://" & File.Combine(File.DirRootExternal, "pictures/1.jpg"))
	 *Dim p As Phone
	 *p.SendBroadcastIntent(i)</code> 
	 */
	public static void SendBroadcastIntent(Intent Intent) {
		BA.applicationContext.sendBroadcast(Intent);
	}
	/**
	 * Sets the brightness of the current activity. This method cannot be called from a service module.
	 *Value - A float between 0 to 1. Set -1 for automatic brightness.
	 *Example:<code>
	 *Sub Process_Globals
	 *	Dim phone1 As Phone
	 *End Sub
	 *
	 *Sub Globals
	 *	Dim sb As SeekBar
	 *End Sub
	 *
	 *Sub Activity_Create(FirstTime As Boolean)
	 *	sb.Initialize("sb")
	 *	sb.Max = 100
	 *	sb.Value = 50
	 *	Activity.AddView(sb, 10dip, 10dip, 90%x, 30dip)
	 *End Sub
	 *Sub sb_ValueChanged (Value As Int, UserChanged As Boolean)
	 *	phone1.SetScreenBrightness(Max(Value, 5) / 100)
	 *End Sub</code>
	 */
	public static void SetScreenBrightness(BA ba, float Value) {
		WindowManager.LayoutParams lp = ba.sharedProcessBA.activityBA.get().activity.getWindow().getAttributes();
		lp.screenBrightness = Value;
		ba.sharedProcessBA.activityBA.get().activity.getWindow().setAttributes(lp);
	}
	/**
	 * Changes the current activity orientation. This method cannot be called from a service module.
	 *Orientation - -1 for unspecified, 0 for landscape and 1 for portrait.
	 */
	public static void SetScreenOrientation(BA ba, int Orientation) {
		ba.sharedProcessBA.activityBA.get().activity.setRequestedOrientation(Orientation);
	}
	
	/**
	 * Returns an integer describing the SDK version.
	 */
	public static int getSdkVersion() {
		return Build.VERSION.SDK_INT;
	}
	public static String getModel() {
		return Build.MODEL;
	}
	public static String getManufacturer() {
		return Build.MANUFACTURER;
	}
	public static String getProduct() {
		return Build.PRODUCT;
	}

	/**
	 * Hides the soft keyboard if it is displayed.
	 *Example:<code>
	 *Dim p As Phone
	 *p.HideKeyboard(Activity)</code>
	 */
	public static void HideKeyboard(ActivityWrapper Activity) {
		InputMethodManager imm = (InputMethodManager)BA.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(Activity.getObject().getWindowToken(), 0);
		
	}
	/**
	 * Returns the code of the SIM provider.
	 *Returns an empty string if it is not available.
	 */
	public static String GetSimOperator() {
		TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
		String s = tm.getSimOperator();
		return s == null ? "" : s;
	}
	/**
	 * Returns the name of current registered operator.
	 *Returns an empty string if it is not available.
	 */
	public static String GetNetworkOperatorName() {
		TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
		String s = tm.getNetworkOperatorName();
		return s == null ? "" : s;
	}
	/**
	 * Returns true if the device is considered roaming on the current network.
	 */
	public static boolean IsNetworkRoaming() {
		TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.isNetworkRoaming();
	}
	/**
	 * Returns the currently used cellular network type.
	 *Possible values: 1xRTT, CDMA, EDGE, EHRPD, EVDO_0, EVDO_A, EVDO_B, GPRS, HSDPA, 
	 *HSPA, HSPAP, HSUPA, IDEN, LTE, UMTS, UNKNOWN.
	 */
	public static String GetNetworkType() {
		TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
		int i = tm.getNetworkType();
		switch (i) {
		case 7:
			return "1xRTT";
		case 4:
			return "CDMA";
		case 14:
			return "EHRPD";
		case 2:
			return "EDGE";
		case 5:
			return "EVDO_0";
		case 6:
			return "EVDO_A";
		case 12:
			return "EVDO_B";
		case 1:
			return "GPRS";
		case 8:
			return "HSDPA";
		case 10:
			return "HSPA";
		case 15:
			return "HSPAP";
		case 9:
			return "HSUPA";
		case 11:
			return "IDEN";
		case 13:
			return "LTE";
		case 3:
			return "UMTS";
		default:
			return "UNKNOWN";
		}
	}
	/**
	 * Returns the phone radio type. Possible values: CDMA, GSM, NONE.
	 */
	public static String GetPhoneType() {
		TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
		int i = tm.getPhoneType();
		switch (i) {
		case 2:
			return "CDMA";
		case 1:
			return "GSM";
		default:
			return "NONE";
		}
	}
	/**
	 * Returns the current cellular data connection state.
	 *Possible values: DISCONNECTED, CONNECTING, CONNECTED, SUSPENDED.
	 */
	public static String GetDataState() {
		TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
		int i = tm.getDataState();
		switch (i) {
		case 2:
			return "CONNECTED";
		case 0:
			return "DISCONNECTED";
		case 3:
			return "SUSPENDED";
		case 1:
			return "CONNECTING";
		}
		return "DISCONNECTED";
	}
	/**
	 * Phone ring channel.
	 */
	public static final int VOLUME_RING = AudioManager.STREAM_RING;
	/**
	 * Alarms channel.
	 */
	public static final int VOLUME_ALARM = AudioManager.STREAM_ALARM;
	/**
	 * Music channel.
	 */
	public static final int VOLUME_MUSIC = AudioManager.STREAM_MUSIC;
	/**
	 * Notifications channel.
	 */
	public static final int VOLUME_NOTIFICATION = AudioManager.STREAM_NOTIFICATION;
	/**
	 * System sounds channel.
	 */
	public static final int VOLUME_SYSTEM = AudioManager.STREAM_SYSTEM;
	/**
	 * Voice calls channel.
	 */
	public static final int VOLUME_VOICE_CALL = AudioManager.STREAM_VOICE_CALL;
	/**
	 * Phone ringer will be silent and it will not vibrate.
	 */
	public static final int RINGER_SILENT = AudioManager.RINGER_MODE_SILENT;
	/**
	 * Normal phone ringer mode.
	 */
	public static final int RINGER_NORMAL = AudioManager.RINGER_MODE_NORMAL;
	/**
	 * Phone ringer will vibrate and silent.
	 */
	public static final int RINGER_VIBRATE = AudioManager.RINGER_MODE_VIBRATE;
	/**
	 * Gets the maximum volume index (value) for the given channel.
	 *Channel - One of the VOLUME constants.
	 */
	public static int GetMaxVolume(int Channel) {
		return getAudioManager().getStreamMaxVolume(Channel);
	}
	/**
	 * Sets the volume of the specified channel.
	 *Channel - One of the VOLUME constants.
	 *VolumeIndex - The volume index. GetMaxVolume can be used to find the largest possible value.
	 *ShowUI - Whether to show the volume UI windows.
	 *Starting from Android 7+ this method will throw an exception if the user set the Do Not Disturb mode,
	 *unless your app has requested a special permission with NOTIFICATION_POLICY_ACCESS_SETTINGS.
	 *Example:<code>
	 *Dim p As Phone
	 *p.SetVolume(p.VOLUME_MUSIC, 3, True)</code>
	 */
	public static void SetVolume(int Channel, int VolumeIndex, boolean ShowUI) {
		getAudioManager().setStreamVolume(Channel, VolumeIndex, ShowUI ? AudioManager.FLAG_SHOW_UI : 0);
	}
	/**
	 * Returns the volume of the specified channel.
	 *Channel - One of the VOLUME constants.
	 */
	public static int GetVolume(int Channel) {
		return getAudioManager().getStreamVolume(Channel);
	}
	/**
	 * Mutes or unmutes the given channel.
	 *Channel - One of the VOLUME constants.
	 *Mute - Whether to mute or unmute the channel.
	 *Starting from Android 7+ this method will throw an exception if the user set the Do Not Disturb mode,
	 *unless your app has requested a special permission with NOTIFICATION_POLICY_ACCESS_SETTINGS.
	 */
	public static void SetMute(int Channel, boolean Mute) {
		getAudioManager().setStreamMute(Channel, Mute);
	}
	/**
	 * Sets the phone ringer mode.
	 *Mode - One of the RINGER constants.
	 *Starting from Android 7+ this method will throw an exception if the user set the Do Not Disturb mode,
	 *unless your app has requested a special permission with NOTIFICATION_POLICY_ACCESS_SETTINGS.
	 *Example:<code>
	 *Dim p As Phone
	 *p.SetRingerMode(p.RINGER_VIBRATE)</code>
	 */
	public static void SetRingerMode(int Mode) {
		getAudioManager().setRingerMode(Mode);
	}
	
	/**
	 * Returns the phone ringer mode.
	 *Value will be one of the RINGER constants.
	 */
	public static int GetRingerMode() {
		return getAudioManager().getRingerMode();
	}
	private static AudioManager getAudioManager() {
		return (AudioManager)BA.applicationContext.getSystemService(Context.AUDIO_SERVICE);
	}
	@ShortName("PhoneId")
	@Permissions(values={"android.permission.READ_PHONE_STATE"})
	public static class PhoneId {
		/**
		 * Returns a unique device Id. Returns an empty string if the device Id is not available (usually on wifi only devices).
		 *<b>This method will not work on Android 10+ devices. It will throw an exception in some cases. Do not use.</b>
		 */
		public static String GetDeviceId() {
			TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			String s = tm.getDeviceId();
			return s == null ? "" : s;
		}
		/**
		 * Returns the phone number string for line 1 as configured in the SIM card.
		 *Returns an empty string if it is not available.
		 */
		public static String GetLine1Number() {
			TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			String s = tm.getLine1Number();
			return s == null ? "" : s;
		}
		/**
		 * Returns the unique subscriber id.
		 *Returns an empty string if it is not available.
		 */
		public static String GetSubscriberId() {
			TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			String s = tm.getSubscriberId();
			return s == null ? "" : s;
		}
		/**
		 * Returns the serial number of the SIM card.
		 *Returns an empty string if it is not available.
		 */
		public static String GetSimSerialNumber() {
			TelephonyManager tm = (TelephonyManager) BA.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			String s = tm.getSimSerialNumber();
			return s == null ? "" : s;
		}
	}
	@ShortName("PhoneVibrate")
	@Permissions(values={"android.permission.VIBRATE"})
	public static class PhoneVibrate {
		/**
		 * Vibrates the phone for the specified duration.
		 */
		public static void Vibrate(BA ba, long TimeMs) {
			Vibrator v = (Vibrator)ba.context.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(TimeMs);
		}
	}
	/**
	 * The PhoneWakeState object allows you to prevent the device from turning off the screen.
	 *Once you call KeepAlive the phone screen will stay on till you call ReleaseKeepAlive.
	 *It is important to eventually release it.
	 *A recommended usage is to call KeepAlive in Activity_Resume and call ReleaseKeepAlive in Activity_Pause.
	 *Note that the user can still turn off the screen by pressing on the power button.
	 *Calling PartialLock will prevent the CPU from going to sleep even if the user presses on the power button.
	 *It will not however affect the screen.
	 */
	@ShortName("PhoneWakeState")
	@Permissions(values={"android.permission.WAKE_LOCK"})
	public static class PhoneWakeState {
		private static PowerManager.WakeLock wakeLock;
		private static PowerManager.WakeLock partialLock;
		/**
		 * Prevents the device from going to sleep.
		 *Call ReleaseKeepAlive to release the power lock.
		 *BrightScreen - Whether to keep the screen bright or dimmed.
		 */
		public static void KeepAlive(BA ba, boolean BrightScreen) {
			if (wakeLock != null && wakeLock.isHeld()) {
				Common.Log("WakeLock already held.");
				return;
			}
			PowerManager pm = (PowerManager)ba.context.getSystemService(
					Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock((BrightScreen ? PowerManager.SCREEN_BRIGHT_WAKE_LOCK : 
				PowerManager.SCREEN_DIM_WAKE_LOCK )
				| PowerManager.ACQUIRE_CAUSES_WAKEUP,
			"B4A");
			wakeLock.acquire();
		}
		/**
		 * Acquires a partial lock. This will prevent the CPU from going to sleep, even if the user presses on the power button.
		 *Make sure to call ReleasePartialLock eventually to release this lock.
		 */
		public static void PartialLock(BA ba) {
			if (partialLock != null && partialLock.isHeld()) {
				Common.Log("Partial wakeLock already held.");
				return;
			}
			PowerManager pm = (PowerManager)ba.context.getSystemService(
					Context.POWER_SERVICE);
			partialLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "B4A-Partial");
			partialLock.acquire();
		}
		/**
		 * Releases the power lock and allows the device to go to sleep.
		 */
		public static void ReleaseKeepAlive() {
			if (wakeLock == null || wakeLock.isHeld() == false) {
				Common.Log("No wakelock.");
				return;
			}
			wakeLock.release();
		}
		/**
		 * Releases a partial lock that was previously acquired by calling PartialLock.
		 */
		public static void ReleasePartialLock() {
			if (partialLock == null || partialLock.isHeld() == false) {
				Common.Log("No partial wakelock.");
				return;
			}
			partialLock.release();
		}
	}
	
	/**
	 * This object contains methods that create intents objects. An intent does nothing until you call StartActivity with the intent.
	 *Calling StartActivity sends the intent to the OS.
	 */
	@ShortName("PhoneIntents")
	public static class PhoneIntents {
		/**
		 * Creates an intent that will open the specified URI.
		 *Example:<code>
		 *StartActivity (PhoneIntents.OpenBrowser("http://www.google.com"))</code>
		 */
		public static Intent OpenBrowser(String Uri) {
			Intent i = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(Uri));
			return i;
		}
		/**
		 * Creates an intent that will start playing the given audio file with the default player.
		 *This method cannot work with internal files.
		 */
		public static Intent PlayAudio(String Dir, String File) {
			Uri uri;
			if (Dir.equals(anywheresoftware.b4a.objects.streams.File.ContentDir))
				uri = Uri.parse(File);
			else
				uri = Uri.parse("file://" + new java.io.File(Dir, File).toString());
			Intent it = new Intent(Intent.ACTION_VIEW);
			it.setDataAndType(uri, "audio/*");
			return it;
		}
		/**
		 * Creates an intent that will start playing the given video file with the default player.
		 *This method cannot work with internal files.
		 */
		public static Intent PlayVideo(String Dir, String File) {
			Uri uri;
			if (Dir.equals(anywheresoftware.b4a.objects.streams.File.ContentDir))
				uri = Uri.parse(File);
			else
				uri = Uri.parse("file://" + new java.io.File(Dir, File).toString());
			Intent it = new Intent(Intent.ACTION_VIEW);
			it.setDataAndType(uri, "video/*");
			return it;
		}
		
	}
	/**
	 * This object creates an intent that launches the phone application.
	 *The reason that it is not part of the PhoneIntents library is that it requires an additional permission.
	 */
	@ShortName("PhoneCalls")
	@Permissions(values={"android.permission.CALL_PHONE"})
	public static class PhoneCalls {
		/**
		 * Creates an intent that will call a phone number.
		 *Example:<code>
		 *Dim p As PhoneCalls
		 *StartActivity(p.Call("1234567890"))</code>
		 */
		public static Intent Call(String PhoneNumber) {
			Uri uri = Uri.parse("tel:" + PhoneNumber);
			Intent it = new Intent(Intent.ACTION_CALL, uri);
			return it;
		}
	}

	@ShortName("PhoneSms")
	@Permissions(values={"android.permission.SEND_SMS"})
	public static class PhoneSms {
		/**
		 * Sends an Sms message. Note that this method actually sends the message (unlike most other methods that
		 *create an intent object).
		 *You can use PhoneEvents to handle the SmsSentStatus and SmsDelivered events.
		 *This method is equivalent to calling Send2(PhoneNumber, Text, True, True).
		 */
		public static void Send(String PhoneNumber, String Text) {
			Send2(PhoneNumber, Text, true, true);
		}
		/**
		 * Sends an Sms message. Note that this method actually sends the message (unlike most other methods that
		 *create an intent object).
		 *You can use PhoneEvents to handle the SmsSentStatus and SmsDelivered events.
		 *ReceiveSentNotification - If true then the SmsSentStatus event (PhoneEvents) will be raised when the message is sent.
		 *ReceiveDeliveredNotification - If true then the SmsDelivered event (PhoneEvents) will be raised when the message is delivered.
		 *Note that the two above notifications might incur an additional payment.
		 */
		public static void Send2(String PhoneNumber, String Text, boolean ReceiveSentNotification, boolean ReceiveDeliveredNotification) {
			SmsManager sm = SmsManager.getDefault();
			Intent i1 = new Intent("b4a.smssent");
			i1.putExtra("phone", PhoneNumber);
			PendingIntent pi = ReceiveSentNotification ? PendingIntent.getBroadcast(BA.applicationContext, 0,i1, PendingIntent.FLAG_UPDATE_CURRENT) : null;
			Intent i2 = new Intent("b4a.smsdelivered");
			i2.putExtra("phone", PhoneNumber);
			PendingIntent pi2 = ReceiveDeliveredNotification ? PendingIntent.getBroadcast(BA.applicationContext, 0,i2, PendingIntent.FLAG_UPDATE_CURRENT) : null;
			sm.sendTextMessage(PhoneNumber, null, Text, pi, pi2);
		}

		
	}
	
	/**
	 * Using an Email object you can create an intent that holds a complete email message.
	 *You can then launch the email application by calling StartActivity. Note that the email will not be sent automatically. The user will need to press on the send button.
	 *Example:<code>
	 *Dim Message As Email
	 *Message.To.Add("SomeEmail@example.com")
	 *Message.Attachments.Add(File.Combine(File.DirRootExternal, "SomeFile.txt"))
	 *StartActivity(Message.GetIntent)</code>
	 */
	@ShortName("Email")
	public static class Email {
		public String Subject = "";
		public String Body = "";
		public anywheresoftware.b4a.objects.collections.List To = new anywheresoftware.b4a.objects.collections.List();
		public anywheresoftware.b4a.objects.collections.List CC = new anywheresoftware.b4a.objects.collections.List();
		public anywheresoftware.b4a.objects.collections.List BCC = new anywheresoftware.b4a.objects.collections.List();
		public anywheresoftware.b4a.objects.collections.List Attachments = new anywheresoftware.b4a.objects.collections.List();
		public Email() {
			To.Initialize();
			CC.Initialize();
			BCC.Initialize();
			Attachments.Initialize();
		}
		/**
		 * Returns the Intent that should be sent with StartActivity.
		 *The email message will be a Html message.
		 */
		public Intent GetHtmlIntent() {
			return getIntent(true);
		}
		private Intent getIntent(boolean html) {
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType(html ? "text/html" : "text/plain");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, 
					To.getObject().toArray(new String[0]));
			emailIntent.putExtra(android.content.Intent.EXTRA_CC, 
					CC.getObject().toArray(new String[0]));
			emailIntent.putExtra(android.content.Intent.EXTRA_BCC, 
					BCC.getObject().toArray(new String[0]));
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, Subject);
			emailIntent.putExtra(Intent.EXTRA_TEXT, html ? Html.fromHtml(Body) : Body);
			ArrayList<Uri> uris = new ArrayList<Uri>();
			for (Object file : Attachments.getObject())
			{
				if (file instanceof Uri) {
					uris.add((Uri)file);
				}
				else {
					File fileIn = new File((String)file);
					Uri u = Uri.fromFile(fileIn);
					uris.add(u);
				}
			}
			if (uris.size() == 1) {
				emailIntent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
				emailIntent.setAction(Intent.ACTION_SEND);
			}
			else if (uris.size() > 1) {
				emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			}
			return emailIntent;
		}
		/**
		 * Returns the Intent that should be sent with StartActivity.
		 */
		public Intent GetIntent() {
			return getIntent(false);
		}
	}
	/**
	 * LogCat allows you to read the internal phone logs.
	 *Refer to the <link>LogCat documentation|http://developer.android.com/intl/fr/guide/developing/tools/adb.html#logcat</link> for more information about the optional arguments.
	 *The LogCatData event is raised when there is new data available.
	 *You should use BytesToString to convert the raw bytes to string.
	 *<b>Note that the LogCatData event is raised in a different thread.</b> This means that you can only log the messages.
	 *You can also use the Threading library to delegate the data to the main thread.
	 */
	@ShortName("LogCat")
	@Events(values={"LogCatData (Buffer() As Byte, Length As Int)"})
	public static class LogCat {
		private static Thread logcatReader;
		private static volatile boolean logcatWorking;
		private static volatile InputStream logcatStream;
		private static Process lc;
		/**
		 * Starts tracking the logs.
		 *Args - Optional arguments passed to the internal LogCat command.
		 *EventName - The Sub that will handle the LogCatData event.
		 */
		public static void LogCatStart(final BA ba, String[] Args, String EventName) throws InterruptedException, IOException {
			LogCatStop();
			if (logcatReader != null) {
				int wait = 10;
				while (logcatReader.isAlive() && wait-- > 0) {
					Thread.sleep(50);
					logcatReader.interrupt();
				}
			}
			final String[] a = new String[1 + Args.length];
			a[0] = "/system/bin/logcat";
			System.arraycopy(Args, 0, a, 1, Args.length);
			final String ev = EventName.toLowerCase(BA.cul) + "_logcatdata";
			logcatReader = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						lc = Runtime.getRuntime().exec(a);
						logcatStream = lc.getInputStream();
						InputStream in = logcatStream;
						logcatWorking = true;
						byte[] buffer = new byte[4092];
						while (logcatWorking) {
							int count = in.read(buffer);
							Thread.sleep(100);
							while (count > 0 && in.available() > 0 && count < buffer.length) {
								count += in.read(buffer, count, buffer.length - count);
								Thread.sleep(100);
							}
							if (count == -1) {
								logcatWorking = false;
								break;
							}
							ba.raiseEvent(null, ev, buffer, count);
						}
						lc.destroy();
					} catch (Exception e) {
						if (lc != null)
							lc.destroy();
					}
					
				}

			});
			logcatReader.setDaemon(true);
			logcatReader.start();
		}
		/**
		 * Stops tracking the logs.
		 */
		public static void LogCatStop() throws IOException {
			logcatWorking = false;
			if (logcatStream != null)
				logcatStream.close();
			if (lc != null)
				lc.destroy();
		}
	}
	/**
	 * The ContentChooser object allows the user to select a specific type of content using other installed applications.
	 *For example the user can use the internal Gallery application to select an image.
	 *If the user has installed a file manager then the ContentChooser can be used to select general files.
	 *This object should usually be declared as a process global object.
	 *After initializing the object you can let the user select content by calling Show with the required MIME types.
	 *The Result event will be raised with a Success flag and with the content Dir and FileName.
	 *Note that these values may point to resources other than regular files. Still you can pass them to methods that expect Dir and FileName.
	 *Only content types that can be opened with an InputStream are supported.
	 */
	@ShortName("ContentChooser")
	@Events(values={"Result (Success As Boolean, Dir As String, FileName As String)"})
	public static class ContentChooser implements CheckForReinitialize{
		private String eventName;
		private IOnActivityResult ion;
		/**
		 * Initializes the object and sets the Sub that will handle the Result event.
		 *Example:<code>
		 *Dim CC As ContentChooser
		 *CC.Initialize("CC")</code>
		 */
		public void Initialize(String EventName) {
			eventName = EventName.toLowerCase(BA.cul);
		}
		@Override
		public boolean IsInitialized() {
			return eventName != null;
		}
		/**
		 * Sends the request to the system. If there are more than one applications that support the given Mime then a list with the applications will be displayed to the user.
		 *The Result event will be raised after the user chose an item or canceled the dialog.
		 *Mime - The content MIME type.
		 *Title - The title of the chooser dialog (when there is more than one application).
		 *Examples:<code>
		 *CC.Show("image/*", "Choose image")
		 *CC.Show("audio/*", "Choose audio file")</code>
		 */
		public void Show(final BA ba, String Mime, String Title) {
			if (eventName == null)
				throw new RuntimeException("ContentChooser not initialized.");
			Intent in = new Intent(Intent.ACTION_GET_CONTENT);
			in.setType(Mime);
			in.addCategory(Intent.CATEGORY_OPENABLE);
			in = Intent.createChooser(in, Title);
			ion = new IOnActivityResult() {
				@Override
				public void ResultArrived(int resultCode, Intent intent) {
					String Dir = null, File = null;
					if (resultCode == Activity.RESULT_OK && intent != null && intent.getData() != null) {
						try {
							Uri uri = intent.getData();
							String scheme = uri.getScheme();
							if (ContentResolver.SCHEME_FILE.equals(scheme)) {
								Dir = "";
								File = uri.getPath();
							}
							else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
								Dir = anywheresoftware.b4a.objects.streams.File.ContentDir;
								File = uri.toString();
							}

						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					ion = null;
					if (Dir != null && File != null) {
						ba.raiseEvent(ContentChooser.this, eventName + "_result", true, Dir, File);
					}
					else {
						ba.raiseEvent(ContentChooser.this, eventName + "_result", false, "", "");
					}
				}
			};
			ba.startActivityForResult(ion, in);
		}

	}
	/**
	 * Most Android devices support voice recognition (speech to text). Usually the service works by sending the audio stream to some external server
	 *which analyzes the stream and returns the possible results.
	 *Working with this object is quite simple.
	 *You should declare a VoiceRecognition object as a process global object and initialize it in Activity_Create when FirstTime is True.
	 *Later when you call Listen a dialog will be displayed, asking the user to speak. The Result event will be raised with a Success flag and a list with the possible results (usually one result).
	 */
	@ShortName("VoiceRecognition")
	@Events(values={"Result (Success As Boolean, Texts As List)"})
	public static class VoiceRecognition {
		private String eventName;
		private IOnActivityResult ion;
		private String prompt;
		private String language;
		/**
		 * Initializes the object and sets the Sub that will catch the Result event.
		 *Example:<code>
		 *Dim VR As VoiceRecognition
		 *VR.Initialize("VR")</code>
		 */
		public void Initialize(String EventName) {
			eventName = EventName.toLowerCase(BA.cul);
		}
		/**
		 * Sets the prompt that is displayed in the "Speak now" dialog in addition to the "Speak now" message.
		 */
		public void setPrompt(String value) {
			prompt = value;
		}
		/**
		 * Sets the language used. By default the device default language is used.
		 *Example:<code>
		 *VR.Language = "en"</code>
		 */
		public void setLanguage(String value) {
			language = value;
		}
		/**
		 * Tests whether voice recognition is supported on this device.
		 */
		public boolean IsSupported() {
			PackageManager pm = BA.applicationContext.getPackageManager();
			return pm.queryIntentActivities(
					new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0).size() > 0;
		}
		/**
		 * Starts listening. The Result event will be raised when the result arrives.
		 */
		public void Listen(final BA ba) {
			if (eventName == null)
				throw new RuntimeException("VoiceRecognition was not initialized.");
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			
			Listen2(ba, i);
		}
		/**
		 * Similar to Listen. Allows you to build the intent yourself.
		 */
		public void Listen2(final BA ba, Intent RecognizeIntent) {
			if (prompt != null && prompt.length() > 0)
				RecognizeIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
			if (language != null && language.length() > 0)
				RecognizeIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
			ion = new IOnActivityResult() {

				@SuppressWarnings("unchecked")
				@Override
				public void ResultArrived(int resultCode, Intent intent) {
					List list = new List();
					if (resultCode == Activity.RESULT_OK) {
						ArrayList<String> t = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
						if (t.size() > 0) {
							list.setObject((java.util.ArrayList)t);
						}
					}
					ion = null;
					ba.raiseEvent(VoiceRecognition.this, eventName + "_result", list.IsInitialized(), list);

				}
			};
			ba.startActivityForResult(ion, RecognizeIntent);
		}
	}
	/**
	 * This object gives access to the internal orientation sensors.
	 *See the <link>Orientation and accelerometers example|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6647-orientation-accelerometer.html</link>.
	 *This object should be declared as a process global object.
	 */
	@ShortName("PhoneOrientation")
	@Events(values={"OrientationChanged (Azimuth As Float, Pitch As Float, Roll As Float)"})
	public static class PhoneOrientation {
		private SensorEventListener listener;
		/**
		 * Starts listening for OrientationChanged events.
		 */
		public void StartListening(final BA ba, String EventName) {
			SensorManager sm = (SensorManager) ba.context.getSystemService(Context.SENSOR_SERVICE);
			final String s = EventName.toLowerCase(BA.cul) + "_orientationchanged";
			listener = new SensorEventListener() {

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					//
				}

				@Override
				public void onSensorChanged(SensorEvent event) {
					ba.raiseEvent(this, s, event.values[0], event.values[1], event.values[2]);
				}

			};
			sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
		}
		/**
		 * Stops listening for events.
		 */
		public void StopListening(BA ba) {
			if (listener != null) {
				SensorManager sm = (SensorManager) ba.context.getSystemService(Context.SENSOR_SERVICE);
				sm.unregisterListener(listener);
			}
		}
	}
	/**
	 * This object gives access to the internal accelerometers sensors.
	 *See the <link>Orientation and accelerometers example|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6647-orientation-accelerometer.html</link>.
	 *This object should be declared as a process global object.
	 */
	@ShortName("PhoneAccelerometer")
	@Events(values={"AccelerometerChanged (X As Float, Y As Float, Z As Float)"})
	public static class PhoneAccelerometer {
		private SensorEventListener listener;
		/**
		 * Starts listening for AccelerometerChanged events.
		 */
		public void StartListening(final BA ba, String EventName) {
			SensorManager sm = (SensorManager) ba.context.getSystemService(Context.SENSOR_SERVICE);
			final String s = EventName.toLowerCase(BA.cul) + "_accelerometerchanged";
			listener = new SensorEventListener() {

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					//
				}

				@Override
				public void onSensorChanged(SensorEvent event) {
					ba.raiseEvent(this, s, event.values[0], event.values[1], event.values[2]);
				}

			};
			sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
		/**
		 * Stops listening for events.
		 */
		public void StopListening(BA ba) {
			if (listener != null) {
				SensorManager sm = (SensorManager) ba.context.getSystemService(Context.SENSOR_SERVICE);
				sm.unregisterListener(listener);
			}
		}
	}
	/**
	 * The PhoneSensors object allows you to listen for changes in one of the device sensors.
	 *See the <link>Sensors example|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6647-orientation-accelerometer.html</link>.
	 *Most devices do not support all sensors. The StartListening method returns False if the sensor is not supported.
	 *After initializing the object and calling StartListening, the SensorChanged event will be raised each time the sensor value changes.
	 *The value is passed as an array of Floats. Some sensors pass a single value and some pass three values. 
	 */
	@ShortName("PhoneSensors")
	@Events(values={"SensorChanged (Values() As Float)"})
	public static class PhoneSensors{
		/**
		 * Single value - Ambient light level measured in SI lux units.
		 */
		public static int TYPE_LIGHT = Sensor.TYPE_LIGHT;
		/**
		 * Three values - Ambient magnetic field measured in micro-Tesla for the X, Y and Z axis.
		 */
		public static int TYPE_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD;
		/**
		 * Single value - Atmospheric pressure.
		 */
		public static int TYPE_PRESSURE = Sensor.TYPE_PRESSURE;
		/**
		 * Single value - Proximity measured in centimeters. Most devices will return only two possible values representing "near" and "far".
		 *"far" should match MaxRange and "near" should be a value smaller than MaxRange.
		 */
		public static int TYPE_PROXIMITY = Sensor.TYPE_PROXIMITY;
		/**
		 * Single value - Ambient temperature.
		 */
		public static int TYPE_TEMPERATURE = Sensor.TYPE_TEMPERATURE;
		/**
		 * Three values - Angular velocity measured in Radians / Second around each of the three axis.
		 */
		public static int TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
		/**
		 * Three values - Acceleration measured in Meters / Second ^ 2 for each axis (X, Y and Z). 
		 */
		public static int TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
		/**
		 * Three values - Orientation measured in degrees for azimuth, pitch and roll.
		 */
		public static int TYPE_ORIENTATION = Sensor.TYPE_ORIENTATION;
		private int currentType;
		private int sensorDelay;
		private SensorEventListener listener;
		/**
		 * Initializes the object and sets the sensor type (one of the TYPE_ constants).
		 */
		public void Initialize(int SensorType) {
			Initialize2(SensorType, 3);
		}
		/**
		 * Initializes the object and sets the sensor type and sensor events rate.
		 *SensorType - One of the TYPE_ constants.
		 *SensorDelay - A value between 0 (fastest rate) to 3 (slowest rate). This is only a hint to the system.
		 */
		public void Initialize2(int SensorType, int SensorDelay) {
			this.currentType = SensorType;
			this.sensorDelay = SensorDelay;
		}
		@Hide
		public SensorEvent sensorEvent;
		/**
		 * Starts listening for sensor events.
		 *Returns True if the sensor is supported.
		 *Usually you will want to start listening in Sub Activity_Resume and stop listening in Sub Activity_Pause.
		 */
		public boolean StartListening(final BA ba, String EventName) {
			SensorManager sm = (SensorManager) BA.applicationContext.getSystemService(Context.SENSOR_SERVICE);
			final String s = EventName.toLowerCase(BA.cul) + "_sensorchanged";
			listener = new SensorEventListener() {

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					//
				}

				@Override
				public void onSensorChanged(SensorEvent event) {
					sensorEvent = event;
					ba.raiseEvent(PhoneSensors.this, s, event.values);
				}

			};
			return sm.registerListener(listener, sm.getDefaultSensor(currentType), sensorDelay);
		}
		/**
		 * Returns the event accuracy, between 0 (unreliable) to 3 (maximum accuracy).
		 */
		public int getAccuracy() {
			if (sensorEvent == null)
				return SensorManager.SENSOR_STATUS_UNRELIABLE;
			return sensorEvent.accuracy;
		}
		/**
		 * Returns the event timestamp measured in nanoseconds. Note that the actual value has different meanings on different devices.
		 *Thus it should only be used to compare between sensor events. 
		 */
		public long getTimestamp() {
			if (sensorEvent == null)
				return 0;
			return sensorEvent.timestamp;
		}
		/**
		 * Stops listening for events.
		 */
		public void StopListening(BA ba) {
			if (listener != null) {
				SensorManager sm = (SensorManager) BA.applicationContext.getSystemService(Context.SENSOR_SERVICE);
				sm.unregisterListener(listener);
			}
		}
		/**
		 * Returns the maximum value for this sensor.
		 *Returns -1 if this sensor is not supported.
		 */
		public float getMaxValue() {
			java.util.List<Sensor> l = ((SensorManager) BA.applicationContext.getSystemService(Context.SENSOR_SERVICE)).getSensorList(currentType);
			if (l == null || l.size() == 0)
				return -1;
			return l.get(0).getMaximumRange();
		}


	}

}
