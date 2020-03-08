package anywheresoftware.b4a.objects;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.R;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils.TruncateAt;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.LayoutBuilder.DesignerTextSizeMethod;
import anywheresoftware.b4a.keywords.constants.TypefaceWrapper;
import anywheresoftware.b4a.objects.streams.File;

@Hide
public class TextViewWrapper<T extends TextView> extends ViewWrapper<T> implements DesignerTextSizeMethod{
	public String getText() {
		return getObject().getText().toString();
	}
	public void setText(CharSequence Text) {
		getObject().setText(Text);
	}
	@Hide
	public void setText(Object Text) {
		setText(BA.ObjectToCharSequence(Text));
	}
	public void setTextColor(int Color) {
		getObject().setTextColor(Color);
	}
	public int getTextColor() {
		return getObject().getTextColors().getDefaultColor();
	}
	/**
	 * Gets or sets the truncation mode. Only affects single line fields.
	 *Possible values:
	 *NONE
	 *START - The three dots appear at the beginning.
	 *MIDDLE - The three dots appear at the middle.
	 *END - The three dots appear at the end.
	 */
	public void setEllipsize(String e) {
		getObject().setEllipsize(e.equals("NONE") ? null : TruncateAt.valueOf(e));
	}
	public String getEllipsize() {
		TruncateAt t = getObject().getEllipsize();
		return t == null ? "NONE" : t.toString();
	}
	/**
	 * Sets whether the text field should be in single line mode or multiline mode.
	 */
	public void setSingleLine(boolean singleLine) {
		getObject().setSingleLine(singleLine);
	}
	
	/**
	 * Changes the text color with a transition animation between the current color and the ToColor colors.
	 *The transition is based on the HSV color space.
	 *Note that the animation will only be applied when running on Android 3+ devices.
	 *Duration - Animation duration measured in milliseconds.
	 */
	public void SetTextColorAnimated(int Duration, int ToColor) {
		if (android.os.Build.VERSION.SDK_INT >= 11 && Duration > 0) {
			final TextView target = getObject();
			final float[] from = new float[3], to = new float[3];
			int FromColor = getTextColor();
			android.graphics.Color.colorToHSV(FromColor, from);
			android.graphics.Color.colorToHSV(ToColor, to);
			ValueAnimator anim = ValueAnimator.ofFloat(0, 1); 
			anim.setDuration(Duration);
			final float[] hsv  = new float[3];      
			final int fromAlpha = android.graphics.Color.alpha(FromColor), toAlpha = android.graphics.Color.alpha(ToColor);
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
			    @Override public void onAnimationUpdate(ValueAnimator animation) {
			        hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
			        hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
			        hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();
			        int alpha = (int) (fromAlpha + (toAlpha - fromAlpha) * animation.getAnimatedFraction());
			        target.setTextColor(Color.HSVToColor(alpha, hsv));
			    }
			});
			anim.start();
		}
		else {
			setTextColor(ToColor);
		}
	}
	/**
	 * Changes the text size with an animation effect.
	 */
	public void SetTextSizeAnimated(int Duration, float TextSize) {
		if (android.os.Build.VERSION.SDK_INT >= 11 && Duration > 0) {
			final TextView target = getObject();
			ObjectAnimator.ofFloat(target, "TextSize", getTextSize(), TextSize).setDuration(Duration).start();
		}
		else {
			setTextSize(TextSize);
		}
	}
	/**
	 * Get or sets the view's text size.
	 */
	public void setTextSize(float TextSize) {
		getObject().setTextSize(TextSize);
	}
	
	public float getTextSize() {
		float pixels =  getObject().getTextSize();
		return pixels / getObject().getContext().getResources().getDisplayMetrics().scaledDensity;
	}
	public void setGravity(int Gravity) {
		getObject().setGravity(Gravity);
	}
	public int getGravity() {
		return getObject().getGravity();
	}
	public void setTypeface(Typeface Typeface) {
		getObject().setTypeface(Typeface);
	}
	public Typeface getTypeface() {
		return getObject().getTypeface();
	}
	@Override
	@Hide
	public String toString() {
		String s = super.toString();
		if (IsInitialized())
			return s += ", Text=" + getText();
		else
			return s;
	}
	
	private static final HashMap<String, Typeface> cachedTypefaces = new HashMap<String, Typeface>();
	@Hide
	public static Typeface getTypeface(String name) {
		Typeface tf = cachedTypefaces.get(name);
		if (tf == null) {
			tf = Typeface.createFromAsset(BA.applicationContext.getAssets(), name);
			cachedTypefaces.put(name, tf);
		}
		return tf;
	}
	@Hide
	public static String fontAwesomeFile = "b4x_fontawesome.otf";
	@Hide
	public static String materialIconsFile = "b4x_materialicons.ttf";
	@Hide
	public static View build(Object prev, Map<String, Object> props, boolean designer) throws Exception {
		TextView v = (TextView) ViewWrapper.build(prev, props, designer);
		
		
		ColorStateList defaultTextColor = null;
		if (designer) {
			defaultTextColor = (ColorStateList) ViewWrapper.getDefault(v, "textColor", v.getTextColors());
		}
		String typeFace = (String) props.get("typeface");
		Typeface tf;
		if (typeFace.contains(".")) {
			if (designer) {
				tf = Typeface.createFromFile(File.Combine(File.getDirInternal(), typeFace.toLowerCase(BA.cul)));
			}
			else
				tf = TypefaceWrapper.LoadFromAssets(typeFace);
		}
		else if (typeFace.equals("FontAwesome")) {
			tf = getTypeface(fontAwesomeFile);
			props.put("text", props.get("fontAwesome"));
		}
		else if (typeFace.equals("Material Icons")) {
			tf = getTypeface(materialIconsFile);
			props.put("text", props.get("materialIcons"));
		}
		else {
			
			tf = (Typeface) Typeface.class.getField(typeFace).get(null);
		}
		v.setText((CharSequence) props.get("text"));
		int style = (Integer) Typeface.class.getField((String)props.get("style")).get(null);
		v.setTextSize((Float)props.get("fontsize"));
		v.setTypeface(tf, style);
		int vAlign = (Integer) Gravity.class.getField((String)props.get("vAlignment")).get(null);
		int hAlign = (Integer) Gravity.class.getField((String)props.get("hAlignment")).get(null);
		v.setGravity(vAlign | hAlign);
		int textColor = (Integer)props.get("textColor");
		if (textColor != ViewWrapper.defaultColor)
			v.setTextColor(textColor);
		if (designer && textColor == ViewWrapper.defaultColor) {
			v.setTextColor(defaultTextColor);
		}
		if (designer)
			setHint(v, (String)props.get("name"));
		boolean singleLine =  BA.gm(props, "singleLine", false);
		v.setSingleLine(singleLine);
		String ellipsizeMode = BA.gm(props, "ellipsize", "NONE");
		if (ellipsizeMode.equals("NONE") == false)
			v.setEllipsize(TruncateAt.valueOf(ellipsizeMode));
		else if (designer)
			v.setEllipsize(null);
		return v;
	}
	@Hide
	public static void setHint(TextView v, String name) {
		if (v.getText().length() == 0 && v instanceof EditText == false) {
			v.setText(name);
			v.setTextColor(Color.GRAY);
		}
	}
	
	
}
