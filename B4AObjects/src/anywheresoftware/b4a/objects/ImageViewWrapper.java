package anywheresoftware.b4a.objects;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DesignerName;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.constants.Colors;

/**
 * A view that shows an image.
 *You can assign a bitmap using the Bitmap property.
 *The Gravity property changes the way the image appears.
 *The two most relevant values are Gravity.FILL (which will cause the image to fill the entire view) 
 *and Gravity.CENTER (which will draw the image in the view's center).
 */
@ActivityObject
@ShortName("ImageView")
public class ImageViewWrapper extends ViewWrapper<ImageView>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new ImageView(ba.context));
		super.innerInitialize(ba, eventName, true);
	}
	/**
	 * Gets or sets the gravity assigned to the bitmap.
	 *Example:<code>
	 *ImageView1.Gravity = Gravity.Fill</code>
	 */
	public int getGravity() {
		Drawable d = getObject().getBackground();
		if (d == null)
			return 0;
		else if (d instanceof BitmapDrawable)
			return ((BitmapDrawable)d).getGravity();
		else if (d instanceof ColorDrawable)
			return d.getLevel();
		else
			return 0;
	}
	public void setGravity(int value) {
		Drawable d = getObject().getBackground();
		if (d == null || !(d instanceof BitmapDrawable)) {
			anywheresoftware.b4a.objects.drawable.BitmapDrawable bd = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
			bd.Initialize(null);
			getObject().setBackgroundDrawable(bd.getObject());
			d = bd.getObject();
		}
		((BitmapDrawable)d).setGravity(value);
	}
	/**
	 * Gets or sets the bitmap assigned to the ImageView.
	 *Example:<code>
	 *ImageView1.Bitmap = LoadBitmap(File.DirAssets, "someimage.jpg")</code>
	 */
	public Bitmap getBitmap() {
		Drawable d = getObject().getBackground();
		if (d == null || !(d instanceof BitmapDrawable))
			return null;
		else
			return ((BitmapDrawable)d).getBitmap();
	}
	public void setBitmap(Bitmap value) {
		SetBackgroundImage(value);
	}
	/**
	 * Creates a BitmapDrawable with the given Bitmap and sets it as the view's background. The Gravity is not changed.
	 *The BitmapDrawable is returned. You can use it to change the Gravity.
	 */
	@Override
	@DesignerName("SetBackgroundImage")
	public anywheresoftware.b4a.objects.drawable.BitmapDrawable SetBackgroundImageNew(Bitmap Bitmap) {
		int gravity = getGravity();
		anywheresoftware.b4a.objects.drawable.BitmapDrawable bd = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
		bd.Initialize(Bitmap);
		bd.setGravity(gravity);
		this.setBackground(bd.getObject());	
		return bd;
	}
	@Override
	public void SetBackgroundImage(Bitmap Bitmap) {
		SetBackgroundImageNew(Bitmap);
	}
	@Hide
	public static void setImage(View v,HashMap<String, Object> drawProps, boolean designer) {
		Drawable d;
		try {
			d = anywheresoftware.b4a.objects.drawable.BitmapDrawable.build(v, drawProps, designer, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (d == null) {
			ColorDrawable cd = new ColorDrawable(Colors.White);
			Integer gravity = (Integer)	drawProps.get("gravity");
			if (gravity == null)
				gravity = Integer.valueOf(0);
			v.setBackgroundDrawable(cd);
			cd.setLevel(gravity); //stored for later use.
		}
		else
			v.setBackgroundDrawable(d);
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, ImageView.class, props, designer);
		}
		ImageView iv = (ImageView)ViewWrapper.build(prev, props, designer);
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		setImage(iv, drawProps, designer);
		return iv;
	}
}
