package anywheresoftware.b4a.objects.drawable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.security.auth.Destroyable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Paint.Align;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Pixel;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.constants.Colors;
import anywheresoftware.b4a.objects.ViewWrapper;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
/**
 * A Canvas is an object that draws on other views or (mutable) bitmaps.
 *When the canvas is initialized and set to draw on a view, a new mutable bitmap is created for that view background, the current view's background
 *is copied to the new bitmap and the canvas is set to draw on the new bitmap.
 *The canvas drawings are not immediately updated on the screen. You should call the target view Invalidate method to make it refresh the view.
 *This is useful as it allows you to make several drawings and only then refresh the display.
 *The canvas can be temporary limited to a specific region (and thus only affect this region). This is done by calling ClipPath. Removing the clipping is done by calling RemoveClip.
 *You can get the bitmap that the canvas draws on with the Bitmap property.
 */
@ShortName("Canvas")
@ActivityObject
public class CanvasWrapper {

	@Hide
	public Canvas canvas;
	@Hide
	public Paint paint;
	private BitmapWrapper bw;
	private RectF rectF;
	@Hide
	public PorterDuffXfermode eraseMode;
	/**
	 * Initializes the canvas for drawing on a view.
	 *The view background will be drawn on the canvas during initialization.
	 *Note that you should not change the view's background after calling this method.
	 *
	 *Example: <code>
	 *Dim Canvas1 As Canvas
	 *Canvas1.Initialize(Activity) 'this canvas will draw on the activity background</code>
	 */
	public void Initialize(View Target) {
		paint = new Paint();
		LayoutParams lp = Target.getLayoutParams();
		Bitmap bitmap = Bitmap.createBitmap(lp.width , lp.height , Config.ARGB_8888);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		canvas = new Canvas(bitmap);
		if (Target.getBackground() != null) {
			Target.getBackground().setBounds(0, 0, lp.width, lp.height);
			Target.getBackground().draw(canvas);
		}
		Target.setBackgroundDrawable(bd);
		bw = new BitmapWrapper();
		bw.setObject(bitmap);

	}
	/**
	 * Gets or sets whether antialiasing will be applied.
	 */
	public void setAntiAlias(boolean b) {
		paint.setAntiAlias(b);
	}
	public boolean getAntiAlias() {
		return paint.isAntiAlias();
	}
	@Hide
	public void checkAndSetTransparent(int color) {
		if (color != Colors.Transparent) {
			paint.setXfermode(null);
			return;
		}
		if (eraseMode == null)
			eraseMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
		paint.setXfermode(eraseMode);
	}
	/**
	 * Initializes the canvas for drawing on this bitmap.
	 *The bitmap must be mutable. Bitmaps created from files or input streams are NOT mutable.
	 */
	public void Initialize2(Bitmap Bitmap) {
		paint = new Paint();
		if (!Bitmap.isMutable())
			throw new RuntimeException("Bitmap is not mutable.");
		canvas = new Canvas(Bitmap);
		bw = new BitmapWrapper();
		bw.setObject(Bitmap);
	}

	/**
	 * Draws a line from (x1, y1) to (x2, y2). StrokeWidth determines the width of the line.
	 *Example:<code>
	 *Canvas1.DrawLine(100dip, 100dip, 200dip, 200dip, Colors.Red, 10dip)
	 *Activity.Invalidate
	 *</code> 
	 */
	public void DrawLine(float x1, float y1, float x2, float y2, int Color, float StrokeWidth) {
		checkAndSetTransparent(Color);
		paint.setColor(Color);
		paint.setStrokeWidth(StrokeWidth);
		canvas.drawLine(x1, y1, x2, y2, paint);
	}
	/**
	 * Fills the entire canvas with the given color.
	 *Example:<code>
	 *Canvas1.DrawColor(Colors.ARGB(100, 255, 0, 0)) 'fills with semi-transparent red color.
	 *Activity.Invalidate</code>
	 */
	public void DrawColor(int Color) {
		if (Color == Colors.Transparent) {
			canvas.drawColor(Color, Mode.CLEAR);
		}
		else
			canvas.drawColor(Color);
	}

