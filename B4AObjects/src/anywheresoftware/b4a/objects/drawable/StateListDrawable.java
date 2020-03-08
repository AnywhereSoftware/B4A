package anywheresoftware.b4a.objects.drawable;

import java.util.HashMap;

import android.R;
import android.graphics.drawable.Drawable;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
/**
 * A drawable that holds other drawables and chooses the current one based on the view's state.
 *See the <link>StateListDrawable example|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6589-statelistdrawable-example.html</link>.
 */
@ActivityObject
@ShortName("StateListDrawable")
public class StateListDrawable extends AbsObjectWrapper<android.graphics.drawable.StateListDrawable>{
	public static final int State_Pressed = R.attr.state_pressed;
	public static final int State_Enabled = R.attr.state_enabled;
	public static final int State_Disabled = -R.attr.state_enabled;
	public static final int State_Unchecked = -R.attr.state_checked;
	public static final int State_Checked = R.attr.state_checked;
	public static final int State_Selected = R.attr.state_selected;
	public static final int State_Focused = R.attr.state_focused;
	/**
	 * Initializes the object.
	 *Example:<code>
	 *Dim Button1 As Button
	 *Button1.Initialize("")
	 *Dim sld As StateListDrawable
	 *sld.Initiailize
	 *sld.AddState (sld.State_Disabled, DisabledDrawable)
	 *sld.AddState (sld.State_Pressed, PressedDrawable)
	 *sld.AddCatchAllState (DefaultDrawable)
	 *Button1.Background = sld</code>
	 */
	public void Initialize() {
		setObject(new android.graphics.drawable.StateListDrawable());
	}
	/**
	 * Adds a state and drawable pair.
	 *Note that the order of states is very important. The first state that matches will be used.
	 */
	public void AddState(int State, Drawable Drawable) {
		getObject().addState(new int[] {State}, Drawable);
	}
	/**
	 * Adds a state and drawable pair. The state is made from a combination of states.
	 *You should not reuse the array specified as it is used internally by StateListDrawable.
	 *Note that the order of states is very important. The first state that matches will be used.
	 */
	public void AddState2(int[] State, Drawable Drawable) {
		getObject().addState(State, Drawable);
	}
	/**
	 * Adds the drawable that will be used if no other state matched the current state.
	 *This should always be the last state (states added after this one will never be used).
	 */
	public void AddCatchAllState(Drawable Drawable) {
		getObject().addState(new int[] {}, Drawable);
	}
	

	
	@SuppressWarnings("unchecked")
	@Hide
	public static Drawable build(Object prev, HashMap<String, Object> d, boolean designer, Object tag) {
		android.graphics.drawable.StateListDrawable sld = new android.graphics.drawable.StateListDrawable();
		Drawable disabled = DynamicBuilder.<Drawable>build(prev /*view*/, (HashMap)d.get("disabledDrawable"), 
				designer, tag);
		Drawable enabled = DynamicBuilder.<Drawable>build(prev, (HashMap)d.get("enabledDrawable"), 
				designer, tag);
		Drawable pressed = DynamicBuilder.<Drawable>build(prev, (HashMap)d.get("pressedDrawable"), 
				designer, tag);
		sld.addState(new int[] {-R.attr.state_enabled}, disabled);
		sld.addState(new int[] {R.attr.state_pressed}, pressed);
		sld.addState(new int[] {}, enabled);
		return sld;
		
	}
}
