package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.constants.Colors;
import anywheresoftware.b4a.objects.collections.List;
/**
 * An enhanced version of EditText which shows the user a drop down list with all items matching the currently entered characters.
 *Items matching are items starting with the current input or items that include a word that starts with the current input (words must be separated with spaces).
 *Call SetItems with the list of possible items.
 *
 *ItemClick event is raised when the user clicks on an item from the list.
 *Example:<code>
 *Sub Process_Globals
 *
 *End Sub
 *
 *Sub Globals
 *	Dim ACT As AutoCompleteEditText
 *End Sub
 *
 *Sub Activity_Create(FirstTime As Boolean)
 *	ACT.Initialize("ACT")
 *	Activity.AddView(ACT, 10dip, 10dip, 500dip, 80dip)
 *	Dim countries() As String
 *	countries = Array As String( _
 *		"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", _
 *		"Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina", _
 *		"Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", _
 *		"Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", _
 *		"Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", _
 *		"Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", _
 *		"British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi", _
 *		"Cote d'Ivoire", "Cambodia", "Cameroon", "Canada", "Cape Verde", _
 *		"Cayman Islands", "Central African Republic", "Chad", "Chile", "China", _
 *		"Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", _
 *		"Cook Islands", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic", _
 *		"Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic", _
 *		"East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", _
 *		"Estonia", "Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji", "Finland", _
 *		"Former Yugoslav Republic of Macedonia", "France", "French Guiana", "French Polynesia", _
 *		"French Southern Territories", "Gabon", "Georgia", "Germany", "Ghana", "Gibraltar", _
 *		"Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", _
 *		"Guyana", "Haiti", "Heard Island and McDonald Islands", "Honduras", "Hong Kong", "Hungary", _
 *		"Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", _
 *		"Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos", _
 *		"Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", _
 *		"Macau", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", _
 *		"Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia", "Moldova", _
 *		"Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", _
 *		"Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", _
 *		"Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea", "Northern Marianas", _
 *		"Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", _
 *		"Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar", _
 *		"Reunion", "Romania", "Russia", "Rwanda", "Sqo Tome and Principe", "Saint Helena", _
 *		"Saint Kitts and Nevis", "Saint Lucia", "Saint Pierre and Miquelon", _
 *		"Saint Vincent and the Grenadines", "Samoa", "San Marino", "Saudi Arabia", "Senegal", _
 *		"Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", _
 *		"Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "South Korea", _
 *		"Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard and Jan Mayen", "Swaziland", "Sweden", _
 *		"Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "The Bahamas", _
 *		"The Gambia", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", _
 *		"Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Virgin Islands", "Uganda", _
 *		"Ukraine", "United Arab Emirates", "United Kingdom", _
 *		"United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan", _
 *		"Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Wallis and Futuna", "Western Sahara", _
 *		"Yemen", "Yugoslavia", "Zambia", "Zimbabwe")
 *	ACT.SetItems(countries)
 *End Sub
 *
 *Sub Activity_Pause (UserClosed As Boolean)
 *
 *End Sub</code>
 */
@ShortName("AutoCompleteEditText")
@Events(values={"ItemClick (Value As String)"})
public class AutoCompleteEditTextWrapper extends EditTextWrapper {
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		super.ba = ba;
		if (!keepOldObject)
		{
			setObject(new AutoCompleteTextView(ba.context));
			getObject().setSingleLine(true);
			getObject().setImeOptions(EditorInfo.IME_ACTION_DONE);
		}
		super.innerInitialize(ba, eventName, true);
		
		final AutoCompleteTextView a = (AutoCompleteTextView) getObject();
		if ((a.getInputType() & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT)
			a.setInputType(a.getInputType() | 0x00080000);
		a.setThreshold(1);
		a.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				InputMethodManager imm = (InputMethodManager)BA.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getObject().getWindowToken(), 0);
				ba.raiseEventFromUI(getObject(), eventName + "_itemclick", String.valueOf(a.getAdapter().getItem(position)));
			}
		});
	}
	/**
	 * Sets the list of possible items.
	 *The items visual style will be the same as the style of the main text.
	 */
	public void SetItems(BA ba, List Items) {
		MyArrayAdapter aa = new MyArrayAdapter(ba.context, Items.getObject(), 
				getTextSize(),getTypeface(), getGravity(), getTextColor());
		AutoCompleteTextView a = (AutoCompleteTextView) getObject();
		a.setAdapter(aa);
	}
	/**
	 * Sets the list of possible items and specifies their style.
	 *Example:<code>
	 *Dim act As AutoCompleteEditText
	 *act.Initialize("act")
	 *Activity.AddView(act, 10dip, 10dip, 200dip, 80dip)
	 *act.SetItems2(Array As String("aab", "abc"), act.Typeface, Gravity.LEFT, 12, Colors.Green) </code>
	 */
	public void SetItems2(BA ba, List Items, Typeface Typeface, int Gravity, float TextSize, int TextColor) {
		MyArrayAdapter aa = new MyArrayAdapter(ba.context, Items.getObject(), 
				TextSize, Typeface, Gravity, TextColor);
		AutoCompleteTextView a = (AutoCompleteTextView) getObject();
		a.setAdapter(aa);
	}
	/**
	 * Forces the drop down list to appear.
	 */
	public void ShowDropDown() {
		AutoCompleteTextView a = (AutoCompleteTextView) getObject();
		a.showDropDown();
	}
	/**
	 * Forces the drop down list to disappear.
	 */
	public void DismissDropDown() {
		AutoCompleteTextView a = (AutoCompleteTextView) getObject();
		a.dismissDropDown();
	}
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		AutoCompleteTextView v;
		if (prev == null) {
			v = ViewWrapper.buildNativeView((Context)tag, AutoCompleteTextView.class, props, designer);
		}
		else
			v = (AutoCompleteTextView) prev;
		return EditTextWrapper.build(v, props, designer, tag);
	}
	@Hide
	public static class MyArrayAdapter extends ArrayAdapter<String> {
		private Typeface typeface;
		int textColor, gravity;
		float textSize;
		@SuppressWarnings("unchecked")
		public MyArrayAdapter(Context context, java.util.List list, float textSize, Typeface typeface, int gravity, int textColor) {
			super(context, 0, list);
			this.typeface = typeface;
			this.textColor = textColor;
			this.textSize = textSize;
			this.gravity = gravity;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv;
			if (convertView == null) {
				tv = new TextView(this.getContext());
				tv.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				int p = Common.DipToCurrent(10);
				tv.setPadding(p, p, p, p);
				tv.setTextColor(textColor);
				tv.setTextSize(textSize);
				tv.setTypeface(typeface);
				tv.setGravity(gravity);
			}
			else
				tv = (TextView) convertView;
			tv.setText(getItem(position));
			return tv;
		}
	}
}
