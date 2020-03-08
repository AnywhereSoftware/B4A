package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

/**
 * A Label view that shows read-only text.
 */
@ShortName("Label")
@ActivityObject
public class LabelWrapper extends TextViewWrapper<TextView> {
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new TextView(ba.context));
		super.innerInitialize(ba, eventName, true);
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, TextView.class, props, designer);
		}
		TextView v = (TextView) TextViewWrapper.build(prev, props, designer);
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		if (drawProps != null) {
			Drawable d = DynamicBuilder.build(prev, drawProps, designer, null);
			if (d != null)
				v.setBackgroundDrawable(d);
		}
		return v;
	}
	
}
