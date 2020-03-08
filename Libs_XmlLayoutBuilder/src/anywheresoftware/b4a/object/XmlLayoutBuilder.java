
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
 
 package anywheresoftware.b4a.object;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater.Factory;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.AnimationWrapper;
import anywheresoftware.b4a.objects.AutoCompleteEditTextWrapper;
import anywheresoftware.b4a.objects.ButtonWrapper;
import anywheresoftware.b4a.objects.EditTextWrapper;
import anywheresoftware.b4a.objects.ImageViewWrapper;
import anywheresoftware.b4a.objects.LabelWrapper;
import anywheresoftware.b4a.objects.PanelWrapper;
import anywheresoftware.b4a.objects.SeekBarWrapper;
import anywheresoftware.b4a.objects.ViewWrapper;
import anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper;
import anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper;
import anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper;

@ActivityObject
@ShortName("XmlLayoutBuilder")
@DependsOn(values={"Animation"})
@Version(1.0f)
public class XmlLayoutBuilder {
	private BA ba;
	private static final Class<?>[] mConstructorSignature = new Class[] {
        Context.class, AttributeSet.class};
	/**
	 * Loads an Xml layout file and adds the layout to the given parent.
	 */
	public void LoadXmlLayout(BA ba, ViewGroup Parent, String LayoutFile) {
		this.ba = ba;
		if (ba.activity.getLayoutInflater().getFactory() == null)
			ba.activity.getLayoutInflater().setFactory(new MyFactory());
		ba.activity.getLayoutInflater().inflate(GetResourceId("layout", LayoutFile), Parent);
	}
	/**
	 * Gets the resource ID of the resource with the given type and name.
	 */
	public int GetResourceId(String Type, String Name) {
		return BA.applicationContext.getResources().getIdentifier(Name, Type, BA.packageName);
	}
	/**
	 * Returns the view with the given ID.
	 */
	public View GetView(BA ba, String Name) {
		return ba.activity.findViewById(GetResourceId("id", Name));
	}
	/**
	 * Returns the drawable object with the given ID.
	 */
	public Drawable GetDrawable(String Name) {
		return BA.applicationContext.getResources().getDrawable(GetResourceId("drawable", Name));
	}
	/**
	 * Returns the string with the given ID.
	 */
	public String GetString(String Name) {
		return BA.applicationContext.getResources().getString(GetResourceId("string", Name));
	}
	/**
	 * Loads an animation object from the Xml file with the given name.
	 *AnimationEventName - The Animation event name.
	 */
	public AnimationWrapper LoadAnimation(BA ba, String Name, String AnimationEventName) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		AnimationWrapper aw = new AnimationWrapper();
		Animation a = AnimationUtils.loadAnimation(ba.context, GetResourceId("anim", Name));
		Method m = AnimationWrapper.class.getDeclaredMethod("init", anywheresoftware.b4a.BA.class, String.class, Animation.class);
		m.setAccessible(true);
		m.invoke(aw, ba, AnimationEventName.toLowerCase(BA.cul), a);
		return aw;
		
	}
	class MyFactory implements Factory {
		private final HashMap<String, Class<?>> wrappers = new HashMap<String, Class<?>>();
		MyFactory() {
			wrappers.put("EditText", EditTextWrapper.class);
			wrappers.put("Button", ButtonWrapper.class);
			wrappers.put("CheckBox", CheckBoxWrapper.class);
			wrappers.put("RadioButton", RadioButtonWrapper.class);
			wrappers.put("TextView", LabelWrapper.class);
			wrappers.put("AutoCompleteTextView", AutoCompleteEditTextWrapper.class);
			wrappers.put("ToggleButton", ToggleButtonWrapper.class);
			wrappers.put("ImageView", ImageViewWrapper.class);
			wrappers.put("SeekBar", SeekBarWrapper.class);
			
		}
		@SuppressWarnings("unchecked")
		@Override
		public View onCreateView(String tagName, Context arg1, AttributeSet attributes) {
			Class<?> cls = wrappers.get(tagName);
			if (cls != null) {
				String eventName = attributes.getAttributeValue("http://schemas.android.com/apk/res/android", "tag");
				if (eventName != null) {
					try {
						ViewWrapper vw = (ViewWrapper<?>) cls.newInstance();
						View v = (View)Class.forName("android.widget." + tagName).getConstructor(mConstructorSignature).newInstance(ba.context, attributes);
						int id = v.getId();
						vw.setObject(v);
						vw.innerInitialize(ba, eventName.toLowerCase(BA.cul), true);
						v.setId(id);
						return v;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			} else if (tagName.equals("Panel")) {
				View v = new View(ba.context, attributes);
				BALayout bl = new BALayout(ba.context);
				bl.setLayoutParams(new BALayout.LayoutParams(v.getLeft(), v.getTop(), v.getWidth(), v.getHeight()));
				bl.setId(v.getId());
				return bl;
			}
			return null;
		}
		
	}
}
