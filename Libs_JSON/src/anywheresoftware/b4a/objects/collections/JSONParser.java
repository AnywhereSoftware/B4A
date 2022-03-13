
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
 
 package anywheresoftware.b4a.objects.collections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.collections.Map.MyMap;

/**
 * Parses JSON formatted strings: <link>Description of JSON|http://www.json.org/</link>.
 *JSON objects are converted to Maps and JSON arrays are converted to Lists.
 *After initializing the object you will usually call NextObject to get a single Map object.
 *If the JSON string top level value is an array you should call NextArray.
 *Afterward you should work with the Map or List and fetch the required data.
 *See the <link>JSON tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6923-android-json-tutorial.html</link>.
 *Typical code:<code>
 *Dim JSON As JSONParser
 *Dim Map1 As Map
 *JSON.Initialize(File.ReadString(File.DirAssets, "example.json")) 'Read the text from a file.
 *Map1 = JSON.NextObject</code>
 */
@Version(1.21f)
@ShortName("JSONParser")
public class JSONParser extends AbsObjectWrapper<JSONTokener>{
	/**
	 * Initializes the object and sets the text that will be parsed.
	 */
	public void Initialize(String Text) {
		JSONTokener j = new JSONTokener(Text);
		setObject(j);
	}
	/**
	 * Parses the text assuming that the top level value is an object.
	 */
	public Map NextObject() throws JSONException {
		Object o = getObject().nextValue();
		if (o instanceof JSONObject == false) {
			throw new RuntimeException("JSON Object expected.");
		}
		Map m = new Map();
		m.setObject(convertObjToMap((JSONObject)o));
		return m;
	}
	/**
	 * Parses the text assuming that the top level value is an array.
	 */
	public List NextArray() throws JSONException {
		Object o = getObject().nextValue();
		if (o instanceof JSONArray == false) {
			throw new RuntimeException("JSON Array expected.");
		}
		List l = new List();
		l.setObject(convertObjToList((JSONArray)o));
		return l;
	}
	/**
	 * Parses the text assuming that the top level value is a simple value.
	 */
	public Object NextValue() throws JSONException {
		Object o = getObject().nextValue();
		if (o instanceof JSONObject || o instanceof JSONArray) {
			throw new RuntimeException("Simple value expected.");
		}
		return o;
	}
	
