package anywheresoftware.b4a.objects;

import java.io.Serializable;
import java.net.URISyntaxException;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;

/**
 * Intent objects are messages which you can send to the OS in order to do some external action.
 *The Intent object should be sent with <code>StartActivity</code> keyword.
 *See this <link>page|https://developer.android.com/reference/android/content/Intent.html</link> for a list of standard constants.
 *Example, launch YouTube application:<code>
 *Dim Intent1 As Intent
 *Intent1.Initialize(Intent1.ACTION_MAIN, "")
 *Intent1.SetComponent("com.google.android.youtube/.HomeActivity")
 *StartActivity(Intent1)</code>
 */
@ShortName("Intent")
public class IntentWrapper extends AbsObjectWrapper<Intent> {
	public static final String ACTION_VIEW = Intent.ACTION_VIEW;
	public static final String ACTION_CALL = Intent.ACTION_CALL;
	public static final String ACTION_EDIT = Intent.ACTION_EDIT;
	public static final String ACTION_PICK = Intent.ACTION_PICK;
	public static final String ACTION_SEND = Intent.ACTION_SEND;
	public static final String ACTION_MAIN = Intent.ACTION_MAIN;
	public static final String ACTION_APPWIDGET_UPDATE = AppWidgetManager.ACTION_APPWIDGET_UPDATE;
	/**
	 * Initializes the object using the given Action and data Uri. Action can be one of the action constants or any other string.
	 *Pass an empty string if a Uri is not required.
	 */
	public void Initialize(String Action, String Uri) {
		Intent i = new Intent(Action, Uri.length() > 0 ? android.net.Uri.parse(Uri) : null);
		setObject(i);
	}
	/**
	 *Initializes the object by parsing the Uri.
	 *Flags - Additional integer value. Pass 0 if it is not required.
	 *Example:<code>
	 *Dim Intent1 As Intent
	 *Intent1.Initialize2("http://www.basic4ppc.com", 0)
	 *StartActivity(Intent1)</code>
	 */
	public void Initialize2(String Uri, int Flags) throws URISyntaxException {
		setObject(Intent.parseUri(Uri, Flags));
	}
	/**
	 * Gets or sets the Intent action.
	 */
	public String getAction() {
		return getObject().getAction() == null ? "" : getObject().getAction();
	}
	public void setAction(String v) {
		getObject().setAction(v);
	}
	/**
	 * Sets the MIME type.
	 *Example:<code>
	 *Intent1.SetType("text/plain")</code>
	 */
	public void SetType(String Type) {
		getObject().setDataAndType(getObject().getData(), Type);
	}
	/**
	 * Gets or sets the flags component.
	 */
	public int getFlags() {
		return getObject().getFlags();
	}
	public void setFlags(int flags) {
		getObject().setFlags(flags);
	}
	/**
	 * Adds a category describing the intent required operation.
	 */
	public void AddCategory(String Category) {
		getObject().addCategory(Category);
	}
	/**
	 * Retrieves the data component as a string.
	 */
	public String GetData() {
		return getObject().getDataString();
	}
	/**
	 * Adds extra data to the intent.
	 */
	public void PutExtra (String Name, Object Value) {
		Intent i = getObject();
		if (Value instanceof Serializable)
			i.putExtra(Name, (Serializable)Value);
		else if (Value instanceof Parcelable)
			i.putExtra(Name, (Parcelable)Value);
	}
	/**
	 * Returns the item value with the given key.
	 */
	public Object GetExtra(String Name) {
		return getObject().getSerializableExtra(Name);
	}
	/**
	 * Tests whether an item with the given key exists.
	 */
	public boolean HasExtra(String Name) {
		return getObject().hasExtra(Name);
	}
	/**
	 * Returns a string containing the extra items. This is useful for debugging.
	 */
	public String ExtrasToString() {
		if (IsInitialized() == false) {
			return "not initialized";
		}
		Bundle b = getObject().getExtras();
		if (b == null)
			return "no extras";
		b.size(); //unparcel the bundle if needed
		return b.toString();

	}
	/**
	 * Wraps the intent in another "chooser" intent. A dialog will be displayed to the user with the available services that can act on the intent.
	 *<code>WrapAsIntentChooser</code> should be the last method called before sending the intent.
	 */
	public void WrapAsIntentChooser(String Title) {
		Intent i = Intent.createChooser(getObject(), Title);
		setObject(i);
	}
	/**
	 * Explicitly sets the component that will handle this intent.
	 */
	public void SetComponent(String Component) {
		getObject().setComponent(ComponentName.unflattenFromString(Component));
	}
	/**
	 * Explicitly sets the package name of the target application.
	 */
	public void SetPackage(String PackageName) {
		getObject().setPackage(PackageName);
	}

}