package anywheresoftware.b4a.keywords;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.ConnectorUtils;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import anywheresoftware.b4a.objects.CustomViewWrapper;
import anywheresoftware.b4a.objects.ViewWrapper;
import anywheresoftware.b4a.objects.streams.File;

@Hide
public class LayoutBuilder {
	private static BA tempBA;
	private static HashMap<String, WeakReference<MapAndCachedStrings>> cachedLayouts = new HashMap<String, WeakReference<MapAndCachedStrings>>();
	private static List<CustomViewWrapper> customViewWrappers;
	private static HashMap<String, Object> viewsToSendInShellMode;
	private static LayoutValues chosen;
	private static HashMap<String, Field> classFields;
	private static String currentClass;
	public static class LayoutValuesAndMap {
		public final LayoutValues layoutValues;
		public final LinkedHashMap<String, ViewWrapperAndAnchor> map;
		public LayoutValuesAndMap(LayoutValues layoutValues, LinkedHashMap<String, ViewWrapperAndAnchor> map) {
			this.layoutValues = layoutValues;
			this.map = map;
		}
	}
	private static class MapAndCachedStrings {
		public final HashMap<String, Object> map;
		public final String[] cachedStrings;
		public MapAndCachedStrings(HashMap<String, Object> map, String[] cachedStrings) {
			this.map = map;
			this.cachedStrings = cachedStrings;
		}
	}
	public static LayoutValuesAndMap loadLayout(String file,  
			BA ba, boolean isActivity, ViewGroup parent, LinkedHashMap<String, ViewWrapperAndAnchor> dynamicTable) throws IOException {
		try {
			tempBA = ba;
			file = file.toLowerCase(BA.cul);
			if (!file.endsWith(".bal"))
				file = file + ".bal";
			MapAndCachedStrings mcs = null;
			WeakReference<MapAndCachedStrings> cl = cachedLayouts.get(file);
			if (cl != null)
				mcs = cl.get();
			InputStream in = File.OpenInput(File.getDirAssets(), file).getObject();
			DataInputStream din = new DataInputStream(in);
			int version = ConnectorUtils.readInt(din);
			int pos = ConnectorUtils.readInt(din);
			while (pos > 0) {
				pos -= din.skip(pos);
			}
			String[] cache = null;
			if (version >= 3) {
				if (mcs != null) { //take strings from cache
					cache = mcs.cachedStrings;
					ConnectorUtils.readInt(din); //length
					for (int i = 0;i < cache.length;i++) {
						int stringSize = ConnectorUtils.readInt(din);
						din.skipBytes(stringSize);
					}
				}
				else {
					cache = new String[ConnectorUtils.readInt(din)];
					for (int i = 0;i < cache.length;i++) {
						cache[i] = ConnectorUtils.readString(din);
					}
				}
			}
			int numberOfVariants = ConnectorUtils.readInt(din);
			chosen = null;
			LayoutValues device = Common.GetDeviceLayoutValues(ba);
			int variantIndex = 0;
			float distance = Float.MAX_VALUE;
			for (int i = 0;i < numberOfVariants;i++) {
				LayoutValues test = LayoutValues.readFromStream(din);
				if (chosen == null) {
					chosen = test;
					distance = test.calcDistance(device);
					variantIndex = i;
				}
				else {
					float testDistance = test.calcDistance(device);
					if (testDistance < distance) {
						chosen = test;
						distance = testDistance;
						variantIndex = i;
					}
				}
			}
			BALayout.setUserScale(chosen.Scale);
			int mainWidth, mainHeight;
			if (isActivity || parent.getLayoutParams() == null) {
				mainWidth = ba.vg.getWidth();
				mainHeight = ba.vg.getHeight();
			} else {
				mainWidth = parent.getLayoutParams().width;
				mainHeight = parent.getLayoutParams().height;
			}
			int animationDuration = 0;
			HashMap<String, Object> props = null;
			if (dynamicTable == null) { //only not null in rerunScript
				dynamicTable = new LayoutHashMap<String, ViewWrapperAndAnchor>();
				
				if (mcs != null)
					props = mcs.map;
				else {
					props = ConnectorUtils.readMap(din, cache);
					cachedLayouts.put(file, new WeakReference<MapAndCachedStrings>(new MapAndCachedStrings(props, cache)));
				}

				loadLayoutHelper(props, ba, ba.eventsTarget == null ? ba.activity : ba.eventsTarget, parent, isActivity, "variant" + variantIndex
						, true, dynamicTable, mainWidth, mainHeight);
				if (BA.isShellModeRuntimeCheck(ba) && viewsToSendInShellMode != null) {
					ba.raiseEvent2(null, true, "SEND_VIEWS_AFTER_LAYOUT", true, viewsToSendInShellMode);
					viewsToSendInShellMode = null;
				}
				animationDuration = BA.gm(props, "animationDuration", 0);

			}
			din.close();
			runScripts(file, ba, chosen, parent, dynamicTable, mainWidth, mainHeight, Common.Density, props);
			BALayout.setUserScale(1f);
			if (customViewWrappers != null) {
				for (CustomViewWrapper cvw : customViewWrappers) {
					cvw.AfterDesignerScript();
				}
			}
			animateLayout(dynamicTable, parent, mainWidth, mainHeight, animationDuration);
			return new LayoutValuesAndMap(chosen, dynamicTable);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			tempBA = null;
			customViewWrappers = null;
		}
	}
	private static void animateLayout(LinkedHashMap<String, ViewWrapperAndAnchor> views, View parent, int parentWidth, int parentHeight, int duration) {
		if (duration <= 0)
			return;
		for (ViewWrapperAndAnchor vwa : views.values()) {
			if (vwa.parent == parent) {
				int pl = 0, pt = 0, pw = 0, ph =0;
				if (vwa.hanchor == ViewWrapperAndAnchor.RIGHT)
					pl = parentWidth;
				else if (vwa.hanchor == ViewWrapperAndAnchor.BOTH)
					pl = parentWidth / 2;
				if (vwa.vanchor == ViewWrapperAndAnchor.BOTTOM)
					pt = parentHeight;
				else if (vwa.vanchor == ViewWrapperAndAnchor.BOTH)
					pt = parentHeight / 2;
				ViewWrapper.AnimateFrom(vwa.vw.getObject(), duration, pl, pt, pw, ph);
			}
		}
	}
	private static void runScripts(String file, BA ba, LayoutValues lv, View parent, LinkedHashMap<String, ViewWrapperAndAnchor> views, int w, int h, float s,
			java.util.Map<String, Object> props) throws IllegalArgumentException, IllegalAccessException {
		StringBuilder sb = new StringBuilder();
		sb.append("LS_");
		for (int i = 0;i < file.length() - 4;i++) {
			char c = file.charAt(i);
			if (Character.isLetterOrDigit(c))
				sb.append(c);
			else
				sb.append("_");
		}
		try {
			Class<?> c = Class.forName(BA.packageName + ".designerscripts." + sb.toString());
			try {
				//global script
				runScriptMethod(c, variantToMethod(null), lv, ba, parent, views, props, w, h, s);
			} catch (NoSuchMethodException e) {
				//do nothing
			}
			runScriptMethod(c, variantToMethod(lv), lv, ba, parent, views, props, w, h, s);
		} catch (ClassNotFoundException e) {
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}


	}
	private static void runScriptMethod(Class<?> c, String methodName, LayoutValues lv, BA ba, View parent, java.util.Map<String, anywheresoftware.b4a.keywords.LayoutBuilder.ViewWrapperAndAnchor> views
			, java.util.Map<String, Object> props
			, int w, int h, float scale) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m; 
		m = c.getMethod(methodName, BA.class, View.class, LayoutValues.class, java.util.Map.class, java.util.Map.class, int.class, int.class, float.class);
		m.invoke(null, ba, parent, lv, props, views, w, h, scale);
	}
	private static double autoscale;
	private static double screenSize = 0;
	public static void setScaleRate(double rate) {
		double deviceSize = (tempBA.vg.getWidth() + tempBA.vg.getHeight()) / Common.Density;
		double variantSize = (chosen.Width + chosen.Height - 25) / chosen.Scale;
		double deviceToLayout = deviceSize / variantSize;
		if (System.getProperty("autoscaleall_old_behaviour", "false").equals("true")) {
			double delta = ((tempBA.vg.getWidth() + tempBA.vg.getHeight()) / ((320 + 430) * Common.Density) - 1);
			autoscale = (1 + rate * delta);
		}
		else {
			if (deviceToLayout > 0.95 && deviceToLayout < 1.05)
				autoscale = 1;
			else {
				double delta = deviceSize / (320 + 430) - 1;
				double vdelta = variantSize / (320 + 430) - 1;
				double vscale = 1 + rate * vdelta;
				autoscale = (1 + rate * delta) / vscale;
			}
		}

		screenSize = 0; //reset the screen size value

	}
	public static double getScreenSize() {
		if (screenSize == 0)
			screenSize = Math.sqrt(Math.pow(tempBA.vg.getWidth(), 2) + Math.pow(tempBA.vg.getHeight(), 2)) / 160 / Common.Density;
		return screenSize;
	}
	public static boolean isPortrait() {
		return tempBA.vg.getHeight() >= tempBA.vg.getWidth();
	}
	public static void scaleAll(java.util.Map<String, ViewWrapperAndAnchor> views) {
		for (ViewWrapperAndAnchor vwa : views.values()) {
			if (vwa.vw.IsInitialized() == false)
				continue;
			if (!(vwa.vw instanceof ActivityWrapper))
				scaleView(vwa);
		}
	}
	public static void scaleView(ViewWrapperAndAnchor vwa) {
		ViewWrapper<?> v = vwa.vw;
		int left = v.getLeft();
		int width = v.getWidth();
		int height = v.getHeight();
		int top = v.getTop();
		int pw = vwa.parent == null  || vwa.parent.getLayoutParams() == null ? vwa.pw : vwa.parent.getLayoutParams().width;
		int ph = vwa.parent == null || vwa.parent.getLayoutParams() == null ? vwa.ph : vwa.parent.getLayoutParams().height;
		int newLeft, newTop, newWidth, newHeight;
		int right = vwa.right, bottom = vwa.bottom;
		if (vwa.hanchor == vwa.LEFT) {
			newLeft = (int)(left * autoscale + 0.5);
			newWidth = (int)((left + width) * autoscale + 0.5) - newLeft;
		}
		else if (vwa.hanchor == vwa.RIGHT) {
			int newRight = (int)(right * autoscale + 0.5);
			newWidth = (int)((right + width) * autoscale + 0.5) - newRight;
			newLeft = pw - newRight - newWidth;

		}
		else // if (view.hanchor == ControlsManager.BOTH)
		{
			newLeft = (int)(left * autoscale + 0.5);
			int newRight = (int)(right * autoscale + 0.5);
			newWidth = pw - newRight - newLeft;

		}

		v.setLeft(newLeft);
		v.setWidth(newWidth);

		if (vwa.vanchor == vwa.TOP)
		{
			newTop = (int)(top * autoscale + 0.5);
			newHeight = (int)((top + height) * autoscale + 0.5) - newTop;

		}
		else if (vwa.vanchor == vwa.BOTTOM)
		{
			int newBottom = (int)(bottom * autoscale + 0.5);
			newHeight = (int)((bottom + height) * autoscale + 0.5) - newBottom;
			newTop = ph - newBottom - newHeight;
		}
		else //if (view.vanchor == ControlsManager.BOTH)
		{
			newTop = (int)(top * autoscale + 0.5);
			int newBottom = (int)(bottom * autoscale + 0.5);
			newHeight = ph - newTop - newBottom;
		}
		v.setTop(newTop);
		v.setHeight(newHeight);

		if (v instanceof DesignerTextSizeMethod) {
			DesignerTextSizeMethod t = (DesignerTextSizeMethod) v;
			t.setTextSize((float) (t.getTextSize() * autoscale));
		}
	}
	private static String variantToMethod(LayoutValues lv)
	{
		String variant;
		if (lv == null)
			variant = "general";
		else
			variant = String.valueOf(lv.Width) + "x" + String.valueOf(lv.Height) + "_" + BA.NumberToString(lv.Scale).replace(".", "_");
		return "LS_" + variant;
	}
	@SuppressWarnings("unchecked")
	private static void loadLayoutHelper(HashMap<String, Object> props, BA ba, Object fieldsTarget,
			ViewGroup parent, boolean isActivity, String currentVariant, boolean firstCall, HashMap<String, ViewWrapperAndAnchor> dynamicTable,
			int parentWidth, int parentHeight)
	throws Exception {

		HashMap<String, Object> variant = (HashMap<String, Object>) props.get(currentVariant);
		View o = null;
		if (isActivity || !firstCall) {
			Object act = isActivity ? parent : null; //only activity gets an already built object
			props.put("left", variant.get("left"));
			props.put("top", variant.get("top"));
			props.put("width", variant.get("width"));
			props.put("height", variant.get("height"));

			o = DynamicBuilder.build(act, props, false, parent.getContext());
			if (!isActivity) {
				String upperCaseName = (String)props.get("name");
				String name = upperCaseName.toLowerCase(BA.cul);
				String cls = (String) props.get("type");
				if (cls.startsWith("."))
					cls = "anywheresoftware.b4a.objects" + cls;
				ViewWrapper ow = (ViewWrapper) Class.forName(cls).newInstance();
				ViewWrapperAndAnchor vwa = new ViewWrapperAndAnchor(ow, isActivity ? null : parent);
				if (variant.containsKey("hanchor")) {
					vwa.hanchor = (Integer)variant.get("hanchor");
					vwa.vanchor = (Integer)variant.get("vanchor");
				}
				vwa.pw = parentWidth;
				vwa.ph = parentHeight;
				dynamicTable.put(name, vwa);
				if (classFields == null || currentClass != ba.className) {
					classFields = new HashMap<String, Field>();
					currentClass = ba.className;
					for (Field field : Class.forName(ba.className).getDeclaredFields()) {
						if (field.getName().startsWith("_"))
							classFields.put(field.getName(), field);
					}
				}
				Field field = classFields.get("_" + name);
				Object assigningObject = ow;
				if (ow instanceof CustomViewWrapper) {
					if (customViewWrappers == null)
						customViewWrappers = new ArrayList<CustomViewWrapper>();
					customViewWrappers.add((CustomViewWrapper)ow);
					String cclass = (String)props.get("customType");
					if (cclass == null || cclass.length() == 0)
						throw new RuntimeException("CustomView CustomType property was not set.");
					Class<?> customClass;
					try {
						customClass = Class.forName(cclass);
					} catch (ClassNotFoundException cnfe) {
						int dollar = cclass.lastIndexOf(".");
						if (dollar > -1) {
							String corrected = BA.packageName + cclass.substring(dollar);
							customClass = Class.forName(corrected);
						}
						else
							throw cnfe;
					}
					Object customObject = customClass.newInstance();
					CustomViewWrapper cvw = (CustomViewWrapper)ow;
					cvw.customObject = customObject;
					cvw.props = new HashMap<String, Object>(props); //create a copy as it can be later modified
					assigningObject = customObject;
				} else {
					if (field != null && field.getType() != assigningObject.getClass()) {
						//field type doesn't match
						if (BA.debugMode) {
							Type t = ow.getClass().getGenericSuperclass();
							if (t instanceof ParameterizedType) {
								ParameterizedType pt = (ParameterizedType)t;
								if (pt.getActualTypeArguments().length > 0) {
									ParameterizedType fieldParamType = (ParameterizedType) field.getType().getGenericSuperclass();
									if (((Class)fieldParamType.getActualTypeArguments()[0]).isAssignableFrom((Class)(pt.getActualTypeArguments()[0])) == false) {
										throw new RuntimeException("Cannot convert: " + ow.getClass() + ", to: " + field.getType());
									}
								}
							}
						}
						ObjectWrapper nw = (ObjectWrapper) field.getType().newInstance();
						nw.setObject(o);
						assigningObject = nw;
					}
				}
				
				if (BA.isShellModeRuntimeCheck(ba)) {
					if (viewsToSendInShellMode == null)
						viewsToSendInShellMode = new HashMap<String, Object>();
					viewsToSendInShellMode.put(name, assigningObject);
				} 
				
				if (field != null) { //object was declared in Sub Globals
					try {
						field.set(fieldsTarget, assigningObject);
					} catch (IllegalArgumentException ee) {
						throw new RuntimeException("Field " + name  + " was declared with the wrong type.");
					}
				}
				ow.setObject(o);
				ow.innerInitialize(ba, ((String)props.get("eventName")).toLowerCase(BA.cul), true); //keep old object = true
				parent.addView(o, o.getLayoutParams());
				if (vwa.hanchor != 0 || vwa.vanchor != 0)
					ViewWrapper.fixAnchor(parentWidth, parentHeight, vwa);

			}
		}
		else {
			o = parent; //loadlayout for non activities (panels).
			HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
			Drawable d = DynamicBuilder.build(parent, drawProps, false, null);
			parent.setBackgroundDrawable(d);
		}
		HashMap<String, Object> kids = 
			(HashMap<String, Object>) props.get(":kids");
		if (kids != null) {
			int pw = o.getLayoutParams() == null ? 0 : o.getLayoutParams().width;
			int ph = o.getLayoutParams() == null ? 0 : o.getLayoutParams().height;
			for (int i = 0;i < kids.size();i++) {
				loadLayoutHelper((HashMap<String, Object>)kids.get(String.valueOf(i)), ba, fieldsTarget, (ViewGroup) o, false,
						currentVariant, false, dynamicTable, pw, ph);
			}
		}
	}
	@Hide
	public static class LayoutHashMap<K, V> extends LinkedHashMap<K, V> {
		@Override
		public V get(Object key) {
			V v = super.get(key);
			if (v == null) {
				throw new RuntimeException("Cannot find view: " + key.toString()
						+ "\nAll views in script should be declared.");
			}
			return v;
		}
	}
	@Hide
	public static class ViewWrapperAndAnchor {
		public static int LEFT = 0, RIGHT = 1, BOTH = 2, TOP = 0, BOTTOM = 1;
		public final ViewWrapper<?> vw;
		public final View parent;
		public int right, bottom, vanchor, hanchor, pw, ph;
		public ViewWrapperAndAnchor(ViewWrapper<?> vw, View parent) {
			this.vw = vw;
			this.parent = parent;
		}
	}
	@Hide
	public static interface DesignerTextSizeMethod {
		public void setTextSize(float TextSize);
		public float getTextSize();
	}
}