	@SuppressWarnings("unchecked")
	private MyMap convertObjToMap(JSONObject j) throws JSONException {
		MyMap m = new MyMap();
		Iterator<String> it = (Iterator<String>)j.keys();
		while (it.hasNext()) {
			String key = it.next();
			
			Object value = j.get(key);
			if (JSONObject.NULL.equals(value)) {
				m.put(key, null);
			}
			else if (value instanceof JSONObject) {
				m.put(key, convertObjToMap((JSONObject)value));
			}
			else if (value instanceof JSONArray) {
				m.put(key, convertObjToList((JSONArray)value));
			}
			else {
				m.put(key, value);
			}
		}
		return m;
	}
	private ArrayList<Object> convertObjToList(JSONArray ja) throws JSONException {
		ArrayList<Object> a = new ArrayList<Object>(ja.length());
		for (int i = 0;i < ja.length();i++) {
			Object value = ja.get(i);
			if (JSONObject.NULL.equals(value))
				a.add(null);
			else if (value instanceof JSONObject) {
					a.add(convertObjToMap((JSONObject)value));
				}
				else if (value instanceof JSONArray) {
					a.add(convertObjToList((JSONArray)value));
				}
				else {
					a.add(value);
				}
		}
		return a;
	}
	/**
	 * This object generates JSON strings.
	 *It can be initialized with a Map or a List. Both can contain other Maps or Lists, arrays and other primitive values.
	 *See the <link>JSON tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6923-android-json-tutorial.html</link>.
	 */
	@ShortName("JSONGenerator")
	public static class JSONGenerator {
		Object json;
		/**
		 * Initializes the object with the given Map.
		 */
		public void Initialize(Map Map) throws Exception {
			json = convertMapToJO(Map.getObject());
		}
		/**
		 * Initializes the object with the given List.
		 */
		public void Initialize2(List List) throws Exception {
			json = convertListToJA(List.getObject());
		}
		/**
		 * Creates a JSON string from the initialized object.
		 *This string does not include any extra whitespace.
		 */
		public String ToString() {
			if (json == null)
				throw new RuntimeException("JSON was not initialized.");
			if (json instanceof JSONObject)
				return ((JSONObject)json).toString();
			else
				return ((JSONArray)json).toString();
		}
		/**
		 * Creates a JSON string from the initialized object.
		 *The string will be indented and easier for reading.
		 *Note that the string created is a valid JSON string.
		 *Indent - Number of spaces to add to each level.
		 */
		public String ToPrettyString(int Indent) throws JSONException {
			if (json == null)
				throw new RuntimeException("JSON was not initialized.");
			if (json instanceof JSONObject)
				return ((JSONObject)json).toString(Indent);
			else
				return ((JSONArray)json).toString(Indent);
		}
		@SuppressWarnings("unchecked")
		private JSONObject convertMapToJO(MyMap map) throws JSONException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			JSONObject j = new JSONObject();
			for (Entry<Object, Object> e : map.entrySet()) {
				String key = String.valueOf(e.getKey());
				Object o = e.getValue();
				if (o instanceof MyMap) {
					j.put(key, convertMapToJO((MyMap)o));
				}
				else if (o instanceof Map) {
					j.put(key, convertMapToJO(((Map)o).getObject()));
				}
				else if (o instanceof java.util.List) {
					j.put(key, convertListToJA((java.util.List)o));
				}
				else if (o instanceof List) {
					j.put(key, convertListToJA(((List)o).getObject()));
				}
				else if (o == null) {
					j.put(key, JSONObject.NULL);
				}
				else if (o.getClass().isArray()) {
					j.put(key, convertListToJA((arrayToList(o)).getObject()));
				}
				else {
					j.put(key, o);
				}
			}
			return j;
		}
		@SuppressWarnings("unchecked")
		private JSONArray convertListToJA(java.util.List<Object> list) throws JSONException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			JSONArray ja = new JSONArray();
			for (Object o : list) {
				if (o instanceof MyMap) {
					ja.put(convertMapToJO((MyMap)o));
				}
				else if (o instanceof Map) {
					ja.put(convertMapToJO(((Map)o).getObject()));
				}
				else if (o instanceof java.util.List) {
					ja.put(convertListToJA((java.util.List<Object>)o));
				}
				else if (o instanceof List) {
					ja.put(convertListToJA(((List)o).getObject()));
				}
				else if (o == null) {
					ja.put(JSONObject.NULL);
				}
				else if (o.getClass().isArray()) {
					ja.put(convertListToJA((arrayToList(o)).getObject()));
				}
				else {
					ja.put(o);
				}
			}
			return ja;
		}
		private List arrayToList(Object o) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			List l;
			if (o.getClass().getComponentType().isPrimitive()) {
				l = (List) Common.class.getMethod("ArrayToList", o.getClass()).invoke(null, o);
			}
			else {
				l = Common.ArrayToList((Object[])o);
			}
			return l;
		}
		
	}
	/**
	 * Works with the As method to convert strings to objects and vice versa.
	 *Example:<code>
	 *Dim m As Map = CreateMap("Key1": "Value1")
	 *Dim s As String = m.As(JSON).ToString</code>
	 */
	@ShortName("JSON")
	public static class JSONConverter extends AbsObjectWrapper<Object> {
		/**
		 * Converts a Map or List to a JSON string. Same as JsonGenerator.ToPrettyString(4).
		 */
		public String ToString() throws Exception {
			return ToStringImpl(true);
		}
		/**
		 * Converts a Map or List to a JSON string, without whitespace. Same as JsonGenerator.ToString.
		 */
		public String ToCompactString()throws Exception {
			return ToStringImpl(false);
		}
		private String ToStringImpl(boolean pretty) throws Exception{
			JSONGenerator g = new JSONGenerator();
			Object o = getObject();
			if (o instanceof MyMap)
				g.Initialize((Map) AbsObjectWrapper.ConvertToWrapper(new Map(), (MyMap)o));
			else if (o instanceof java.util.List)
				g.Initialize2((List) AbsObjectWrapper.ConvertToWrapper(new List(), o));
			else
				throw new RuntimeException("Only Maps and Lists are supported");
			return pretty ? g.ToPrettyString(4) : g.ToString();
		}
		
		/**
		 * Converts the string to a Map.
		 */
		public Map ToMap() throws JSONException {
			return stringToObject().NextObject();
		}
		/**
		 * Converts the string to a List.
		 */
		public List ToList() throws JSONException {
			return stringToObject().NextArray();
		}
		private JSONParser stringToObject() {
			Object o = getObject();
			if (o instanceof String == false) {
				throw new RuntimeException("String expected");
			}
			JSONParser p = new JSONParser();
			p.Initialize((String)o);
			return p;
		}
	}
			
}
