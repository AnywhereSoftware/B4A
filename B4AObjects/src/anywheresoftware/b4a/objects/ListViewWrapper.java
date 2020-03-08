package anywheresoftware.b4a.objects;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.SimpleListAdapter.SimpleItem;
import anywheresoftware.b4a.objects.SimpleListAdapter.SingleLineData;
import anywheresoftware.b4a.objects.SimpleListAdapter.SingleLineLayout;
import anywheresoftware.b4a.objects.SimpleListAdapter.TwoLinesAndBitmapData;
import anywheresoftware.b4a.objects.SimpleListAdapter.TwoLinesAndBitmapLayout;
import anywheresoftware.b4a.objects.SimpleListAdapter.TwoLinesData;
import anywheresoftware.b4a.objects.SimpleListAdapter.TwoLinesLayout;
import anywheresoftware.b4a.objects.drawable.ColorDrawable;

/**
 * ListView is a very useful view that can handle large and small lists.
 *The ListView raises two events. ItemClick is raised when an item is clicked and ItemLongClick is raised when an item is clicked and held.
 *See the <link>ListView tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6537-listview-tutorial.html</link> for more information.
 */
@ShortName("ListView")
@ActivityObject
@DontInheritEvents
@Events(values={"ItemClick (Position As Int, Value As Object)", "ItemLongClick (Position As Int, Value As Object)"})
public class ListViewWrapper extends ViewWrapper<ListViewWrapper.SimpleListView>{
	
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject) {
			SimpleListView s = new SimpleListView(ba.context);
			setObject(s);
		}
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_itemclick")) {
			getObject().setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					ba.raiseEventFromUI(getObject(),eventName + "_itemclick", 
							position, getObject().adapter.getItem(position));
				}
				
			});
		}
		if (ba.subExists(eventName + "_itemlongclick")) {
			getObject().setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int position, long id) {
					ba.raiseEventFromUI(getObject(), eventName + "_itemlongclick", 
							position, getObject().adapter.getItem(position));
					return true;
				}
				
			});
		}
	}
	/**
	 * Returns the number of items stored in the list.
	 */
	public int getSize() {
		return getObject().adapter.getCount();
	}
	/**
	 *Returns the layout that is used to show single line items.
	 *You can change the layout values to change the appearance of such items.
	 *Example:<code>
	 *Dim Label1 As Label
	 *Label1 = ListView1.SingleLineLayout.Label
	 *Label1.TextSize = 20
	 *Label1.TextColor = Colors.Green</code>
	 */
	public SingleLineLayout getSingleLineLayout() {
		return getObject().adapter.SingleLine;
	}
	/**
	 * Returns the layout that is used to show two lines items.
	 *You can change the layout values to change the appearance of such items.
	 *Example:<code>
	 *Dim Label1 As Label
	 *Label1 = ListView1.TwoLinesLayout.SecondLabel
	 *Label1.TextSize = 20
	 *Label1.TextColor = Colors.Green</code>
	 */
	public TwoLinesLayout getTwoLinesLayout() {
		return getObject().adapter.TwoLines;
	}
	/**
	 * Returns the layout that is used to show two lines and bitmap items.
	 *You can change the layout values to change the appearance of such items.
	 *For example if you want to remove the second label (in all items with this layout):<code>
	 *ListView1.TwoLinesAndBitmap.SecondLabel.Visible = False</code>
	 */
	public TwoLinesAndBitmapLayout getTwoLinesAndBitmap() {
		return getObject().adapter.TwoLinesAndBitmap;
	}
	/**
	 * Adds a single line item.
	 * Example:<code>
	 * ListView1.AddSingleLine("Sunday")</code>
	 */
	public void AddSingleLine(CharSequence Text) {
		AddSingleLine2(Text, null);
	}
	/**
	 * Adds a single line item.
	 *The specified return value will be returned when calling GetItem or in the ItemClick event.
	 *Example:<code>
	 *ListView1.AddSingleLine2("Sunday", 1)</code>
	 */
	public void AddSingleLine2(CharSequence Text, Object ReturnValue) {
		SimpleListAdapter.SingleLineData sl = new SingleLineData();
		sl.Text = Text;
		sl.ReturnValue = ReturnValue;
		add(sl);
	}
	/**
	 * Adds a two lines item.
	 *Example:<code>
	 *ListView1.AddTwoLines("This is the first line.", "And this is the second")</code>
	 */
	public void AddTwoLines(CharSequence Text1, CharSequence Text2) {
		AddTwoLines2(Text1, Text2, null);
	}
	/**
	 * Adds a two lines item.
	 *The specified return value will be returned when calling GetItem or in the ItemClick event.
	 */
	public void AddTwoLines2(CharSequence Text1, CharSequence Text2, Object ReturnValue) {
		TwoLinesData t = new TwoLinesData();
		t.Text = Text1;
		t.ReturnValue = ReturnValue;
		t.SecondLineText = Text2;
		add(t);
	}
	/**
	 * Adds a two lines and a bitmap item.
	 *Example:<code>
	 *ListView1.AddTwoLinesAndBitmap("First line", "Second line", LoadBitmap(File.DirAssets, "SomeImage.png"))</code>
	 */
	public void AddTwoLinesAndBitmap(CharSequence Text1, CharSequence Text2, Bitmap Bitmap) {
		AddTwoLinesAndBitmap2(Text1, Text2, Bitmap, null);
	}
	/**
	 * Adds a two lines and a bitmap item.
	 *The specified return value will be returned when calling GetItem or in the ItemClick event.
	 */
	public void AddTwoLinesAndBitmap2(CharSequence Text1, CharSequence Text2, Bitmap Bitmap, Object ReturnValue) {
		TwoLinesAndBitmapData t = new TwoLinesAndBitmapData();
		t.Text = Text1;
		t.ReturnValue = ReturnValue;
		t.SecondLineText = Text2;
		t.Bitmap = Bitmap;
		add(t);
	}
	@Hide
	public void add(SimpleItem si) {
		getObject().adapter.items.add(si);
		getObject().adapter.notifyDataSetChanged();
	}
	/**
	 * Returns the value of the item at the specified position.
	 *Returns the "return value" if it was set and if not returns the text of the first line.
	 */
	public Object GetItem(int Index) {
		return getObject().adapter.getItem(Index);
	}
	/**
	 * Removes the item at the specified position.
	 */
	public void RemoveAt(int Index) {
		getObject().adapter.items.remove(Index);
		getObject().adapter.notifyDataSetChanged();
	}
	/**
	 * Clears all items from the list.
	 */
	public void Clear() {
		getObject().adapter.items.clear();
		getObject().adapter.notifyDataSetChanged();
	}
	/**
	 * Gets or sets whether the fast scroll icon will appear when the user scrolls the list.
	 *The default is false.
	 */
	public void setFastScrollEnabled(boolean Enabled) {
		getObject().setFastScrollEnabled(Enabled);
	}
	public boolean getFastScrollEnabled() {
		return getObject().isFastScrollEnabled();
	}
	/**
	 * Sets the background color that will be used while scrolling the list.
	 *This is an optimization done to make the scrolling smoother.
	 *Set to <code>Colors.Transparent</code> if the background behind the list is not solid color.
	 *The default is black.
	 */
	public void setScrollingBackgroundColor(int Color) {
		getObject().setCacheColorHint(Color);
	}
	/**
	 * Sets the currently selected item. Calling this method will make this item visible.
	 *If the user is interacting with the list with the keyboard or the wheel button the item will also be visibly selected.
	 *Example:<code>ListView1.SetSelection(10)</code>
	 */
	public void SetSelection(int Position) {
		getObject().setSelection(Position);
	}
	
	@Hide
	public static class SimpleListView extends ListView {
		public SimpleListAdapter adapter;
		public SimpleListView(Context context) {
			super(context);
			adapter = new SimpleListAdapter(context);
			this.setAdapter(adapter);
		}
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = ViewWrapper.buildNativeView((Context)tag, SimpleListView.class, props, designer);
		}
		ListView list = (ListView)ViewWrapper.build(prev, props, designer);
		HashMap<String, Object> drawProps = (HashMap<String, Object>) props.get("drawable");
		Drawable d = DynamicBuilder.build(list, drawProps, designer, null);
		if (d != null)
			list.setBackgroundDrawable(d);
		list.setFastScrollEnabled((Boolean)props.get("fastScrollEnabled"));
		if (designer) {
			SimpleListView slv = (SimpleListView) list;
			if (slv.adapter.items.size() == 0) {
				for (int i = 1;i <= 10;i++) {
					SimpleListAdapter.SingleLineData s = new SingleLineData();
					s.Text = "Item #" + i;
					slv.adapter.items.add(s);
				}
				slv.adapter.notifyDataSetChanged();
			}
		}
		return list;
	}
}
