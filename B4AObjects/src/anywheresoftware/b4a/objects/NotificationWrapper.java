package anywheresoftware.b4a.objects;

import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DesignerName;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;

/**
 * A status bar notification. The user can open the notifications screen and press on the notification.
 *Pressing on the notification will start an activity as set by the notification object.
 *Notifications are usually used by services as services are not expected to directly start activities.
 *The notification must have an icon and its "info" must be set.
 *Example:<code>
 *Dim n As Notification
 *n.Initialize
 *n.Icon = "icon"
 *n.SetInfo("This is the title", "and this is the body.", Main) 'Change Main to "" if this code is in the main module.
 *n.Notify(1)</code>
 */
@ShortName("Notification")
@Permissions(values={"android.permission.VIBRATE"})
public class NotificationWrapper extends AbsObjectWrapper<Object>{
	@Hide
	public static class NotificationData {
		public int defaults;
		public int flags;
		public int number;
		public int icon;
		public int importanceLevel;
	}
	private static int pendingId = 1;
	/**
	 * Minimum notification importance: only shows in the shade, below the fold.
	 *Cannot be used with foreground services.
	 */
	public static final int IMPORTANCE_MIN = 1;

	/**
	 * Low notification importance: shows everywhere, but is not intrusive.
	 */
	public static final int IMPORTANCE_LOW = 2;

	/**
	 * Default notification importance: shows everywhere, makes noise, but does not visually intrude.
	 */
	public static final int IMPORTANCE_DEFAULT = 3;

	/**
	 * Higher notification importance: shows everywhere, makes noise and peeks. May use full screen
	 * intents.
	 */
	public static final int IMPORTANCE_HIGH = 4;



	/**
	 * Initializes the notification. By default the notification plays a sound, shows a light and vibrates the phone.
	 */
	public void Initialize() {
		Initialize2(IMPORTANCE_DEFAULT);
	}
	/**
	 * Initializes the notification and sets the notification channel importance level.
	 *Note that the importance level only affect Android 8+ devices.
	 *<code>
	 *Dim no As Notification
	 *no.Initialize2(no.IMPORTANCE_DEFAULT)</code>
	 */
	public void Initialize2(int ImportanceLevel) {
		NotificationData nd = new NotificationData();
		nd.importanceLevel = ImportanceLevel;
		nd.defaults = Notification.DEFAULT_ALL;
		setObject(nd);

	}
	private NotificationData getND() {
		Object o = getObject();
		if (o instanceof NotificationData == false)
			throw new RuntimeException("Cannot change properties after call to SetInfo. Initialize the notification again.");
		return (NotificationData)o;
	}
	/**
	 * Sets whether the notification will vibrate.
	 *Example:<code>
	 *n.Vibrate = False</code>
	 */
	public void setVibrate(boolean v) {
		setValue(v, Notification.DEFAULT_VIBRATE);
	}
	/**
	 * Sets whether the notification will play a sound.
	 *Example:<code>
	 *n.Sound = False</code>
	 */
	public void setSound(boolean v) {
		setValue(v, Notification.DEFAULT_SOUND);
	}
	/**
	 * Sets whether the notification will show a light.
	 *Example:<code>
	 *n.Light = False</code>
	 */
	public void setLight(boolean v) {
		setValue(v, Notification.DEFAULT_LIGHTS);
		setFlag(v, Notification.FLAG_SHOW_LIGHTS);
	}
	private void setValue(boolean v, int Default) {
		if (v)
			getND().defaults |= Default;
		else
			getND().defaults &= ~Default;
	}
	/**
	 * Sets whether the notification will be canceled automatically when the user clicks on it.
	 */
	public void setAutoCancel(boolean v) {
		setFlag(v, Notification.FLAG_AUTO_CANCEL);
	}
	/**
	 * Sets whether the sound will play repeatedly until the user opens the notifications screen.
	 */
	public void setInsistent(boolean v) {
		setFlag(v, Notification.FLAG_INSISTENT);
	}
	/**
	 * Sets whether this notification is an "ongoing event". The notification will be displayed in the ongoing section
	 *and it will not be cleared.
	 */
	public void setOnGoingEvent(boolean v) {
		setFlag(v, Notification.FLAG_ONGOING_EVENT);
	}
	/**
	 * Gets or sets a number that will be displayed on the icon. This is useful to represent multiple events in a single notification.
	 */
	public int getNumber() {
		return getND().number;
	}
	public void setNumber(int v) {
		getND().number = v;
	}
	private void setFlag(boolean v, int Flag) {
		if (v)
			getND().flags |= Flag;
		else
			getND().flags &= ~Flag;
	}
	/**
	 * Sets the icon displayed.
	 *The icon value is the name of the image file without the extension. <b>The name is case sensitive.
	 *The image file should be manually copied to the following folder: source folder\Objects\res\drawable.</b>
	 *You can use "icon" to use the application icon (which is also located in this folder):<code>
	 *n.Icon = "icon"</code>
	 */
	public void setIcon(String s) {
		getND().icon = BA.applicationContext.getResources().getIdentifier(s, "drawable", BA.packageName);
	}

