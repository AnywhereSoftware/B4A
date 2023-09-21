package anywheresoftware.b4a.objects;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.view.View;
import android.widget.RemoteViews;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.ConnectorUtils;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.LayoutValues;

/**
 * RemoteViews allows indirect access to a home screen widget.
 *See this tutorial for more information <link>Widgets tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/10166-android-home-screen-widgets-tutorial-part-i.html</link>.
 */
@ShortName("RemoteViews")
@Events(values={"RequestUpdate", "Disabled"})
public class RemoteViewsWrapper {
	protected Parcel original;
	protected RemoteViews current;
	protected String eventName;
	@Hide
	public static RemoteViewsWrapper createRemoteViews(BA ba, int id, String layout, String eventName) throws Exception {
		RemoteViews rv = new RemoteViews(BA.packageName, id);
		layout = layout.toLowerCase(BA.cul);
		if (!layout.endsWith(".bal"))
			layout = layout + ".bal";
		InputStream in = BA.applicationContext.getAssets().open(layout);
		DataInputStream din = new DataInputStream(in);
		int version = ConnectorUtils.readInt(din);
		int pos = ConnectorUtils.readInt(din);
		while (pos > 0) {
			pos -= in.skip(pos);
		}
		String[] cache = null;
		if (version >= 3) {
			cache = new String[ConnectorUtils.readInt(din)];
			for (int i = 0;i < cache.length;i++) {
				cache[i] = ConnectorUtils.readString(din);
			}
		}
		int numberOfVariants = ConnectorUtils.readInt(din);
		for (int i = 0;i < numberOfVariants;i++) {
			LayoutValues.readFromStream(din);
		}
		HashMap<String, Object> props = ConnectorUtils.readMap(din, cache);
		loadLayoutHelper(ba, props, rv);
		din.close();
		RemoteViewsWrapper rvw = new RemoteViewsWrapper();
		rvw.original = Parcel.obtain();
		rv.writeToParcel(rvw.original, 0);
		rvw.eventName = eventName.toLowerCase(BA.cul);
		return rvw;
	}

	@SuppressWarnings("unchecked")
	private static void loadLayoutHelper(BA ba, HashMap<String, Object> props, 
			RemoteViews rv)
	throws Exception {
		String eventName = ((String)props.get("eventName")).toLowerCase(BA.cul);
		String name = ((String)props.get("name")).toLowerCase(BA.cul);
		HashMap<String, Object> kids = 
			(HashMap<String, Object>) props.get(":kids");
		if (kids != null) {
			for (int i = 0;i < kids.size();i++) {
				loadLayoutHelper(ba, (HashMap<String, Object>)kids.get(String.valueOf(i)), rv);
			}
		}
		if (ba.htSubs.containsKey(eventName + "_click")) {
			Intent i = new Intent(BA.applicationContext, Common.getComponentClass(ba, null, true));
			i.putExtra("b4a_internal_event", eventName + "_click");
			int id = getIdForView(ba, name);
			int flags = PendingIntent.FLAG_UPDATE_CURRENT;
			if (Build.VERSION.SDK_INT >= 31)
				flags |= 33554432; //FLAG_MUTABLE
			PendingIntent pi = PendingIntent.getBroadcast(ba.context,
					id, i , flags);
			rv.setOnClickPendingIntent(id, pi);
		}
	}

	protected static int getIdForView(BA ba, String viewName)  {
		try {
			Class<?> Rid = Class.forName(BA.packageName + ".R$id");
			return Rid.getField(ba.getClassNameWithoutPackage() + "_" + viewName.toLowerCase(BA.cul)).getInt(null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	protected void checkNull() {
		if (original == null)
			throw new RuntimeException("RemoteViews should be set by calling ConfigureHomeWidget.");
		if (current == null) {
			original.setDataPosition(0);
			current = new RemoteViews(original);
		}
	}
	/**
	 * Checks if the intent starting this service was sent from the widget and raises events based on the intent.
	 *Returns True if an event was raised.
	 */
	public boolean HandleWidgetEvents(BA ba, Intent StartingIntent) {
		if (StartingIntent == null)
			return false;
		if (StartingIntent.hasExtra("b4a_internal_event")) {
			raiseEventWithDebuggingSupport(ba, StartingIntent.getStringExtra("b4a_internal_event"));
			return true;
		}
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(StartingIntent.getAction())) {
			raiseEventWithDebuggingSupport(ba, eventName + "_requestupdate");
			return true;
		}
		if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(StartingIntent.getAction())) {
			raiseEventWithDebuggingSupport(ba, eventName + "_disabled");
			return true;
		}
		return false;
	}
	private void raiseEventWithDebuggingSupport(final BA ba,final String event) {
		if (BA.debugMode) {
			BA.handler.post(new BA.B4ARunnable() {
				@Override
				public void run() {
					ba.raiseEvent(this, event);
				}
			});
		}
		else {
			ba.raiseEvent(this, event);
		}
	}
	/**
	 * Sets the text of the given view.
	 *Example:<code>rv.SetText("Label1", "New text")</code>
	 */
	public void SetText(BA ba, String ViewName, CharSequence Text)  {
		checkNull();
		current.setTextViewText(getIdForView(ba, ViewName), Text);
	}
	/**
	 * Sets the visibility of the given view.
	 *Example:<code>rv.SetVisibile("Button1", False)</code>
	 */
	public void SetVisible(BA ba, String ViewName, boolean Visible)  {
		checkNull();
		current.setViewVisibility(getIdForView(ba, ViewName), Visible ? View.VISIBLE : View.INVISIBLE);
	}
	/**
	 * Sets the image of the given ImageView.
	 *Example:<code>rv.SetImage("ImageView1", LoadBitmap(File.DirAssets, "1.jpg"))</code>
	 */
	public void SetImage(BA ba, String ImageViewName, Bitmap Image) {
		checkNull();
		current.setImageViewBitmap(getIdForView(ba, ImageViewName), Image);
	}
	/**
	 * Sets the text color of the given button or label.
	 *Example:<code>rv.SetTextColor("Label1", Colors.Red)</code>
	 */
	public void SetTextColor(BA ba, String ViewName, int Color)  {
		checkNull();
		current.setTextColor(getIdForView(ba, ViewName), Color);
	}
	/**
	 * Sets the text size of the given button or label.
	 *Example:<code>rv.SetTextSize("Label1", 20)</code>
	 */
	public void SetTextSize(BA ba, String ViewName, float Size)  {
		checkNull();
		current.setFloat(getIdForView(ba, ViewName), "setTextSize", Size);
	}
	/**
	 * Sets the progress value of the given ProgressBar. Value should be between 0 to 100.
	 *Example:<code>rv.SetProgress("ProgressBar1", 50)</code>
	 */
	public void SetProgress(BA ba, String ProgressBarName, int Progress) {
		checkNull();
		current.setInt(getIdForView(ba, ProgressBarName), "setProgress", Progress);
	}
	/**
	 * Updates the widget with the changes done. This method is also responsible for configuring the events.
	 */
	public void UpdateWidget(BA ba) throws ClassNotFoundException {
		checkNull();
		ComponentName cn = new ComponentName(ba.context, Class.forName(ba.className));
		AppWidgetManager.getInstance(ba.context).updateAppWidget(cn, current);
		current = null;
	}

}
