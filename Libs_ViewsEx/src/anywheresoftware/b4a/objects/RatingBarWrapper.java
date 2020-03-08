
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
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar.OnRatingBarChangeListener;
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

@ShortName("RatingBar")
@ActivityObject
@Events(values={"ValueChanged (Value As Float, UserChanged As Boolean)"})
@DesignerProperties(values = {
		@Property(key="Rating", displayName="Rating", defaultValue="0", fieldType="Float", minRange = "0"),
		@Property(key="NumberOfStars", displayName="Max Number Of Stars", defaultValue="5", fieldType="Int", minRange = "1"),
		@Property(key="Indicator", displayName="Indicator (read-only)", defaultValue="False", fieldType="Boolean", description="If true then the user will not be able to change the rating."),
		@Property(key="StepSize", displayName="Step Size", defaultValue="1", fieldType="Float", minRange="0")
})
public class RatingBarWrapper extends ViewWrapper<RatingBar> implements DesignerCustomView{
	@Override
	public void DesignerCreateView(PanelWrapper base, LabelWrapper lw, Map props) {
		RatingBar rb = getObject();
		CustomViewWrapper.replaceBaseWithView2(base, rb);
		LayoutParams lp = rb.getLayoutParams();
		lp.width = LayoutParams.WRAP_CONTENT;
		lp.height = LayoutParams.WRAP_CONTENT;
		rb.setIsIndicator((Boolean)props.Get("Indicator"));
		rb.setNumStars((Integer)props.Get("NumberOfStars"));
		rb.setRating((Float)props.Get("Rating"));
		rb.setStepSize((Float)props.Get("StepSize"));
	}
	@Hide
	@Override
	public void _initialize(final BA ba, Object activityClass, String EventName) {
		final RatingBar rb = new RatingBar(ba.context);
		final String eventName = EventName.toLowerCase(BA.cul);
		setObject(rb);
		innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_valuechanged")) {
			getObject().setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

				@Override
				public void onRatingChanged(RatingBar ratingBar, float rating,
						boolean fromUser) {
					ba.raiseEventFromUI(rb, eventName + "_valuechanged", rating, fromUser);
				}
			});
		}
	}
	/**
	 * Gets or sets the rating.
	 */
	public float getRating() {
		return getObject().getRating();
	}
	public void setRating(float f) {
		getObject().setRating(f);
	}
	
}
