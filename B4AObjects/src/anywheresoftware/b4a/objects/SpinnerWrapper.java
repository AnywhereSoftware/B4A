package anywheresoftware.b4a.objects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.LayoutBuilder.DesignerTextSizeMethod;
import anywheresoftware.b4a.keywords.constants.Colors;
import anywheresoftware.b4a.objects.collections.List;

/**
 * A folded list that opens when the user clicks on it and allows the user to choose an item. Similar to WinForms ComboBox.
 *The ItemClick event is raised each time a user presses on an item (even if it is the already selected item).
 */
@ActivityObject
@ShortName("Spinner")
@DontInheritEvents
@Events(values={"ItemClick (Position As Int, Value As Object)"})
public class SpinnerWrapper extends ViewWrapper<SpinnerWrapper.B4ASpinner> implements DesignerTextSizeMethod{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject) {
			setObject(new B4ASpinner(ba.context));
		}
		super.innerInitialize(ba, eventName, true);
		getObject().ba = ba;
		getObject().eventName = eventName;
		getObject().disallowItemClick = false;

	}
	/**
	 * Returns the number of items.
	 */
	public int getSize() {
		return getObject().adapter.getCount();
	}
	/**
	 * Gets or sets the index of the selected item. Returns -1 if no item is selected.
	 */
	public int getSelectedIndex() {
		return getObject().selectedItem;
	}
	public void setSelectedIndex(int value) {
		getObject().disallowItemClick = true;
		try {
			getObject().setSelection(value);
			getObject().selectedItem = value;
		} finally {
			getObject().disallowItemClick = false;
		}
	}
	/**
	 * Returns the value of the selected item.
	 */
	public String getSelectedItem() {
		Object o = getObject().getItemAtPosition(getObject().selectedItem);
		return String.valueOf(o == null ? "" : o);
	}
	public int IndexOf(String value) {
		return getObject().adapter.items.indexOf(value);
	}
	/**
	 * Gets or sets the title that will be displayed when the spinner is opened.
	 */
	public String getPrompt() {
		return (String) getObject().getPrompt();
	}
	public void setPrompt(CharSequence title) {
		if (title == null || title.length() == 0)
			title = null;
		getObject().setPrompt(title);
	}
	/**
	 * Adds an item.
	 *Example:<code>
	 *Spinner1.Add("Sunday")</code>
	 */
	public void Add(String Item) {
		getObject().disallowItemClick = true;
		try {
			getObject().adapter.items.add(Item);
			getObject().adapter.notifyDataSetChanged();
			if (getObject().selectedItem == -1)
				getObject().selectedItem = 0;


		} finally {
			getObject().disallowItemClick = false;
		}
	}

	/**
	 * Adds multiple items.
	 *Example:<code>
	 *Spinner1.AddAll(Array As String("Sunday", "Monday", ...))</code>
	 */
	public void AddAll(List List) {
		getObject().disallowItemClick = true;
		try {
			getObject().adapter.items.addAll(List.getObject());
			getObject().adapter.notifyDataSetChanged();
			if (getObject().selectedItem == -1)
				getObject().selectedItem = 0;
		} finally {
			getObject().disallowItemClick = false;
		}
	}
	/**
	 *Returns the item at the specified index.
	 */
	public String GetItem(int Index) {
		return String.valueOf(getObject().adapter.getItem(Index));
	}
	/**
	 *Removes the item at the specified index.
	 */
	public void RemoveAt(int Index) {
		getObject().disallowItemClick = true;
		try {
			getObject().adapter.items.remove(Index);
			getObject().adapter.notifyDataSetChanged();
			if (getObject().selectedItem == getObject().adapter.getCount()) {
				getObject().selectedItem--;			
			}
		} finally {
			getObject().disallowItemClick = false;
		}
	}
	/**
	 *Clears all items.
	 */
	public void Clear() {
		getObject().disallowItemClick = true;
		try {
			getObject().adapter.items.clear();
			getObject().adapter.notifyDataSetChanged();
			getObject().selectedItem = -1;
		} finally {
			getObject().disallowItemClick = false;
		}
	}
	/**
	 * Gets or sets the text color. The color should be set before adding items.
	 *Setting the color to transparent will make the spinner use the default text color.
	 */
	public void setTextColor(int Color) {
		getObject().adapter.textColor = Color;
	}
	public int getTextColor() {
		return getObject().adapter.textColor;
	}
	
	/**
	 * Gets or sets the dropdown text color. The color should be set before adding items.
	 *Setting the color to transparent will make the spinner use the default text color.
	 */
	public void setDropdownTextColor(int Color) {
		getObject().adapter.dropdownTextColor = Color;
	}
	public int getDropdownTextColor() {
		return getObject().adapter.dropdownTextColor;
	}
	
	/** Gets or sets the dropdown items background color. The color should be set before adding items.
	 *Setting the color to transparent will make the spinner use the default background color.
	 */
	public void setDropdownBackgroundColor(int Color) {
		getObject().adapter.ddbackgroundColor = Color;
	}
	public int getDropdownBackgroundColor() {
		return getObject().adapter.ddbackgroundColor;
	}
	/**
	 * Gets or sets the text size. The size should be set before adding items.
	 */
	public void setTextSize(float TextSize) {
		getObject().adapter.textSize = TextSize;
	}
	public float getTextSize() {
		float pixels =  getObject().adapter.textSize;
		return pixels;
	}
	@Hide
	public static class B4ASpinner extends Spinner {
		public B4ASpinnerAdapter adapter;
		int selectedItem = -1;
		public String eventName;
		public BA ba;
		public boolean disallowItemClick = true;
		public B4ASpinner(Context context) {
			super(context);
			adapter = new B4ASpinnerAdapter(context);
			this.setAdapter(adapter);
		}

		@Override
		public void setSelection(int position) {
			super.setSelection(position);
			selectedItem = position;
			if (ba != null && !disallowItemClick) {
				ba.raiseEventFromUI(this, eventName + "_itemclick", 
						selectedItem, adapter.getItem(selectedItem));
			}
		}

	}
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, B4ASpinner.class, props, designer);
		}
		B4ASpinner list = (B4ASpinner)ViewWrapper.build(prev, props, designer);
		Float f = (Float)props.get("fontsize");
		if (f != null) {
			list.adapter.textSize = f;
			list.adapter.textColor = (Integer)props.get("textColor");
			if (Color.alpha(list.adapter.textColor) == 0 || list.adapter.textColor == ViewWrapper.defaultColor)
				list.adapter.textColor = Colors.Transparent;
		}
		if (designer) {
			list.adapter.items.clear();
			list.adapter.items.add(props.get("name"));
			list.adapter.notifyDataSetChanged();
		}
		String prompt = (String)props.get("prompt");
		if (prompt != null && prompt.length() > 0)
			list.setPrompt(prompt);
		return list;
	}
	@Hide
	public static class B4ASpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
		ArrayList<Object> items = new ArrayList<Object>();
		private LayoutInflater inflater;
		public float textSize = 16;
		public int textColor = Colors.Transparent;
		public int dropdownTextColor = Colors.Transparent;
		public int ddbackgroundColor = Colors.Transparent;
		public B4ASpinnerAdapter(Context context) {
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getDropDownView(final int position, View convertView, final ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
				((TextView)convertView).setTextSize(textSize);
				
				if (textColor != Color.TRANSPARENT && dropdownTextColor == Color.TRANSPARENT)
					((TextView)convertView).setTextColor(textColor);
				else if (dropdownTextColor != Color.TRANSPARENT)
					((TextView)convertView).setTextColor(dropdownTextColor);
				if (ddbackgroundColor != Color.TRANSPARENT) {
					convertView.setBackgroundColor(ddbackgroundColor);
				}
			}
			TextView tv = (TextView)convertView;
			Object o = items.get(position);
			if (o instanceof CharSequence)
				tv.setText((CharSequence)o);
			else
				tv.setText(String.valueOf(o));
			return convertView;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
				((TextView)convertView).setTextSize(textSize);
				if (textColor != Color.TRANSPARENT)
					((TextView)convertView).setTextColor(textColor);
			}
			TextView tv = (TextView)convertView;
			Object o = items.get(position);
			if (o instanceof CharSequence)
				tv.setText((CharSequence)o);
			else
				tv.setText(String.valueOf(o));

			return convertView;
		}

	}
}