	/**
	 * Draws an oval shape.
	 *Filled - Whether the rectangle will be filled.
	 *StrokeWidth - The stroke width. Relevant when Filled = False.
	 *
	 *Example:<code>
	 *Dim Rect1 As Rect
	 *Rect1.Initialize(100dip, 100dip, 200dip, 150dip)
	 *Canvas1.DrawOval(Rect1, Colors.Gray, False, 5dip)
	 *Activity.Invalidate</code>
	 */
	public void DrawOval(Rect Rect1, int Color, boolean Filled, float StrokeWidth) {
		checkAndSetTransparent(Color);
		paint.setColor(Color);
		paint.setStyle(Filled ? Style.FILL : Style.STROKE);
		paint.setStrokeWidth(StrokeWidth);
		if (rectF == null)
			rectF = new RectF();
		rectF.set(Rect1);
		canvas.drawOval(rectF, paint);
	}
	/**
	 * Rotates the oval and draws it.
	 *Filled - Whether the rectangle will be filled.
	 *StrokeWidth - The stroke width. Relevant when Filled = False.
	 *Degrees - Number of degrees to rotate the oval (clockwise).
	 */
	public void DrawOvalRotated(Rect Rect1, int Color, boolean Filled, float StrokeWidth, float Degrees) {
		canvas.save();
		try {
			canvas.rotate(Degrees, Rect1.centerX(), Rect1.centerY());
			DrawOval(Rect1, Color, Filled, StrokeWidth);
		} finally {
			canvas.restore();
		}
	}

