package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
/**
 * A progress bar view. The Progress property sets the progress value which is between 0 to 100.
 */
@ActivityObject
@ShortName("ProgressBar")
@DontInheritEvents
public class ProgressBarWrapper extends ViewWrapper<ProgressBar>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject) {
			ProgressBar pb1 = new ProgressBar(ba.context, null, android.R.attr.progressBarStyleHorizontal);
			ProgressBar pb = new ProgressBar(ba.context, null, android.R.attr.progressBarStyle);
			pb1.setIndeterminateDrawable(pb.getIndeterminateDrawable());
			setObject(pb1);
			getObject().setMax(100);
			getObject().setIndeterminate(false);
		}
		super.innerInitialize(ba, eventName, true);
	}
	
	/**
	 * Gets or sets the progress value.
	 */
	public int getProgress() {
		return getObject().getProgress();
	}
	public void setProgress(int value) {
		getObject().setProgress(value);
	}
	/**
	 * Gets or sets whether the progress bar is in indeterminate mode (cyclic animation).
	 */
	public void setIndeterminate(boolean value) {
		getObject().setIndeterminate(value);
		getObject().invalidate();
	}
	public boolean getIndeterminate() {
		return getObject().isIndeterminate();
	}
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		boolean indeterminate = (Boolean) props.get("indeterminate");
		if (prev == null) {
			String nativeClass = (String)props.get("nativeClass");
			if (nativeClass != null && nativeClass.length() > 0) {
				ViewWrapper.buildNativeView((Context)tag, ProgressBar.class, props, designer);
			}
			else {
				ProgressBar pb1 = new ProgressBar((Context)tag, null, android.R.attr.progressBarStyleHorizontal);
				ProgressBar pb = new ProgressBar((Context)tag, null, android.R.attr.progressBarStyle);
				pb1.setIndeterminateDrawable(pb.getIndeterminateDrawable());
				prev = pb1;
			}
		}
		ProgressBar v = (ProgressBar) ViewWrapper.build(prev, props, designer);
		v.setIndeterminate(indeterminate);
		v.setMax(100);
		if (designer)
			v.setProgress(20);
		return v;
	}
}
