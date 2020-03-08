
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
 
 /**
 * 
 */
package anywheresoftware.b4a;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sql.CommonDataSource;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import anywheresoftware.b4a.BA.B4ARunnable;
import anywheresoftware.b4a.BA.Hide;

@Hide
public class Msgbox {
	private static boolean visible = false;
	private static WeakReference<AlertDialog> visibleAD;
	private static Object closeMyLoop = new Object();
	private static boolean stopCodeAfterDismiss = false;
	public static boolean isDismissing = false;
	public static WeakReference<ProgressDialog> pd;
	private static Method nextM;
	private static Field flagsF;
	private static Field whenF;
	private static Method recycleUnchecked;
	private final static ArrayList<WeakReference<Dialog>> listOfAsyncDialogs = new ArrayList<WeakReference<Dialog>>();
	static {
		try {
			nextM = MessageQueue.class.getDeclaredMethod("next",(Class[]) null);
			nextM.setAccessible(true);
			whenF = Message.class.getDeclaredField("when");
			whenF.setAccessible(true);
			flagsF = null;
			try {
				flagsF = Message.class.getDeclaredField("flags");
				flagsF.setAccessible(true);
				recycleUnchecked = Message.class.getDeclaredMethod("recycleUnchecked");
				recycleUnchecked.setAccessible(true);
			}
			catch (Exception e) {
				//e.printStackTrace();
				//do nothing
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static boolean msgboxIsVisible() {
		return visible;
	}
	public static boolean isItReallyAMsgboxAndNotDebug() {
		return visibleAD != null;
	}
	public static void dismiss(boolean stopCodeAfterDismiss) {
		dismissProgressDialog();
		if (BA.debugMode) {
			try {
				Class.forName("anywheresoftware.b4a.debug.Debug").getMethod("hideProgressDialogToAvoidLeak", (Class[])null)
				.invoke(null, (Object[])null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		isDismissing = true;
		if (visible) {
			if (visibleAD != null) {
				AlertDialog ad = visibleAD.get();
				if (ad != null) {
					ad.dismiss();
				}
			}
			else {
				sendCloseMyLoopMessage(); //close debug loop

			}
			Msgbox.stopCodeAfterDismiss = stopCodeAfterDismiss;
		}
		for (WeakReference<Dialog> asyncDialog : listOfAsyncDialogs) {
			Dialog d = asyncDialog.get();
			if (d != null && d.isShowing()) {
				try {
					d.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void trackAsyncDialog(Dialog d) {
		Iterator<WeakReference<Dialog>> it = listOfAsyncDialogs.iterator();
		while (it.hasNext()) {
			WeakReference<Dialog> wd = it.next();
			if (wd.get() == null) {
				it.remove();
			}
		}
		listOfAsyncDialogs.add(new WeakReference<Dialog>(d));
	}

	public static void sendCloseMyLoopMessage() {
		Message msg = Message.obtain();
		msg.setTarget(BA.handler);
		msg.obj = Msgbox.closeMyLoop;
		msg.sendToTarget();
	}
	public static void dismissProgressDialog() {
		if (pd != null) {
			ProgressDialog p = pd.get();
			if (p != null) {
				try {
					p.dismiss();
				} catch (Exception e) {
					BA.LogInfo("Error while dismissing ProgressDialog");
					e.printStackTrace();
				}
				pd = null;

			}
		}
	}
	public static class DialogResponse implements DialogInterface.OnClickListener {
		public int res = DialogInterface.BUTTON_NEUTRAL;
		private boolean dismiss;
		public DialogResponse(boolean dismissAfterClick) {
			this.dismiss = dismissAfterClick;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			res = which;
			if (dismiss) //for InputList
				visibleAD.get().dismiss();
		}
	}

	public static void msgbox(AlertDialog ad , boolean isTopMostInStack) {

		if (visible) //finally should not be run here
			return;
		try {
			if (isDismissing)
				return ;
			stopCodeAfterDismiss = false;
			Message msg = Message.obtain();
			msg.setTarget(BA.handler);
			msg.obj = closeMyLoop;
			ad.setDismissMessage(msg);
			visible = true;
			visibleAD = new WeakReference<AlertDialog>(ad);
			ad.show();
			waitForMessage(false);
			if (stopCodeAfterDismiss && !isTopMostInStack)
				throw new B4AUncaughtException();
		} 
		finally {
			visible = false;
			visibleAD = null;
		}
	}
	public static void debugWait(Dialog d) {
		if (visible) {
			System.out.println("already visible");
			return;
		}
		try {
			if (isDismissing)
				return;
			stopCodeAfterDismiss = false;
			visible = true;

			waitForMessage(true);
			if (stopCodeAfterDismiss) {
				Log.w("", "throwing b4a uncaught exception");
				throw new B4AUncaughtException();
			}
		} finally {
			visible = false;
		}
	}
	public static void waitForMessage(boolean notUsed, boolean onlyDrawableEvents) {
		waitForMessage(onlyDrawableEvents);
	}
	private static void waitForMessage(boolean onlyDrawableEvents) {
		final boolean allowB4ARunnables = false; 
		try {
			MessageQueue queue = Looper.myQueue();
			while (true) {
				Message msg = (Message) nextM.invoke(queue,(Object[]) null); // might block
				if (msg != null) {
					if (msg.obj == closeMyLoop) {
						recycle(msg);
						return;
					}
					//					BA.Log(msg.getCallback() != null ? msg.getCallback().toString() : "callback null");
					//					BA.Log("msg.what: " + String.valueOf(msg.what));

					if (!allowB4ARunnables && msg.getCallback() != null && msg.getCallback() instanceof B4ARunnable) {
						//events raised from other threads
						skipMessage(msg);
						continue;
					}

					if (onlyDrawableEvents) {
						if (msg.obj == null || !(msg.obj instanceof Drawable)) {
							if (msg.what >= 100 && msg.what <= 150) { //hack to ignore activity messages
								skipMessage(msg);
								continue;
							}
						}
					}

					msg.getTarget().dispatchMessage(msg);
					recycle(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void recycle(Message msg) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (recycleUnchecked != null)
			recycleUnchecked.invoke(msg);
		else
			msg.recycle();
	}
	private static void skipMessage(Message msg) throws IllegalArgumentException, IllegalAccessException {
		whenF.set(msg, Integer.valueOf(0));
		if (flagsF != null) { //honeycomb
			int flags = flagsF.getInt(msg);
			flagsF.setInt(msg, flags & ~1);
		}
		//Log.v("B4A", "msg value=" + whenF.getLong(msg));
		msg.getTarget().sendMessage(msg);
	}


}