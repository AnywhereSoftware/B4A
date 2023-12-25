package anywheresoftware.b4a.objects;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Display;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.B4AExceptionHandler;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;

/**
 * Each Service module includes a Service object.
 *This object is used to bring the service in and out of the foreground state.
 *See the Services tutorial for more information.
 */
@ActivityObject //activity object as this is referenced with an instance variable.
public class ServiceHelper {
	@Hide
	public static final String FOREGROUND_KEY = "b4a_foreground", AUTO_WAKE_ID = "b4a_wakelock";
	@Hide
	public static void init() {
	}
	private Service service;
	NotificationManager mNM;

	/**
	 * Never enter automatic foreground mode. This means that you must handle it yourself to avoid the app from crashing.
	 */
	public static final int AUTOMATIC_FOREGROUND_NEVER = 1;
	/**
	 * Automatic foreground mode will be set when needed. This will only happen on Android 8+ devices when the service is started while the app is in the background.
	 */
	public static final int AUTOMATIC_FOREGROUND_WHEN_NEEDED = 2;
	/**
	 * Always enter foreground mode when the service is started. This is useful when you want to make sure that the OS doesn't kill the process until the task completes.
	 */
	public static final int AUTOMATIC_FOREGROUND_ALWAYS = 3;
	/**
	 * Sets the automatic foreground mode. It should be one of the AUTOMATIC_FOREGROUND constants.
	 *Default value is AUTOMATIC_FOREGROUND_WHEN_NEEDED.
	 *Should be set in Service_Create.
	 */
	public int AutomaticForegroundMode = AUTOMATIC_FOREGROUND_WHEN_NEEDED;
	@Hide
	public int autoNotificationId;
	/**
	 * The notification that will show when entering automatic foreground state.
	 *A default notification will show if not set.
	 */
	public Notification AutomaticForegroundNotification;

