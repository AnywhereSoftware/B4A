package anywheresoftware.b4a.objects;

import android.view.View;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

/**
 * View is a special type of object. You cannot create new View objects. However all other view types can be assigned to a view variable.
 *This allows you to access the shared properties of all views.
 *For example this code hides all views of an activity:<code>
 *For i = 0 To Activity.NumberOfViews - 1
 *	Dim v As View
 *	v = Activity.GetView(i)
 *	v.Visible = False
 *Next</code>
 */
@ActivityObject
@ShortName("View")
public class ConcreteViewWrapper extends ViewWrapper<View>{
	@Hide
	@Override
	public void Initialize(final BA ba, String eventName) {
		throw new RuntimeException("Cannot initialize object.");
	}
}
