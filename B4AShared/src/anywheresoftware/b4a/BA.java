
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
 
 package anywheresoftware.b4a;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Process;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.Log;
import anywheresoftware.b4a.B4AClass.ImplB4AClass;

public class BA {
	//Static fields
	public static Application applicationContext;
	public static NumberFormat numberFormat, numberFormat2;
	public static boolean debugMode = false;
	/**
	 * Don't use without thinking about compiled libraries.
	 */
	public static boolean shellMode = false;
	public static String packageName;
	public static float density = 1f;
	private volatile static B4AThreadPool threadPool;
	public static String debugLine;
	public static int debugLineNum;
	public final static Handler handler = new Handler();
	public final static Locale cul = Locale.US;
	private static HashMap<String, ArrayList<Runnable>> uninitializedActivitiesMessagesDuringPaused;
	public static IBridgeLog bridgeLog;
	public static WarningEngine warningEngine;
	public static final ThreadLocal<Object> senderHolder = new ThreadLocal<Object>();
	//instance fields
	public final Object eventsTarget;
	public final HashMap<String, Method> htSubs;
	public HashMap<String, LinkedList<WaitForEvent>> waitForEvents;
	public final SharedProcessBA sharedProcessBA;
	public final Context context;
	public final Activity activity;
	public Service service;
	public final BALayout vg;
	public final String className;
	public final BA processBA; //from activity to process

	public static class SharedProcessBA {
		public WeakReference<BA> activityBA; //from process to activity
		public final boolean isService;
		int numberOfStackedEvents = 0;
		Exception lastException = null;
		boolean ignoreEventsFromOtherThreadsDuringMsgboxError = false;
		ArrayList<Runnable> messagesDuringPaused; 
		volatile boolean isActivityPaused = true; 
		HashMap<Integer, WeakReference<IOnActivityResult>> onActivityResultMap;
		int onActivityResultCode = 1;
		public Object sender; //not used
		public SharedProcessBA(boolean isService) {
			this.isService = isService;
		}
	}
	static {
		Thread.setDefaultUncaughtExceptionHandler(new B4AExceptionHandler());

	}
	public BA(BA otherBA, Object eventTarget, HashMap<String, Method> subs, String className) {
		this.vg = otherBA.vg;
		this.eventsTarget = eventTarget;
		this.htSubs = subs == null ? new HashMap<String, Method>() : subs;
		this.processBA = null;
		this.activity = otherBA.activity;
		this.context = otherBA.context;
		this.service = otherBA.service;
		this.sharedProcessBA = otherBA.sharedProcessBA == null ? otherBA.processBA.sharedProcessBA : otherBA.sharedProcessBA;
		this.className = className;
	}