	public ServiceHelper (Service service) {
		this.service = service;
		mNM = (NotificationManager)BA.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	/**
	 * Brings the current service to the foreground state and displays the given notification.
	 *Id - The notification Id (see the notification object documentation).
	 *Notification - The notification that will be displayed.
	 */
	public void StartForeground(int Id, Notification Notification) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		service.startForeground(Id, Notification);
	}
	/**
	 * Takes the current service out of the foreground state and cancels the notification with the given Id.
	 */
	public void StopForeground(int Id) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		service.stopForeground(true);
	}
	/**
	 * Stops the automatic foreground state. Does nothing if the service was not in that state.
	 *You can call this method at the end of the background task as an alternative to calling StopService(Me).
	 */
	public void StopAutomaticForeground() {
		if (autoNotificationId > 0) {
			service.stopForeground(true);
			autoNotificationId = 0;
		}
	}
	@Hide
	public static class StarterHelper {
		private static boolean alreadyRun;
		private static Runnable waitForLayouts;
		private static BA serviceProcessBA;
		private static final HashMap<Integer, PowerManager.WakeLock> wakeLocks = new HashMap<Integer, PowerManager.WakeLock>();
		private static int wakeLockId;
		public static void startServiceFromReceiver(Context context, Intent intent, boolean starterService, Class<?> baClass) {
			if (starterService)
				BA.LogError("The Starter service should never be started from a receiver.");
			if (baClass.getName().equals("anywheresoftware.b4a.ShellBA") && BA.applicationContext == null) {
				BA.LogError("Cannot start from a receiver in debug mode.");
				return;
			}
			boolean foreground = BA.isAnyActivityVisible();
			if (!foreground && context.getPackageManager().checkPermission(permission.WAKE_LOCK,
					BA.packageName) == PackageManager.PERMISSION_GRANTED) {
				wakeLockId++;
				PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, String.valueOf(intent.getComponent()));
				wl.setReferenceCounted(false);
				wl.acquire(60*1000);
				wakeLocks.put(wakeLockId, wl);
				intent.putExtra(AUTO_WAKE_ID, wakeLockId);
			}
			if (Build.VERSION.SDK_INT < 26 || foreground) {
				try {
				context.startService(intent);
				} catch (IllegalStateException i) {
					if (Build.VERSION.SDK_INT >= 26) {
						intent.putExtra(FOREGROUND_KEY, true);
						context.startForegroundService(intent);
					} else {
						throw new RuntimeException(i);
					}
				}
			} else {
				intent.putExtra(FOREGROUND_KEY, true);
				context.startForegroundService(intent);
			}


		}
		public static IntentWrapper handleStartIntent(Intent intent, ServiceHelper sh, BA ba) {
			anywheresoftware.b4a.objects.IntentWrapper iw = new anywheresoftware.b4a.objects.IntentWrapper();
			boolean startForegroundServiceCalled = false;
			if (intent != null) {
				if (intent.getBooleanExtra(FOREGROUND_KEY, false) == true) 
					startForegroundServiceCalled = true;
				int wakeLockId = intent.getIntExtra(AUTO_WAKE_ID, 0);
				if (wakeLockId > 0) {
					WakeLock wl = wakeLocks.remove(wakeLockId);
					wl.release();
				}
				if (intent.hasExtra("b4a_internal_intent"))
					iw.setObject((android.content.Intent) intent.getParcelableExtra("b4a_internal_intent"));
				else
					iw.setObject(intent);
			}
			if (startForegroundServiceCalled)
				BA.LogInfo("Service started in foreground mode.");
			if (sh.AutomaticForegroundMode != sh.AUTOMATIC_FOREGROUND_NEVER && 
					(sh.AutomaticForegroundMode == sh.AUTOMATIC_FOREGROUND_ALWAYS || 
					(sh.AutomaticForegroundMode == sh.AUTOMATIC_FOREGROUND_WHEN_NEEDED && startForegroundServiceCalled))) {
				if (sh.AutomaticForegroundNotification == null)
					sh.AutomaticForegroundNotification = createAutoNotification(sh, ba);
				sh.autoNotificationId = 51042;
				try {
					sh.StartForeground(sh.autoNotificationId, sh.AutomaticForegroundNotification);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			return iw;
		}
		private static Notification createAutoNotification(ServiceHelper sh, BA ba) {
			NotificationWrapper nw = new NotificationWrapper();
			nw.Initialize2(NotificationWrapper.IMPORTANCE_LOW);
			nw.setIcon("icon");
			try {
				nw.SetInfoNew(ba, Common.Application.getLabelName(), Common.Application.getLabelName(), Class.forName(BA.packageName + ".main"));
				return (Notification) nw.getObject();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		/**
		 * Returns true if already run
		 */
		public static boolean startFromActivity(Activity act, BA ba, Runnable waitForLayout, boolean noStarter) {
			if (alreadyRun || noStarter)
				return true;
			alreadyRun = true;
			addWaitForLayout(waitForLayout);
			try {
				Common.StartService(ba, "starter");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			return false;
		}
	
		public static boolean startFromServiceCreate(BA ba, boolean noStarter) {
			if (alreadyRun || noStarter)
				return true;
			alreadyRun = true;
			serviceProcessBA = ba;
			return false;
		}
		public static boolean runWaitForLayouts() {
			if (waitForLayouts != null) {
				BA.handler.post(waitForLayouts);
				return true;
			}
			return false; //this will happen after a spontaneous service create.
		}
		public static void addWaitForLayout(Runnable r) {
			waitForLayouts = r;
		}
		public static void removeWaitForLayout() {
			waitForLayouts = null;
		}
		public static boolean onStartCommand(BA ba, Runnable handleStart) {
			if (ba != null && ba == serviceProcessBA) {
				try {
					Common.StartService(ba, "starter");
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
				serviceProcessBA = null;
				return false;
			} else {
				if (ba.isActivityPaused() && waitForLayouts != null) {
					BA.handler.postDelayed(handleStart, 500);
				}
				else
					handleStart.run();
				return true;
			}
		}
		private static boolean insideHandler;
		public static boolean handleUncaughtException(Throwable t, BA ba) throws Exception {
			if (insideHandler) {
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
				return true;
			}
			try {
				insideHandler = true;
				if (alreadyRun) {
					if (Common.SubExists(ba, "starter", "application_error") == false)
						return false;
					if (Common.IsPaused(ba, "starter")) {
						Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
						return true;
					}
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(out);
					t.printStackTrace(pw);
					pw.close();
					byte[] b = out.toByteArray();
					B4AException exc = new B4AException();
					if (t instanceof Exception)
						exc.setObject((Exception)t);
					else
						exc.setObject(new Exception(t));

					Boolean res = (Boolean) Common.CallSubNew3(ba, "starter", "application_error", exc, Common.BytesToString(b, 0, b.length, "UTF8"));
					if (Boolean.TRUE.equals(res)) {
						UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
						if (handler instanceof B4AExceptionHandler) {
							((B4AExceptionHandler)handler).original.uncaughtException(Thread.currentThread(), t);
						} else
							handler.uncaughtException(Thread.currentThread(), t);
					}
					return true;
				}
				return false;
			} finally {
				insideHandler = false;
			}
		}
		public static void callOSExceptionHandler(B4AException e) {
			UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
			if (handler instanceof B4AExceptionHandler) {
				((B4AExceptionHandler)handler).original.uncaughtException(Thread.currentThread(), e.getObject());
			} else
				handler.uncaughtException(Thread.currentThread(), e.getObject());
		}
	}
}
