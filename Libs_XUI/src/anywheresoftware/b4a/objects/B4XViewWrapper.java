
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URLEncoder;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.CustomClass;
import anywheresoftware.b4a.BA.CustomClasses;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.IterableList;
import anywheresoftware.b4a.BA.Pixel;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.BALayout.LayoutParams;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.constants.TypefaceWrapper;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper;
import anywheresoftware.b4a.objects.drawable.ColorDrawable;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;
import anywheresoftware.b4a.objects.drawable.ColorDrawable.GradientDrawableWithCorners;
import anywheresoftware.b4a.objects.streams.File;

@CustomClasses(values = {
		@CustomClass(fileNameWithoutExtension="customview", name="Custom View (XUI)")	
	})
@Version(2.00f)
@ShortName("B4XView")
@ActivityObject
public class B4XViewWrapper extends AbsObjectWrapper<Object>{
	public static final int TOUCH_ACTION_DOWN = 0;
	public static final int TOUCH_ACTION_UP = 1;
	public static final int TOUCH_ACTION_MOVE = 2;
	public static final int TOUCH_ACTION_MOVE_NOTOUCH = 100;
	private ConcreteViewWrapper nodeWrapper = new ConcreteViewWrapper();
	private ConcreteViewWrapper asViewWrapper() {
		nodeWrapper.setObject((View)getObject());
		return nodeWrapper;
	}
	@Override
	public void setObject(Object o) {
		if (o instanceof ObjectWrapper) {
			o = ((ObjectWrapper<?>)o).getObjectOrNull();
		}

		super.setObject(o);
	}
	@Hide
	public View getViewObject() {
		return (View)getObject();
	}
	private PanelWrapper asPanelWrapper() {
		View v = getViewObject();
		if (v instanceof ViewGroup)
			return (PanelWrapper)AbsObjectWrapper.ConvertToWrapper(new PanelWrapper(), v);
		else
			throw typeDoesNotMatch();
	}
	public boolean getVisible() {
		return asViewWrapper().getVisible();
	}
	public void setVisible(boolean b) {
		asViewWrapper().setVisible(b);
	}
	public boolean getEnabled() {
		return asViewWrapper().getEnabled();
	}
	public void setEnabled(boolean b) {
		asViewWrapper().setEnabled(b);
	}
	public void setLeft(@Pixel int d) {
		asViewWrapper().setLeft(d);
	}
	public void setTop(@Pixel int d) {
		asViewWrapper().setTop(d);
	}
	public void setWidth(@Pixel int d) {
		asViewWrapper().setWidth(d);
	}
	public void setHeight(@Pixel int d) {
		asViewWrapper().setHeight(d);
	}
	public int getLeft() {
		return asViewWrapper().getLeft();
	}
	public int getTop() {
		return asViewWrapper().getTop();
	}
	public int getWidth() {
		return asViewWrapper().getWidth();
	}
	public int getHeight() {
		return asViewWrapper().getHeight();
	}
	/**
	 * Sets the view size and position.
	 *Duration - Animation duration in milliseconds. Pass 0 to make the change immediately.
	 */
	public void SetLayoutAnimated(int Duration, @Pixel int Left, @Pixel int Top, @Pixel int Width, @Pixel int Height) {
		View target = getViewObject();
		BALayout.LayoutParams lp = (LayoutParams)target.getLayoutParams();
		if (lp == null) {
			asViewWrapper().SetLayout(Left, Top, Width, Height);
			return;
		}
		int pLeft = lp.left, pTop = lp.top, pWidth = lp.width, pHeight = lp.height;
		asViewWrapper().SetLayout(Left, Top, Width, Height);
		lp = (LayoutParams) target.getLayoutParams();
		if (android.os.Build.VERSION.SDK_INT >= 11 && Duration > 0 && pWidth >= 0 && pHeight >= 0) {
			target.setPivotX(0);
			target.setPivotY(0);
			@SuppressWarnings("unchecked")
			WeakReference<AnimatorSet> wr = (WeakReference<AnimatorSet>) AbsObjectWrapper.getExtraTags(target).get("prevSet");
			AnimatorSet prevSet = (AnimatorSet) (wr != null ? wr.get() : null);
			if (prevSet != null && prevSet.isRunning())
				prevSet.end();
			AnimatorSet set = new AnimatorSet();
			AbsObjectWrapper.getExtraTags(target).put("prevSet", new WeakReference<AnimatorSet>(set));
			set.playTogether(
					ObjectAnimator.ofFloat(target, "translationX", (pLeft - lp.left), 0),
					ObjectAnimator.ofFloat(target, "translationY",(pTop - lp.top), 0),
					ObjectAnimator.ofFloat(target, "scaleX",pWidth / (float) lp.width, 1),
					ObjectAnimator.ofFloat(target, "scaleY",pHeight / (float)lp.height, 1));
			set.setDuration(Duration);
			set.setInterpolator(new LinearInterpolator());
			set.start();
		}
	}

