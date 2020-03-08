
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;

public class BALayout extends ViewGroup {
	public static float scale = 0f;
	private static float deviceScale = 0f;
	public static final int LEFT = 0, RIGHT = 1, BOTH = 2, TOP = 0, BOTTOM = 1;
	public static boolean disableAccessibility = false;
	public BALayout(Context context) {
		super(context);
		
	}
	public static void setDeviceScale(float scale) {
		BALayout.deviceScale = scale;
	}
	public static void setUserScale(float userScale) {
		if (Float.compare(deviceScale, userScale) == 0)
			scale = 1f;
		else
			scale = deviceScale / userScale;
	}
	public static float getDeviceScale() {
		return deviceScale;
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				if (child.getLayoutParams() instanceof LayoutParams) {
					LayoutParams lp =
						(LayoutParams) child.getLayoutParams();
					child.layout(lp.left ,lp.top, 
							lp.left + child.getMeasuredWidth(),
							lp.top + child.getMeasuredHeight());
				}
				else {
					child.layout(0, 0, getLayoutParams().width, getLayoutParams().height);
				}

			}
		}
		//Log.w("B4A", "onLayout: " + r + ", " + b);
	}
	@Override
	public void addChildrenForAccessibility(ArrayList<View> c) {
		if (disableAccessibility)
			return;
		super.addChildrenForAccessibility(c);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//Log.w("B4A", "measure: " + MeasureSpec.getSize(widthMeasureSpec) + " " + MeasureSpec.getSize(heightMeasureSpec));
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(resolveSize(getLayoutParams().width, widthMeasureSpec),
				resolveSize(getLayoutParams().height, heightMeasureSpec));
	}
	
	public static class LayoutParams extends ViewGroup.LayoutParams {
		public int left;
		public int top;
		public LayoutParams(int left, int top, int width, int height) {
			super(width, height);
			this.left = left;
			this.top = top;
		}
		public LayoutParams() {
			super(0,0);
		}
		//returns the map that is sent to the desktop designer when a control was updated in the device.
		public HashMap<String, Object> toDesignerMap() {
			HashMap<String, Object> props = new HashMap<String, Object>();
			props.put("left", (int)Math.round(left / scale));
			props.put("top", (int)Math.round(top / scale));
			props.put("width", (int)Math.round(width / scale));
			props.put("height", (int)Math.round(height / scale));
			return props;
		}
		/**
		 * converts the user scale to the device scale.
		 */
		public void setFromUserPlane(int left, int top, int width, int height) {
			this.left = (int)Math.round(left * scale);
			this.top = (int)Math.round(top * scale);
			this.width = width > 0 ? (int)Math.round(width * scale) : width;
			this.height = height > 0 ?(int)Math.round(height * scale) : height;
		}
		
		
	}
}