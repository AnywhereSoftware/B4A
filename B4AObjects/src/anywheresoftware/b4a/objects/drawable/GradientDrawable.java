package anywheresoftware.b4a.objects.drawable;

import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
/**
 * A drawable that has a gradient color and can have round corners.
 */
@ActivityObject
@ShortName("GradientDrawable")
public class GradientDrawable extends AbsObjectWrapper<android.graphics.drawable.GradientDrawable>{
	
	/**
	 * Initializes this object.
	 *Orientation - The gradient orientation. Can be one of the following value:
	 *"TOP_BOTTOM"
     *"TR_BL" (Top-Right to Bottom-Left)
     *"RIGHT_LEFT"
     *"BR_TL" (Bottom-Right to Top-Left)
     *"BOTTOM_TOP"
     *"BL_TR" (Bottom-Left to Top-Right)
     *"LEFT_RIGHT"
     *"TL_BR" (Top-Left to Bottom-Right)
     * 
     *Colors - An array with the gradient colors.
     * 
     *Example:<code>
	 *Dim Gradient1 As GradientDrawable
	 *Dim Clrs(2) As Int
	 *Clrs(0) = Colors.Black
	 *Clrs(1) = Colors.White
	 *Gradient1.Initialize("TOP_BOTTOM", Clrs)</code>
     * 
     */
	public void Initialize(Orientation Orientation, int[] Colors) {
		setObject(new android.graphics.drawable.GradientDrawable(Orientation, Colors));
	}
	/**
	 * Sets the radius of the "rectangle" corners.
	 *Set to 0 for square corners.
	 * 
	 *Example:<code>
	 *Gradient1.CornerRadius = 20dip</code>
	 */
	public void setCornerRadius(float Radius) {
		getObject().setCornerRadius(Radius);
	}
	@Hide
	public static Drawable build(Object prev, HashMap<String, Object> d, boolean designer, Object tag) {
		android.graphics.drawable.GradientDrawable gd = 
			new android.graphics.drawable.GradientDrawable(Enum.valueOf(Orientation.class, 
					(String)d.get("orientation")),
					new int[] {(Integer)d.get("firstColor"), (Integer)d.get("secondColor")});
			gd.setCornerRadius((int)(BALayout.getDeviceScale() * (Integer)d.get("cornerRadius")));
		return gd;
	}
	
}