	/**
	 * Returns the parent. The object returned will be uninitialized if there is no parent.
	 */
	public B4XViewWrapper getParent() {
		return (B4XViewWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(),asViewWrapper().getParent());
	}
	/**
	 * Removes the view from its parent.
	 */
	public void RemoveViewFromParent() {
		asViewWrapper().RemoveView();
	}
	public boolean RequestFocus() {
		return asViewWrapper().RequestFocus();
	}
	public void SetVisibleAnimated(int Duration, boolean Visible) {
		asViewWrapper().SetVisibleAnimated(Duration, Visible);
	}
	/**
	 * Gets or sets the ProgressBar progress. Value should be between 0 to 100.
	 */
	public void setProgress(int i) {
		ProgressBar pb = (ProgressBar)getObject();
		pb.setProgress(i);
	}
	public int getProgress() {
		return ((ProgressBar)getObject()).getProgress();
	}
	private LabelWrapper asLabelWrapper() {
		if (getObject() instanceof TextView)
			return (LabelWrapper)AbsObjectWrapper.ConvertToWrapper(new LabelWrapper(), getObject());
		throw typeDoesNotMatch();
	}
	
	public String getEditTextHint() {
		EditText e = (EditText)getObject();
		return String.valueOf(e.getHint() == null ? "" : e.getHint());
	}
	public void setEditTextHint(CharSequence s) {
		((EditText)getObject()).setHint(s);
	}
	/**
	 * Gets or sets the text. Supported types:  EditText, Label, Button, CheckBox, RadioButton and ToggleButton.
	 */
	public void setText(CharSequence s) {
		asLabelWrapper().setText(s);
	}
	public String getText() {
		return asLabelWrapper().getText();
	}
	/**
	 * Gets or sets the text color. Supported types:  EditText, Label, Button, CheckBox, RadioButton and ToggleButton.
	 */
	public void setTextColor(int c) {
		asLabelWrapper().setTextColor(c);
	}
	public int getTextColor() {
		return asLabelWrapper().getTextColor();
	}
	
	public void SetTextSizeAnimated(int Duration, float TextSize) {
		asLabelWrapper().SetTextSizeAnimated(Duration, TextSize);		
	}
	/**
	 * Gets or sets the text size. Supported types:  EditText, Label, Button, CheckBox, RadioButton and ToggleButton.
	 */
	public float getTextSize() {
		return asLabelWrapper().getTextSize();
	}
	public void setTextSize(float d) {
		asLabelWrapper().setTextSize(d);
	}
	/**
	 * Gets or sets the font (typeface and text size). Supported types: EditText, Label, Button, CheckBox, RadioButton and ToggleButton.
	 */
	public B4XFont getFont() {
		LabelWrapper lw = asLabelWrapper();
		return XUI.CreateFont(lw.getTypeface(), lw.getTextSize());
	}
	public void setFont(B4XFont f) {
		LabelWrapper lw = asLabelWrapper();
		lw.setTextSize(f.textSize);
		lw.setTypeface(f.typeface);
	}

