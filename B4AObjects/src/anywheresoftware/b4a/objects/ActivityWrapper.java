package anywheresoftware.b4a.objects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.B4AMenuItem;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.IterableList;
import anywheresoftware.b4a.BA.Pixel;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BALayout.LayoutParams;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.LayoutBuilder;
import anywheresoftware.b4a.keywords.LayoutValues;
import anywheresoftware.b4a.keywords.LayoutBuilder.ViewWrapperAndAnchor;
/**
 * Each activity module include a predefined Activity object.
 *Activity is the main component of your application.
 *Activities have three special life cycle related event: Activity_Create, Activity_Resume and Activity_Pause.
 *See this tutorial for more information about activities and processes life cycle: <link>Life cycle tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6487-android-process-activities-life-cycle.html</link>.
 *
 *You can add and remove views to this activity with AddView and RemoveViewAt methods.
 *You can also load a layout file with LoadLayout.
 *The Touch event can be used to handle user touches. 
 *The first parameter of this event is the Action parameter. The parameter values can be ACTION_DOWN,
 *ACTION_MOVE or ACTION_UP. Use this value to find the user current action.
 *The KeyPress and KeyUp events occur when the user presses or releases a key, assuming that no other view has consumed this event (like EditText).
 *When handling the KeyPress or KeyUp event you should return a boolean value which tells whether the event was consumed.
 *For example if the user pressed on the Back key and you return True then the OS will not close your activity.
 *<code>
 *Sub Activity_KeyPress (KeyCode As Int) As Boolean
 *	If Keycode = KeyCodes.KEYCODE_BACK Then
 *		Return True
 *	Else
 *		Return False
 *	End If
 *End Sub</code>
 *You can add menu items to the activity with AddMenuItem method. Note that this method should only be called inside
 *Activity_Create event.
 *Starting from Android 4.3 it is not possible to show a modal dialog inside the KeyPress or KeyUp events, with one exception which is in the case of the Back key.
 *If you need to show a modal dialog for other keys then you should call a sub with CallSubDelayed and show the modal dialog in that sub.
 */
@Events(values={"Touch (Action As Int, X As Float, Y As Float)",
		"KeyPress (KeyCode As Int) As Boolean 'Return True to consume the event",
		"KeyUp (KeyCode As Int) As Boolean", "WindowFocusChanged (Focused As Boolean)", "ActionBarHomeClick",
		"PermissionResult (Permission As String, Result As Boolean)"})
@ShortName("Activity")
@ActivityObject
public class ActivityWrapper extends ViewWrapper<BALayout> implements IterableList{
	public static final int ACTION_DOWN = MotionEvent.ACTION_DOWN;
	public static final int ACTION_UP = MotionEvent.ACTION_UP;
	public static final int ACTION_MOVE = MotionEvent.ACTION_MOVE;
	public ActivityWrapper() { //this constructor is used to allow: Dim a As Activity
		
	}
	public ActivityWrapper(final BA ba, String name) {
		if (BA.shellMode)
			return; //subs are not yet ready
		reinitializeForShell(ba, name);
	}
	
