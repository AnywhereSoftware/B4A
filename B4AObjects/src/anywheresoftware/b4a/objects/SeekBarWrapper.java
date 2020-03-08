package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
/**
 * A view that allows the user to set a value by dragging a slider. Similar to WinForms TrackBar.
 *The ValueChanged event is raised whenever the value is changed. The UserChanged parameter can be used to distinguish between changes done by the user and changes done programmatically.
 */
@ActivityObject
@ShortName("SeekBar")
@DontInheritEvents
@Events(values={"ValueChanged (Value As Int, UserChanged As Boolean)"})
public class SeekBarWrapper extends ViewWrapper<SeekBar>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new SeekBar(ba.context));
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_valuechanged")) {
			getObject().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
					ba.raiseEventFromUI(getObject(), eventName + "_valuechanged", progress, fromUser);
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
					
				}

				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}
				
			});
		}
	}
	/**
	 * Gets or sets the maximum allowed value.
	 */
	public int getMax() {
		return getObject().getMax();
	}
	public void setMax(int value) {
		getObject().setMax(value);
	}
	/**
	 * Gets or sets the current value.
	 */
	public int getValue() {
		return getObject().getProgress();
	}
	public void setValue(int value) {
		getObject().setProgress(value);
	}
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, SeekBar.class, props, designer);
		}
		SeekBar v = (SeekBar) ViewWrapper.build(prev, props, designer);
		int oldMax = v.getMax();
		v.setMax((Integer)props.get("max"));
		if (v.getMax() != oldMax)
			v.setProgress(-1);
		v.setProgress((Integer)props.get("value"));
		return v;
	}
}
