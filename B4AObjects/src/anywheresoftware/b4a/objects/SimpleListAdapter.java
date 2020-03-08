package anywheresoftware.b4a.objects;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView.ScaleType;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.constants.Colors;
import anywheresoftware.b4a.objects.ListViewWrapper.SimpleListView;

@Hide
public class SimpleListAdapter extends BaseAdapter implements ListAdapter{
	private static final int SINGLE_LINE_ITEM = 0;
	private static final int TWO_LINES_ITEM = 1;
	private static final int TWO_LINES_AND_BITMAP_ITEM = 2;
	public ArrayList<SimpleItem> items = new ArrayList<SimpleItem>();
	public SingleLineLayout SingleLine;
	public TwoLinesLayout TwoLines;
	public TwoLinesAndBitmapLayout TwoLinesAndBitmap;
	public BALayout dummyParent;
	private ItemLayout[] layouts;
	public SimpleListAdapter (Context context) {
		dummyParent = new BALayout(context);
		SingleLine = new SingleLineLayout(dummyParent, false);
		TwoLines = new TwoLinesLayout(dummyParent, false);
		TwoLinesAndBitmap = new TwoLinesAndBitmapLayout(dummyParent);
		layouts = new ItemLayout[3];
		layouts[0] = SingleLine;
		layouts[1] = TwoLines;
		layouts[2] = TwoLinesAndBitmap;
	}
	@Override
	public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
	public int getViewTypeCount() {
        return 3;
    }
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position).getReturnValue();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleItem item = items.get(position);
		if (convertView == null) {
			BALayout bl = new BALayout(parent.getContext());
			bl.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, layouts[item.getType()].getItemHeight()));
			item.addNewToLayout(bl, this);
			convertView = bl;
		}
		item.updateExisting((ViewGroup) convertView, this);
		return convertView;
	}
	public interface SimpleItem {
		void addNewToLayout(ViewGroup layout, SimpleListAdapter adapter);
		void updateExisting(ViewGroup layout, SimpleListAdapter adapter);
		int getType();
		Object getReturnValue();
	}
	public interface ItemLayout {
		int getItemHeight();
	}
	public static class SingleLineLayout implements ItemLayout{
		/**
		 * The label that is used for the the first line (and only line in case of SingleLineLayout).
		 */
		public LabelWrapper Label;
		/**
		 * The background of items with this layout.
		 */
		public Drawable Background;
		protected int itemHeight;
		private SingleLineLayout(ViewGroup dummyParent, boolean dontSetLayout) {
			TextView t = new TextView(dummyParent.getContext());
			Label = new LabelWrapper();
			Label.setObject(t);
			if (!dontSetLayout) {
				t.setTextSize(22.5f);
				t.setTextColor(Color.WHITE);
				itemHeight = Common.DipToCurrent(50);
				t.setGravity(Gravity.CENTER_VERTICAL);
				dummyParent.addView(t, new BALayout.LayoutParams(Common.DipToCurrent(5), 0, ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.FILL_PARENT));
			}
		}
		/**
		 * Gets or sets the height of items with this layout.
		 */
		@Override
		public int getItemHeight() {
			return itemHeight;
		}
		public void setItemHeight(int Height) {
			itemHeight = Height;
		}
	}
	public static class TwoLinesLayout extends SingleLineLayout{
		/**
		 * The label that is used for the second line.
		 */
		public LabelWrapper SecondLabel;
		private TwoLinesLayout(ViewGroup dummyParent, boolean dontSetLayout) {
			super(dummyParent, true);
			TextView t2 = new TextView(dummyParent.getContext());
			SecondLabel = new LabelWrapper();
			SecondLabel.setObject(t2);
			if (!dontSetLayout) {
				TextView t = Label.getObject();
				t.setTextSize(19.5f);
				t2.setTextSize(16.5f);
				itemHeight = Common.DipToCurrent(60);
				t.setGravity(Gravity.BOTTOM);
				t2.setGravity(Gravity.TOP);
				t.setTextColor(Colors.White);
				t2.setTextColor(Colors.Gray);
				dummyParent.addView(t, new BALayout.LayoutParams(Common.DipToCurrent(5), 0, ViewGroup.LayoutParams.FILL_PARENT,
						Common.DipToCurrent(30)));	
				dummyParent.addView(t2, new BALayout.LayoutParams(Common.DipToCurrent(5), Common.DipToCurrent(32), ViewGroup.LayoutParams.FILL_PARENT,
						Common.DipToCurrent(28)));
			}
		}
		
	}
	public static class TwoLinesAndBitmapLayout extends TwoLinesLayout{
		/**
		 * The ImageView that holds the bitmap.
		 */
		public ImageViewWrapper ImageView;
		private TwoLinesAndBitmapLayout(ViewGroup dummyParent) {
			super(dummyParent, true);
			TextView t = Label.getObject();
			TextView t2 = SecondLabel.getObject();
			t.setTextSize(19.5f);
			t2.setTextSize(16.5f);
			itemHeight = Common.DipToCurrent(60);
			t.setGravity(Gravity.BOTTOM);
			t2.setGravity(Gravity.TOP);
			t.setTextColor(Colors.White);
			t2.setTextColor(Colors.Gray);
			dummyParent.addView(t, new BALayout.LayoutParams(Common.DipToCurrent(60), 0, ViewGroup.LayoutParams.FILL_PARENT,
					Common.DipToCurrent(30)));	
			dummyParent.addView(t2, new BALayout.LayoutParams(Common.DipToCurrent(60), Common.DipToCurrent(32), ViewGroup.LayoutParams.FILL_PARENT,
					Common.DipToCurrent(28)));
			ImageView iv = new ImageView(dummyParent.getContext());
			
			ImageView = new ImageViewWrapper();
			ImageView.setObject(iv);
			
			dummyParent.addView(iv, new BALayout.LayoutParams(Common.DipToCurrent(5), Common.DipToCurrent(5),
					Common.DipToCurrent(50), Common.DipToCurrent(50)));
		}
	}
	
	@Hide
	public static class SingleLineData implements SimpleItem {
		public CharSequence Text;
		public Object ReturnValue;
		@Hide
		@Override
		public void addNewToLayout(ViewGroup layout, SimpleListAdapter adapter) {
			layout.setBackgroundDrawable(adapter.SingleLine.Background);
			addNewToLayoutImpl(layout, adapter.SingleLine.Label.getObject());
		}
		protected void addNewToLayoutImpl(ViewGroup layout, TextView model) {
			TextView tv = new TextView(layout.getContext());
			layout.addView(tv, model.getLayoutParams());
			tv.setBackgroundDrawable(model.getBackground());
			tv.setTextSize(model.getTextSize() / layout.getContext().getResources().getDisplayMetrics().scaledDensity);
			tv.setTextColor(model.getTextColors());
			tv.setGravity(model.getGravity());
			tv.setVisibility(model.getVisibility());
			tv.setTypeface(model.getTypeface());
		}
		
		@Hide
		@Override
		public void updateExisting(ViewGroup layout, SimpleListAdapter adapter) {
			((TextView)layout.getChildAt(0)).setText(Text);
		}
		@Hide
		@Override
		public int getType() {
			return SINGLE_LINE_ITEM;
		}
		@Hide
		@Override
		public Object getReturnValue() {
			return ReturnValue == null ? String.valueOf(Text) : ReturnValue;
		}
		
	}
	@Hide
	public static class TwoLinesData extends SingleLineData {
		public CharSequence SecondLineText;
		@Hide
		@Override
		public void addNewToLayout(ViewGroup layout, SimpleListAdapter adapter) {
			layout.setBackgroundDrawable(adapter.TwoLines.Background);
			super.addNewToLayoutImpl(layout, adapter.TwoLines.Label.getObject());
			super.addNewToLayoutImpl(layout, adapter.TwoLines.SecondLabel.getObject());
		}
		@Hide
		@Override
		public void updateExisting(ViewGroup layout, SimpleListAdapter adapter) {
			super.updateExisting(layout, adapter);
			((TextView)layout.getChildAt(1)).setText(SecondLineText);
		}
		@Hide
		@Override
		public int getType() {
			return TWO_LINES_ITEM;
		}
	}
	@Hide
	public static class TwoLinesAndBitmapData extends TwoLinesData {
		public Bitmap Bitmap;
		@Hide
		@Override
		public void addNewToLayout(ViewGroup layout, SimpleListAdapter adapter) {
			layout.setBackgroundDrawable(adapter.TwoLinesAndBitmap.Background);
			super.addNewToLayoutImpl(layout, adapter.TwoLinesAndBitmap.Label.getObject());
			super.addNewToLayoutImpl(layout, adapter.TwoLinesAndBitmap.SecondLabel.getObject());
			ImageView iv = new ImageView(layout.getContext());
			ImageView model = adapter.TwoLinesAndBitmap.ImageView.getObject();
			layout.addView(iv, model.getLayoutParams());
			iv.setVisibility(model.getVisibility());
		}
		@Hide
		@Override
		public void updateExisting(ViewGroup layout, SimpleListAdapter adapter) {
			super.updateExisting(layout, adapter);
			ImageView iv = (ImageView)layout.getChildAt(2);
			iv.setVisibility(Bitmap != null ? View.VISIBLE : View.GONE);
			if (Bitmap != null)
				iv.setBackgroundDrawable(new BitmapDrawable(Bitmap));
		}
		@Hide
		@Override
		public int getType() {
			return TWO_LINES_AND_BITMAP_ITEM;
		}
	}
}