	public BA (Context context, BALayout vg, BA processBA, String notUsed, String className) {

		Activity activity;
		boolean isService;
		if (context != null) {
			density = context.getResources().getDisplayMetrics().density;
			try {
				Class.forName("anywheresoftware.b4a.keywords.Common").getField("Density").set(null, density);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (context != null && context instanceof Activity){
			activity = (Activity) context;
			applicationContext = activity.getApplication();
		}
		else
			activity = null;
		if (context != null && context instanceof Service)  {
			isService = true;
			applicationContext = ((Service)context).getApplication();
		}
		else 
			isService = false;
		if (context != null && packageName == null) {
			this.packageName = context.getPackageName();
			try {
				Class<?> c = Class.forName("anywheresoftware.b4a.remotelogger.RemoteLogger");
				c.getMethod("Start").invoke(c.newInstance());
			} catch (ClassNotFoundException cnfe) {
				System.out.println("Bridge logger not enabled.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.eventsTarget = null;
		if (className.endsWith(".starter")) {
			this.context = applicationContext;
		}
		else {
			this.context = context;
		}
		this.activity = activity;
		htSubs = new HashMap<String, Method>();
		this.className = className;
		this.processBA = processBA;
		this.vg = vg;
		if (processBA == null)
			sharedProcessBA = new SharedProcessBA(isService);
		else
			sharedProcessBA = null;
	}

	public boolean subExists(String sub) {
		if (processBA != null)
			return processBA.subExists(sub);
		else
			return htSubs.containsKey(sub);
	}
	public boolean runHook(String hook, Object target, Object[] args) {
		if (subExists(hook)) {
			try {
				Boolean b = (Boolean) htSubs.get(hook).invoke(target, args);
				return b != null && b.booleanValue();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return false;
	}

	public Object raiseEvent(Object sender, String event, Object... params) {
		return raiseEvent2(sender, false, event, false, params);
	}
	public Object raiseEvent2(Object sender, boolean allowDuringPause, String event, boolean throwErrorIfMissingSub, Object... params) {
		if (processBA != null) {
			return processBA.raiseEvent2(sender, allowDuringPause, event, throwErrorIfMissingSub, params);
		}
		if (sharedProcessBA.isActivityPaused && !allowDuringPause) {
			System.out.println("ignoring event: " + event);
			return null;
		}
		try {
			sharedProcessBA.numberOfStackedEvents++;
			senderHolder.set(sender);
			if (waitForEvents != null) {
				if (checkAndRunWaitForEvent(sender, event, params))
					return null;
			}
			Method m = htSubs.get(event);
			if (m != null) {
				try {
					return m.invoke(eventsTarget, params);
				} catch (IllegalArgumentException e) {
					throw new Exception("Sub " + event + " signature does not match expected signature.");
				}
			}
			else if (throwErrorIfMissingSub) {
				throw new Exception("Sub " + event + " was not found.");
			}
		} catch (B4AUncaughtException e) {
			throw e;
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException)
				e = e.getCause();
			if (e instanceof B4AUncaughtException) {
				if (sharedProcessBA.numberOfStackedEvents > 1)
					throw (B4AUncaughtException)e;
				else {
					System.out.println("catching B4AUncaughtException");
					return null;
				}
			}

			String sub = printException(e, !debugMode); //already printed in debug
			if (!debugMode) {
				try {
					Boolean b = (Boolean) Class.forName("anywheresoftware.b4a.objects.ServiceHelper$StarterHelper")
							.getDeclaredMethod("handleUncaughtException", Throwable.class, BA.class).invoke(null, e, this);
					if (Boolean.TRUE.equals(b))
						return null;
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
			if (e instanceof Error) {
				throw (Error)e;
			}
			if (sharedProcessBA.activityBA == null)
				throw new RuntimeException(e);
			ShowErrorMsgbox(e.toString(), sub);
		}
		finally {
			sharedProcessBA.numberOfStackedEvents--;
			senderHolder.set(null);
		}
		return null;
	}
	public boolean checkAndRunWaitForEvent(Object sender, String event, Object[] params) throws Exception {
		LinkedList<WaitForEvent> events = waitForEvents.get(event);
		if (events != null) {
			Iterator<WaitForEvent> it = events.iterator();
			while (it.hasNext()) {
				WaitForEvent wfe = it.next();
				if (wfe.senderFilter == null || (sender != null && sender == wfe.senderFilter.get())) {
					it.remove();
					wfe.rs.resume(this, params);
					senderHolder.set(null);
					return true;
				}
			}
		}
		return false;
	}
	public void ShowErrorMsgbox(String errorMessage, String sub) {
		sharedProcessBA.ignoreEventsFromOtherThreadsDuringMsgboxError = true;
		try {
			LogError(errorMessage);
			AlertDialog.Builder builder = new AlertDialog.Builder(sharedProcessBA.activityBA.get().context);
			builder.setTitle("Error occurred");
			String msg = sub != null ? "An error has occurred in sub:" + sub + "\n" : "";
			msg = msg + errorMessage + "\nContinue?";
			builder.setMessage(msg);
			Msgbox.DialogResponse dr = new Msgbox.DialogResponse(false);
			builder.setPositiveButton("Yes", dr);
			builder.setNegativeButton("No", dr);
			Msgbox.msgbox(builder.create(), sharedProcessBA.numberOfStackedEvents == 1);
			if (dr.res == DialogInterface.BUTTON_NEGATIVE) {
				Process.killProcess(Process.myPid());
				System.exit(0);
			} 
		} 
		finally {
			sharedProcessBA.ignoreEventsFromOtherThreadsDuringMsgboxError = false;
		}
	}
	public static String printException(Throwable e, boolean print) {
		String sub = "";
		if (!BA.shellMode) {
			StackTraceElement[] stes = e.getStackTrace();

			for (StackTraceElement ste : stes) {
				if (ste.getClassName().startsWith(packageName)) {
					sub = ste.getClassName().substring(packageName.length() + 1) 
							+ ste.getMethodName();

					if (debugLine != null)
						sub += " (B4A line: " + debugLineNum + ")\n" + debugLine;
					else
						sub += " (java line: " + ste.getLineNumber() + ")";
					break;
				}

			}
		}
		if (print) {
			if (sub.length() > 0)
				LogError(sub);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(out);
			e.printStackTrace(pw);
			pw.close();
			byte[] b = out.toByteArray();
			try {
				LogError(new String(b, "UTF8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		return sub;
	}
	public void raiseEventFromUI(final Object sender, final String event, final Object... params) {
		if (processBA != null) {
			processBA.raiseEventFromUI(sender, event, params);
			return;
		}
		Runnable runnable = new B4ARunnable() {
			@Override
			public void run() {
				if (sharedProcessBA.ignoreEventsFromOtherThreadsDuringMsgboxError) {
					LogInfo("Event: " + event + ", was ignored.");
					return;
				}
				if (!sharedProcessBA.isService && sharedProcessBA.activityBA == null) {
					LogInfo("Reposting event: " + event);
					handler.post(this); //don't raise event during the activity creation.
				}
				else if (sharedProcessBA.isActivityPaused) {
					LogInfo("Ignoring event: " + event);
				}
				else {
					raiseEvent2(sender, false, event, false, params);
				}
			}
		};
		handler.post(runnable);
	}
	public Object raiseEventFromDifferentThread(final Object sender,
			final Object container, final int TaskId, 
			final String event,
			final boolean throwErrorIfMissingSub, final Object[] params) {
		if (processBA != null)
			return processBA.raiseEventFromDifferentThread(sender, container, TaskId,
					event, throwErrorIfMissingSub, params);
		Runnable runnable = new B4ARunnable() {
			@Override
			public void run() {
				if (sharedProcessBA.ignoreEventsFromOtherThreadsDuringMsgboxError) {
					Log("Event: " + event + ", was ignored.");
					return;
				}
				if (!sharedProcessBA.isService && sharedProcessBA.activityBA == null) {
					Log("Reposting event: " + event);
					handler.post(this); //don't raise event during the activity creation.
				}
				else if (sharedProcessBA.isActivityPaused) {
					if (sharedProcessBA.isService)  {
						Log("Ignoring event as service was destroyed: " + event);
					}
					else {
						addMessageToPausedMessageQueue(event, this);
					}
				}
				else {
					if (container != null)
						markTaskAsFinish(container, TaskId);
					raiseEvent2(sender, false, event, throwErrorIfMissingSub, params);
				}
			}
		};
		handler.post(runnable);
		return null;
	}
	public static void addMessageToUninitializeActivity(String className, String eventName, Object sender, Object[] arguments) {
		if (uninitializedActivitiesMessagesDuringPaused == null)
			uninitializedActivitiesMessagesDuringPaused = new HashMap<String, ArrayList<Runnable>>();
		ArrayList<Runnable> list = uninitializedActivitiesMessagesDuringPaused.get(className);
		if (list == null) {
			list = new ArrayList<Runnable>();
			uninitializedActivitiesMessagesDuringPaused.put(className, list);
		}
		if (list.size() < 30) {
			RaiseEventWhenFirstCreate r = new RaiseEventWhenFirstCreate();
			r.eventName = eventName;
			r.arguments = arguments;
			r.sender = sender;
			Log("sending message to waiting queue of uninitialized activity (" + eventName + ")");
			list.add(r);
		}
	}
	private static class RaiseEventWhenFirstCreate implements Runnable {
		BA ba;
		String eventName;
		Object[] arguments;
		Object sender;
		@Override
		public void run() {
			ba.raiseEvent2(sender, true, eventName, true, arguments);
		}

	}
	public void addMessageToPausedMessageQueue(String event, Runnable msg) {
		if (processBA != null) {
			processBA.addMessageToPausedMessageQueue(event, msg);
			return;
		}
		Log( "sending message to waiting queue (" + event + ")");
		if (sharedProcessBA.messagesDuringPaused == null)
			sharedProcessBA.messagesDuringPaused = new ArrayList<Runnable>();
		if (sharedProcessBA.messagesDuringPaused.size() > 20) {
			Log("Ignoring event (too many queued events: " + event + ")");
		}
		else {
			sharedProcessBA.messagesDuringPaused.add(msg);
		}
	}
	public void setActivityPaused(boolean value) {
		if (processBA != null) {
			processBA.setActivityPaused(value);
			return;
		}
		sharedProcessBA.isActivityPaused = value;
		if (value == false) { //run waiting messages (only in activities).
			if (sharedProcessBA.isService)
				return;
			if (sharedProcessBA.messagesDuringPaused == null && uninitializedActivitiesMessagesDuringPaused != null) {
				String cls = className;
				//check whether there is a queue in the uninitialized activities map
				sharedProcessBA.messagesDuringPaused = uninitializedActivitiesMessagesDuringPaused.get(cls);
				uninitializedActivitiesMessagesDuringPaused.remove(cls);
			}
			if (sharedProcessBA.messagesDuringPaused != null && sharedProcessBA.messagesDuringPaused.size() > 0) {
				try {
					Log("running waiting messages (" + sharedProcessBA.messagesDuringPaused.size() + ")");
					for (Runnable msg : sharedProcessBA.messagesDuringPaused) {
						if (msg instanceof RaiseEventWhenFirstCreate) {
							((RaiseEventWhenFirstCreate)msg).ba = this;
						}
						msg.run();
					}
				} finally {
					sharedProcessBA.messagesDuringPaused.clear();
				}
			}
		}
	}
	public String getClassNameWithoutPackage() {
		return className.substring(className.lastIndexOf(".") + 1);
	}
	public static void runAsync(final BA ba, final Object Sender, String FullEventName, 
			final Object[] errorResult, final Callable<Object[]> callable) {
		final String eventName = FullEventName.toLowerCase(BA.cul);
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				try {
					Object[] ret = callable.call();
					Object send = Sender;
					if (Sender instanceof ObjectWrapper)
						send = ((ObjectWrapper<?>)Sender).getObjectOrNull();
					ba.raiseEventFromDifferentThread(send, null, 0, eventName,
							false, ret);
				} catch (Exception e) {
					e.printStackTrace();
					ba.setLastException(e);
					Object send = Sender;
					if (Sender instanceof ObjectWrapper)
						send = ((ObjectWrapper<?>)Sender).getObjectOrNull();
					ba.raiseEventFromDifferentThread(send, null, 0, eventName,
							false, errorResult);
				}
			}
		}, null, 0);
	}
	private static void markTaskAsFinish(Object container, int TaskId) {
		if (threadPool == null)
			return;
		threadPool.markTaskAsFinished(container, TaskId);
	}
	public static Future<?> submitRunnable(Runnable runnable, Object container, int TaskId) {
		if (threadPool == null) {
			synchronized (BA.class) {
				if (threadPool == null) {
					threadPool = new B4AThreadPool();
				}
			}
		}
		if (container instanceof ObjectWrapper)
			container = ((ObjectWrapper<?>)container).getObject();
		threadPool.submit(runnable, container, TaskId);
		return null;
	}

	public static boolean isTaskRunning(Object container, int TaskId) {
		if (threadPool == null)
			return false;

		return threadPool.isRunning(container, TaskId);
	}
	public void loadHtSubs(Class<?> cls) {
		for (Method m : cls.getDeclaredMethods()) {
			if (m.getName().startsWith("_")) {
				htSubs.put(m.getName().substring(1).toLowerCase(cul), m);
			}
		}
	}
	public boolean isActivityPaused() {
		if (processBA != null)
			return processBA.isActivityPaused();
		return sharedProcessBA.isActivityPaused;
	}
	
	public static boolean isAnyActivityVisible() {
		try {
			if (BA.packageName == null)
				return false;
			return ((Boolean)Class.forName(BA.packageName + ".main").getMethod("isAnyActivityVisible",(Class<?>[]) null)
					.invoke(null,(Object[]) null) == true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void startActivityForResult(IOnActivityResult iOnActivityResult, Intent intent) {
		if (processBA != null) {
			processBA.startActivityForResult(iOnActivityResult, intent);
			return;
		}
		if (sharedProcessBA.activityBA == null)
			return; //not expected.
		BA aBa = sharedProcessBA.activityBA.get();
		if (aBa == null)
			return; //not expected
		if (sharedProcessBA.onActivityResultMap == null)
			sharedProcessBA.onActivityResultMap = new HashMap<Integer, WeakReference<IOnActivityResult>>();
		sharedProcessBA.onActivityResultMap.put(sharedProcessBA.onActivityResultCode, new WeakReference<IOnActivityResult>(iOnActivityResult));
		try {
			aBa.activity.startActivityForResult(intent, sharedProcessBA.onActivityResultCode++);
		} catch (ActivityNotFoundException e) {
			sharedProcessBA.onActivityResultMap.remove(sharedProcessBA.onActivityResultCode - 1);
			iOnActivityResult.ResultArrived(Activity.RESULT_CANCELED, null);
		}

	}
	//processBA
	public void onActivityResult(int request, final int result, final Intent intent) {
		if (sharedProcessBA.onActivityResultMap != null) {
			WeakReference<IOnActivityResult> wi = sharedProcessBA.onActivityResultMap.get(request);
			if (wi == null) {
				Log( "onActivityResult: wi is null");
				return;
			}
			sharedProcessBA.onActivityResultMap.remove(request);
			final IOnActivityResult i = wi.get();
			if (i == null) {
				Log("onActivityResult: IOnActivityResult was released");
				return;
			}
			addMessageToPausedMessageQueue("OnActivityResult", new Runnable() {

				@Override
				public void run() {
					try {
						i.ResultArrived(result, intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	public static void Log(String Message) {
		if (Message == null)
			Message = "null";
		Log.i("B4A", Message);
		if (Message.length() > 4000)
			BA.LogInfo("Message longer than Log limit (4000). Message was truncated.");
		if (bridgeLog != null)
			bridgeLog.offer(Message);
	}
	public static void addLogPrefix(String prefix, String message) {
		prefix = "~" + prefix + ":";
		if (message == null) {
			message = "(null string)";
		}
		if (message.length() < 3900) {
			StringBuilder sb = new StringBuilder();
			for (String line : message.split("\\n")) {
				if (line.length() > 0) {
					sb.append(prefix).append(line);
				} 
				sb.append("\n");
			}
			message = sb.toString();
		}
		Log(message);
	}

	public static void LogError(String Message) {
		addLogPrefix("e", Message);
	}
	public static void LogInfo(String Message) {
		addLogPrefix("i", Message);
	}
	public static boolean parseBoolean(String b) {
		if (b.equals("true"))
			return true;
		else if (b.equals("false"))
			return false;
		else
			throw new RuntimeException("Cannot parse: " + b + " as boolean");
	}
	public static char CharFromString(String s) {
		if (s == null || s.length() == 0)
			return '\0';
		else
			return s.charAt(0);
	}
	public Object getSender() {
		return senderHolder.get();
	}
	public Exception getLastException() {
		if (processBA != null)
			return processBA.getLastException();
		else
			return sharedProcessBA.lastException;
	}
	/**
	 * Should be called with processBA.
	 */
	public void setLastException(Exception e) {
		while (e != null && e.getCause() != null && e instanceof Exception)
			e = (Exception) e.getCause();
		sharedProcessBA.lastException = e;
	}
	//used by generated calls when method parameter is enum.
	public static <T extends Enum<T>> T getEnumFromString(Class<T> enumType, String name) {
		return Enum.valueOf(enumType, name);
	}
	public static interface B4ARunnable extends Runnable {
		//this way we can treat internal events different than other messages.
	}
	public static String NumberToString(double value) {
		String s = Double.toString(value);
		if (s.length() > 2 && s.charAt(s.length() - 2) == '.' && s.charAt(s.length() - 1) == '0')
			return s.substring(0, s.length() - 2);
		return s;
	}
	public static String NumberToString(float value) {
		return NumberToString((double)value);
	}
	public static String NumberToString(int value) {
		return String.valueOf(value);
	}
	public static String NumberToString(long value) {
		return String.valueOf(value);
	}
	public static String NumberToString(Number value) {
		return String.valueOf(value);
	}
	public static double ObjectToNumber(Object o) {
		if (o instanceof Number) {
			return ((Number)o).doubleValue();
		}
		else {
			return Double.parseDouble(String.valueOf(o));
		}
	}
	public static long ObjectToLongNumber(Object o) {
		if (o instanceof Number) {
			return ((Number)o).longValue();
		}
		else {
			return Long.parseLong(String.valueOf(o));
		}
	}
	public static boolean ObjectToBoolean(Object o) {
		if (o instanceof Boolean)
			return ((Boolean)o).booleanValue();
		else
			return parseBoolean(String.valueOf(o));
	}
	public static char ObjectToChar(Object o) {
		if (o instanceof Character)
			return ((Character)o).charValue();
		else
			return CharFromString(o.toString());
	}

	private static int checkStackTraceEvery50;
	public static String TypeToString(Object o, boolean clazz)  {
		try {
			if (++checkStackTraceEvery50 % 50 == 0 || checkStackTraceEvery50 < 0) {
				if (Thread.currentThread().getStackTrace().length >= (checkStackTraceEvery50 < 0 ? 20 : 150)) {
					checkStackTraceEvery50 = -100; //continue checking...
					return "";					
				}
				else {
					checkStackTraceEvery50 = 0;
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			int i = 0;
			for (Field f : o.getClass().getDeclaredFields()) {
				String fname = f.getName();
				if (clazz) {
					if (fname.startsWith("_") == false)
						continue;
					fname = fname.substring(1);
					if (fname.startsWith("_")) //_c
						continue;
				}
				f.setAccessible(true);
				sb.append(fname).append("=")
				.append(String.valueOf(f.get(o)));
				if (++i % 3 == 0)
					sb.append("\n");
				sb.append(", ");
			}
			if (sb.length() >= 2)
				sb.setLength(sb.length() - 2);
			sb.append("]");
			return sb.toString();
		} catch (Exception e) {
			if (o != null)
				return o.getClass() + ": " + System.identityHashCode(o);
			return "N/A";
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> T gm(Map map, Object key, T defValue) {
		T o = (T)map.get(key);
		if (o == null)
			return defValue;
		return o;
	}
	public static String returnString(String s) {
		return s == null ? "" : s;
	}
	public static String ObjectToString(Object o) {
		return String.valueOf(o);
	}
	public static CharSequence ObjectToCharSequence(Object Text) {
		if (Text instanceof CharSequence)
			return (CharSequence) Text;
		else
			return String.valueOf(Text);
	}
	public static int switchObjectToInt(Object test, Object... values) {
		int res = -1;
		if (test instanceof Number){
			double t = ((Number)test).doubleValue();
			for (int i = 0;i < values.length;i++) {
				if (t == ((Number)values[i]).doubleValue()) {
					res = i;
					break;
				}
			}
		}
		else {
			for (int i = 0;i < values.length;i++) {
				if (test.equals(values[i])) {
					res = i;
					break;
				}
			}
		}
		return res;
	}
	public static boolean fastSubCompare(String s1, String s2) {
		if (s1 == s2)
			return true;
		if (s1.length() != s2.length())
			return false;
		for (int i = 0;i < s1.length();i++) {
			if ((((int)s1.charAt(i)) & 0xDF) != (((int)s2.charAt(i)) & 0xDF))
				return false;
		}
		return true;
	}
	
	public static boolean isShellModeRuntimeCheck(BA ba) {
		if (ba.processBA != null)
			return isShellModeRuntimeCheck(ba.processBA);
		return ba.getClass().getName().endsWith("ShellBA");
	}
	public interface IBridgeLog {
		void offer(String msg);
	}
	public static class B4AExceptionHandler implements UncaughtExceptionHandler {
		public final Thread.UncaughtExceptionHandler original;
		public B4AExceptionHandler() {
			original = Thread.getDefaultUncaughtExceptionHandler();
		}
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			printException(e, true);
			if (bridgeLog != null) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException ie) {
				}
			}
			original.uncaughtException(t, e);
		}

	}
	public static abstract class WarningEngine {
		public static final int ZERO_SIZE_PANEL = 1001;
		public static final int SAME_OBJECT_ADDED_TO_LIST = 1002;
		public static final int OBJECT_ALREADY_INITIALIZED = 1003;
		public static final int FULLSCREEN_MISMATCH = 1004;
		public static void warn(int warning) {
			if (warningEngine != null)
				warningEngine.warnImpl(warning);
		}
		public abstract void checkFullScreenInLayout(boolean fullscreen, boolean includeTitle);

		protected abstract void warnImpl(int warning);
	}
	public interface IterableList {
		int getSize();
		Object Get(int index);
	}
	public interface B4aDebuggable {
		Object[] debug(int limit, boolean[] outShouldAddReflectionFields);
	}
	public interface CheckForReinitialize {
		boolean IsInitialized();
	}
	public interface SubDelegator {
		public static final Object SubNotFound = new Object();
		Object callSub(String sub, Object Sender, Object[] args) throws Exception;

	}
	public static abstract class ResumableSub {
		public int state;
		public int catchState;
		public BA waitForBA;
		public boolean completed;
		public abstract void resume(BA ba, Object[] result) throws Exception;
		//		protected void finalize() throws Throwable {
		//			System.out.println("finalized: " + this.getClass());
		//		}
	}
	public static class WaitForEvent {
		public ResumableSub rs;
		public WeakReference<Object> senderFilter;
		public WaitForEvent(ResumableSub rs, Object senderFilter) {
			this.rs = rs;
			if (senderFilter == null)
				this.senderFilter = null;
			else
				this.senderFilter = new WeakReference<Object>(senderFilter);
		}
		public boolean noFilter() {
			return senderFilter == null;
		}
		public boolean cleared() {
			return senderFilter != null && senderFilter.get() == null;
		}
		//		public String toString() {
		//			return "WaitForEvent: " + (senderFilter == null ? "no filter" : senderFilter.get());
		//		}
	}
	public static @interface Hide {}
	public static @interface Pixel {}
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ShortName {
		String value();
	}
	/**
	 * Used by String2. Should not be used normally.
	 */
	public static @interface DesignerName {
		String value();
	}
	/**
	 * Should only be applied to classes or to Object types.
	 */
	public static @interface ActivityObject {}
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	public static @interface Events {
		String[] values();
	}
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	public static @interface DependsOn {
		String[] values();
	}
	@Target(ElementType.METHOD)
	public static @interface RaisesSynchronousEvents {}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	public static @interface DontInheritEvents {
	}
	@Retention(RetentionPolicy.SOURCE)
	public static @interface Permissions {
		String[] values();
	}
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Version {
		float value();
	}
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Author {
		String value();
	}
	@Hide
	@Retention(RetentionPolicy.SOURCE)
	public static @interface Property {
		String key();
		String displayName();
		String description() default "";
		String defaultValue();
		String fieldType();
		String minRange() default "";
		String maxRange() default "";
		String list() default "";
	}
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	@Hide
	public static @interface DesignerProperties {
		Property[] values();
	}
	
	@Hide
	@Retention(RetentionPolicy.SOURCE)
	public static @interface CustomClass {
		String name();
		String fileNameWithoutExtension();
		int priority() default 0;
	}
	@Hide
	@Retention(RetentionPolicy.SOURCE)
	public static @interface CustomClasses {
		CustomClass[] values();
	}

}