	/**
	 * Draws a rectangle.
	 *Filled - Whether the rectangle will be filled.
	 *StrokeWidth - The stroke width. Relevant when Filled = False.
	 *
	 *Example:<code>
	 *Dim Rect1 As Rect
	 *Rect1.Initialize(100dip, 100dip, 200dip, 150dip)
	 *Canvas1.DrawRect(Rect1, Colors.Gray, False, 5dip)
	 *Activity.Invalidate</code>
	 */
	public void DrawRect(Rect Rect1, int Color, boolean Filled, float StrokeWidth) {
		checkAndSetTransparent(Color);
		paint.setColor(Color);
		paint.setStyle(Filled ? Style.FILL : Style.STROKE);
		paint.setStrokeWidth(StrokeWidth);
		canvas.drawRect(Rect1, paint);
	}
	/**
	 * Rotates the rectangle and draws it.
	 *Filled - Whether the rectangle will be filled.
	 *StrokeWidth - The stroke width. Relevant when Filled = False.
	 *Degrees - Number of degrees to rotate the rectangle (clockwise).
	 */
	public void DrawRectRotated(Rect Rect1, int Color, boolean Filled, float StrokeWidth, float Degrees) {
		canvas.save();
		try {
			canvas.rotate(Degrees, Rect1.centerX(), Rect1.centerY());
			DrawRect(Rect1, Color, Filled, StrokeWidth);
		} finally {
			canvas.restore();
		}
	}
	/**
	 * Draws a circle.
	 *Filled - Whether the circle will be filled.
	 *StrokeWidth - The stroke width. Relevant when Filled = False.
	 *
	 *Example:<code>
	 *Canvas1.DrawCircle(150dip, 150dip, 20dip, Colors.Red, False, 10dip)</code>
	 */
	public void DrawCircle(float x, float y, float Radius, int Color, boolean Filled, float StrokeWidth) {
		checkAndSetTransparent(Color);
		paint.setColor(Color);
		paint.setStyle(Filled ? Style.FILL : Style.STROKE);
		paint.setStrokeWidth(StrokeWidth);
		canvas.drawCircle(x, y, Radius, paint);
	}
	/**
	 * Draws a bitmap.
	 *SrcRect - The subset of the bitmap that will be drawn. If Null then the complete bitmap will be drawn.
	 *DestRect - The rectangle that the bitmap will be drawn to.
	 *
	 *Example:<code>
	 *Dim Bitmap1 As Bitmap
	 *Bitmap1.Initialize(File.DirAssets, "X.jpg")
	 *Dim DestRect As Rect
	 *DestRect.Initialize(10dip, 10dip, 10dip + 100dip, 10dip + 100dip)
	 *Canvas1.DrawBitmap(Bitmap1, Null, DestRect) 'draws the bitmap to the destination rectangle.
	 *
	 *Dim SrcRect As Rect
	 *SrcRect.Initialize(0, 0, Bitmap1.Width / 2, Bitmap1.Height) 'the left half of the bitmap.
	 *DestRect.Top = 200dip
	 *DestRect.Bottom = 200dip + 100dip
	 *Canvas1.DrawBitmap(Bitmap1, SrcRect, DestRect) 'draws half of the bitmap.
	 *Activity.Invalidate</code>
	 */
	public void DrawBitmap(Bitmap Bitmap1, Rect SrcRect, Rect DestRect) {
		canvas.drawBitmap(Bitmap1, SrcRect, DestRect, null);
	}
	/**
	 * Rotates the bitmap and draws it.
	 *SrcRect - The subset of the bitmap that will be drawn. If Null then the complete bitmap will be drawn.
	 *DestRect - The rectangle that the bitmap will be drawn to.
	 *Degrees - Number of degrees to rotate the bitmap (clockwise).
	 *Example:<code>
	 *Canvas1.DrawBitmapRotated(Bitmap1, Null, DestRect, 70)</code>
	 */
	public void DrawBitmapRotated(Bitmap Bitmap1, Rect SrcRect, Rect DestRect, float Degrees) {
		canvas.save();
		try {
			canvas.rotate(Degrees, DestRect.centerX(), DestRect.centerY());
			DrawBitmap(Bitmap1, SrcRect, DestRect);

		} finally {
			canvas.restore();
		}
	}
	/**
	 * Flips the bitmap and draws it.
	 *SrcRect - The subset of the bitmap that will be drawn. If Null then the complete bitmap will be drawn.
	 *DestRect - The rectangle that the bitmap will be drawn to.
	 *Vertically - Whether to flip the bitmap vertically.
	 *Horizontally - Whether to flip the bitmap horizontally.
	 *Example:<code>
	 *Canvas1.DrawBitmapFlipped(Bitmap1, Null, DestRect, False, True)</code>
	 */
	public void DrawBitmapFlipped(Bitmap Bitmap1, Rect SrcRect, Rect DestRect, boolean Vertically, boolean Horizontally) {
		canvas.save();
		try {
			canvas.scale(Horizontally ? -1 : 1, Vertically ? -1 : 1, DestRect.centerX(), DestRect.centerY());
			DrawBitmap(Bitmap1, SrcRect, DestRect);

		} finally {
			canvas.restore();
		}
	}
	/**
	 * Draws the text.
	 *Text - The text to be drawn.
	 *x, y - The origin point.
	 *Typeface1 - Typeface (font) to use.
	 *TextSize - This value will be automatically scaled so do not scale it yourself.
	 *Color - Text color.
	 *Align - The alignment related to the origin. One of the following values: "LEFT", "CENTER", "RIGHT".
	 *Example:<code>
	 *Canvas1.DrawText("This is a nice sentence.", 200dip, 200dip, Typeface.DEFAULT_BOLD, 30, Colors.Blue, "LEFT")</code>
	 */
	public void DrawText(BA ba, String Text, float x, float y, Typeface Typeface1, float TextSize, int Color, 
			Align Align1) {
		checkAndSetTransparent(Color);
		paint.setTextAlign(Align1);
		paint.setTextSize(TextSize * ba.context.getResources().getDisplayMetrics().scaledDensity);
		paint.setTypeface(Typeface1);
		paint.setColor(Color);
		boolean aa = paint.isAntiAlias();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(0);
		paint.setStyle(Style.FILL);
		canvas.drawText(Text, x, y, paint);
		paint.setAntiAlias(aa);
	}
	/**
	 * Rotates the text and draws it.
	 *Text - The text to be drawn.
	 *x, y - The origin point.
	 *Typeface1 - Typeface (font) to use.
	 *TextSize - This value will be automatically scaled so do not scale it yourself.
	 *Color - Text color.
	 *Align - The alignment related to the origin. One of the following values: "LEFT", "CENTER", "RIGHT".
	 *Degrees - Number of degrees to rotate (clockwise).
	 *Example:<code>
	 *Canvas1.DrawTextRotated("This is a nice sentence.", 200dip, 200dip, _
	 *  Typeface.DEFAULT_BOLD, 30, Colors.Blue, "CENTER", -45)</code>
	 */
	public void DrawTextRotated(BA ba, String Text, float x, float y, Typeface Typeface1, float TextSize, int Color, 
			Align Align1, float Degree) {
		canvas.save();
		try {
			canvas.rotate(Degree, x, y);
			DrawText(ba, Text, x, y, Typeface1, TextSize, Color, Align1);
		}
		finally {
			canvas.restore();
		}
	}
	/**
	 * Returns the width of the given text.
	 *Example of drawing a blue text with white rectangle as the background:<code>
	 *Dim Rect1 As Rect
	 *Dim width, height As Float
	 *Dim t As String
	 *t = "Text to write"
	 *width = Canvas1.MeasureStringWidth(t, Typeface.DEFAULT, 14)
	 *height = Canvas1.MeasureStringHeight(t, Typeface.DEFAULT, 14)
	 *Rect1.Initialize(100dip, 100dip, 100dip + width, 100dip + height)
	 *Canvas1.DrawRect(Rect1, Colors.White, True, 0)
	 *Canvas1.DrawText(t, Rect1.Left, Rect1.Bottom, Typeface.DEFAULT, 14, Colors.Blue, "LEFT")</code>
	 */
	public float MeasureStringWidth(String Text, Typeface Typeface, float TextSize) {
		paint.setTextSize(TextSize * BA.applicationContext.getResources().getDisplayMetrics().scaledDensity);
		paint.setTypeface(Typeface);
		paint.setStrokeWidth(0);
		paint.setStyle(Style.STROKE);
		paint.setTextAlign(Align.LEFT);
		return paint.measureText(Text);
	}
	/**
	 * Returns the height of the given text.
	 *Example of drawing a blue text with white rectangle as the background:<code>
	 *Dim Rect1 As Rect
	 *Dim width, height As Float
	 *Dim t As String
	 *t = "Text to write"
	 *width = Canvas1.MeasureStringWidth(t, Typeface.DEFAULT, 14)
	 *height = Canvas1.MeasureStringHeight(t, Typeface.DEFAULT, 14)
	 *Rect1.Initialize(100dip, 100dip, 100dip + width, 100dip + height)
	 *Canvas1.DrawRect(Rect1, Colors.White, True, 0)
	 *Canvas1.DrawText(t, Rect1.Left, Rect1.Bottom, Typeface.DEFAULT, 14, Colors.Blue, "LEFT")</code>
	 */
	public float MeasureStringHeight(String Text, Typeface Typeface, float TextSize) {
		paint.setTextSize(TextSize * BA.applicationContext.getResources().getDisplayMetrics().scaledDensity);
		paint.setTypeface(Typeface);
		paint.setStrokeWidth(0);
		paint.setStyle(Style.STROKE);
		paint.setTextAlign(Align.LEFT);
		Rect r = new Rect();
		paint.getTextBounds(Text, 0, Text.length(), r);
		return r.height();
	}
	/**
	 * Draws a point at the specified position and color.
	 * 
	 *Example:<code>
	 *Canvas1.DrawPoint(50%x, 50%y, Colors.Yellow) 'draws a point in the middle of the screen.</code>
	 */
	public void DrawPoint(float x, float y, int Color) {
		checkAndSetTransparent(Color);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(0);
		paint.setColor(Color);
		canvas.drawPoint(x, y, paint);
	}

