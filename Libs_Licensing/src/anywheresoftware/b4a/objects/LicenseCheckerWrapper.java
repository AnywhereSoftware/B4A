
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

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;

/**
 * LicenseChecker allows you to add protection to your application and check whether the user is allowed to access your application.
 *See the <link>tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/11429-protect-your-android-application-licensing-library.html</link> for more information.
 */
@Version(1.21f)
@ShortName("LicenseChecker")
@Permissions(values={"com.android.vending.CHECK_LICENSE"})
@Events(values={"Allow", "DontAllow", "Error (ErrorCode As String)"})
public class LicenseCheckerWrapper {
	private LicenseChecker lc;
	private ServerManagedPolicy policy;
	private String eventName;
	@Hide
	public static String v, vv;
	/**
	 * Initializes the object.
	 *EventName - Sets the subs that will handle the events.
	 *DeviceId - A unique id that is used to encrypt the cached result.
	 *PublicKey - The publisher key as shown in your publisher account.
	 *Salt - A "random" array of bytes that is used during encryption / decryption.
	 *Example:<code>
	 *lc.Initialize("lc", PhoneId.GetDeviceId, PUBLIC_KEY, "kljsdflkj".GetBytes("UTF8"))</code>
	 */
	public void Initialize(String EventName, String DeviceId, String PublicKey, byte[] Salt) {
		policy = new ServerManagedPolicy(BA.applicationContext, new AESObfuscator(Salt, BA.packageName,
				DeviceId));
		this.eventName = EventName.toLowerCase(BA.cul);
		lc = new LicenseChecker(BA.applicationContext, policy, PublicKey);
	}
	/**
	 * The given value will be assigned to the given variable if the license check was successful.
	 *Preferably you should use this variable data in a later stage. This helps to prevent a hacker from hacking your application by removing the license check code.
	 *Variable - The name of a process global string variable (in the main activity).
	 *Value - The value that will be assigned to the variable if the check was successful.
	 *Example:<code>lc.SetVariableAndValue("NameOfSomeVariable", "Value")</code>
	 */
	public void SetVariableAndValue(String Variable, String Value) {
		v = Variable;
		vv = Value;
	}
	/**
	 * Checks whether the user is allowed to access the application.
	 *One of the events will be raised when the result is available.
	 */
	public void CheckAccess(final BA ba) {
		lc.checkAccess(new LicenseCheckerCallback() {

			@Override
			public void allow() {
				ba.raiseEventFromDifferentThread(null, null, -1, eventName + "_allow", true, null);
			}

			@Override
			public void applicationError(ApplicationErrorCode errorCode) {
				ba.raiseEventFromDifferentThread(null, null, -1, eventName + "_error", false, new Object[] {errorCode.toString()});
			}

			@Override
			public void dontAllow() {
				ba.raiseEventFromDifferentThread(null, null, -1, eventName + "_dontallow", false, null);
			}
		});
	}
}
