package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.IterableList;
import anywheresoftware.b4a.BA.Pixel;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.WarningEngine;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.LayoutBuilder;
import anywheresoftware.b4a.keywords.LayoutValues;

/**
 * A Panel is a view that holds other child views.
 *You can add child views programmatically or by loading a layout file.
 *The Panel raises the Touch event. The first parameter of this event is the Action which is one of the Activity action constants.
 *Return True from the Touch event sub to consume the event (otherwise other views behind the Panel will receive the event).
 */
@ShortName("Panel")
@Events(values={"Touch (Action As Int, X As Float, Y As Float)"})
@ActivityObject
public class PanelWrapper extends ViewWrapper<ViewGroup> implements IterableList{
	public static final int ACTION_DOWN = MotionEvent.ACTION_DOWN;
	public static final int ACTION_UP = MotionEvent.ACTION_UP;
	public static final int ACTION_MOVE = MotionEvent.ACTION_MOVE;
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new BALayout(ba.context));
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_touch")) {
			getObject().setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					ba.raiseEventFromUI(getObject(), eventName + "_touch", event.getAction(), event.getX(),
							event.getY());
					return true;

				}

			});
		}
	}
	/**
	 * Adds a view to this panel.
	 */
	public void AddView(View View, int Left, int Top , int Width, int Height) {
		getObject().addView(View , new BALayout.LayoutParams(Left, Top, Width, Height));
	}
	/**
	 * Gets the view that is stored in the specified index.
	 */
	public ConcreteViewWrapper GetView(int Index) {
		ConcreteViewWrapper c = new ConcreteViewWrapper();
		c.setObject(getObject().getChildAt(Index));
		return c;
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
	public float getElevation() throws Exception{
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			return (Float)View.class.getDeclaredMethod("getElevation").invoke(getObject());
		}
		return 0;
	}
	public void setElevation(@Pixel float e) throws Exception{
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			View.class.getDeclaredMethod("setElevation", float.class).invoke(getObject(), e);
		}
	}
	public void SetElevationAnimated(int Duration, @Pixel float Elevation) throws Exception {
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			float current = getElevation();
			setElevation(Elevation);
			ObjectAnimator.ofFloat(getObject(), "translationZ", (current - Elevation), 0).setDuration(Duration).start();
		}
	}
	/**
	 * Loads a layout file to this panel. Returns the value of the chosen layout variant.
	 */
	@RaisesSynchronousEvents
	public LayoutValues LoadLayout(String LayoutFile, BA ba) throws Exception {
		LayoutParams lp = getObject().getLayoutParams();
		boolean zeroSize = false;
		boolean width_fill_parent = false;
		if (lp == null || lp.width == 0 || lp.height == 0)
			zeroSize = true;
		if (!zeroSize && lp.width == LayoutParams.FILL_PARENT) {
			if (getObject().getParent() == null || ((View)getObject().getParent()).getLayoutParams() == null)
				zeroSize = true;
			else {
				setWidth(((View)getObject().getParent()).getLayoutParams().width);
				width_fill_parent = true;
			}
		}
		if (zeroSize) {
			BA.LogInfo("Panel size is unknown. Layout may not be loaded correctly.");
			if (false)
				WarningEngine.warn(WarningEngine.ZERO_SIZE_PANEL);
		}
		LayoutValues lv = LayoutBuilder.loadLayout(LayoutFile, ba, false, this.getObject(), null).layoutValues;
		if (width_fill_parent)
			setWidth(LayoutParams.FILL_PARENT);
		return lv;
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
	/**
	 * Returns an iterator that iterates over all the child views including views that were added to other child views.
	 *Example:<code>
	 *For Each v As View In Panel1.GetAllViewsRecursive
	 *	...
	 *Next</code>
	 */
	public IterableList GetAllViewsRecursive() {
		return new ActivityWrapper.AllViewsIterator(this.getObject());
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		View vg = (View) prev;
		if (vg == null) {
			vg = ViewWrapper.buildNativeView((Context)tag, BALayout.class, props, designer);
		}
		vg = ViewWrapper.build(vg, props, designer);
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		Drawable d = DynamicBuilder.build(vg, drawProps, designer, null);
		if (d != null)
			vg.setBackgroundDrawable(d);
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			float elevation = BA.gm(props, "elevation", 0f);
			View.class.getDeclaredMethod("setElevation", float.class).invoke(vg, (float)(BALayout.getDeviceScale() * elevation));
		}
		return vg;
	}
}
