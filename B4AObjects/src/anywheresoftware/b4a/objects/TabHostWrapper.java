package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.R;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;

/**
 * TabHost is a view that contains multiple tab pages. Each tab page contains other child views.
 *See the <link>TabHost tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6721-tabhost-tutorial.html</link> for more information.
 *
 */
@ShortName("TabHost")
@ActivityObject
@Events(values={"TabChanged"})
public class TabHostWrapper extends ViewWrapper<TabHost>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new TabHost(ba.context, null));
		super.innerInitialize(ba, eventName, true);
		initializeTabWidget(ba.context, getObject());
		if (ba.subExists(eventName + "_tabchanged")) {
			getObject().setOnTabChangedListener(new TabHost.OnTabChangeListener() {
				@Override
				public void onTabChanged(String tabId) {
					ba.raiseEvent2(getObject(), false, eventName + "_tabchanged", false);
				}
			});
		}
		MyContentFactory m = new MyContentFactory(new View(ba.context));
		TabSpec ts = getObject().newTabSpec("~temp");
		ts.setContent(m);
		ts.setIndicator("");
		getObject().addTab(ts);
	}
	private static void initializeTabWidget(Context context, TabHost tabHost) {
		TabWidget tw = new TabWidget(context);
		LinearLayout ll = new LinearLayout(context);
		int pad = Common.DipToCurrent(5);
		ll.setPadding(pad, pad, pad, pad);
		ll.setOrientation(LinearLayout.VERTICAL);
		tw.setId(android.R.id.tabs);
		ll.addView(tw, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		FrameLayout fl = new FrameLayout(context);
		fl.setId(android.R.id.tabcontent);
		fl.setPadding(pad, pad, pad, pad);
		ll.addView(fl, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		tabHost.addView(ll,new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		tabHost.setup();

	}
	/**
	 * Adds a tab page.
	 *Title - The page title.
	 *View - The page content. Usually the view should be a panel containing other views.
	 */
	public void AddTab2(String Title, View View) {
		if (getObject().getCurrentTabTag().equals("~temp"))
			getObject().clearAllTabs();
		MyContentFactory m = new MyContentFactory(View);
		TabSpec ts = getObject().newTabSpec("");
		ts.setContent(m);
		ts.setIndicator(Title);
		getObject().addTab(ts);
	}
	/**
	 * Adds a tab page.
	 *Title - The page title.
	 *LayoutFile - A layout file describing the page layout.
	 *Example:<code>
	 *TabHost1.AddTab("Page 1", "page1.bal")</code>
	 */
	@RaisesSynchronousEvents
	public void AddTab(BA ba, String Title, String LayoutFile) throws Exception {
		AddTab2(Title, createPanelForLayoutFile(ba, LayoutFile));
	}
	private View createPanelForLayoutFile(BA ba, String LayoutFile) throws Exception {
		PanelWrapper pw = new PanelWrapper();
		pw.Initialize(ba, "");
		int yfix = 84;
		if (BA.applicationContext.getApplicationInfo().targetSdkVersion >= 11 && VERSION.SDK_INT >= 11
				&& Common.GetDeviceLayoutValues(ba).getApproximateScreenSize() < 5) {
			yfix = 68;
		}
		pw.getObject().setLayoutParams(new ViewGroup.LayoutParams(getWidth() - Common.DipToCurrent(20),
				getHeight() - Common.DipToCurrent(yfix)));
		pw.LoadLayout(LayoutFile, ba);
		return pw.getObject();
	}
	/**
	 * Adds a tab page. The tab title includes an icon.
	 *Title - The page title.
	 *DefaultBitmap - The icon that will be drawn when the page is not selected.
	 *SelectedBitmap - The icon that will be drawn when the page is selected.
	 *View - The page content. Usually the view should be a panel containing other views.
	 */
	public void AddTabWithIcon2(String Title, Bitmap DefaultBitmap, Bitmap SelectedBitmap, View View) {
		if (getObject().getCurrentTabTag().equals("~temp"))
			getObject().clearAllTabs();
		MyContentFactory m = new MyContentFactory(View);
		TabSpec ts = getObject().newTabSpec("");
		ts.setContent(m);
		anywheresoftware.b4a.objects.drawable.BitmapDrawable bd1, bd2;
		bd1 = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
		bd1.Initialize(DefaultBitmap);
		bd2 = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
		bd2.Initialize(SelectedBitmap);
		StateListDrawable sd = new StateListDrawable();
		sd.addState(new int[] {anywheresoftware.b4a.objects.drawable.StateListDrawable.State_Selected},
				bd2.getObject());
		sd.addState(new int[0], bd1.getObject());
		ts.setIndicator(Title, sd);
		getObject().addTab(ts);
	}
	/**
	 * Adds a tab page. The tab title includes an icon.
	 *Title - The page title.
	 *DefaultBitmap - The icon that will be drawn when the page is not selected.
	 *SelectedBitmap - The icon that will be drawn when the page is selected.
	 *LayoutFile - A layout file describing the page layout.
	 *Example:<code>
	 *Dim bmp1, bmp2 As Bitmap
	 *bmp1 = LoadBitmap(File.DirAssets, "ic.png")
	 *bmp2 = LoadBitmap(File.DirAssets, "ic_selected.png")
	 *TabHost1.AddTabWithIcon("Page 1", bmp1, bmp2,"tabpage1.bal")</code>
	 */
	@RaisesSynchronousEvents
	public void AddTabWithIcon(BA ba, String Title, Bitmap DefaultBitmap, Bitmap SelectedBitmap, 
			String LayoutFile) throws Exception {
		AddTabWithIcon2(Title, DefaultBitmap, SelectedBitmap, createPanelForLayoutFile(ba, LayoutFile));
	}
	/**
	 * Gets or sets the current tab.
	 *Example:<code>
	 *TabHost1.CurrentTab = (TabHost1.CurrentTab + 1) Mod TabHost1.TabCount 'switch to the next tab.
	 *</code>
	 */
	public int getCurrentTab() {
		return getObject().getCurrentTab();
	}
	@RaisesSynchronousEvents
	public void setCurrentTab(int Index) {
		getObject().setCurrentTab(Index);
	}
	/**
	 * Returns the number of tab pages.
	 */
	public int getTabCount() {
		return getObject().getTabWidget().getTabCount();
	}
	private static class MyContentFactory implements TabHost.TabContentFactory {
		private View view;
		public MyContentFactory(View view) {
			this.view = view;
		}
		@Override
		public View createTabContent(String tag) {
			return view;
		}
		
	}
	@Hide
	public static class MyTabHost extends TabHost {

		public MyTabHost(Context context) {
			super(context, null);
		}
		
	}
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		boolean firstTime = false;
		if (prev == null) {
			firstTime = true;
			prev = ViewWrapper.buildNativeView((Context)tag, MyTabHost.class, props, designer);
		}
		TabHost th = (TabHost)ViewWrapper.build(prev, props, designer);
		if (designer && firstTime) {
			initializeTabWidget((Context)tag, th);
			TextView v = new TextView((Context)tag);
			v.setText("This is an example page.\nTab pages should be added programmatically.");
			for (int i = 1;i <= 3;i++) {
				MyContentFactory m = new MyContentFactory(v);
				TabSpec ts = th.newTabSpec("");
				ts.setContent(m);
				ts.setIndicator("Page " + i);
				th.addTab(ts);
			}
		}
		return th;
	}


}
