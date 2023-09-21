
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

import java.io.File;
import java.util.ArrayList;

import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

@ShortName("RuntimePermissions")
@Version(1.20f)
@DependsOn(values={"androidx.core:core"})
/**
 *This type is compatible with all versions of Android. It allows your app to use the new permissions system that was introduced in Android 6 (API 23).
 */
public class RuntimePermissions {

	public static final java.lang.String PERMISSION_READ_CALENDAR = "android.permission.READ_CALENDAR";
	public static final java.lang.String PERMISSION_WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";
	public static final java.lang.String PERMISSION_CAMERA = "android.permission.CAMERA";
	public static final java.lang.String PERMISSION_READ_CONTACTS = "android.permission.READ_CONTACTS";
	public static final java.lang.String PERMISSION_WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
	public static final java.lang.String PERMISSION_GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";
	public static final java.lang.String PERMISSION_ACCESS_CHECKIN_PROPERTIES = "android.permission.ACCESS_CHECKIN_PROPERTIES";
	public static final java.lang.String PERMISSION_ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
	public static final java.lang.String PERMISSION_ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
	public static final java.lang.String PERMISSION_RECORD_AUDIO = "android.permission.RECORD_AUDIO";
	public static final java.lang.String PERMISSION_CALL_PHONE = "android.permission.CALL_PHONE";
	public static final java.lang.String PERMISSION_READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
	public static final java.lang.String PERMISSION_READ_CALL_LOG = "android.permission.READ_CALL_LOG";
	public static final java.lang.String PERMISSION_WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
	public static final java.lang.String PERMISSION_ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";
	public static final java.lang.String PERMISSION_USE_SIP = "android.permission.USE_SIP";
	public static final java.lang.String PERMISSION_PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
	public static final java.lang.String PERMISSION_BODY_SENSORS = "android.permission.BODY_SENSORS";
	public static final java.lang.String PERMISSION_READ_SMS = "android.permission.READ_SMS";
	public static final java.lang.String PERMISSION_RECEIVE_SMS = "android.permission.RECEIVE_SMS";
	public static final java.lang.String PERMISSION_SEND_SMS = "android.permission.SEND_SMS";
	public static final java.lang.String PERMISSION_RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";
	public static final java.lang.String PERMISSION_RECEIVE_MMS = "android.permission.RECEIVE_MMS";
	public static final java.lang.String PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
	public static final java.lang.String PERMISSION_WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
	public static final java.lang.String PERMISSION_POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
	
	/**
	 * Checks whether the application has been granted the specified permission.
	 *This method can be called from a Service.
	 */
	public boolean Check(String Permission) {
		return ContextCompat.checkSelfPermission(BA.applicationContext, Permission) == PackageManager.PERMISSION_GRANTED;
	}
	static class RequestHandler implements Runnable {
		public BA aba;
		public ArrayList<String> permissions = new ArrayList<String>();
		public RequestHandler(BA aba) {
			this.aba = aba;
		}
		@Override
		public void run() {
			ActivityCompat.requestPermissions(aba.activity, permissions.toArray(new String[permissions.size()]), 0);
			aba = null;
		}
	}
	private RequestHandler reqHandler;
	/**
	 * Checks whether the application has been granted the specified permission. If not then the user will be shown a dialog asking for permission.
	 *The Activity_PermissionResult will be raised with the result (in all cases).
	 *This method can only be called from an Activity.
	 */
	public void CheckAndRequest(BA ba, final String Permission) {
		final BA aba = ba.sharedProcessBA.activityBA.get();
		if (aba == null)
			throw new RuntimeException("Can only be called from an Activity");
		final boolean result = Check(Permission);
		if (!result && Build.VERSION.SDK_INT >= 23) {
			if (reqHandler == null || reqHandler.aba == null || reqHandler.aba != aba) {
				reqHandler = new RequestHandler(aba);
				BA.handler.postDelayed(reqHandler, 200);
			}
			reqHandler.permissions.add(Permission);
		} else {
			aba.processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, new Object[] {Permission, result});
		}
	}
	/**
	 * Returns the path to the app's default folder on the secondary storage device.
	 *The path to File.DirInternal will be returned if there is no secondary storage available.
	 *It is a better alternative to File.DirDefaultExternal. On Android 4.4+ no permission is required to access this folder.
	 *You should add this code to the manifest editor to add the permission on older versions of Android:
	 *<code>AddManifestText(<uses-permission
	 *android:name="android.permission.WRITE_EXTERNAL_STORAGE"
	 *android:maxSdkVersion="18" />
	 *)</code>
	 *SubFolder - A sub folder that will be created for your app. Pass an empty string if not needed.
	 */
	public String GetSafeDirDefaultExternal(String SubFolder) {
		File[] f = ContextCompat.getExternalFilesDirs(BA.applicationContext, SubFolder);
		if (f == null || f.length == 0 || f[0] == null)
			return anywheresoftware.b4a.objects.streams.File.Combine(anywheresoftware.b4a.objects.streams.File.getDirInternal(), SubFolder);
		else
			return f[0].toString();
	}
	/**
	 * Returns an array with all the external folders available to your app. 
	 *The first element will be the same as the value returned from GetSafeDirDefaultExternal.
	 *On Android 4.4+ no permission is required to access these folders.
	 *On older versions only one folder will be returned. You should add the permission as explained in GetSafeDirDefaultExternal documentation.
	 *SubFolder - A sub folder that will be created for your app. Pass an empty string if not needed.
	 */
	public String[] GetAllSafeDirsExternal(String SubFolder) {
		File[] f = ContextCompat.getExternalFilesDirs(BA.applicationContext, SubFolder);
		if (f == null || f.length == 0)
			return new String[0];
		String[] s = new String[f.length];
		for (int i = 0;i < f.length;i++)
			s[i] = f[i] == null ? "" : f[i].toString();
		return s;
	}
}
