package anywheresoftware.b4a.objects;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.Button;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.BA.B4aDebuggable;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.DesignerName;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Pixel;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.WarningEngine;
import anywheresoftware.b4a.BALayout.LayoutParams;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.LayoutBuilder.ViewWrapperAndAnchor;
import anywheresoftware.b4a.objects.drawable.ColorDrawable;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;
import anywheresoftware.b4a.objects.drawable.ColorDrawable.GradientDrawableWithCorners;

@Hide
@Events(values={"Click", "LongClick"})
public class ViewWrapper<T extends View> extends AbsObjectWrapper<T> implements B4aDebuggable {
	protected BA ba;
	@Hide
	public static int lastId = 0;
	@Hide
	public static final int defaultColor = -984833;
	@Hide
	public static int animationDuration = 400;

	/**
	 * Initializes the view and sets the subs that will handle the events.
	 *<b>Views added with the designer should NOT be initialized. These views are initialized when the layout is loaded.</b>
	 */
	public void Initialize(final BA ba, String EventName) {
		innerInitialize(ba, EventName.toLowerCase(BA.cul), false);

	}
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		this.ba = ba;
		getObject().setId(++lastId);
		if (ba.subExists(eventName + "_click")) {

			getObject().setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					ba.raiseEvent(v, eventName + "_click");
				}

			});
		}
		if (ba.subExists(eventName + "_longclick")) {
			getObject().setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					ba.raiseEvent(v, eventName + "_longclick");
					return true;
				}

			});
		}

	}
	/**
	 * Gets or sets the background drawable.
	 */
	public Drawable getBackground() {
		return getObject().getBackground();
	}
	public void setBackground(Drawable drawable) {
		getObject().setBackgroundDrawable(drawable);
	}
	
	/**
	 * Creates a BitmapDrawable with the given Bitmap and sets it as the view's background. The Gravity is set to FILL.
	 *The BitmapDrawable is returned. You can use it to change the Gravity.
	 */
	@DesignerName("SetBackgroundImage")
	public anywheresoftware.b4a.objects.drawable.BitmapDrawable SetBackgroundImageNew(Bitmap Bitmap) {
		anywheresoftware.b4a.objects.drawable.BitmapDrawable bd = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
		bd.Initialize(Bitmap);
		getObject().setBackgroundDrawable(bd.getObject());
		return bd;
	}
	public void SetBackgroundImage(Bitmap Bitmap) {
		SetBackgroundImageNew(Bitmap);
	}
	/**
	 * Invalidates the whole view forcing the view to redraw itself.
	 *Redrawing will only happen when the program can process messages. Usually when it finishes running the current code. 
	 */
	public void Invalidate() {
		getObject().invalidate();
	}
	/**
	 * Invalidates the given rectangle.
	 *Redrawing will only happen when the program can process messages. Usually when it finishes running the current code. 
	 */
	public void Invalidate2(Rect Rect) {
		getObject().invalidate(Rect);
	}
	/**
	 * Invalidates the given rectangle.
	 *Redrawing will only happen when the program can process messages. Usually when it finishes running the current code. 
	 */
	public void Invalidate3(int Left, int Top, int Right, int Bottom) {
		getObject().invalidate(Left, Top, Right, Bottom);
	}
	/**
	 * Gets or sets the view's width.
	 */
	public void setWidth(@Pixel int width) {
		ViewGroup.LayoutParams lp = getLayoutParams();
		lp.width = width;
		requestLayout();
	}
	public int getWidth() {
		return ((ViewGroup.LayoutParams)getLayoutParams()).width;
	}
	public int getHeight() {
		return ((ViewGroup.LayoutParams)getLayoutParams()).height;

	}
	/**
	 * Gets or sets the view's left position.
	 */
	public int getLeft() {
		BALayout.LayoutParams lp = (LayoutParams) getLayoutParams();
		return lp.left;
	}
	/**
	 * Gets or sets the view's top position.
	 */
	public int getTop() {
		BALayout.LayoutParams lp = (LayoutParams) getLayoutParams();
		return lp.top;
	}
	/**
	 * Gets or sets the view's height.
	 */
	public void setHeight(@Pixel int height) {
		ViewGroup.LayoutParams lp = getLayoutParams();
		lp.height = height;
		requestLayout();
	}

	public void setLeft(@Pixel int left) {
		BALayout.LayoutParams lp = (LayoutParams) getLayoutParams();
		lp.left = left;
		requestLayout();
	}

	public void setTop(@Pixel int top) {
		BALayout.LayoutParams lp = (LayoutParams) getLayoutParams();
		lp.top = top;
		requestLayout();
	}
	private void requestLayout() {
		ViewParent parent = getObject().getParent();
		if (parent != null)
			parent.requestLayout();
	}
	/**
	 * Gets or sets the view's padding (distance between border and content).
	 *The data is stored in a 4 element array with the following values: left, top, right and bottom.
	 *Make sure to use 'dip' units when setting the padding. 
	 *Example: <code>Button1.Padding = Array As Int (30dip, 10dip, 10dip, 10dip)</code>
	 */
	public void setPadding(int[] p) {
		getObject().setPadding(p[0], p[1], p[2], p[3]);
	}
	public int[] getPadding() {
		return new int[] {getObject().getPaddingLeft(), getObject().getPaddingTop(), getObject().getPaddingRight(), getObject().getPaddingBottom()};
	}
	/**
	 * Sets the background of the view to be a ColorDrawable with the given color.
	 *If the current background is of type GradientDrawable or ColorDrawable the round corners will be kept.
	 */
	public void setColor(int color) {
		Drawable d = getObject().getBackground();
		if (d != null && d instanceof GradientDrawable) {
			if (d instanceof GradientDrawableWithCorners && ((GradientDrawableWithCorners)d).borderWidth != 0) {
				((GradientDrawableWithCorners)d).setColor(color);
				getObject().invalidate();
				getObject().requestLayout();
			} else {
				ColorDrawable cd = new ColorDrawable();
				cd.Initialize(color, (int)findRadius());
				getObject().setBackgroundDrawable(cd.getObject());
			}
		}
		else {
			getObject().setBackgroundColor(color);
		}

	}
	private float findRadius() {
		float radius = 0;
		Drawable d = getObject().getBackground();
		if (d != null && d instanceof GradientDrawable) {
			if (d instanceof GradientDrawableWithCorners) {
				radius = ((GradientDrawableWithCorners)d).cornerRadius;
			}
			else {
				GradientDrawable g = (GradientDrawable) getObject().getBackground();
				try {
					Field state = g.getClass().getDeclaredField("mGradientState");
					state.setAccessible(true);
					Object gstate = state.get(g);
					Field radiusF = gstate.getClass().getDeclaredField("mRadius");
					radius = (Float) radiusF.get(gstate);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return radius;
	}
	public void setTag(Object tag) {
		getObject().setTag(tag);
	}
	/**
	 * Gets or sets the Tag value. This is a place holder which can used to store additional data.
	 */
	public Object getTag() {
		return getObject().getTag();
	}
	/**
	 * Returns the view's parent. In most cases the returned value can be casted to a Panel.
	 *Returns Null if there is no parent.
	 */
	public Object getParent() {
		return getObject().getParent();
	}
	public void setVisible(boolean Visible) {
		getObject().setVisibility(Visible ? View.VISIBLE : View.GONE);
	}

	public boolean getVisible() {
		return getObject().getVisibility() == View.VISIBLE ? true : false;
	}
	public void setEnabled(boolean Enabled) {
		getObject().setEnabled(Enabled);
	}
	public boolean getEnabled() {
		return getObject().isEnabled();
	}
	/**
	 * Changes the Z order of this view and brings it to the front.
	 */
	public void BringToFront() {
		if (getObject().getParent() instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) getObject().getParent();
			vg.removeView(getObject());
			vg.addView(getObject());
		}
	}
	/**
	 * Changes the Z order of this view and sends it to the back.
	 */
	public void SendToBack() {
		if (getObject().getParent() instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) getObject().getParent();
			vg.removeView(getObject());
			vg.addView(getObject(), 0);
		}
	}
	/**
	 * Removes this view from its parent.
	 */
	public void RemoveView() {
		if (getObject().getParent() instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) getObject().getParent();
			vg.removeView(getObject());
		}
	}
	/**
	 * Changes the view position and size.
	 */
	public void SetLayout(@Pixel int Left,@Pixel  int Top,@Pixel  int Width,@Pixel  int Height) {
		LayoutParams lp = (LayoutParams)getLayoutParams();
		lp.left = Left;
		lp.top = Top;
		lp.width = Width;
		lp.height = Height;
		if (getObject().getParent() != null)
			getObject().getParent().requestLayout();
	}
	private ViewGroup.LayoutParams getLayoutParams() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getObject().getLayoutParams();
		if (lp == null) {
			lp = new LayoutParams(0, 0, 0, 0);
			getObject().setLayoutParams(lp);
		}
		return lp;
	}
	/**
	 * Similar to SetLayout. Animates the change. Note that the animation will only be applied when running on Android 3+ devices.
	 *Duration - Animation duration measured in milliseconds.
	 */
	public void SetLayoutAnimated(int Duration, @Pixel final int Left,@Pixel  final int Top,@Pixel  final int Width,@Pixel  final int Height) {
		BALayout.LayoutParams lp = (LayoutParams) getObject().getLayoutParams();
		if (lp == null) {
			SetLayout(Left, Top, Width, Height);
			return;
		}
		int pLeft = lp.left, pTop = lp.top, pWidth = lp.width, pHeight = lp.height;
		SetLayout(Left, Top, Width, Height);
		ViewWrapper.AnimateFrom(getObject(), Duration, pLeft, pTop, pWidth, pHeight);
	}
	/**
	 * Changes the background color with a transition animation between the FromColor and the ToColor colors.
	 *The transition is based on the HSV color space.
	 *Note that the animation will only be applied when running on Android 3+ devices.
	 *Duration - Animation duration measured in milliseconds.
	 */
	public void SetColorAnimated(int Duration, int FromColor, int ToColor) {
		if (android.os.Build.VERSION.SDK_INT >= 11 && Duration > 0) {
			final View target = getObject();
			final GradientDrawableWithCorners gdc;
			if (target.getBackground() instanceof GradientDrawableWithCorners)
				gdc = (GradientDrawableWithCorners) target.getBackground();
			else
				gdc = new GradientDrawableWithCorners();
			gdc.setColor(FromColor);
			target.setBackgroundDrawable(gdc);
			final float[] from = new float[3], to = new float[3];
			Color.colorToHSV(FromColor, from);
			Color.colorToHSV(ToColor, to);
			ValueAnimator anim = ValueAnimator.ofFloat(0, 1); 
			anim.setDuration(Duration);
			final float[] hsv  = new float[3];      
			final int fromAlpha = Color.alpha(FromColor), toAlpha = Color.alpha(ToColor);
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
				@Override public void onAnimationUpdate(ValueAnimator animation) {
					hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
					hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
					hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();
					int alpha = (int) (fromAlpha + (toAlpha - fromAlpha) * animation.getAnimatedFraction());
					gdc.setColor(Color.HSVToColor(alpha, hsv));
					target.invalidate();
				}
			});
			anim.start();
		}
		else {
			setColor(ToColor);
		}
	}
	/**
	 * Changes the view visibility with a fade-in or fade-out animation.
	 *You must 
	 *Note that the animation will only be applied when running on Android 3+ devices.
	 *Duration - Animation duration measured in milliseconds.
	 *Visible - New visibility state.
	 */
	public void SetVisibleAnimated(int Duration, final boolean Visible) {
		if (Visible == getVisible())
			return;
		if (android.os.Build.VERSION.SDK_INT >= 11 && Duration > 0) {
			final View target = getObject();
			ObjectAnimator oa;
			if (Visible)
				oa = ObjectAnimator.ofFloat(target, "alpha", 0f, 1.0f);
			else
				oa = ObjectAnimator.ofFloat(target, "alpha", 1f, 0f);

			oa.addListener(new AnimatorListener() {

				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					if (!Visible) {
						target.setVisibility(View.GONE);
					}
					target.setAlpha(1);

				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub

				}

			});
			oa.setDuration(Duration).start();
			if (Visible)
				target.setVisibility(View.VISIBLE);
		}
		else {
			setVisible(Visible);
		}
	}

	@SuppressWarnings("unchecked")
	@Hide
	public static void AnimateFrom(View target, int Duration, int pLeft, int pTop, int pWidth, int pHeight) {
		BALayout.LayoutParams lp = (LayoutParams) target.getLayoutParams();
		if (android.os.Build.VERSION.SDK_INT >= 11 && Duration > 0 && pWidth >= 0 && pHeight >= 0) {
			target.setPivotX(0);
			target.setPivotY(0);
			WeakReference<AnimatorSet> wr = (WeakReference<AnimatorSet>) AbsObjectWrapper.getExtraTags(target).get("prevSet");
			AnimatorSet prevSet = (AnimatorSet) (wr != null ? wr.get() : null);
			if (prevSet != null && prevSet.isRunning())
				prevSet.end();
			AnimatorSet set = new AnimatorSet();
			AbsObjectWrapper.getExtraTags(target).put("prevSet", new WeakReference<AnimatorSet>(set));
			set.playTogether(
					ObjectAnimator.ofFloat(target, "translationX", (pLeft - lp.left), 0),
					ObjectAnimator.ofFloat(target, "translationY",(pTop - lp.top), 0),
					ObjectAnimator.ofFloat(target, "scaleX",pWidth / (float) Math.max(1, lp.width), 1),
					ObjectAnimator.ofFloat(target, "scaleY",pHeight / (float)Math.max(1, lp.height), 1));
			set.setDuration(Duration);
			set.start();
		}
	}
	/**
	 * Tries to set the focus to this view.
	 *Returns True if the focus was set.
	 */
	public boolean RequestFocus() {
		return getObject().requestFocus();
	}
	@Hide
	@Override
	public String toString() {
		String s = baseToString();
		if (IsInitialized()) {
			s += ": ";
			if (getEnabled() == false)
				s += "Enabled=false, ";
			if (getVisible() == false)
				s += "Visible=false, ";
			if (getObject().getLayoutParams() == null || 
					getObject().getLayoutParams() instanceof LayoutParams == false) {
				s += "Layout not available";
			}
			else {
				s += "Left=" + getLeft() + ", Top=" + getTop() + ", Width=" + getWidth() + 
				", Height=" + getHeight();
			}
			if (getTag() != null)
				s += ", Tag=" + getTag().toString();
		}
		return s;
	}
	@Hide
	public static View build(Object prev, Map<String, Object> props, boolean designer) throws Exception {
		View v = (View) prev;
		if (v.getTag() == null && designer) {
			HashMap<String, Object> defaults = new HashMap<String, Object>();
			defaults.put("background", v.getBackground());
			defaults.put("padding",  new int[] {v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom()});
			v.setTag(defaults);
		}
		BALayout.LayoutParams lp = (LayoutParams) v.getLayoutParams();
		if (lp == null) {
			lp = new BALayout.LayoutParams();
			v.setLayoutParams(lp);
		}
		lp.setFromUserPlane((Integer) props.get("left"), (Integer) props.get("top"),
				(Integer) props.get("width"), (Integer) props.get("height"));
		v.setEnabled((Boolean)props.get("enabled"));
		if (!designer) {
			int visible = View.VISIBLE;
			if ((Boolean)props.get("visible") == false)
				visible = View.GONE;
			v.setVisibility(visible);
			v.setTag(props.get("tag"));
		}
		int[] padding = (int[]) props.get("padding");
		if (padding != null)
		{
			v.setPadding((int)Math.round(BALayout.getDeviceScale() * padding[0]), (int)Math.round(BALayout.getDeviceScale() * padding[1]),
					(int)Math.round(BALayout.getDeviceScale() * padding[2]), (int)Math.round(BALayout.getDeviceScale() * padding[3]));
		} else if (designer) {
			int[] defaultPadding = (int[]) getDefault(v, "padding", null);
			v.setPadding(defaultPadding[0], defaultPadding[1], defaultPadding[2], defaultPadding[3]);
		}
		return v;
	}
	@Hide
	public static void fixAnchor(int pw, int ph, ViewWrapperAndAnchor vwa) {
		if (vwa.hanchor == ViewWrapperAndAnchor.RIGHT) {
			vwa.right = vwa.vw.getLeft();
			vwa.vw.setLeft(pw - vwa.right - vwa.vw.getWidth());
		}
		else if (vwa.hanchor == ViewWrapperAndAnchor.BOTH) {
			vwa.right = vwa.vw.getWidth();
			vwa.vw.setWidth(pw - vwa.right - vwa.vw.getLeft());
		}
		if (vwa.vanchor == ViewWrapperAndAnchor.BOTTOM) {
			vwa.bottom = vwa.vw.getTop();
			vwa.vw.setTop(ph - vwa.bottom - vwa.vw.getHeight());
		}
		else if (vwa.vanchor == ViewWrapperAndAnchor.BOTH) {
			vwa.bottom = vwa.vw.getHeight();
			vwa.vw.setHeight(ph - vwa.bottom - vwa.vw.getTop());
		}
	}
	@Hide
	@Override
	public Object[] debug(int limit, boolean[] outShouldAddReflectionFields) {
		Object[] res = new Object[1 * 2];
		res[0] = "ToString";
		res[1] = toString();
		outShouldAddReflectionFields[0] = true;
		return res;
	}
	@Hide
	@SuppressWarnings("unchecked")
	public static <T> T buildNativeView(Context context, Class<T> cls, HashMap<String, Object> props, boolean designer) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		String overideClass = (String) props.get("nativeClass");
		if (overideClass != null && overideClass.startsWith(".")) {
			overideClass = BA.applicationContext.getPackageName() + overideClass;

		}
		Class<?> c;
		try {
			c = designer || overideClass == null || overideClass.length() == 0 ? cls : Class.forName(overideClass);
		} catch (ClassNotFoundException e) {
			int i = overideClass.lastIndexOf(".");
			c = Class.forName(overideClass.substring(0, i) + "$" + overideClass.substring(i + 1));
		}
		return (T) c.getConstructor(Context.class).newInstance(context);
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static Object getDefault(View v, String key, Object defaultValue) {
		HashMap<String, Object> map = (HashMap<String, Object>) v.getTag();
		if (map.containsKey(key))
			return map.get(key);
		else {
			map.put(key, defaultValue);
			return defaultValue;
		}
	}

}
