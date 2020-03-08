package anywheresoftware.b4a.objects;

import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.keywords.Common.DesignerCustomView;
import anywheresoftware.b4a.keywords.LayoutBuilder.DesignerTextSizeMethod;
import anywheresoftware.b4a.objects.collections.Map;

@Hide
public class CustomViewWrapper extends ViewWrapper<BALayout> implements DesignerTextSizeMethod{
	public Object customObject;
	public HashMap<String, Object> props;
	private String eventName;
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		this.ba = ba;
		//don't call parent
		this.eventName = eventName;
	}
	public void AfterDesignerScript() throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class<?> c = customObject.getClass();
		boolean userClass = customObject instanceof B4AClass;
		Map m = new Map();
		m.Initialize();
		m.Put("defaultcolor", ViewWrapper.defaultColor);
		PanelWrapper pw = new PanelWrapper();
		pw.setObject(getObject());
		LabelWrapper lw = new LabelWrapper();
		lw.setObject((TextView)getTag());
		lw.setTextSize((Float)props.get("fontsize"));
		pw.setTag(props.get("tag"));
		m.Put("activity", ba.vg);
		m.Put("ba", ba);
		eventName = eventName.toLowerCase(BA.cul);
		m.Put("eventName", eventName);
		if (props.containsKey("customProperties")) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> cp = (HashMap<String, Object>)props.get("customProperties");
			for (Entry<String, Object> e : cp.entrySet()) {
				Object value = e.getValue();
				m.Put(e.getKey(), value);
			}
		}
		Object target = ba.eventsTarget != null ? ba.eventsTarget : ba.activity.getClass();
		if (BA.isShellModeRuntimeCheck(ba) && userClass) {
			ba.raiseEvent2(null, true, "CREATE_CUSTOM_VIEW", true, customObject, ba, target, eventName, pw, lw, m);
		}
		else {
			c.getMethod("_initialize", BA.class, Object.class, String.class).invoke(customObject, ba, target, eventName);
			if (userClass) {
				B4AClass bc = (B4AClass)customObject;
				bc.getBA().raiseEvent2(null, true, "designercreateview", true, pw, lw, m);
			}
			else {
				((DesignerCustomView)customObject).DesignerCreateView(pw, lw, m);
			}
		}
	}
	@Override
	public float getTextSize() {
		return (Float) props.get("fontsize");
	}
	@Override
	public void setTextSize(float TextSize) {
		props.put("fontsize", TextSize);
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, BALayout.class, props, designer);
		}
		ViewGroup v = (ViewGroup)ViewWrapper.build(prev, props, designer);
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		Drawable d = DynamicBuilder.build(prev, drawProps, designer,null);
		if (d != null) //will be null in !designer and defaultDrawable.
			v.setBackgroundDrawable(d);
		TextView label = (TextView) TextViewWrapper.build(ViewWrapper.buildNativeView((Context)tag, TextView.class, props, designer),
				props, designer);

		if (!designer)
			v.setTag(label);
		if (designer) {
			v.removeAllViews();
			v.addView(label, new BALayout.LayoutParams(0, 0, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
		return v;
	}
	@Hide
	@Deprecated
	public static void replaceBaseWithView(PanelWrapper base, View view) {
		ViewGroup basePanel = base.getObject();
		ViewGroup vg = (ViewGroup) basePanel.getParent();
		vg.addView(view, vg.indexOfChild(basePanel), basePanel.getLayoutParams());
		base.RemoveView();
	}
	@Hide
	public static void replaceBaseWithView2(PanelWrapper base, View view) {
		ViewGroup basePanel = base.getObject();
		ViewGroup vg = (ViewGroup) basePanel.getParent();
		vg.addView(view, vg.indexOfChild(basePanel), basePanel.getLayoutParams());
		view.setTag(basePanel.getTag());
		base.RemoveView();
	}
	

}
