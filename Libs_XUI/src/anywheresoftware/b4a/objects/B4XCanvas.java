
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

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.view.View;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Pixel;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.Regex;
import anywheresoftware.b4a.keywords.constants.Colors;
import anywheresoftware.b4a.objects.B4XViewWrapper.B4XBitmapWrapper;
import anywheresoftware.b4a.objects.B4XViewWrapper.B4XFont;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper;

/**
 * A cross platform canvas.
 */
@ShortName("B4XCanvas")
public class B4XCanvas {
	@Hide
	public CanvasWrapper cvs;
	@Hide
	public B4XViewWrapper target;
	private B4XRect targetRect;
	public void Initialize(B4XViewWrapper View) {
		cvs = new CanvasWrapper();
		target = View;
		cvs.Initialize(View.getViewObject());
		cvs.setAntiAlias(true);
		targetRect = new B4XRect();
		targetRect.Initialize(0, 0, View.getWidth(), View.getHeight());
	}
	public void Resize(float Width, float Height) {
		target.SetLayoutAnimated(0, target.getLeft(), target.getTop(), (int)(Width + 0.5), (int)(Height + 0.5));
		Initialize(target);
	}
	public B4XRect getTargetRect() {
		return targetRect;
	}
	public B4XViewWrapper getTargetView() {
		return target;
	}
	public void Invalidate() {
		target.getViewObject().invalidate();
	}
	public void DrawLine(float x1, float y1, float x2, float y2, int Color, float StrokeWidth) {
		cvs.DrawLine(x1, y1, x2, y2, Color, StrokeWidth);
	}
	public B4XBitmapWrapper CreateBitmap() {
		return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), cvs.getBitmap().getObject());
	}
	public void DrawRect(B4XRect Rect, int Color, boolean Filled, float StrokeWidth) {
		cvs.DrawRect(Rect.toRect(), Color, Filled, StrokeWidth);
	}
	public void DrawCircle(float x, float y, float Radius, int Color, boolean Filled, float StrokeWidth) {
		cvs.DrawCircle(x, y, Radius, Color, Filled, StrokeWidth);
	}
	public void DrawBitmap (Bitmap Bitmap, B4XRect Destination) {
		cvs.DrawBitmap(Bitmap, null, Destination.toRect());
	}
	public void DrawBitmapRotated(Bitmap Bitmap, B4XRect Destination, float Degrees) {
		cvs.DrawBitmapRotated(Bitmap, null, Destination.toRect(), Degrees);
	}
	
	public void ClipPath(B4XPath Path) throws Exception{
		cvs.ClipPath(Path.getObject());
	}
	public void RemoveClip() {
		cvs.RemoveClip();
	}
	public void DrawPath(B4XPath Path, int Color, boolean Filled, float StrokeWidth) throws Exception{
		cvs.DrawPath(Path.getObject(), Color, Filled, StrokeWidth);
	}
	public void DrawPathRotated(B4XPath Path, int Color, boolean Filled, float StrokeWidth, float Degrees, float CenterX, float CenterY) throws Exception{
		cvs.canvas.save();
		try {
			cvs.canvas.rotate(Degrees, CenterX, CenterY);
			cvs.DrawPath(Path.getObject(), Color, Filled, StrokeWidth);
		} finally {
			cvs.canvas.restore();
		}
	}
	public void ClearRect(B4XRect Rect) {
		cvs.DrawRect(Rect.toRect(), Colors.Transparent, true, 0);
	}
	public void DrawText(BA ba, String Text, float x, float y, B4XFont Font, int Color, Align Alignment)  {
		cvs.DrawText(ba, Text, x, y, Font.typeface, Font.getSize(), Color, Alignment);
		cvs.setAntiAlias(true);
	}
	public void DrawTextRotated(BA ba, String Text, float x, float y, B4XFont Font, int Color, Align Alignment, float Degree)  {
		cvs.DrawTextRotated(ba, Text, x, y, Font.typeface, Font.getSize(), Color, Alignment, Degree);
		cvs.setAntiAlias(true);
	}
	public void Release() {
		
	}
	public B4XRect MeasureText(String Text, B4XFont Font) {
		Paint paint = cvs.paint;
		paint.setTextSize(Font.getSize() * BA.applicationContext.getResources().getDisplayMetrics().scaledDensity);
		paint.setTypeface(Font.typeface);
		paint.setStrokeWidth(0);
		paint.setStyle(Style.STROKE);
		paint.setTextAlign(Align.LEFT);
		Rect r = new Rect();
		if (Text.startsWith(" "))
			Text = "." + Text.substring(1);
		if (Text.endsWith(" "))
			Text = Text.substring(0, Text.length() - 1) + ".";
		paint.getTextBounds(Text, 0, Text.length(), r);
		B4XRect xr = new B4XRect();
		xr.Initialize(r.left, r.top, r.right, r.bottom);
		return xr;
	}
	
	@ShortName("B4XRect")
	public static class B4XRect {
		private RectF rf;
		private Rect ri;
		public void Initialize(@Pixel float Left, @Pixel float Top, @Pixel float Right, @Pixel float Bottom) {
			rf = new RectF(Left, Top, Right, Bottom);
			ri = new Rect();
		}
		public float getLeft() {return rf.left;} public void setLeft(float Left) {rf.left = Left;}
		public float getTop() {return rf.top;} public void setTop(float Top) {rf.top = Top;}
		public float getRight() {return rf.right;} public void setRight(float Right) {rf.right = Right;}
		public float getBottom() {return rf.bottom;} public void setBottom(float Bottom) {rf.bottom = Bottom;}
		/**
		 * Gets or sets the rectangle width.
		 */
		public float getWidth() {
			return rf.right - rf.left;
		}
		public void setWidth(float w) {
			rf.right = rf.left + w;
		}
		/**
		 * Gets or sets the rectangle height.
		 */
		public float getHeight() {
			return rf.bottom - rf.top;
		}
		public void setHeight(float h) {
			rf.bottom = rf.top + h;
		}
		/**
		 * Returns the horizontal center.
		 */
		public float getCenterX() {return rf.centerX();};
		/**
		 * Returns the vertical center.
		 */
		public float getCenterY() {return rf.centerY();};
		@Hide
		public Rect toRect() {
			rf.round(ri);
			return ri;
		}
		@Hide
		@Override
		public String toString() {
			if (rf != null)
				return rf.toString();
			else
				return "Not initialized";
		}
		
	}
	@ShortName("B4XPath")
	public static class B4XPath extends AbsObjectWrapper<Path> {
		/**
		 * Initializes the path and sets the value of the first point.
		 */
		public B4XPath Initialize(@Pixel float x,@Pixel float y) {
			Path path = new Path();
			path.moveTo(x, y);
			setObject(path);
			return this;
		}
		public B4XPath InitializeOval(B4XRect Rect) {
			Path path = new Path();
			path.addOval(Rect.rf, Direction.CW);
			setObject(path);
			return this;
		}
		public B4XPath InitializeArc(@Pixel float x, @Pixel float y, @Pixel float Radius, float StartingAngle, float SweepAngle) {
			Path path = new Path();
			path.moveTo(x, y);
			path.arcTo(new RectF(x - Radius, y - Radius, x + Radius, y + Radius), StartingAngle, SweepAngle);
			setObject(path);
			return this;
		}
		public B4XPath InitializeRoundedRect (B4XRect Rect, float CornersRadius) {
			Path path = new Path();
			path.addRoundRect(Rect.rf, CornersRadius, CornersRadius, Direction.CW);
			setObject(path);
			return this;
		}
		/**
		 * Adds a line from the last point to the specified point.
		 */
		public B4XPath LineTo(@Pixel float x,@Pixel float y) {
			getObject().lineTo(x, y);
			return this;
		}
		
	}
}
