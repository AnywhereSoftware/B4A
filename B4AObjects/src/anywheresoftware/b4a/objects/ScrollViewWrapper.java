package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

/**
 * ScrollView is a view that contains other views and allows the user to vertically scroll those views.
 *See the <link>ScrollView tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6612-scrollview-example.html</link> for more information.
 *The ScrollView has an inner panel which actually contains the child views.
 *You can add views by calling: <code>ScrollView1.Panel.AddView(...)</code>
 *Note that it is not possible to nest scrolling views. For example a multiline EditText cannot be located inside a ScrollView.
 */
@ActivityObject
@ShortName("ScrollView")
@Events(values={"ScrollChanged(Position As Int)"})
@DontInheritEvents
public class ScrollViewWrapper extends ViewWrapper<ScrollView>{
	private PanelWrapper pw = new PanelWrapper();
	/**
	 * Initializes the ScrollView and sets its inner panel height to the given height.
	 *You can later change this height by calling ScrollView.Panel.Height.
	 *<code>
	 *Dim ScrollView1 As ScrollView
	 *ScrollView1.Initialize(1000dip)</code>
	 */
	public void Initialize(final BA ba, int Height) {
		Initialize2(ba, Height, "");
	}
	/**
	 * Similar to Initialize. Sets the Sub that will handle the ScrollChanged event.
	 */
	public void Initialize2(final BA ba, int Height, String EventName) {
		super.Initialize(ba, EventName);
		PanelWrapper p = new PanelWrapper();
		p.Initialize(ba, "");
		getObject().addView(p.getObject(), LayoutParams.FILL_PARENT, Height);
	}
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String EventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new MyScrollView(ba.context));
		super.innerInitialize(ba, EventName, true);
		if (ba.subExists(EventName + "_scrollchanged")) {
			if (getObject() instanceof MyScrollView) {
				MyScrollView m = (MyScrollView)getObject();
				m.ba = ba;
				m.eventName = EventName;
			}
		}
	}
	/**
	 * Returns the panel which you can use to add views to.
	 *Example:<code>
	 *ScrollView1.Panel.AddView(...)</code>
	 */
	public PanelWrapper getPanel() {
		pw.setObject((ViewGroup) this.getObject().getChildAt(0));
		return pw;
	}
	/**
	 * Scrolls the scroll view to the top or bottom.
	 */
	public void FullScroll(boolean Bottom) {
		getObject().fullScroll(Bottom ? View.FOCUS_DOWN : View.FOCUS_UP);
	}
	/**
	 * Gets or sets the scroll position.
	 */
	public int getScrollPosition() {
		return getObject().getScrollY();
	}
	public void setScrollPosition(int Scroll) {
		getObject().smoothScrollTo(0, Scroll);
	}
	/**
	 * Immediately scrolls the ScrollView (with no animations).
	 */
	public void ScrollToNow(int Scroll) {
		getObject().scrollTo(0, Scroll);
	}
	
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, MyScrollView.class, props, designer);
		}
		ScrollView v = (ScrollView) ViewWrapper.build(prev, props, designer);
		if (v.getChildCount() > 0)
			v.removeAllViews();
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		Drawable d = DynamicBuilder.build(prev, drawProps, designer, null);
		if (props.containsKey("innerHeight")) {
			BALayout b = new BALayout((Context)tag);
			v.addView(b, LayoutParams.FILL_PARENT, (int) (BALayout.getDeviceScale() * (Integer)props.get("innerHeight")));
	
			b.setBackgroundDrawable(d);
		}
		else {
			v.setBackgroundDrawable(d);
		}
		return v;
	}
	@Hide
	public static class MyScrollView extends ScrollView  {
		public String eventName;
		public BA ba;
		public MyScrollView(Context context) {
			super(context);
		}
		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			super.onScrollChanged(l, t, oldl, oldt);
			if (ba != null)
				ba.raiseEventFromUI(this, eventName + "_scrollchanged", t);
		}
	}
}
