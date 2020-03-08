package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

/**
 * HorizontalScrollView is a view that contains other views and allows the user to horizontally scroll those views.
 *The HorizontalScrollView is similar to ScrollView which scrolls vertically.
 *See the <link>ScrollView tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6612-scrollview-example.html</link> for more information.
 *The HorizontalScrollView has an inner panel which actually contains the child views.
 *You can add views by calling: <code>HorizontalScrollView1.Panel.AddView(...)</code>
 *Note that it is not possible to nest scrolling views.
 */
@ActivityObject
@ShortName("HorizontalScrollView")
@Events(values={"ScrollChanged(Position As Int)"})
@DontInheritEvents
public class HorizontalScrollViewWrapper extends ViewWrapper<HorizontalScrollView>{
	private PanelWrapper pw = new PanelWrapper();
	
	/**
	 *Initializes the object.
	 *Width - The width of the inner panel.
	 *EventName - Sets the sub that will handle the event.
	 */
	public void Initialize(final BA ba, int Width, String EventName) {
		super.Initialize(ba, EventName);
		PanelWrapper p = new PanelWrapper();
		p.Initialize(ba, "");
		getObject().addView(p.getObject(), Width, LayoutParams.FILL_PARENT);
	}
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String EventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new MyHScrollView(ba.context));
		super.innerInitialize(ba, EventName, true);
		if (ba.subExists(EventName + "_scrollchanged")) {
			if (getObject() instanceof MyHScrollView) {
				MyHScrollView m = (MyHScrollView)getObject();
				m.ba = ba;
				m.eventName = EventName;
			}
		}
	}
	/**
	 * Returns the panel which you can use to add views to.
	 *Example:<code>
	 *HorizontalScrollView1.Panel.AddView(...)</code>
	 */
	public PanelWrapper getPanel() {
		pw.setObject((ViewGroup) this.getObject().getChildAt(0));
		return pw;
	}
	/**
	 * Scrolls the view to the right or left.
	 */
	public void FullScroll(boolean Right) {
		getObject().fullScroll(Right ? View.FOCUS_RIGHT : View.FOCUS_LEFT);
	}
	/**
	 * Gets or sets the scroll position.
	 */
	public int getScrollPosition() {
		return getObject().getScrollX();
	}
	public void setScrollPosition(int Scroll) {
		getObject().smoothScrollTo(Scroll, 0);
	}
	/**
	 * Immediately scrolls the HorizontalScrollView (with no animations).
	 */
	public void ScrollToNow(int Scroll) {
		getObject().scrollTo(Scroll, 0);
	}
	
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, MyHScrollView.class, props, designer);
		}
		HorizontalScrollView v = (HorizontalScrollView) ViewWrapper.build(prev, props, designer);
		if (v.getChildCount() > 0)
			v.removeAllViews();
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		Drawable d = DynamicBuilder.build(prev, drawProps, designer, null);
		if (props.containsKey("innerWidth")) {
			BALayout b = new BALayout((Context)tag);
			v.addView(b, (int) (BALayout.getDeviceScale() * (Integer)props.get("innerWidth")), LayoutParams.FILL_PARENT);
			if (d != null)
				b.setBackgroundDrawable(d);
		}
		else {
			if (d != null)
				v.setBackgroundDrawable(d);
		}
		return v;
	}
	@Hide
	public static class MyHScrollView extends HorizontalScrollView  {
		public String eventName;
		public BA ba;
		public MyHScrollView(Context context) {
			super(context);
		}
		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			super.onScrollChanged(l, t, oldl, oldt);
			if (ba != null)
				ba.raiseEventFromUI(this, eventName + "_scrollchanged", l);
		}
	}
}
