package anywheresoftware.b4a.objects.drawable;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BALayout.LayoutParams;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;

/**
 * A drawable that draws a bitmap. The bitmap is set during initialization.
 *You can change the way the bitmap appears by changing the Gravity property.
 *Example:<code>
 *Dim bd As BitmapDrawable
 *bd.Initialize(LoadBitmap(File.DirAssets, "SomeImage.png"))
 *bd.Gravity = Gravity.FILL
 *Activity.Background = bd</code>
 */
@ActivityObject
@ShortName("BitmapDrawable")
public class BitmapDrawable extends AbsObjectWrapper<android.graphics.drawable.BitmapDrawable> {
	public void Initialize(Bitmap Bitmap) {
		android.graphics.drawable.BitmapDrawable bd = 
			new android.graphics.drawable.BitmapDrawable(BA.applicationContext.getResources(), Bitmap);
		setObject(bd);
	}
	/**
	 * Returns the internal Bitmap.
	 */
	public Bitmap getBitmap() {
		return getObject().getBitmap();
	}
	/**
	 * Gets or sets the gravity value. This value affects the way the image will be drawn.
	 *Example:<code>
	 *BitmapDrawable1.Gravity = Gravity.FILL</code>
	 */
	public int getGravity() {
		return getObject().getGravity();
	}
	public void setGravity(int value) {
		getObject().setGravity(value);
	}
	
	@Hide
	public static Drawable build(Object prev, HashMap<String, Object> d, boolean designer, Object tag) throws IOException {
		BitmapDrawable bd = null;
		String file = ((String) d.get("file")).toLowerCase(BA.cul);
		if (file.length() == 0) {
			return null;
		}
		String Dir;
		if (designer) {
			Dir = anywheresoftware.b4a.objects.streams.File.getDirInternal();
		}
		else {
			Dir = anywheresoftware.b4a.objects.streams.File.getDirAssets();
		}
		bd = new BitmapDrawable();
		BitmapWrapper bw = new BitmapWrapper();
		bw.Initialize(Dir, file);
		bd.Initialize(bw.getObject());
		Integer gravity = (Integer)d.get("gravity");
		if (gravity != null) {
			bd.getObject().setGravity(gravity);
		}
		return bd.getObject();
	}
}
