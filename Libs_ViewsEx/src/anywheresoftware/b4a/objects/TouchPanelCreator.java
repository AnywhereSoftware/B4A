
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


import android.content.Context;
import android.view.MotionEvent;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
@ShortName("TouchPanelCreator")
@ActivityObject
@Events(values={"OnInterceptTouchEvent (Action As Int, X As Float, Y As Float, MotionEvent As Object) As Boolean",
	"OnTouchEvent (Action As Int, X As Float, Y As Float, MotionEvent As Object) As Boolean"})
/**
 * Creates a panel that exposes the following two internal events: OnTouchEvent and OnInterceptTouchEvent.
 * With these events it is possible to handle the touch events before they reach the child views.
 */
public class TouchPanelCreator {

	public PanelWrapper CreateTouchPanel(BA ba, String EventName) {
		PanelWrapper pw = new PanelWrapper();
		pw.setObject(new TouchView(ba.context, ba, EventName.toLowerCase(BA.cul)));
		return pw;
	}
	@Hide
	public static class TouchView extends BALayout {
		private final BA ba;
		private final String eventName;
		public TouchView(Context context, BA ba, String eventName) {
			super(context);
			this.ba = ba;
			this.eventName = eventName;
		}
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			Object b = ba.raiseEvent(this, eventName + "_onintercepttouchevent", ev.getAction(), ev.getX(), ev.getY(), ev);
			if (b != null)
				return ((Boolean)b).booleanValue();
			return false;
		}
		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			Object b = ba.raiseEvent(this, eventName + "_ontouchevent", ev.getAction(), ev.getX(), ev.getY(), ev);
			if (b != null)
				return ((Boolean)b).booleanValue();
			return false;
		}

	}
}
