package anywheresoftware.b4a.objects.drawable;

import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.constants.Colors;
import anywheresoftware.b4a.objects.ViewWrapper;

/**
 * A drawable that has a solid color and can have round corners.
 *Example:<code>
 *Dim cd As ColorDrawable
 *cd.Initialize(Colors.Green, 5dip)
 *Button1.Background = cd</code>
 */
@ShortName("ColorDrawable")
@ActivityObject
public class ColorDrawable extends AbsObjectWrapper<Drawable>{
	/**
	 * Initializes the drawable with the given color and corners radius.	 
	 */
	public void Initialize(int Color, int CornerRadius) {
		Initialize2(Color, CornerRadius, 0, 0);
	}
	public void Initialize2(int Color, int CornerRadius, int BorderWidth, int BorderColor) {
		GradientDrawableWithCorners gd = new GradientDrawableWithCorners();
		gd.setColor(Color);
		gd.setCornerRadius(CornerRadius);
		gd.setStroke(BorderWidth, BorderColor);
		setObject(gd);
	}
	@Hide
	public static class GradientDrawableWithCorners extends GradientDrawable {
		public float cornerRadius;
		public int borderWidth, borderColor;
		public int color;
		public GradientDrawableWithCorners() {
			super();
		}
		public GradientDrawableWithCorners(Orientation o, int[] colors) {
			super(o, colors);
		}
		@Override
		public void setCornerRadius(float radius) {
			super.setCornerRadius(radius);
			this.cornerRadius = radius;
		}
		@Override
		public void setStroke(int borderWidth, int borderColor) {
			super.setStroke(borderWidth, borderColor);
			this.borderWidth = borderWidth;
			this.borderColor = borderColor;
		}
		@Override
		public void setColor(int color) {
			super.setColor(color);
			this.color = color;
		}
	}
	@Hide
	public static Drawable build(Object prev, HashMap<String, Object> d, boolean designer, Object tag) {
		
		int solidColor = (Integer)d.get("color");
		if (solidColor == ViewWrapper.defaultColor) {
			if (designer) {
				Drawable dr = (Drawable) ViewWrapper.getDefault((View)prev, "background", null);
				if (dr == null) {
					dr = new android.graphics.drawable.ColorDrawable(Colors.Transparent);
				}
				return dr;
			}
			else
				return null;
		}
		int color;
		if (d.containsKey("alpha")) {
			int alpha = (Integer)d.get("alpha");
			color = alpha << 24 | (solidColor << 8 >>> 8);
		}
		else
			color = solidColor;
		Integer corners = (Integer) d.get("cornerRadius");
		if (corners == null)
			corners = Integer.valueOf(0);
		ColorDrawable cd = new ColorDrawable();
		int borderColor = BA.gm(d, "borderColor", Colors.Black);
		int borderWidth = BA.gm(d, "borderWidth", 0);
		cd.Initialize2(color, (int)(BALayout.getDeviceScale() * corners), (int)(BALayout.getDeviceScale() * borderWidth), borderColor);
		return cd.getObject();
	}
}
