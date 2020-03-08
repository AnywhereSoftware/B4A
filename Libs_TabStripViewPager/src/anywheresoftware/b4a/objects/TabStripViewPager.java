
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
 
 package anywheresoftware.b4a.objects;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.DesignerProperties;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Property;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.Common.DesignerCustomView;
import anywheresoftware.b4a.objects.collections.Map;

import com.astuetz.PagerSlidingTabStrip;

@ShortName("TabStrip")
@Version(1.20f)
@DependsOn(values={"com.android.support:support-v4"})
@Events(values={"PageSelected (Position As Int)"})
@DesignerProperties(values = {
		@Property(key="TabHeight", displayName="Tab Height", fieldType="Int", defaultValue="50"),
		@Property(key="IndicatorHeight", displayName="Indicator Height", fieldType="Int", defaultValue="8"),
		@Property(key="UnderlineHeight", displayName="Underline Height", fieldType="Int", defaultValue="2"),
		@Property(key="TabTextSize", displayName="Tab Text Size", fieldType="Int", defaultValue="12"),
		@Property(key="TabTextColor", displayName="Tab Text Color", fieldType="Color", defaultValue="0xFF666666"),
		@Property(key="IndicatorColor", displayName="Indicator Color", fieldType="Color", defaultValue="0xFF666666"),
		@Property(key="DividerColor", displayName="Divider Color", fieldType="Color", defaultValue="0x1A000000"),
		@Property(key="UnderlineColor", displayName="Underline Color", fieldType="Color", defaultValue="0x1A000000")
})
public class TabStripViewPager implements DesignerCustomView {
	@Hide
	public ViewPager vp;
	@Hide
	public PagerSlidingTabStrip tabStrip;
	private BA ba;
	private String eventName;
	private PanelWrapper base;
	private int tabsHeight;
	@Hide
	public final ArrayList<ViewGroup> pages = new ArrayList<ViewGroup>();
	@Hide
	public final ArrayList<CharSequence>titles = new ArrayList<CharSequence>();
	@Hide
	@Override
	public void _initialize(BA ba, Object activityClass, String EventName) {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
	}
	@Override
	public void DesignerCreateView(PanelWrapper base, LabelWrapper lw, Map props) {
		tabStrip = new PagerSlidingTabStrip(ba.context);
		tabsHeight = Common.DipToCurrent((Integer)props.Get("TabHeight"));
		tabStrip.setTextColor((Integer)props.Get("TabTextColor"));
		tabStrip.setTextSize((Integer)props.Get("TabTextSize"));
		tabStrip.setDividerColor((Integer)props.Get("DividerColor"));
		tabStrip.setIndicatorColor((Integer)props.Get("IndicatorColor"));
		tabStrip.setUnderlineColor((Integer)props.Get("UnderlineColor"));
		tabStrip.setScrollOffset(Common.DipToCurrent(52));
		tabStrip.setDividerPadding(Common.DipToCurrent(12));
		tabStrip.setTabPaddingLeftRight(Common.DipToCurrent(24));
		tabStrip.setIndicatorHeight(Common.DipToCurrent((Integer)props.Get("IndicatorHeight")));
		tabStrip.setUnderlineHeight(Common.DipToCurrent((Integer)props.Get("UnderlineHeight")));
		
		vp = new ViewPager(ba.context);
		vp.setOffscreenPageLimit(10);
		vp.setAdapter(new B4APageAdapter());
		this.base = base;
		base.AddView(tabStrip, 0, 0, base.getWidth(), tabsHeight);
		base.AddView(vp, 0, tabsHeight, base.getWidth(), base.getHeight() - tabsHeight);
		if (ba.subExists(eventName + "_pageselected")) {
			tabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

				@Override
				public void onPageScrollStateChanged(int arg0) {
					
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					
				}

				@Override
				public void onPageSelected(int arg0) {
					ba.raiseEventFromUI(TabStripViewPager.this, eventName + "_pageselected", arg0);
				}
				
			});
		}
	}
	/**
	 * Scrolls to the specified page.
	 */
	public void ScrollTo(int PagePosition, boolean SmoothScroll) {
		vp.setCurrentItem(PagePosition, SmoothScroll);
	}
	/**
	 * Gets the current page index.
	 */
	public int getCurrentPage() {
		return vp.getCurrentItem();
	}
	@RaisesSynchronousEvents
	public void LoadLayout(String LayoutFile, CharSequence TabText) throws Exception {
		PanelWrapper pw = new PanelWrapper();
		pw.Initialize(ba, "");
		base.AddView(pw.getObject(), 0, 0, base.getWidth(), base.getHeight() - tabsHeight);
		pw.LoadLayout(LayoutFile, ba);
		pw.RemoveView();
		pages.add(pw.getObject());
		titles.add(TabText);
		vp.getAdapter().notifyDataSetChanged();
		if (pages.size() == 1) {
			tabStrip.setViewPager(vp);
		} else {
			tabStrip.notifyDataSetChanged();
		}
	}
	@Hide
	public void resetAdapter() {
		vp = new ViewPager(ba.context);
		vp.setOffscreenPageLimit(10);
		vp.setAdapter(new B4APageAdapter());
		base.RemoveViewAt(1);
		base.AddView(vp, 0, tabsHeight, base.getWidth(), base.getHeight() - tabsHeight);
		tabStrip.setViewPager(vp);
	}
	
	class B4APageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return pages.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		@Override
		public void destroyItem (ViewGroup container, int position, Object object) {
			ViewGroup page = pages.get(position);
			container.removeView(page);
		}
		@Override
		public Object instantiateItem (ViewGroup container, int position) {
			ViewGroup vg = pages.get(position);
			vg.setLayoutParams(new ViewGroup.LayoutParams(base.getWidth(), vp.getHeight()));
			container.addView(vg);
			return vg;
			
		}
		@Override
		public CharSequence getPageTitle(int position) {
			return titles.get(position);
		}
		
		
	}

	
	
}
