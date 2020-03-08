package anywheresoftware.b4a.keywords;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;

public class B4AApplication {
	/**
	 * Returns the application name (ApplicationLabel attribute).
	 */
	public static String getLabelName() throws NameNotFoundException {
		return String.valueOf(BA.applicationContext.getPackageManager().getApplicationLabel(
				BA.applicationContext.getPackageManager().getApplicationInfo(BA.packageName, 0)));
	}
	/**
	 * Returns the application version name (VersionName attribute).
	 */
	public static String getVersionName() throws NameNotFoundException {
		return BA.applicationContext.getPackageManager().getPackageInfo(BA.packageName, 0).versionName;
	}
	/**
	 * Returns the application version code (VersionCode attribute).
	 */
	public static int getVersionCode() throws NameNotFoundException {
		return BA.applicationContext.getPackageManager().getPackageInfo(BA.packageName, 0).versionCode;
	}
	/**
	 * Returns the application package name.
	 */
	public static String getPackageName() {
		return BA.packageName;
	}
	private static BitmapWrapper loadedIcon;
	/**
	 * Returns the application icon.
	 */
	public static BitmapWrapper getIcon() throws NameNotFoundException {
		if (loadedIcon != null)
			return loadedIcon;
		loadedIcon = new BitmapWrapper();
		Drawable d = BA.applicationContext.getPackageManager().getApplicationIcon(BA.packageName);
		if (d instanceof BitmapDrawable) {
			loadedIcon.setObject(((BitmapDrawable)d).getBitmap());
		} else {
			loadedIcon.InitializeMutable(Common.DipToCurrent(108), Common.DipToCurrent(108));
			CanvasWrapper cw = new CanvasWrapper();
			cw.Initialize2(loadedIcon.getObject());
			cw.DrawDrawable(d, new Rect(0, 0, loadedIcon.getWidth(), loadedIcon.getHeight()));
			
		}
		return loadedIcon;
	}
}