	/**
	 * Sets the message text and action.
	 *Title - The message title.
	 *Body - The message body.
	 *Activity - The activity to start when the user presses on the notification.
	 *Pass an empty string to start the current activity (when calling from an activity module).
	 *Example:<code>
	 *n.SetInfo("Some title", "Some text", Main)</code>
	 */
	@DesignerName("SetInfo")
	public void SetInfoNew(BA ba, CharSequence Title, CharSequence Body, Object Activity) throws ClassNotFoundException {
		SetInfo2New(ba, Title, Body, null, Activity);
	}
	/**
	 * Similar to SetInfo. Also sets a string that can be later extracted in Activity_Resume.
	 *Title - The message title.
	 *Body - The message body.
	 *Tag - An arbitrary string that can be later extract when the user clicks on the notification.
	 *Activity - The activity to start when the user presses on the notification.
	 *Pass an empty string to start the current activity (when calling from an activity module).
	 *Example of extracting the tag:<code>
	 *Sub Activity_Resume
	 *	Dim in As Intent
	 *	in = Activity.GetStartingIntent
	 *	If in.HasExtra("Notification_Tag") Then
	 *		Log(in.GetExtra("Notification_Tag")) 'Will log the tag
	 *	End If
	 *End Sub</code>
	 */
	@DesignerName("SetInfo2")
	public void SetInfo2New(BA ba, CharSequence Title, CharSequence Body, CharSequence Tag, Object Activity) throws ClassNotFoundException {
		Intent i = Common.getComponentIntent(ba, Activity);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		if (Tag != null)
			i.putExtra("Notification_Tag", Tag);
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= 31)
			flags |= 67108864; //FLAG_IMMUTABLE
		PendingIntent pi = PendingIntent.getActivity(ba.context, Tag == null ? 0 : pendingId++
				, i,
				flags);

		NotificationData nd = getND();
		Notification n;
		if (Build.VERSION.SDK_INT >= 19) {
			Notification.Builder builder;
			if (Build.VERSION.SDK_INT >= 26) {
				try {
					String channelId = "channel_" + nd.importanceLevel;
					builder = new Notification.Builder(BA.applicationContext, channelId);
					NotificationChannel channel = new NotificationChannel(channelId, Common.Application.getLabelName(), nd.importanceLevel);
					NotificationManager manager = (NotificationManager) BA.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
//					channel.enableLights((nd.flags & Notification.FLAG_SHOW_LIGHTS) != 0);
//					channel.enableVibration((nd.defaults & Notification.DEFAULT_VIBRATE) != 0);
					manager.createNotificationChannel(channel);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			} else {
				builder = new Notification.Builder(BA.applicationContext);
			}
			builder.setContentTitle(Title).setContentText(Body).setContentIntent(pi);
			n = builder.build();
			n.defaults = nd.defaults;
			n.flags = nd.flags;
			n.icon = nd.icon;
			n.when = System.currentTimeMillis();
			n.number = nd.number;
			n.extras.putBoolean("android.showWhen", true);

		} else {

			n = new Notification();
			
			n.defaults = nd.defaults;
			n.flags = nd.flags;
			n.icon = nd.icon;
			n.when = System.currentTimeMillis();
			n.number = nd.number;
			try {
				if (methodSetLastEvent == null) {
					methodSetLastEvent = Notification.class.getDeclaredMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
				}
				methodSetLastEvent.invoke(n, ba.context, Title, Body, pi);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		setObject(n);
	}
	@Hide
	public void SetInfo(BA ba, String Title, String Body, Object Activity) throws ClassNotFoundException {
		SetInfo2New(ba, (CharSequence)Title, Body, null, Activity);
	}
	@Hide
	public void SetInfo2(BA ba, String Title, String Body, String Tag, Object Activity) throws ClassNotFoundException {
		SetInfo2New(ba, (CharSequence)Title, Body, Tag, Activity);
	}
	private static Method methodSetLastEvent;
	/**
	 * Displays the notification.
	 *Id - The notification id. This id can be used to later update this notification (by calling Notify again with the same Id),
	 *or to cancel the notification.
	 */
	public void Notify(int Id) {
		NotificationManager nm = (NotificationManager)BA.applicationContext.getSystemService(
				Context.NOTIFICATION_SERVICE);
		Object o = getObject();
		if (o instanceof Notification == false)
			throw new RuntimeException("You must first call SetInfo or SetInfo2");
		nm.notify(Id, (Notification)getObject());
	}
	/**
	 * Cancels the notification with the given Id.
	 */
	public void Cancel(int Id) {
		NotificationManager nm = (NotificationManager)BA.applicationContext.getSystemService(
				Context.NOTIFICATION_SERVICE);
		nm.cancel(Id);
	}
}
