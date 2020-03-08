
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

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.InputFilter;
import android.text.method.NumberKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.BALayout.LayoutParams;
import anywheresoftware.b4a.keywords.Common;

/**
 * IME includes several utilities that will you help you manage the soft keyboard.
 *A tutorial with a working example is available <link>here|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/14832-handle-soft-keyboard-ime-library.html</link>.
 */
@ShortName("IME")
@Events(values={"HeightChanged (NewHeight As Int, OldHeight As Int)",
		"HandleAction As Boolean"})
@Version(1.10f)
@ActivityObject
public class IME {
	private String eventName;
	/**
	 * Initializes the object and sets the subs that will handle the events.
	 */
	public void Initialize(String EventName) {
		this.eventName = EventName.toLowerCase(BA.cul);
	}
	/**
	 * Hides the soft keyboard if it is visible.
	 */
	public void HideKeyboard(BA ba) {
		InputMethodManager imm = (InputMethodManager)BA.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(ba.vg.getWindowToken(), 0);
	}
	/**
	 * Sets the focus to the given view and opens the soft keyboard.
	 *The keyboard will only show if the view has received the focus.
	 */
	public void ShowKeyboard(View View) {
		View.requestFocus();
		InputMethodManager imm = (InputMethodManager)BA.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(View, 0);
	}
	/**
	 * Adds the HandleAction event to the given EditText.
	 */
	public void AddHandleActionEvent(final EditText EditText, final BA ba) {
		EditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				Boolean b =  (Boolean)ba.raiseEvent(EditText, eventName + "_handleaction");
				if (b != null && b == true)
					return true;
				else
					return false;
			}
			
		});
	}
	/**
	 * Sets a filter that limits the maximum length to the specified value.
	 */
	public void SetLengthFilter(EditText EditText, int MaxLength) {
		EditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MaxLength)});
	}
	/**
	 * Sets a custom filter.
	 *EditText - The target EditText.
	 *DefaultInputType - Sets the keyboard mode.
	 *AcceptedCharacters - The accepted characters.
	 *Example: Create a filter that will accept IP addresses (numbers with multiple dots)
	 *<code>
	 *IME.SetCustomFilter(EditText1, EditText1.INPUT_TYPE_NUMBERS, "0123456789.")</code>
	 */
	public void SetCustomFilter(EditText EditText, final int DefaultInputType, final String AcceptedCharacters) {
		EditText.setKeyListener(new NumberKeyListener() {

			@Override
			protected char[] getAcceptedChars() {
				return AcceptedCharacters.toCharArray();
			}

			@Override
			public int getInputType() {
				return DefaultInputType;
			}
			
		});
	}
	/**
	 * Enables the HeightChanged event. This event is raised when the soft keyboard state changes.
	 *You can use this event to resize other views to fit the new screen size.
	 *Note that this event will not be raised in full screen activities (an Android limitation).
	 */
	public void AddHeightChangedEvent(BA ba) {
		if (ba.vg.getParent() instanceof BALayout)
			return;
		ExtendedBALayout e = new ExtendedBALayout(ba.context, eventName, ba);
		ba.activity.setContentView(e);
		BALayout.LayoutParams lp = new BALayout.LayoutParams();
		lp.height = ba.vg.getLayoutParams().height;
		lp.width = ba.vg.getLayoutParams().width;
		ba.vg.setLayoutParams(lp);
		e.addView(ba.vg);
		ba.activity.getWindow().setSoftInputMode(ba.activity.getWindow().getAttributes().softInputMode | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	
	static class ExtendedBALayout extends BALayout {
		private int lastHeight = -1;
		private final String eventName;
		private final BA ba;
		public ExtendedBALayout(Context context, String EventName, BA ba) {
			super(context);
			this.eventName = EventName.toLowerCase(BA.cul);
			this.ba = ba;
		}
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int height = MeasureSpec.getSize(heightMeasureSpec);
			if (height != lastHeight && lastHeight != -1) {
				ba.raiseEventFromUI(null, eventName + "_heightchanged", height, lastHeight);
			}
			lastHeight = height;
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);       
		}

	}

}
