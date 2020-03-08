
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

import android.os.Build;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

/**
 * This library includes several accessibility related methods. 
 *The SetNextFocus methods allow you to explicitly set the focus order. This order is important when the user navigates your
 *application with a directional controller (such as D-Pad).
 *SetContentDescription sets the content that will be used by accessibility services such as TalkBack to describe the interface.
 */
@Version(1.02f)
@ShortName("Accessibility")
public class Accessibility {
	/**
	 * Sets the next view that will get the focus when the user presses on the up key (and ThisView is focused).
	 */
	public static void SetNextFocusUp(View ThisView, View NextView) {
		ThisView.setNextFocusUpId(NextView.getId());
	}
	/**
	 * Sets the next view that will get the focus when the user presses on the down key (and ThisView is focused).
	 * Example:<code>
	 *Dim Access As Accessibility
	 *Access.SetNextFocusDown(Button1, Button2) 'When the focus is on Button1 and the user presses on the down key,
	 *'the focus will move to Button2.
	 *</code>
	 */
	public static void SetNextFocusDown(View ThisView, View NextView) {
		ThisView.setNextFocusDownId(NextView.getId());
	}
	/**
	 * Sets the next view that will get the focus when the user presses on the right key (and ThisView is focused).
	 */
	public static void SetNextFocusRight(View ThisView, View NextView) {
		ThisView.setNextFocusRightId(NextView.getId());
	}
	/**
	 * Sets the next view that will get the focus when the user presses on the left key (and ThisView is focused).
	 */
	public static void SetNextFocusLeft(View ThisView, View NextView) {
		ThisView.setNextFocusLeftId(NextView.getId());
	}
	/**
	 * Sets the view's description. This text will be used by accessibility services to describe the view.
	 */
	public static void SetContentDescription(View View, CharSequence Content) {
		View.setContentDescription(Content);
	}
	/**
	 * Returns the user set font scale. The user can adjust this scale in the device Settings.
	 *This scale is applied automatically to all text based views.
	 */
	public static float GetUserFontScale() {
		return BA.applicationContext.getResources().getConfiguration().fontScale;
	}
	/**
	 * Same as Accessibility type but with a typo... Use Accessibility instead.
	 */
	@ShortName("Accessiblity")
	public static class Accessibility2 extends Accessibility {
		
	}
	
}
