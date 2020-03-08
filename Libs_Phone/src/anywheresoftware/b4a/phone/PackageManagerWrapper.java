
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

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.IntentWrapper;
import anywheresoftware.b4a.objects.collections.List;

/**
 * The PackageManager allows you to find information about installed applications.
 *Applications are referenced using their package name.
 *You can get a list of all the packages by calling GetInstalledPackages.
 */
@ShortName("PackageManager")
public class PackageManagerWrapper {
	private PackageManager pm = BA.applicationContext.getPackageManager();
	/**
	 * Returns a list of the installed packages.
	 *Example:<code>
	 *Dim pm As PackageManager
	 *Dim packages As List
	 *packages = pm.GetInstalledPackages
	 *For i = 0 To packages.Size - 1
	 *	Log(packages.Get(i))
	 *Next</code>
	 */
	public List GetInstalledPackages() {
		List l = new List();
		java.util.List<PackageInfo> list = pm.getInstalledPackages(0);
		l.Initialize();
		for (PackageInfo pi : list) {
			l.Add(pi.packageName);
		}
		return l;
	}
	/**
	 * Returns the application version code.
	 */
	public int GetVersionCode(String Package) throws NameNotFoundException {
		return pm.getPackageInfo(Package, 0).versionCode;
	}
	/**
	 * Returns the application version name.
	 */
	public String GetVersionName(String Package) throws NameNotFoundException {
		return pm.getPackageInfo(Package, 0).versionName;
	}
	/**
	 * Returns the application label.
	 */
	public String GetApplicationLabel (String Package) throws NameNotFoundException {
		CharSequence cs = pm.getApplicationLabel(pm.getApplicationInfo(Package, 0));
		return cs == null ? "" : cs.toString();
	}
	/**
	 * Returns an Intent object that can be used to start the given application.
	 *Example:<code>
	 *Dim in As Intent
	 *Dim pm As PackageManager
	 *in = pm.GetApplicationIntent("com.google.android.youtube")
	 *If in.IsInitialized Then StartActivity(in)
	 *StartActivity(in)</code>
	 */
	public IntentWrapper GetApplicationIntent (String Package) {
		IntentWrapper iw = new IntentWrapper();
		iw.setObject(pm.getLaunchIntentForPackage(Package));
		return iw;
	}
	/**
	 * Returns the application icon.
	 *Example:<code>
	 *Dim pm As PackageManager
	 *Activity.Background = pm.GetApplicationIcon("com.google.android.youtube")</code>
	 */
	public Drawable GetApplicationIcon(String Package) throws NameNotFoundException {
		return pm.getApplicationIcon(Package);
	}
	/**
	 * Returns a list with the installed activities that can handle the given intent.
	 *Each item in the list is the "component name" of an activity. You can use Intent.SetComponent to explicitly choose the activity.
	 *The first item is considered the best match.
	 *For example, the following code lists all the activities that can "view" a text file:<code>
	 *Dim pm As PackageManager
	 *Dim Intent1 As Intent
	 *Intent1.Initialize(Intent1.ACTION_VIEW, "file://")
	 *Intent1.SetType("text/*")
	 *For Each cn As String In pm.QueryIntentActivities(Intent1)
	 *	Log(cn)
	 *Next</code>
	 */
	public List QueryIntentActivities(Intent Intent) {
		java.util.List<ResolveInfo> res = pm.queryIntentActivities(Intent, 0);
		List l = new List();
		l.Initialize();
		for (ResolveInfo ri : res) {
			l.Add(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name).flattenToShortString());
		}
		return l;
	}
	
}