	/**
	 * Draws a Drawable into the specified rectangle.
	 * 
	 *Example:<code>
	 *Dim Gradient1 As GradientDrawable
	 *Dim Clrs(2) As Int
	 *Clrs(0) = Colors.Green
	 *Clrs(1) = Colors.Blue
	 *Gradient1.Initialize("TOP_BOTTOM", Clrs)
	 *Canvas1.DrawDrawable(Gradient1, DestRect)
	 *Activity.Invalidate</code>
	 */
	public void DrawDrawable(Drawable Drawable1, Rect DestRect) {
		Rect b = Drawable1.copyBounds();
		Drawable1.setBounds(DestRect);
		Drawable1.draw(canvas);
		Drawable1.setBounds(b);
	}
	/**
	 * Rotates and draws a Drawable into the specified rectangle.
	 *Degrees - Number of degrees to rotate (clockwise).
	 */
	public void DrawDrawableRotate(Drawable Drawable1, Rect DestRect, float Degrees) {
		canvas.save();
		try {
			canvas.rotate(Degrees, DestRect.centerX(), DestRect.centerY());
			DrawDrawable(Drawable1, DestRect);
		} finally {
			canvas.restore();
		}
	}
	/**
	 * Draws the path.
	 *Filled - Whether the path will be filled.
	 *StrokeWidth - The stroke width. Relevant when Filled = False.
	 *Example:<code>
	 *Dim Path1 As Path
	 *Path1.Initialize(50%x, 100%y)
	 *Path1.LineTo(100%x, 50%y)
	 *Path1.LineTo(50%x, 0%y)
	 *Path1.LineTo(0%x, 50%y)
	 *Path1.LineTo(50%x, 100%y)
	 *Canvas1.DrawPath(Path1, Colors.Magenta, False, 10dip)</code>
	 */
	public void DrawPath(Path Path1, int Color, boolean Filled, float StrokeWidth) {
		checkAndSetTransparent(Color);
		paint.setColor(Color);
		paint.setStyle(Filled ? Style.FILL : Style.STROKE);
		paint.setStrokeWidth(StrokeWidth);
		canvas.drawPath(Path1, paint);
	}
	/**
	 * Clips the drawing area to the given path.
	 * 
	 *Example: Fills a diamond shape with gradient color.<code>
	 *Dim Gradient1 As GradientDrawable
	 *Dim Clrs(2) As Int
	 *Clrs(0) = Colors.Black
	 *Clrs(1) = Colors.White
	 *Gradient1.Initialize("TOP_BOTTOM", Clrs)
	 *Dim Path1 As Path
	 *Path1.Initialize(50%x, 100%y)
	 *Path1.LineTo(100%x, 50%y)
	 *Path1.LineTo(50%x, 0%y)
	 *Path1.LineTo(0%x, 50%y)
	 *Path1.LineTo(50%x, 100%y)
	 *Canvas1.ClipPath(Path1) 'clip the drawing area to the path.
	 *DestRect.Left = 0%y
	 *DestRect.Top = 0%y
	 *DestRect.Right = 100%x
	 *DestRect.Bottom = 100%y
	 *Canvas1.DrawDrawable(Gradient1, DestRect) 'fill the drawing area with the gradient.
	 *Activity.Invalidate</code>
	 */
	public void ClipPath(Path Path1) {
		canvas.save();
		canvas.clipPath(Path1);
	}
	/**
	 * Removes previous clipped region.
	 */
	public void RemoveClip() {
		canvas.restore();
	}
	/**
	 * Returns the bitmap that the canvas draws to.
	 *Example: Saves the drawing to a file<code>
	 *Dim Out As OutputStream
	 *Out = File.OpenOutput(File.DirRootExternal, "Test.png", False)
	 *Canvas1.Bitmap.WriteToStream(out, 100, "PNG")
	 *Out.Close</code>
	 */
	public BitmapWrapper getBitmap() {
		return bw;
	}
	/**
	 * An object that holds a bitmap image. The bitmap can be loaded from a file or other input stream, or can be set from a different bitmap.
	 *Loading large bitmaps can easily lead to out of memory exceptions. This is true even if the file is compressed and not large as the bitmap is stored uncompressed in memory.
	 *For large images you can call InitializeSample and load a subsample of the image. The whole image will be loaded with a lower resolution.
	 */
	@ShortName("Bitmap")
	public static class BitmapWrapper extends AbsObjectWrapper<Bitmap> {
		/**
		 * Initializes the bitmap from the given file.
		 *Note that the image will be downsampled if there is not enough memory available.
		 *Example:<code>
		 *Dim Bitmap1 As Bitmap
		 *Bitmap1.Initialize(File.DirAssets, "X.jpg")</code>
		 */
		public void Initialize(String Dir, String FileName) throws IOException {
			InputStreamWrapper in = null;
			boolean shouldDownSample = false;
			try {
				in = Common.File.OpenInput(Dir, FileName);
				Initialize2(in.getObject());
				in.Close();

			} catch (OutOfMemoryError oom) {
				System.gc();
				in.Close();
				shouldDownSample = true;

			}
			if (shouldDownSample) {
				BA.Log("Downsampling image due to lack of memory.");
				WindowManager wm = (WindowManager) BA.applicationContext.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				InitializeSample(Dir, FileName, display.getWidth() / 2 , display.getHeight() / 2);
			}
		}
		/**
		 * Initializes the bitmap from the given stream.
		 */
		public void Initialize2(InputStream InputStream) {
			Bitmap bmp = BitmapFactory.decodeStream(InputStream);
			if (bmp == null)
				throw new RuntimeException("Error loading bitmap.");
			bmp.setDensity(160);
			setObject(bmp);
		}
		/**
		 * Initializes the bitmap and sets its size.
		 *Note that the bitmap scale will be the same as the device scale.
		 */
		public void InitializeResize(String Dir, String FileName, @Pixel int Width, @Pixel int Height, boolean KeepAspectRatio) throws IOException {
			setObject(initializeSampleImpl(Dir, FileName, Width, Height));
			setObject(Resize(Width, Height, KeepAspectRatio).getObject());
			
		}
		/**
		 * Returns a <b>new</b> bitmap with the given width and height.
		 *Note that the bitmap scale will be the same as the device scale.
		 */
		public BitmapWrapper Resize(float Width, float Height, boolean KeepAspectRatio) {
			if (KeepAspectRatio) {
				int bw = getObject().getWidth();
				int bh = getObject().getHeight();
				float ratioW = bw / (float)Width;
				float ratioH = bh / (float)Height;
				float ratio = Math.max(ratioH, ratioW);
				Width = bw / ratio;
				Height = bh / ratio;
			}
			Width = Math.round(Width);
			Height = Math.round(Height);
			Bitmap res = Bitmap.createScaledBitmap(getObject(), (int)Width, (int)Height, true );
			res.setDensity(Math.round(BA.applicationContext.getResources().getDisplayMetrics().density * 160));
			return (BitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new BitmapWrapper(), res);
		}
		/**
		 * Returns a <b>new</b> rotated bitmap. The bitmap will be rotated clockwise.
		 */
		public BitmapWrapper Rotate(float Degrees) {
			Matrix matrix = new Matrix();
			matrix.postRotate(Degrees);
			Bitmap res = Bitmap.createBitmap(getObject(), 0, 0, 
					getObject().getWidth(), getObject().getHeight(), 
			                              matrix, true);
			return (BitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new BitmapWrapper(), res);
		}
		/**
		 * Returns a <b>new</b> cropped bitmap.
		 */
		public BitmapWrapper Crop(int Left, int Top, int Width, int Height) {
			Bitmap res = Bitmap.createBitmap(getObject(), Left, Top, 
					Math.min(Width, getObject().getWidth() - Left), Math.min(Height, getObject().getHeight() - Top), null, true);
			return (BitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new BitmapWrapper(), res);
		}
		/**
		 * Initializes the bitmap from the given file.
		 *The decoder will subsample the bitmap if MaxWidth or MaxHeight are smaller than the bitmap dimensions.
		 *This can save a lot of memory when loading large images.
		 *Note that the actual dimensions may be larger than the specified values.
		 */
		public void InitializeSample(String Dir, String FileName, @Pixel int MaxWidth, @Pixel int MaxHeight) throws IOException {
			setObject(initializeSampleImpl(Dir, FileName, MaxWidth, MaxHeight));
		}
		private static Bitmap initializeSampleImpl(String Dir, String FileName, int MaxWidth, int MaxHeight) throws IOException {
			InputStreamWrapper in = Common.File.OpenInput(Dir, FileName);
			Options o = new Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in.getObject(), null, o);
			in.Close();
			float r1 = Math.max(o.outWidth / MaxWidth, o.outHeight / MaxHeight);
			Options o2 = null;
			if (r1 > 1f) {
				o2 = new Options();
				o2.inSampleSize = (int) r1;
			}
			Bitmap bmp = null;
			boolean oomFlag = false;
			for (int retries = 5;retries > 0;retries--) {
				try {
					in = Common.File.OpenInput(Dir, FileName);
					bmp = BitmapFactory.decodeStream(in.getObject(), null, o2);
					in.Close();
					break;
				} catch (OutOfMemoryError oom) {
					if (in != null)
						in.Close();
					System.gc();
					if (o2 == null) {
						o2 = new Options();
						o2.inSampleSize = 1;
					}
					o2.inSampleSize *= 2;
					BA.Log("Downsampling image due to lack of memory: " + o2.inSampleSize);
					oomFlag = true;
				}
			}
			if (bmp == null) {
				if (oomFlag)
					throw new RuntimeException("Error loading bitmap (OutOfMemoryError)");
				else
					throw new RuntimeException("Error loading bitmap.");
			}
			bmp.setDensity(160);
			return bmp;
		}