	@Hide
	public void reinitializeForShell(final BA ba, String name) {
		if (IsInitialized())
			return;
		setObject(ba.vg);
		innerInitialize(ba, name, true);
		if (ba.subExists("activity_touch")) {
			getObject().setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					ba.raiseEventFromUI(ActivityWrapper.this, "activity_touch", event.getAction(), event.getX(),
							event.getY());
					return true;
				}

			});
		}
	}
	/**
	 * (Advanced) Gets the intent object that started this Activity.
	 *This can be used together with SetActivityResult to return results to 3rd party applications.
	 */
	public IntentWrapper GetStartingIntent() {
		IntentWrapper iw = new IntentWrapper();
		iw.setObject(getActivity().getIntent());
		return iw;
	}
	/**
	 * (Advanced) Sets the result that the calling Activity will get after calling StartActivityForResult.
	 */
	public void SetActivityResult(int Result, IntentWrapper Data) {
		getActivity().setResult(Result, Data.getObject());
	}
	/**
	 * Adds a view to this activity.
	 */
	public void AddView(View View, @Pixel int Left,@Pixel int Top ,@Pixel int Width,@Pixel int Height) {
		getObject().addView(View , new BALayout.LayoutParams(Left, Top, Width, Height));
	}
	/**
	 * Gets the view that is stored in the specified index.
	 */
	public ConcreteViewWrapper GetView(int Index) {
		ConcreteViewWrapper vw = new ConcreteViewWrapper();
		vw.setObject(getObject().getChildAt(Index));
		return vw;
	}
	/**
	 * Removes all child views.
	 */
	public void RemoveAllViews() {
		getObject().removeAllViews();
	}
	/**
	 * Removes the view that is stored in the specified index.
	 */
	public void RemoveViewAt(int Index) {
		getObject().removeViewAt(Index);
	}
	/**
	 * Returns the number of child views.
	 */
	public int getNumberOfViews() {
		return getObject().getChildCount();
	}
	
	
	/**
	 * Adds a menu item to the activity.
	 *Title - Menu item title.
	 *EventName - The prefix name of the sub that will handle the click event.
	 *This method should only be called inside sub Activity_Create.
	 *Note that the 'Sender' value inside the click event equals to the clicked menu item text.
	 *Example:<code>
	 *Activity.AddMenuItem("Open File", "OpenFile")
	 *...
	 *Sub OpenFile_Click
	 *...
	 *End Sub</code>
	 */
	public void AddMenuItem(CharSequence Title, String EventName) {
		AddMenuItem3(Title, EventName, null, false);
	}
	/**
	 * Adds a menu item to the activity.
	 *Title - Menu item title.
	 *EventName - The prefix name of the sub that will handle the click event.
	 *Bitmap - Bitmap to draw as the item background.
	 *Only the first five (or six if there are six total) menu items display icons.
	 *This method should only be called inside sub Activity_Create.
	 *Note that the 'Sender' value inside the click event equals to the clicked menu item text.
	 *Example:<code>
	 *Activity.AddMenuItem2("Open File", "OpenFile", LoadBitmap(File.DirAssets, "SomeImage.png"))
	 *...
	 *Sub OpenFile_Click
	 *...
	 *End Sub</code>
	 */
	public void AddMenuItem2(CharSequence Title, String EventName, Bitmap Bitmap) {
		AddMenuItem3(Title, EventName, Bitmap, false);
	}
	/**
	 * Similar to AddMenuItem2. If AddToActionBar is true then the item will be displayed in the action bar (on Android 3.0+ devices) if there is enough room.
	 *If there is not enough room then the item will be displayed together with the other menu items.
	 */
	public void AddMenuItem3(CharSequence Title, String EventName, Bitmap Bitmap, boolean AddToActionBar) {
		Drawable d = null;
		if (Bitmap != null) {
			anywheresoftware.b4a.objects.drawable.BitmapDrawable bd = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
			bd.Initialize(Bitmap);
			d = bd.getObject();
		}
		B4AMenuItem mi = new B4AMenuItem(Title, d, EventName, AddToActionBar);
		((B4AActivity)getActivity()).addMenuItem(mi);
	}
	/**
	 * Loads a layout file (.bal).
	 *Returns the LayoutValues of the actual layout variant that was loaded.
	 */
	@RaisesSynchronousEvents
	public LayoutValues LoadLayout(String LayoutFile, BA ba) throws Exception {
		AbsObjectWrapper.Activity_LoadLayout_Was_Called = true;
		return LayoutBuilder.loadLayout(LayoutFile, ba, true, ba.vg, null).layoutValues;
	}
	/**
	 * <b>This method is deprecated.</b> It ignores the anchoring features and it will fail in Rapid Debug mode.
	 *You should instead remove the views and load the layout again.
	 */
	public void RerunDesignerScript(String Layout, BA ba, int Width, int Height) throws Exception {
		ViewGroup vg = new BALayout(ba.context);
		vg.setLayoutParams(new ViewGroup.LayoutParams(Width, Height));
		LinkedHashMap<String, ViewWrapperAndAnchor> dynamicTable = new LayoutBuilder.LayoutHashMap<String, ViewWrapperAndAnchor>();
		for (Field f : ba.activity.getClass().getFields()) {
			if (f.getName().startsWith("_") && ViewWrapper.class.isAssignableFrom(f.getType())) {
				dynamicTable.put(f.getName().substring(1), new ViewWrapperAndAnchor((ViewWrapper<?>) f.get(ba.activity), null));
			}
		}
		LayoutBuilder.loadLayout(Layout, ba, false, vg, dynamicTable);
	}
	private Activity getActivity() {
		return (Activity)getObject().getContext();
	}
	/**
	 * Programmatically opens the menu.
	 */
	public void OpenMenu() {
		getActivity().openOptionsMenu();
	}
	/**
	 * Programmatically closes the menu.
	 */
	public void CloseMenu() {
		getActivity().closeOptionsMenu();
	}
	
	public void setTitle(CharSequence Title) {
		getActivity().setTitle(Title);
	}
	public CharSequence getTitle() {
		return getActivity().getTitle();
	}
	/**
	 * Gets or sets the title color. This property is only supported by Android 2.x devices. It will not do anything on newer devices.
	 */
	public int getTitleColor() {
		return getActivity().getTitleColor();
	}
	public void setTitleColor(int Color) {
		getActivity().setTitleColor(Color);
	}
	/**
	 * This method was added as a workaround for the following <link>Android bug|https://code.google.com/p/android/issues/detail?id=55933</link>.
	 * By setting the Disable property to True the child views (of all Activities) will not be added to the accessibility enabled list. 
	 */
	public void DisableAccessibility(boolean Disable) {
		BALayout.disableAccessibility = Disable;
	}
	//Activity doesn't have a BALayout.LayoutParams
	@Override
	public int getWidth() {
		return getObject().getWidth();
	}
	@Override
	public int getHeight() {
		return getObject().getHeight();

	}
	@Override
	public int getLeft() {
		return 0;
	}
	@Override
	public int getTop() {
		return 0;
	}
	@Override
	@Hide
	public void setVisible(boolean Visible) {
	}
	@Override
	@Hide
	public boolean getVisible() {
		return true;
	}
	@Override
	@Hide
	public void setEnabled(boolean Enabled) {
	}
	@Override
	@Hide
	public boolean getEnabled() {
		return true;
	}
	@Override
	@Hide
	public void BringToFront() {

	}
	@Override
	@Hide
	public void SendToBack() {

	}
	@Override
	@Hide
	public void RemoveView() {
		
	}
	/**
	 * Closes this activity.
	 */
	public void Finish() {
		getActivity().finish();
	}
	/**
	 * Returns an iterator that iterates over all the child views including views that were added to other child views.
	 *Example:<code>
	 *For Each v As View In Activity.GetAllViewsRecursive
	 *	...
	 *Next</code>
	 */
	public IterableList GetAllViewsRecursive() {
		return new AllViewsIterator(this.getObject());
	}
	@Hide
	@Override
	public Object Get(int index) {
		return GetView(index).getObject();
	}
	@Hide
	@Override
	public int getSize() {
		return getNumberOfViews();
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) 
	throws Exception {
		Drawable d = (Drawable)DynamicBuilder.build(prev, (HashMap<String, Object>)props.get("drawable"),
				designer, null);
		View v = (View)prev;
		int defaultTitleColor = 0;
		if (designer)
			defaultTitleColor = (Integer) ViewWrapper.getDefault(v, "titleColor", ((Activity)v.getContext()).getTitleColor());
		if (d != null) 
			v.setBackgroundDrawable(d);
		((Activity)v.getContext()).setTitle((String)props.get("title"));
		int titleColor = (Integer)props.get("titleColor");
		if (titleColor != ViewWrapper.defaultColor)
			((Activity)v.getContext()).setTitleColor(titleColor);
		else if (designer) {
			((Activity)v.getContext()).setTitleColor(defaultTitleColor);
		}
		if (BA.debugMode) {
			BA.warningEngine.checkFullScreenInLayout((Boolean)props.get("fullScreen"), (Boolean)props.get("includeTitle"));
		}
		//handle fullscreen changes
		if (designer) {
			boolean fullScreen = (Boolean)props.get("fullScreen");
			boolean includeTitle = (Boolean)props.get("includeTitle");
			Class<?> cls = Class.forName("anywheresoftware.b4a.designer.Designer");
			boolean prevFullScreen = cls.getField("fullScreen").getBoolean(v.getContext());
			boolean prevIncludeTitle = cls.getField("includeTitle").getBoolean(v.getContext());
			if (prevFullScreen != fullScreen || includeTitle != prevIncludeTitle) {
				Intent i = new Intent(v.getContext().getApplicationContext(),
						cls);
				i.putExtra("anywheresoftware.b4a.designer.includeTitle", includeTitle);
				i.putExtra("anywheresoftware.b4a.designer.fullScreen", fullScreen);
				cls.getMethod("restartActivity", Intent.class).invoke(v.getContext(), i);
			}
		}
		return (View)prev;
	}
	@Hide
	public static class AllViewsIterator implements IterableList {
		private ArrayList<View> views = new ArrayList<View>();
		public AllViewsIterator(ViewGroup parent) {
			addViews(parent);
		}
		private void addViews(ViewGroup parent) {
			for (int i = 0;i < parent.getChildCount();i++) {
				View v = parent.getChildAt(i);
				views.add(v);
				if (v instanceof ViewGroup)
					addViews((ViewGroup) v);
			}
		}
		@Override
		public Object Get(int index) {
			return views.get(index);
		}

		@Override
		public int getSize() {
			return views.size();
		}
		
	}
}
