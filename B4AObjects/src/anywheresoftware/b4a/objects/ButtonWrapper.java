package anywheresoftware.b4a.objects;

import java.util.Arrays;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
/**
 * A Button view.
 *If you change the button's background you will usually want to use StateListDrawable which allows you to set the "default" drawable
 *and the "pressed" drawable.
 *Note that the Up and Down events are still implemented but should not be used as they will not work properly on all devices.
 */
@ShortName("Button")
@ActivityObject
public class ButtonWrapper extends TextViewWrapper<Button>{

	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject) {
			setObject(new Button(ba.context));
			removeCaps(getObject());
		}
		
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_down") || ba.subExists(eventName + "_up")) {
			getObject().setOnTouchListener(new View.OnTouchListener() {
				private boolean down = false;
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						down = true;
						ba.raiseEventFromUI(getObject(), eventName + "_down");
					}
					else if (down && (
							event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)) {
						down = false;
						ba.raiseEventFromUI(getObject(), eventName + "_up");
					}
					
					else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						int[] states = v.getDrawableState();
						if (states == null)
							return false;
						for (int i = 0;i < states.length;i++) {
							if (states[i] == anywheresoftware.b4a.objects.drawable.StateListDrawable.State_Pressed) {
								if (down)
									return false; //don't do anything
								else {
									ba.raiseEventFromUI(getObject(), eventName + "_down");
									down = true;
									return false;
								}
							}
						}
						//state not found
						if (down) {
							ba.raiseEventFromUI(getObject(), eventName + "_up");
							down = false;
						}
					}
					return false;
				}
				
			});
		}
	}
	private static void removeCaps(Button btn) {
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			try {
				TextView.class.getDeclaredMethod("setAllCaps", boolean.class).invoke(btn, false);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, Button.class, props, designer);
			removeCaps((Button) prev);
		}
		TextView v = (TextView) TextViewWrapper.build(prev, props, designer);
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		Drawable d = DynamicBuilder.build(prev, drawProps, designer,null);
		//there is a bug here in design mode. The default drawable is not stored in the tag...
		if (d != null) //will be null in !designer and defaultDrawable.
			v.setBackgroundDrawable(d);
		if (designer)
			v.setPressed((Boolean)props.get("pressed"));
		return v;
	}
	
}