		/**
		 * Initializes the bitmap with a copy of the original image (copying is done if necessary).
		 */
		public void Initialize3(Bitmap Bitmap) {
			Bitmap bmp = Bitmap.createBitmap(Bitmap);
			setObject(bmp);
		}
		/**
		 * Creates a new mutable bitmap with the specified dimensions. You can use a Canvas object to draw on this bitmap.
		 */
		public void InitializeMutable(@Pixel int Width, @Pixel int Height) {
			Bitmap bmp = Bitmap.createBitmap(Width, Height, Config.ARGB_8888);
			setObject(bmp);
		}
		/**
		 * Returns the color of the pixel at the specified position.
		 */
		public int GetPixel(int x, int y) {
			return getObject().getPixel(x, y);
		}
		/**
		 * Returns the bitmap width.
		 */
		public int getWidth() {
			return getObject().getWidth();
		}
		/**
		 * Returns the bitmap height.
		 */
		public int getHeight() {
			return getObject().getHeight();
		}
		/**
		 * Returns the bitmap scale.
		 */
		public float getScale() {
			return getObject().getDensity() / 160f;
		}
		/**
		 * Writes the bitmap to the output stream.
		 *Quality - Value between 0 (smaller size, lower quality) to 100 (larger size, higher quality), 
		 *which is a hint for the compressor for the required quality.
		 *Format - JPEG or PNG.
		 * 
		 *Example:<code>
		 *Dim Out As OutputStream
		 *Out = File.OpenOutput(File.DirRootExternal, "Test.png", False)
		 *Bitmap1.WriteToStream(out, 100, "PNG")
		 *Out.Close</code>
		 */
		public void WriteToStream(OutputStream OutputStream, int Quality, CompressFormat Format) {
			getObject().compress(Format, Quality, OutputStream);
		}
		@Hide
		@Override
		public String toString() {
			String s = baseToString();
			if (IsInitialized()) {
				s += ": " + getWidth() + " x " + getHeight() + ", scale = " + String.format("%.2f", (getScale()));
			}
			return s;
		}
	}
	/**
	 * Holds four coordinates which represent a rectangle.
	 */
	@ShortName("Rect")
	public static class RectWrapper extends AbsObjectWrapper<Rect> {
		public void Initialize(int Left, int Top, int Right, int Bottom) {
			Rect r = new Rect(Left, Top, Right, Bottom);
			setObject(r);
		}
		public int getLeft() {return getObject().left;} public void setLeft(int Left) {getObject().left = Left;}
		public int getTop() {return getObject().top;} public void setTop(int Top) {getObject().top = Top;}
		public int getRight() {return getObject().right;} public void setRight(int Right) {getObject().right = Right;}
		public int getBottom() {return getObject().bottom;} public void setBottom(int Bottom) {getObject().bottom = Bottom;}
		/**
		 * Gets or sets the rectangle width.
		 */
		public int getWidth() {
			return getObject().right - getObject().left;
		}
		public void setWidth(int w) {
			getObject().right = getObject().left + w;
		}
		/**
		 * Gets or sets the rectangle height.
		 */
		public int getHeight() {
			return getObject().bottom - getObject().top;
		}
		public void setHeight(int h) {
			getObject().bottom = getObject().top + h;
		}
		/**
		 * Returns the horizontal center.
		 */
		public int getCenterX() {return getObject().centerX();};
		/**
		 * Returns the vertical center.
		 */
		public int getCenterY() {return getObject().centerY();};
		@Hide
		@Override
		public String toString() {
			String s = baseToString();
			if (IsInitialized()) {
				s += "(" + getLeft() + ", " + getTop() + ", " + getRight() + ", " + getBottom() + ")";
			}
			return s;
		}
	}
	/**
	 * A path is a collection of points that represent a connected path.
	 *The first point is set when it is initialized, and then other points are added with LineTo.
	 */
	@ShortName("Path")
	public static class PathWrapper extends AbsObjectWrapper<Path> {
		/**
		 * Initializes the path and sets the value of the first point.
		 */
		public void Initialize(float x, float y) {
			Path path = new Path();
			path.moveTo(x, y);
			setObject(path);
		}
		/**
		 * Adds a line from the last point to the specified point.
		 */
		public void LineTo(float x, float y) {
			getObject().lineTo(x, y);
		}
	}
}
