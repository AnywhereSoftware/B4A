package anywheresoftware.b4a.objects;

import java.lang.reflect.Field;
import java.util.HashMap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.constants.Colors;
/**
 * EditText view is a view that allows the user to write free text (similar to WinForms TextBox).
 *The EditText has two modes; SingleLine and MultiLine. You can set it to be multiline by calling <code>EditText1.SingleLine = False</code>
 *On most devices the soft keyboard will show automatically when the user presses on the EditText.
 *You can change the InputType property and change the type of keyboard that appears.
 *For example: <code>EditText1.InputType = EditText1.INPUT_TYPE_NUMBERS</code> will cause the numeric keyboard to appear when
 *the user presses on the EditText. Note that it will also cause the EditText to only accept numbers.
 *
 *The TextChanged event fires whenever the text changes and it includes the old and new strings.
 *The EnterPressed event fires when the user presses on the enter key or action key (Done or Next).
 *The FocusChanged event fires when the view is focused or loses focus. HasFocus parameter value will be set accordingly.
 *Note that most views are not focusable. For example, pressing on a Button will not change the focus state of an EditText.
 */
@ShortName("EditText")
@ActivityObject
@DontInheritEvents
@Events(values={"TextChanged (Old As String, New As String)", "EnterPressed", "FocusChanged (HasFocus As Boolean)"})
public class EditTextWrapper extends TextViewWrapper<EditText> {
	/**
	 * No keyboard will be displayed.
	 */
	public static final int INPUT_TYPE_NONE = InputType.TYPE_NULL;
	/**
	 * Numeric keyboard will be displayed. Only numbers are accepted.
	 */
	public static final int INPUT_TYPE_NUMBERS = InputType.TYPE_CLASS_NUMBER;
	/**
	 * Numeric keyboard will be displayed. Numbers, decimal point and minus sign are accepted.
	 */
	public static final int INPUT_TYPE_DECIMAL_NUMBERS = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
		| InputType.TYPE_NUMBER_FLAG_SIGNED;
	/**
	 * Default text mode.
	 */
	public static final int INPUT_TYPE_TEXT = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
	/**
	 * Keyboard will be displayed in phone mode.
	 */
	public static final int INPUT_TYPE_PHONE = InputType.TYPE_CLASS_PHONE;
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		super.ba = ba;
		if (!keepOldObject)
			setObject(new EditText(ba.context));
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_textchanged")) {
			TextWatcher watcher = new TextWatcher() {
				private CharSequence old;
				@Override
				public void afterTextChanged(Editable s) {
					ba.raiseEvent2(getObject(), false, eventName + "_textchanged", true, 
							old, getObject().getText().toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					old = s.toString();
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}
				
			};
			getObject().addTextChangedListener(watcher);
		}
		if (ba.subExists(eventName + "_enterpressed")) {
			OnEditorActionListener o = new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					ba.raiseEvent(getObject(), eventName + "_enterpressed");
					return false;
				}
				
			};
			getObject().setOnEditorActionListener(o);
		}
		if (ba.subExists(eventName + "_focuschanged")) {
			getObject().setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					ba.raiseEventFromUI(getObject(), eventName + "_focuschanged", hasFocus);
				}
			});
		}
	}
	/**
	 * By default the OS sets the virtual keyboard action key to display Done or Next according to the specific layout.
	 *You can force it to display Done by setting this value to True.
	 *Example:<code>
	 *EditText1.ForceDoneButton = True</code>
	 */
	public void setForceDoneButton(boolean value) {
		if (value)
			getObject().setImeOptions(EditorInfo.IME_ACTION_DONE);
		else
			getObject().setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED);
	}
	/**
	 * Sets whether the EditText should be in single line mode or multiline mode.
	 */
	public void setSingleLine(boolean singleLine) {
		getObject().setSingleLine(singleLine);
	}
	/**
	 * Sets whether the EditText should be in password mode and hide the actual characters.
	 */
	public void setPasswordMode(boolean value) {
		if (value)
			getObject().setTransformationMethod(PasswordTransformationMethod.getInstance());
		else
			getObject().setTransformationMethod(null);
	}
	/**
	 * Gets or sets the selection start position (or the cursor position).
	 *Returns -1 if there is no selection or cursor.
	 */
	public int getSelectionStart() {
		return Selection.getSelectionStart(getObject().getText());
	}
	public void setSelectionStart(int value) {
		getObject().setSelection(value);
	}
	/**
	 * Gets the selection length.
	 */
	public int getSelectionLength() {
		return Math.max(0, Selection.getSelectionEnd(getObject().getText()) - getSelectionStart());
	}
	/**
	 * Sets the selected text.
	 */
	public void SetSelection(int Start, int Length) {
		Selection.setSelection(getObject().getText(), Start, Start + Length);
	}
	/**
	 * Selects the entire text.
	 */
	public void SelectAll() {
		Selection.selectAll(getObject().getText());
	}

	/**
	 * Gets or sets the input type flag. This flag is used to determine the settings of the virtual keyboard.
	 *Note that changing the input type will set the EditText to be in single line mode.
	 *Example:<code>
	 *EditText1.InputType = EditText1.INPUT_TYPE_NUMBERS</code>
	 */
	public void setInputType(int value) {
		getObject().setInputType(value);
	}
	public int getInputType() {
		return getObject().getInputType();
	}
	/**
	 * Sets whether the text content will wrap within the EditText bounds. Relevant when the EditText is in multiline mode.
	 *Example:<code>
	 *EditText1.Wrap = False</code>
	 */
	public void setWrap(boolean value) {
		getObject().setHorizontallyScrolling(!value);
	}
	/**
	 * Gets or sets the text that will appear when the EditText is empty.
	 *Example:<code>
	 *EditText1.Hint = "Enter username"</code>
	 */
	public void setHint(String text) {
		getObject().setHint(text);
	}
	public String getHint() {
		CharSequence c = getObject().getHint();
		return c == null ? "" : String.valueOf(c);
	}
	/**
	 * Gets or sets the hint text color.
	 *Example:<code>
	 *EditText1.HintColor = Colors.Gray</code>
	 */
	public void setHintColor(int Color) {
		getObject().setHintTextColor(Color);
	}
	public int getHintColor() {
		return getObject().getCurrentHintTextColor();
	}
	@Override
	@RaisesSynchronousEvents
	public void setText(CharSequence Text) {
		super.setText(Text);
	}
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		EditText v;
		if (prev == null) {
			v = ViewWrapper.buildNativeView((Context)tag, EditText.class, props, designer);
		}
		else
			v = (EditText) prev;
		TextViewWrapper.build(v, props, designer);
		ColorStateList defaultHintColor = null;
		if (designer) {
			defaultHintColor = (ColorStateList) ViewWrapper.getDefault(v, "hintColor", v.getHintTextColors());
		}
		int hintColor = BA.gm(props, "hintColor", ViewWrapper.defaultColor);
		if (hintColor != ViewWrapper.defaultColor)
			v.setHintTextColor(hintColor);
		else if (designer)
			v.setHintTextColor(defaultHintColor);
		String hint = BA.gm(props, "hint", "");
		if (designer && hint.length() == 0)
			hint = (String)props.get("name");
		v.setHint(hint);
		
		String inputType = (String)props.get("inputType");
		if (inputType != null) {
			Field f = EditTextWrapper.class.getField("INPUT_TYPE_" + inputType);
			v.setInputType((Integer)f.get(null));
		}
		
		boolean singleLine = (Boolean)props.get("singleLine");
		v.setSingleLine(singleLine);
		if (designer && singleLine) {
			v.setInputType(0x00080000);
		}
		if ((Boolean)props.get("password"))
			v.setTransformationMethod(PasswordTransformationMethod.getInstance());
		else
			v.setTransformationMethod(null);
		v.setHorizontallyScrolling(!BA.gm(props, "wrap", true));
		boolean forceDone = BA.gm(props, "forceDone", false);
		if (forceDone)
			v.setImeOptions(EditorInfo.IME_ACTION_DONE);
		else
			v.setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED);
		
		return v;
	}
	
}
