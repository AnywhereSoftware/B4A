
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

import java.util.Iterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.constants.Colors;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper;

/**
 * A view that draws itself with hardware accelerated graphics. Suitable for 2d games.
 *See this <link>tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/20038-gameview-create-2d-android-games-part-i.html</link>.
 *The hardware acceleration method used is only available in Android 3.0 and above (API level 11 and above).
 */
@ActivityObject
@Version(0.9f)
@ShortName("GameView")
@DontInheritEvents
@Events(values={"Touch (Action As Int, X As Float, Y As Float)"})
public class GameViewWrapper extends ViewWrapper<GameViewWrapper.MyPanel>{

	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new MyPanel(ba.context));
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_touch")) {
			getObject().setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					ba.raiseEventFromUI(getObject(), eventName + "_touch", event.getAction(), event.getX(),
							event.getY());
					return true;

				}
			});
		}
	}
	/**
	 * Tests whether hardware acceleration is supported.
	 */
	public boolean getIsHardwareAccelerated() {
		return getObject().isHardwareAccelerated();
	}
	/**
	 * Returns the list of BitmapData objects.
	 */
	public List getBitmapsData() {
		return getObject().sprites;
	}
	@ShortName("BitmapData")
	public static class BitmapData {
		/**
		 * The bitmap that will be drawn.
		 */
		public BitmapWrapper Bitmap = new BitmapWrapper();
		/**
		 * The source rectangle. Determines the bitmap's region that will be drawn. The complete bitmap will
		 *be drawn if the rectangle is uninitialized.
		 */
		public RectWrapper SrcRect = new RectWrapper();
		/**
		 * The target rectangle. Determines the location and size of the drawn bitmap.
		 */
		public RectWrapper DestRect = new RectWrapper();
		/**
		 * Number of degrees to rotate the bitmap.
		 */
		public int Rotate = 0;
		/**
		 * Flips the bitmap based on one of the FLIP constants.
		 */
		public int Flip = 0;
		public static final int FLIP_NONE = 0, FLIP_VERTICALLY = 1, FLIP_HORIZONTALLY = 2, FLIP_BOTH = 3;
		/**
		 * If Delete is True then the BitmapData will be removed from the list when GameView is redrawn.
		 */
		public boolean Delete = false;
		@Hide
		@Override
		public String toString() {
			return "Src: " + String.valueOf(SrcRect) + ", Dest: " + String.valueOf(DestRect) + ", Bitmap: " + 
			String.valueOf(Bitmap) + ", Delete: " + String.valueOf(Delete);
		}
		
	}
	@Hide
	public static class MyPanel extends View {
		public List sprites = new List();
		public Paint paint = new Paint();
		public MyPanel(Context context) {
			super(context);
			sprites.Initialize();
			paint.setAntiAlias(false);
			paint.setDither(false);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		}
		@Override
		public boolean isOpaque() {
			return true;
		}
		@Override
		protected void onDraw(Canvas c) {
			Iterator<Object> it = sprites.getObject().iterator();
			while (it.hasNext()) {
				Object o = it.next();
				BitmapData br = (BitmapData)o;
				if (br.Delete) {
					it.remove();
				}
				else {
					Rect destRect = br.DestRect.getObject();
					if (br.Rotate != 0 || br.Flip != 0) {
						c.save();
						if (br.Rotate != 0)
							c.rotate(br.Rotate, destRect.centerX(), destRect.centerY());
						if (br.Flip != 0) {
							c.scale((br.Flip & BitmapData.FLIP_HORIZONTALLY) == BitmapData.FLIP_HORIZONTALLY ? -1 : 1,
									(br.Flip & BitmapData.FLIP_VERTICALLY) == BitmapData.FLIP_VERTICALLY ? -1 : 1,
											destRect.centerX(), destRect.centerY());
						}
						c.drawBitmap(br.Bitmap.getObject(), br.SrcRect.getObjectOrNull(), destRect, null);
						c.restore();
					}
					else {
						c.drawBitmap(br.Bitmap.getObject(), br.SrcRect.getObjectOrNull(), destRect, paint);
					}
				}
			}
		}
	}
}

