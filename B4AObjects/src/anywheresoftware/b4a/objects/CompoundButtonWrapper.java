package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;

@Hide
@DontInheritEvents
@Events(values={"CheckedChange(Checked As Boolean)"})
public class CompoundButtonWrapper<T extends CompoundButton> extends TextViewWrapper<T> {
	@Hide
	@Override
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		innerInitialize(ba, eventName, keepOldObject, true);
	}
	protected void innerInitialize(final BA ba, final String eventName, boolean keepOldObject, boolean addCheckedChangeEvent) {

		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_checkedchange")) {
			getObject().setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					ba.raiseEvent2(getObject(), false, eventName + "_checkedchange", false, isChecked);
				}
				
			});
		}
	}
	public boolean getChecked() {
		return getObject().isChecked();
	}
	@RaisesSynchronousEvents
	public void setChecked(boolean Value) {
		getObject().setChecked(Value);
	}
	@Hide
	@Override
	public String toString() {
		String s = super.toString();
		if (IsInitialized())
			return s += ", Checked=" + getChecked();
		else
			return s;
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer) throws Exception{
		CompoundButton v = (CompoundButton) TextViewWrapper.build(prev, props, designer);
		v.setChecked((Boolean)props.get("isChecked"));
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		if (drawProps != null) {
			Drawable d = DynamicBuilder.build(prev, drawProps, designer, null);
			if (d != null)
				v.setBackgroundDrawable(d);
		}
		return v;
	}
	/**
	 * A CheckBox view. Unlike RadioButtons each CheckBox can be checked independently.
	 */
	@ActivityObject
	@ShortName("CheckBox")
	public static class CheckBoxWrapper extends CompoundButtonWrapper<CheckBox> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new CheckBox(ba.context));
			super.innerInitialize(ba, eventName, true);
		}
		@Hide
		public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			if (prev == null) {
				prev = ViewWrapper.buildNativeView((Context)tag, CheckBox.class, props, designer);
			}
			CheckBox v = (CheckBox) CompoundButtonWrapper.build(prev, props, designer);
			return v;
		}
	}
	/**
	 * A RadioButton view. Only one RadioButton in a group can be checked. When a different RadioButton is checked all others will
	 * automatically be unchecked. Grouping is done by adding RadioButtons to the same activity or panel.
	 */
	@ActivityObject
	@ShortName("RadioButton")
	public static class RadioButtonWrapper extends CompoundButtonWrapper<RadioButton> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new RadioButton(ba.context));
			super.innerInitialize(ba, eventName, true, false);
			getObject().setOnCheckedChangeListener(new RadioButtonListener(eventName, ba, getObject()));
		}
		private static class RadioButtonListener implements OnCheckedChangeListener {
			private String eventName;
			private BA ba;
			private RadioButton current;
			public RadioButtonListener(String eventName, BA ba, RadioButton current) {
				this.eventName = eventName;
				this.ba = ba;
				this.current = current;
			}
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!isChecked)
					return;
				ViewParent vp = current.getParent();
				if (vp instanceof ViewGroup) {
					ViewGroup vg = (ViewGroup) vp;
					for (int i = 0;i < vg.getChildCount();i++) {
						View v = vg.getChildAt(i);
						if (v instanceof RadioButton) {
							if (v != current) {
								RadioButton rb = (RadioButton) v;
								if (rb.isChecked())
									rb.setChecked(false);
							}
						}
					}
				}
				if (eventName.length() > 0)
					ba.raiseEvent2(current, false, eventName + "_checkedchange", false, isChecked);
			}
			
		}
		@Hide
		public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			if (prev == null) {
				prev = ViewWrapper.buildNativeView((Context)tag, RadioButton.class, props, designer);
			}
			RadioButton v = (RadioButton) CompoundButtonWrapper.build(prev, props, designer);
			return v;
		}
		
	}
	/**
	 * A ToggleButton view. This view which is similar to a button has two modes: ON and OFF.
	 *When the user presses on it, it will change its mode.
	 *You can set the text with the TextOn and TextOff properties.
	 */
	@ActivityObject
	@ShortName("ToggleButton")
	public static class ToggleButtonWrapper extends CompoundButtonWrapper<ToggleButton> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject) {
				setObject(new ToggleButton(ba.context));
				getObject().setText("");
			}
			super.innerInitialize(ba, eventName, true);
		}
		/**
		 * Gets or sets the text that will appear in the ON mode.
		 */
		public String getTextOn() {
			return String.valueOf(getObject().getTextOn());
		}
		public void setTextOn(CharSequence value) {
			getObject().setTextOn(value);
			setChecked(getChecked()); //refresh the SyncState
			
		}
		/**
		 * Gets or sets the text that will appear in the OFF mode.
		 */
		public String getTextOff() {
			return String.valueOf(getObject().getTextOff());
		}
		public void setTextOff(CharSequence value) {
			getObject().setTextOff(value);
			setChecked(getChecked());
		}
		@Hide
		@Override
		public String getText() {
			return "";
		}
		@Hide
		@Override
		public void setText(CharSequence Text) {
			
		}
		@Hide
		public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			if (prev == null) {
				prev = ViewWrapper.buildNativeView((Context)tag, ToggleButton.class, props, designer);
			}
			ToggleButton v = (ToggleButton) prev;
			v.setTextOn((String)props.get("textOn"));
			v.setTextOff((String)props.get("textOff"));
			v = (ToggleButton) CompoundButtonWrapper.build(prev, props, designer);
			v.setTextColor((Integer)props.get("textColor")); //override the grey text that will be assigned because text = ""
			return v;
		}
	}
}
