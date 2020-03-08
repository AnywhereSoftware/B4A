package anywheresoftware.b4a.keywords.constants;

import android.content.DialogInterface;
import anywheresoftware.b4a.Msgbox;
/**
 *A predefined object containing the possible values that dialogs return.
 *For example:<code>
 *Dim result As Int
 *result = Msgbox2("Save changes?", "", "Yes", "", "No", Null)
 *If result = DialogResponse.POSITIVE Then
 *	'save changes
 *End If</code>
 */
public class DialogResponse {
	public static final int POSITIVE = DialogInterface.BUTTON_POSITIVE;
	public static final int CANCEL = DialogInterface.BUTTON_NEUTRAL;
	public static final int NEGATIVE = DialogInterface.BUTTON_NEGATIVE;
}
