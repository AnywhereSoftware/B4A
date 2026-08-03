
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
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.text.InputFilter;
import android.text.method.NumberKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.BALayout.LayoutParams;
import anywheresoftware.b4a.keywords.Common;

/**
 * IME includes several utilities that will you help you manage the soft keyboard.
 *A tutorial with a working example is available <link>here|https://www.b4x.com/android/forum/threads/handle-the-soft-keyboard-with-the-ime-library.14832/</link>.
 */
@ShortName("IME")
@Events(values={"HeightChanged (NewHeight As Int, OldHeight As Int)", "InsetsChanged",
"HandleAction As Boolean"})
@Version(2.01f)
@ActivityObject
@DependsOn(values= {"androidx.core:core", "androidx.collection:collection"})
public class IME {
	private static boolean manualEdgeToEdge;
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

	private static boolean isEdgeToEdgeOptedOut(Activity activity) {
		if (Build.VERSION.SDK_INT < 35) {
			return false;
		}

		TypedArray a = activity.getTheme().obtainStyledAttributes(
				new int[] {
						android.R.attr.windowOptOutEdgeToEdgeEnforcement
				});

		try {
			return a.getBoolean(0, false);
		} finally {
			a.recycle();
		}
	}
	@Hide
	public static void enableEdgeToEdge(Activity activity) {
		manualEdgeToEdge = true;
		Window window = activity.getWindow();	
		window.getDecorView();
		WindowCompat.setDecorFitsSystemWindows(window, false);
		window.setStatusBarColor(0);
		window.setNavigationBarColor(0);
		if (Build.VERSION.SDK_INT >= 28) {
			int newMode = (Build.VERSION.SDK_INT >= 30) ? 3 : 1;
			WindowManager.LayoutParams attrs = window.getAttributes();
			if (attrs.layoutInDisplayCutoutMode != newMode) {
				attrs.layoutInDisplayCutoutMode = newMode;
				window.setAttributes(attrs);
			} 
		} 
		if (Build.VERSION.SDK_INT >= 29) {
			window.setStatusBarContrastEnforced(false);
			window.setNavigationBarContrastEnforced(false);
		} 
	}
	/**
	 * Tests whether activity is running in edge to edge mode, based on targetSdkVersion, device version and windowOptOutEdgeToEdgeEnforcement flag.
	 */
	public boolean IsEdgeToEdge(BA ba) {
		if (manualEdgeToEdge)
			return true;
		int androidVersion = Build.VERSION.SDK_INT;
		int targetSdkVersion = BA.applicationContext.getApplicationInfo().targetSdkVersion;
		if (androidVersion >= 36 && targetSdkVersion >= 36)
			return true;
		if (androidVersion >= 35 && targetSdkVersion>= 35) {
			return !isEdgeToEdgeOptedOut(ba.activity);
		}
		return false;
	}
	/**
	 * Enables the HeightChanged and InsetsChanged events. The InsetsChanged event is only raised in edge to edge mode.
	 */
	public void AddHeightChangedEvent(BA ba) {
		if (IsEdgeToEdge(ba)) {
			addInsetListener(ba);
			return;
		}
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
	/**
	 * Returns the action bar height, or 0 if no action bar.
	 */
	public int GetActionBarHeight(BA ba) {
		int id = ba.context.getResources().getIdentifier("action_bar", "id", "android");
		if (id == 0)
			return 0;
		View actionBar = ba.activity.getWindow().getDecorView().findViewById(id);
		if (actionBar == null)
			return 0;
		return ((ViewGroup.LayoutParams)actionBar.getLayoutParams()).height;
	}

	private void addInsetListener(final BA ba) {
		ViewCompat.setOnApplyWindowInsetsListener(ba.vg, new OnApplyWindowInsetsListener() {
			int lastHeight = -1;
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View arg0, WindowInsetsCompat windowInsets) {
				if (lastHeight == -1)
					lastHeight = GetContentRect(ba).height();
				Insets ins = ViewCompat.getRootWindowInsets(ba.vg).getInsets(Type.systemBars() | Type.displayCutout() | Type.ime());
				int actionBar = GetActionBarHeight(ba);
				int newHeight = ba.vg.getHeight() - ins.bottom - ins.top - actionBar;
					ba.raiseEventFromUI(null, eventName + "_insetschanged");
				if (newHeight != lastHeight) {
					ba.raiseEventFromUI(null, eventName + "_heightchanged", newHeight, lastHeight);
				}
				lastHeight = newHeight;
				return windowInsets;
			}
		});
	}
	/**
	 * Updates the relative width and height for %x and %y calculations.
	 */
	public void UpdatePercentageReference(BA ba, int Width, int Height) {
		ba.referenceSize[0] = Width;
		ba.referenceSize[1] = Height;
	}
	
	/**
	 * Returns the content area after excluding system bars, display cutouts and the ActionBar.
	 */
	public Rect GetContentRect(BA ba) {
		if (IsEdgeToEdge(ba) == false)
			return new Rect(0, 0, ba.vg.getWidth(), ba.vg.getHeight());
		Insets ins = ViewCompat.getRootWindowInsets(ba.vg).getInsets(Type.systemBars() | Type.displayCutout());
		int actionBar = GetActionBarHeight(ba);
		return new Rect(ins.left, ins.top + actionBar, ba.vg.getWidth() - ins.right, ba.vg.getHeight() - ins.bottom);
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
