
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
 
 package anywheresoftware.b4a;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.WeakHashMap;

import android.util.Log;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;


public class AbsObjectWrapper<T> implements ObjectWrapper<T> {
	private final static WeakHashMap<Object, HashMap<String, Object>> extraMap = new WeakHashMap<Object, HashMap<String, Object>>();
	@Hide
	public static boolean Activity_LoadLayout_Was_Called = false;
	private T object;

	public boolean IsInitialized() {
		return object != null;
	}
	@Hide
	@Override
	public T getObjectOrNull() {
		return object;
	}
	@Hide
	@Override
	public T getObject() {
		if (object == null) {
			ShortName typeName = this.getClass().getAnnotation(ShortName.class);
			String msg = "Object should first be initialized";
			if (typeName == null)
				msg += ".";
			else
				msg += " (" + typeName.value() + ").";
			try {
				Class<?> cls = Class.forName("anywheresoftware.b4a.objects.ViewWrapper");
				if (cls.isInstance(this)) {
					if (!Activity_LoadLayout_Was_Called) {
						msg += "\nDid you forget to call Activity.LoadLayout?";
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			throw new RuntimeException(msg);
		}
		return object;
	}
	@Hide
	public static HashMap<String, Object> getExtraTags(Object me) {
		HashMap<String,Object> map = extraMap.get(me);
		if (map == null) {
			map = new HashMap<String, Object>();
			extraMap.put(me, map);
		}
		return map;
	}
	@Hide
	@Override
	public void setObject(T object) {
		this.object = object;	
	}
	@Hide
	@Override
	public int hashCode() {
		return object == null ? 0 : object.hashCode();
	}
	@Hide
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
		{
			return this.object == null;
		}
		if (obj instanceof AbsObjectWrapper)
		{
			AbsObjectWrapper<?> other = (AbsObjectWrapper<?>) obj;
			if (object == null) {
				return other.object == null;
			} else {
				return object.equals(other.object);
			}
		}
		else { //other is not a wrapper
			if (object == null) {
				return false; //null = null was checked at the beginning
			}
			return object.equals(obj);
		}
	}
	@Hide
	public String baseToString() {
		String type;
		if (object != null) {
			type = object.getClass().getSimpleName();

		}
		else {
			ShortName typeName = this.getClass().getAnnotation(ShortName.class);
			if (typeName != null)
				type = typeName.value();
			else
				type = this.getClass().getSimpleName();
		}
		int i = type.lastIndexOf(".");
		if (i > -1)
			type = type.substring(i + 1);
		String s = "(" + type + ")";
		if (object == null)
			s += " Not initialized";
		return s;
	}
	@Hide
	@Override
	public String toString() {
		String s = baseToString();
		return object == null ? s : s + " " + object.toString();
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static ObjectWrapper ConvertToWrapper(ObjectWrapper ow, Object o) {
		ow.setObject(o);
		return ow;
	}

}
