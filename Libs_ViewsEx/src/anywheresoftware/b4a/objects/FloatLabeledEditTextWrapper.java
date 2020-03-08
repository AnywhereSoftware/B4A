
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

import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DesignerProperties;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Property;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common.DesignerCustomView;
import anywheresoftware.b4a.objects.collections.Map;

@ShortName("FloatLabeledEditText")
@DesignerProperties(values = {
		@Property(key="Hint", displayName="Hint", defaultValue="Hint", fieldType="String"),
		@Property(key="HintColor", displayName="Hint Color", defaultValue="Null", fieldType="Color"),
		@Property(key="TextColor", displayName="Text Color", defaultValue="Null", fieldType="Color")
})
@Events(values={"TextChanged (Old As String, New As String)", "EnterPressed", "FocusChanged (HasFocus As Boolean)"})
@ActivityObject
/**
 * An improved EditText with a more useful hint label. This type is based on the this open source project: https://github.com/wrapp/floatlabelededittext.
 */
public class FloatLabeledEditTextWrapper extends ViewWrapper<FloatLabeledEditText> implements DesignerCustomView{
	@Override
	public void DesignerCreateView(PanelWrapper base, LabelWrapper lw, Map props) {
		CustomViewWrapper.replaceBaseWithView2(base, getObject());
		EditTextWrapper et = getEditText();
		et.setText(lw.getText());
		et.setTextSize(lw.getTextSize());
		et.setTypeface(lw.getTypeface());
		int hintColor = BA.gm(props.getObject(), "HintColor",  ViewWrapper.defaultColor);
		if (hintColor != ViewWrapper.defaultColor) {
			et.setHintColor(hintColor);
			getObject().mHintTextView.setTextColor(hintColor);
		}
		int textColor = BA.gm(props.getObject(), "TextColor",  ViewWrapper.defaultColor);
		if (textColor != ViewWrapper.defaultColor) {
			et.setTextColor(textColor);
		}
		getObject().setHint((String)props.Get("Hint"));
	}
	@Hide
	@Override
	public void _initialize(final BA ba, Object activityClass, String EventName) {
		final FloatLabeledEditText fle = new FloatLabeledEditText(ba.context, null);
		final String eventName = EventName.toLowerCase(BA.cul);
		EditTextWrapper etw = new EditTextWrapper();
		etw.Initialize(ba, EventName);
		etw.setSingleLine(true);
		fle.addView(etw.getObject(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		setObject(fle);
		innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_focuschanged")) {
			getObject().MyFocusListener = (new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					ba.raiseEventFromUI(getObject(), eventName + "_focuschanged", hasFocus);
				}
			});
		}
		
	}
	/**
	 * Returns the internal EditText.
	 */
	public EditTextWrapper getEditText() {
		return (EditTextWrapper) AbsObjectWrapper.ConvertToWrapper(new EditTextWrapper(), getObject().getEditText());
	}
	/**
	 * Gets or sets the hint text.
	 */
	public void setHint(String text) {
		getObject().setHint(text);
	}
	public String getHint() {
		CharSequence c = getObject().getHint();
		return c == null ? "" : String.valueOf(c);
	}
	/**
	 * Gets or sets the text.
	 */
	public String getText() {
		return getObject().getEditText().getText().toString();
	}
	public void setText(Object Text) {
		CharSequence cs;
		if (Text instanceof CharSequence)
			cs = (CharSequence) Text;
		else
			cs = String.valueOf(Text);
		getObject().getEditText().setText(cs);
	}
	
}
