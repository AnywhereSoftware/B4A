package anywheresoftware.b4a.keywords;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.view.View;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.LayoutBuilder.ViewWrapperAndAnchor;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;

@ShortName("DesignerArgs")
public class DesignerArgs {
	@Hide
	public java.util.Map<String, ViewWrapperAndAnchor> views;
	private List args;
	private int width, height;
	private View parent;
	private LayoutValues variant;
	private java.util.Map<String, Object> props;
	private Object layoutTarget;
	@Hide
	public final static HashMap<String, B4AClass> targetsCache = new HashMap<String, B4AClass>();

	/**
	 * Shorthand for GetViewByName(Args(Index)).
	 */
	public View GetViewFromArgs(int Index) {
		return GetViewByName((String) args.Get(Index));
	}
	/**
	 * Returns the view with the set name.
	 *Throws an error if no such view exists. 
	 */
	public View GetViewByName(String Name) {
		try {
			return (View) views.get(Name.toLowerCase(BA.cul)).vw.getObject();
		} catch (NullPointerException npe) {
			throw new RuntimeException("No such view: " + Name);
		}
	}
	/**
	 * Returns the layout parent width.
	 */
	public int getParentWidth() {
		return width;
	}
	/**
	 * Returns the layout parent height.
	 */
	public int getParentHeight() {
		return height;
	}
	/**
	 * Returns the passed arguments.
	 */
	public List getArguments() {
		return args;
	}
	/**
	 * Returns the layout parent.
	 */
	public View getParent() {
		return parent;
	}
	/**
	 * Returns the parameters of the chosen variant.
	 */
	public LayoutValues getChosenVariant() {
		return variant;
	}
	/**
	 * Returns the low level layout data.
	 */
	public Map getDesignerProperties() {
		Map m = new Map();
		m.setObject(props);
		return m;
	}
	/**
	 * Returns a list with the views names.
	 */
	public List getViewsNames() {
		List l = new List();
		l.Initialize();
		for (String name : views.keySet())
			l.Add(name);
		return l;
	}
	/**
	 * Returns a list with the layout views.
	 */
	public List getViews() {
		List l = new List();
		l.Initialize();
		for (ViewWrapperAndAnchor nn : views.values()) {
			l.Add(nn.vw.getObject());
		}
		return l;
	}
	/**
	 * Returns true if the layout is being created right now.
	 *It will always be true in B4A as there is no resize event.
	 */
	public boolean getFirstRun() {
		return true;
	}
	/**
	 * Returns a reference to the module that the layout is being loaded to. Can be used with CallSub.
	 */
	public Object getLayoutModule() {
		return layoutTarget;
	}
	
	@Hide
	public static String callsub(BA ba, View parent, LayoutValues lv, java.util.Map<String, Object> props, String module, String method, int width, int height,
			java.util.Map<String, ViewWrapperAndAnchor> views, Object[] args) throws Exception {
		DesignerArgs da = new DesignerArgs();
		da.views = views;
		da.args = Common.ArrayToList(args);
		da.width = width;
		da.height = height;
		da.parent = parent;
		da.variant = lv;
		da.props = props;
		da.layoutTarget = ba.eventsTarget == null ? ba.context.getClass() : ba.eventsTarget;
		B4AClass target = targetsCache.get(module);
		if (target == null) {
			Class<?> cls = Class.forName(BA.packageName + "." + module);
			target = (B4AClass) cls.newInstance();
			if (BA.isShellModeRuntimeCheck(ba)) {
				ba.raiseEvent2(null, true, "CREATE_CLASS_INSTANCE", true, target, ba);
			}
			else {
				cls.getMethod("_initialize", BA.class).invoke(target, ba);
			}
			targetsCache.put(module, target);
		}
		return String.valueOf(target.getBA().raiseEvent2(null, true, method, true, da));
	}
}