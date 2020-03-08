
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

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;

/**
 * See this <link>link|http://www.basic4ppc.com/forum/additional-libraries-classes-official-updates/19208-device-administrator-library.html</link> for more information.
 */
@ShortName("AdminManager")
@Version(1.10f)
public class AdminManager {
	public static final int PASSWORD_QUALITY_ALPHABETIC = 0x40000;
	public static final int PASSWORD_QUALITY_ALPHANUMERIC = 0x50000;
	public static final int PASSWORD_QUALITY_NUMERIC = 0x20000;
	public static final int PASSWORD_QUALITY_UNSPECIFIED = 0;

	@Hide
	public ComponentName rec = new ComponentName(BA.applicationContext, AdminReceiver2.class);

	@Hide
	public DevicePolicyManager dm = (DevicePolicyManager)BA.applicationContext.getSystemService(
			Context.DEVICE_POLICY_SERVICE);
	/**
	 * Enables the admin policy. The user will be shown a dialog with the requested features.
	 *This method can only be called from an Activity context.
	 *Explanation - A message shown at the top of the dialog.
	 */
	public void Enable(BA ba, String Explanation) {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, rec);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, Explanation);
		ba.sharedProcessBA.activityBA.get().context.startActivity(intent);
	}
	/**
	 * Returns true if the admin policy is active.
	 */
	public boolean getEnabled() {
		return dm.isAdminActive(rec);
	}
	/**
	 * Immediately locks the screen. Requires the force-lock tag in the policies file. 
	 */
	public void LockScreen() {
		dm.lockNow();
	}
	/**
	 * Disables the admin policy.
	 */
	public void Disable() {
		if (getEnabled())
			dm.removeActiveAdmin(rec);
	}
	/**
	 * Tests whether the current password meets the requirements.
	 *Requires the limit-password tag in the policies file.
	 */
	public boolean getPasswordSufficient() {
		return dm.isActivePasswordSufficient();
	}
	/**
	 * Sets the given password as the device password.
	 *Requires the reset-password tag in the policies file.	
	 */
	public boolean ResetPassword(String NewPassword) {
		return dm.resetPassword(NewPassword, 0);
	}
	/**
	 * Sets the minimum allowed length and quality for device passwords.
	 *These settings will affect new passwords.
	 *Requires the limit-password tag in the policies file.
	 *
	 *QualityFlag - One of the password quality flags.
	 *MinimumLength - Password minimum length.
	 *
	 *Example:<code>
	 *manager.SetPasswordQuality(manager.PASSWORD_QUALITY_ALPHANUMERIC, 4)</code>
	 */
	public void SetPasswordQuality(int QualityFlag, int MinimumLength) {
		dm.setPasswordMinimumLength(rec, MinimumLength);
		dm.setPasswordQuality(rec, QualityFlag);
	}
	/**
	 * Shows the new password activity. Note that the user might cancel the change.
	 */
	public void RequestNewPassword(BA ba) throws ClassNotFoundException {
		Intent i = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
		Common.StartActivity(ba, i);
	}

	/**
	 * Sets the maximum time (measured in milliseconds) before the device locks.
	 *This limits the length that the user can set in the settings screen.
	 *Requires the force-lock tag in the policies file.
	 */
	public void setMaximumTimeToLock(long value) {
		dm.setMaximumTimeToLock(rec, value);
	}

}
