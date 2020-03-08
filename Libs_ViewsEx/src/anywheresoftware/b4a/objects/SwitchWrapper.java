
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

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DesignerProperties;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Property;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common.DesignerCustomView;
import anywheresoftware.b4a.objects.collections.Map;

@ShortName("Switch")
@ActivityObject
@Events(values={"CheckedChange(Checked As Boolean)"})
@DesignerProperties(values = {
	@Property(key="Checked", displayName="Checked", defaultValue="False", fieldType="Boolean")
})
@Version(1.30f)
public class SwitchWrapper extends ViewWrapper<Switch> implements DesignerCustomView{
	@Override
	public void DesignerCreateView(PanelWrapper base, LabelWrapper lw, Map props) {
		getObject().setChecked((Boolean)props.Get("Checked"));
		CustomViewWrapper.replaceBaseWithView2(base, getObject());
	}
	@Hide
	@Override
	public void _initialize(final BA ba, Object activityClass, String EventName) {
		final Switch _switch = new Switch(ba.context);
		final String eventName = EventName.toLowerCase(BA.cul);
		setObject(_switch);
		innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_checkedchange")) {
			getObject().setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					ba.raiseEventFromUI(_switch,eventName + "_checkedchange", isChecked);
				}
				
			});
		}
	}
	public boolean getChecked() {
		return getObject().isChecked();
	}
	public void setChecked(boolean Value) {
		getObject().setChecked(Value);
	}
	
}