	public void SetTextAlignment(String Vertical, String Horizontal)  {
		int v;
		if (Vertical.equals("TOP"))
			v = Gravity.TOP;
		else if (Vertical.equals("CENTER"))
			v = Gravity.CENTER_VERTICAL;
		else
			v = Gravity.BOTTOM;
		int h;
		if (Horizontal.equals("LEFT"))
			h = Gravity.LEFT;
		else if (Horizontal.equals("CENTER"))
			h = Gravity.CENTER_HORIZONTAL;
		else
			h = Gravity.RIGHT;
		asLabelWrapper().setGravity(v | h);

	}

	/**
	 * Gets or sets the checked state. Supported types: CheckBox, RadioButton, ToggleButton and Switch.
	 */
	public void setChecked(boolean b) {
		((CompoundButton)getObject()).setChecked(b);
	}
	public boolean getChecked() {
		return ((CompoundButton)getObject()).isChecked();
	}
	/**
	 * Returns an iterator that iterates over all the child views including views that were added to other child views.
	 *Make sure to check the view type as it might return subviews as well.
	 *Example:<code>
	 *For Each v As B4XView In Panel1.GetAllViewsRecursive
	 *	...
	 *Next</code>
	 *Supported types: Activity, Panel
	 */
	public IterableList GetAllViewsRecursive() {
		return asPanelWrapper().GetAllViewsRecursive();
	}
	/**
	 * Loads the layout file. Supported types: Activity, Panel
	 */
	@RaisesSynchronousEvents
	public void LoadLayout(String LayoutFile, BA ba) throws Exception {
		asPanelWrapper().LoadLayout(LayoutFile, ba);
	}
	/**
	 * Returns the view at the given index. Supported types: Activity, Panel.
	 */
	public B4XViewWrapper GetView(int Index) {
		return (B4XViewWrapper) AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(),asPanelWrapper().GetView(Index).getObject());
	}
	/**
	 * Adds a view. Supported types: Panel.
	 */
	public void AddView(View View, @Pixel int Left,  @Pixel int Top,  @Pixel int Width,  @Pixel int Height) {
		asPanelWrapper().AddView(View, Left, Top, Width, Height);
	}
	/**
	 * Removes all views. Supported types: Activity, Panel.
	 */
	public void RemoveAllViews() {
		asPanelWrapper().RemoveAllViews();
	}
	/**
	 * Returns the number of direct child views. Supported types: Activity, Panel.
	 */
	public int getNumberOfViews() {
		return asPanelWrapper().getNumberOfViews();
	}
	/**
	 *Captures the views appearance.
	 */
	public B4XBitmapWrapper Snapshot() {
		BitmapWrapper bw = new BitmapWrapper();
		ConcreteViewWrapper vw = asViewWrapper();
		bw.InitializeMutable(vw.getWidth(), vw.getHeight());
		CanvasWrapper cw = new CanvasWrapper();
		cw.Initialize2(bw.getObject());
		View v = getViewObject();
		int currentLeft, currentTop;
		if (v.getLayoutParams() instanceof BALayout.LayoutParams) {
			currentLeft = getLeft();
			currentTop = getTop();
		} else {
			currentLeft = 0;
			currentTop = 0;
		}
		
		int widthSpec = View.MeasureSpec.makeMeasureSpec(vw.getWidth(), View.MeasureSpec.EXACTLY);
		int heightSpec = View.MeasureSpec.makeMeasureSpec(vw.getHeight(), View.MeasureSpec.EXACTLY);
		v.measure(widthSpec, heightSpec);
		v.layout(0, 0, vw.getWidth(), vw.getHeight());
		vw.getObject().draw(cw.canvas);
		v.layout(currentLeft, currentTop, vw.getWidth(), vw.getHeight());
		if (v.getParent() instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup)v.getParent();
			int i = vg.indexOfChild(v);
			if (i > -1) {
				vg.removeViewAt(i);
				vg.addView(v, i);
			}
		}
		return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), bw.getObject());
	}
	/**
	 * Sets the bitmap as the view's background drawable. The gravity is set to CENTER.
	 */
	public void SetBitmap(Bitmap Bitmap) {
		asViewWrapper().SetBackgroundImageNew(Bitmap).setGravity(Gravity.CENTER);
	}
	public B4XBitmapWrapper GetBitmap() {
		B4XBitmapWrapper res = new B4XBitmapWrapper();
		Drawable d = getViewObject().getBackground();
		if (d instanceof BitmapDrawable)
			res.setObject(((BitmapDrawable)d).getBitmap());
		return res;
	}

	private RuntimeException typeDoesNotMatch() {
		return new RuntimeException("Type does not match (" + getObject().getClass() + ")");
	}

	/**
	 * Gets or sets the background color.
	 *Returns 0 if it is not possible to get the color.
	 */
	public void setColor(int Color) {
		asViewWrapper().setColor(Color);
	}
	private static Field solidColorField;
	public int getColor() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Drawable d = getViewObject().getBackground();
		if (d instanceof android.graphics.drawable.ColorDrawable) {
			return ((android.graphics.drawable.ColorDrawable)d).getColor();
		}
		else if (d instanceof GradientDrawableWithCorners) {
			return ((GradientDrawableWithCorners)d).color;
		}
		if (d instanceof GradientDrawable && (Build.VERSION.SDK_INT <= 28 || BA.applicationContext.getApplicationInfo().targetSdkVersion <= 28)) {
			ConstantState cs = d.getConstantState();
			Class<?> c = cs.getClass();
			if (c.getName().equals("android.graphics.drawable.GradientDrawable$GradientState")) {
				if (solidColorField == null) {
					try {
						solidColorField = c.getDeclaredField("mSolidColor");
					} catch (NoSuchFieldException e) {
						try {
							solidColorField = c.getDeclaredField("mSolidColors");
						} catch (NoSuchFieldException ee) {
							solidColorField = c.getDeclaredField("mColorStateList");
						}
					}
					solidColorField.setAccessible(true);
				}
				Object value = solidColorField.get(cs);
				String fieldName = solidColorField.getName();
				if (fieldName.equals("mSolidColor")) {
					return (Integer)value;
				} else {
					ColorStateList list = (ColorStateList) value;
					if (list != null)
						return list.getDefaultColor();
				}
			}
		}
		return 0;
	}
	/**
	 * Changes the background color with a transition animation between the FromColor and the ToColor colors.
	 *Duration - Animation duration measured in milliseconds.
	 */
	public void SetColorAnimated(int Duration, final int FromColor, final int ToColor) {
		if (android.os.Build.VERSION.SDK_INT >= 11 && Duration > 0) {
			final View target = getViewObject();
			final GradientDrawableWithCorners gdc;
			if (target.getBackground() instanceof GradientDrawableWithCorners)
				gdc = (GradientDrawableWithCorners) target.getBackground();
			else
				gdc = new GradientDrawableWithCorners();
			gdc.setColor(FromColor);
			target.setBackgroundDrawable(gdc);
			ValueAnimator anim = ValueAnimator.ofFloat(0, 1); 
			final int fa, ta, fr, tr, fg, tg, fb, tb;
			fa = Color.alpha(FromColor);
			fr = Color.red(FromColor);
			fg = Color.green(FromColor);
			fb = Color.blue(FromColor);
			ta = Color.alpha(ToColor);
			tr = Color.red(ToColor);
			tg = Color.green(ToColor);
			tb = Color.blue(ToColor);
			anim.setDuration(Duration);
			anim.setInterpolator(new LinearInterpolator());
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
				@Override public void onAnimationUpdate(ValueAnimator animation) {
					float p = animation.getAnimatedFraction();
					int clr = Color.argb((int) (fa + (ta-fa) * p),
							(int) (fr + (tr-fr) * p),
							(int) (fg + (tg-fg) * p),
							(int) (fb + (tb-fb) * p));
					gdc.setColor(clr);
					target.invalidate();
				}
			});
			anim.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					gdc.setColor(ToColor);
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
					
				}
			});
			anim.start();
		}
		else {
			asViewWrapper().setColor(ToColor);
		}
	}
	/**
	 * Sets the background drawable to a ColorDrawable with the given color and border.
	 */
	@SuppressWarnings("deprecation")
	public void SetColorAndBorder(int BackgroundColor,@Pixel int BorderWidth, int BorderColor, @Pixel int BorderCornerRadius) {
		ColorDrawable cd = new ColorDrawable();
		cd.Initialize2(BackgroundColor, BorderCornerRadius, BorderWidth, BorderColor);
		getViewObject().setBackgroundDrawable(cd.getObject());
	}
	private ScrollViewWrapper asVScrollViewWrapper() {
		return (ScrollViewWrapper)AbsObjectWrapper.ConvertToWrapper(new ScrollViewWrapper(), getObject());
	}
	private HorizontalScrollViewWrapper asHScrollViewWrapper() {
		return (HorizontalScrollViewWrapper)AbsObjectWrapper.ConvertToWrapper(new HorizontalScrollViewWrapper(), getObject());
	}
	/**
	 * Gets or sets the ScrollView vertical scroll position. Returns 0 for HorizontalScrollView.
	 */
	public int getScrollViewOffsetY() {
		if (getObject() instanceof ScrollView)
			return asVScrollViewWrapper().getScrollPosition();
		else
			return 0;
	}
	public void setScrollViewOffsetY(int d) {
		if (getObject() instanceof ScrollView)
			asVScrollViewWrapper().ScrollToNow(d);
	}
	/**
	 * Gets or sets the HorizontalScrollView horizontal scroll position. Return 0 for ScrollView.
	 */
	public int getScrollViewOffsetX() {
		if (getObject() instanceof HorizontalScrollView)
			return asHScrollViewWrapper().getScrollPosition();
		else
			return 0;
	}
	public void setScrollViewOffsetX(int d) {
		if (getObject() instanceof HorizontalScrollView)
			asHScrollViewWrapper().ScrollToNow(d);
	}

	private PanelWrapper getScrollViewPanel() {
		if (getObject() instanceof HorizontalScrollView)
			return asHScrollViewWrapper().getPanel();
		else if (getObject() instanceof ScrollView)
			return asVScrollViewWrapper().getPanel();
		throw typeDoesNotMatch();
	}
	/**
	 * Returns the ScrollView or HorizontalScrollView inner panel.
	 */
	public B4XViewWrapper getScrollViewInnerPanel() {
		return (B4XViewWrapper) AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(),getScrollViewPanel().getObject());
	}
	/**
	 * Gets or sets the ScrollView or HorizontalScrollView inner panel height.
	 */
	public int getScrollViewContentHeight() {
		return getScrollViewInnerPanel().getHeight();
	}

	public void setScrollViewContentHeight(int d) {
		getScrollViewInnerPanel().setHeight(d);
	}
	/**
	 * Gets or sets the ScrollView or HorizontalScrollView inner panel width.
	 */
	public int getScrollViewContentWidth() {
		return getScrollViewInnerPanel().getWidth();
	}
	public void setScrollViewContentWidth(int d) {
		getScrollViewInnerPanel().setWidth(d);
	}

	/**
	 * Gets or sets the view's tag object.
	 */
	public Object getTag() {
		return asViewWrapper().getTag();
	}
	public void setTag(Object o) {
		asViewWrapper().setTag(o);
	}
	/**
	 * Changes the Z order of this view and sends it to the back.
	 */
	public void SendToBack() {
		asViewWrapper().SendToBack();
	}
	/**
	 * Changes the Z order of this view and brings it to the front.
	 */
	public void BringToFront() {
		asViewWrapper().BringToFront();
	}
	/**
	 * Rotates the view with animation.
	 *Duration - Animation duration in milliseconds.
	 *Degree - Rotation degree.
	 */
	public void SetRotationAnimated(int Duration, float Degree) {
		float current = getRotation();
		View v = getViewObject();
		v.setRotation(Degree);
		v.setPivotX(getWidth() / 2);
		v.setPivotY(getHeight() / 2);
		Animator a = ObjectAnimator.ofFloat(v, "rotation", current, Degree).setDuration(Duration);
		a.setInterpolator(new LinearInterpolator());
		a.start();;
	}
	/**
	 * Gets or sets the view's rotation transformation (in degrees).
	 */
	public float getRotation() {
		return getViewObject().getRotation();
	}
	public void setRotation(float f) {
		View v = getViewObject();
		v.setRotation(f);
		v.setPivotX(getWidth() / 2);
		v.setPivotY(getHeight() / 2);
	}
	
	


	@ShortName("B4XBitmap")
	public static class B4XBitmapWrapper extends AbsObjectWrapper<Bitmap> {
		public double getWidth() {
			return getObject().getWidth();
		}
		public double getHeight() {
			return getObject().getHeight();
		}
		private BitmapWrapper asBitmapWrapper() {
			return (BitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new CanvasWrapper.BitmapWrapper(), getObject());
		}

		public void WriteToStream(OutputStream Out, int Quality, CompressFormat Format) {
			asBitmapWrapper().WriteToStream(Out, Quality, Format);
		}

		public B4XBitmapWrapper Resize(int Width, int Height, boolean KeepAspectRatio) {
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), asBitmapWrapper().Resize(Width, Height, KeepAspectRatio).getObject()); 
		}

		public B4XBitmapWrapper Rotate(int Degrees) {
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), asBitmapWrapper().Rotate(Degrees).getObject()); 
		}

		public B4XBitmapWrapper Crop(int Left, int Top, int Width, int Height) {
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), asBitmapWrapper().Crop(Left, Top, Width, Height).getObject());
		}
		/**
		 * Returns the bitmap scale. It will always be 1 in B4J and B4i.
		 */
		public float getScale() {
			return getObject().getDensity() / 160f;
		}
	}

	@ShortName("B4XFont")
	public static class B4XFont {
		@Hide public Typeface typeface;
		private float textSize;
		public float getSize() {
			return textSize;
		}
		public boolean getIsInitialized() {
			return typeface != null;
		}
		public TypefaceWrapper ToNativeFont() {
			return (TypefaceWrapper)AbsObjectWrapper.ConvertToWrapper(new TypefaceWrapper(), typeface);
		}

	}
	@ShortName("XUI")
	public static class XUI {
		public static final int Color_Black       = (0xFF000000);
		public static final int Color_DarkGray      = (0xFF444444);
		public static final int Color_Gray        = (0xFF888888);
		public static final int Color_LightGray      = (0xFFCCCCCC);
		public static final int Color_White       = (0xFFFFFFFF);
		public static final int Color_Red         = (0xFFFF0000);
		public static final int Color_Green       = (0xFF00FF00);
		public static final int Color_Blue        = (0xFF0000FF);
		public static final int Color_Yellow      = (0xFFFFFF00);
		public static final int Color_Cyan       = (0xFF00FFFF);
		public static final int Color_Magenta     = (0xFFFF00FF);
		public static final int Color_Transparent = (0); 

		public static B4XBitmapWrapper LoadBitmap(String Dir, String FileName) throws IOException  {
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), Common.LoadBitmap(Dir, FileName).getObject());
		}
		public static B4XBitmapWrapper LoadBitmapResize(String Dir, String FileName, int Width, int Height, boolean KeepAspectRatio) throws IOException {
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), Common.LoadBitmapResize(Dir, FileName, Width, Height, KeepAspectRatio).getObject());
		}
		public static String getDefaultFolder() {
			return File.getDirInternal();
		}
		public static void SetDataFolder(String AppName) {

		}
		public static boolean SubExists(BA ba, Object Component, String Sub, int NotUsed) throws IllegalArgumentException, SecurityException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
			return Common.SubExists(ba, Component, Sub);
		}
		public static int PaintOrColorToColor(Object Color) {
			return (Integer)Color;
		}
		public static B4XFont CreateFont(Typeface Typeface, float Size) {
			B4XFont f = new B4XFont();
			f.typeface = Typeface;
			f.textSize = Size;
			return f;
		}
		public static B4XFont CreateFont2(B4XFont B4XFont, float Size) {
			return CreateFont(B4XFont.typeface, Size);
		}
		public static B4XFont CreateDefaultFont(float Size) {
			return CreateFont(TypefaceWrapper.DEFAULT, Size);
		}
		public static B4XFont CreateDefaultBoldFont(float Size) {
			return CreateFont(TypefaceWrapper.DEFAULT_BOLD, Size);
		}
		public static B4XFont CreateFontAwesome(float Size) {
			return CreateFont(TypefaceWrapper.getFONTAWESOME(), Size);
		}
		public static B4XFont CreateMaterialIcons(float Size) {
			return CreateFont(TypefaceWrapper.getMATERIALICONS(), Size);
		}
		public static boolean getIsB4A() {
			return true;
		}
		public static boolean getIsB4i() {
			return false;
		}
		public static boolean getIsB4J() {
			return false;
		}
		public static float getScale() {
			return Common.Density;
		}
		
		public static B4XViewWrapper CreatePanel(BA ba, String EventName) {
			PanelWrapper p = new PanelWrapper();
			if (ba.eventsTarget != null && EventName.length() > 0) {
				if (ba.activity == null)
					throw new RuntimeException("Class must have an Activity context.");
				p.Initialize(ba, EventName);
			}
			else
				p.Initialize(ba.sharedProcessBA.activityBA.get(), EventName);
			return (B4XViewWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(), p.getObject());
		}
		public static int Color_RGB(int R, int G, int B) {
			return Color_ARGB(0xff, R, G, B);
		}
		public static int Color_ARGB(int Alpha, int R, int G, int B) {
			return (Alpha << 24) | (R << 16) | (G << 8) | B;
		}

		public static final int DialogResponse_Positive = -1;
		public static final int DialogResponse_Cancel = -3;
		public static final int DialogResponse_Negative = -2;

		public static Object MsgboxAsync(BA ba, CharSequence Message, CharSequence Title) throws Exception {
			return Common.Msgbox2Async(Message, Title, "OK", "", "",null, ba, true);
		}
		public static Object Msgbox2Async(final BA ba, CharSequence Message, CharSequence Title, String Positive, String Cancel, String Negative, BitmapWrapper Icon) {
			return Common.Msgbox2Async(Message, Title, Positive, Cancel, Negative, Icon, ba, false);
		}
		public static String FileUri(String Dir, String FileName) throws IOException {
			if (Dir.equals(File.getDirAssets()) == false) {
				return "file://" + File.Combine(Dir, urlencode(FileName));
			} else {
				if (File.virtualAssetsFolder == null)
					return "file:///android_asset/" + urlencode(FileName.toLowerCase(BA.cul));
				else
					return "file://" + File.Combine(File.virtualAssetsFolder, 
							urlencode(File.getUnpackedVirtualAssetFile(FileName)));
			}
		}
		private static String urlencode(String s) throws UnsupportedEncodingException {
			return URLEncoder.encode(s, "utf8").replace("+", "%20");
		}



	}

}