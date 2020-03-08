package anywheresoftware.b4a.objects;

import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ScaleXSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.B4aDebuggable;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Pixel;
import anywheresoftware.b4a.BA.ShortName;

/**
 * CSBuilder is similar to StringBuilder, however it creates CharSequences instead of regular strings.
 *These objects include styling information.
 *Except of the Image method, all styling methods begin a span. The span will end (closed) when you call Pop or PopAll.
 *It is convenient to call PopAll at the end to close any open spans. 
 *Example: <code>
 *Dim cs As CSBuilder
 *cs.Initialize.Color(Colors.Red).Bold.Append("Hello ").Pop.Underline.Append("World!!!").PopAll
 *Label1.Text = cs</code>
 *The above code shows two red words. The first is bold and the second is underlined.
 */
@ShortName("CSBuilder")
@Events(values={"Click (Tag As Object)"})
public class CSBuilder extends AbsObjectWrapper<SpannableStringBuilder> implements B4aDebuggable {
	@SuppressWarnings("unchecked")
	private LinkedList<SpanMark> spanOpenings() {
		return (LinkedList<SpanMark>) AbsObjectWrapper.getExtraTags(getObject()).get("marks");
	}
	/**
	 * Initializes the builder. You can call this method multiple times to create new CharSequences.
	 *Note that like most other methods it returns the current object.
	 */
	public CSBuilder Initialize() {
		setObject(new SpannableStringBuilder() {
			@Override
			public int hashCode() {
				return System.identityHashCode(this);
			}
			@Override
			public boolean equals(Object o) {
				return this == o;
			}
		});

		AbsObjectWrapper.getExtraTags(getObject()).put("marks", new LinkedList<SpanMark>());
		return this;
	}
	/**
	 * Appends the provided String or CharSequence.
	 */
	public CSBuilder Append(CharSequence Text) {
		getObject().append(Text);
		
		return this;
	}
	/**
	 * Starts an underline span.
	 */
	public CSBuilder Underline() {
		return open(new UnderlineSpan());
	}
	/**
	 * Starts a clickable span. For the event to be raised you need to call the EnableClickEvents method.
	 *Example:<code>
		 *Sub Activity_Create(FirstTime As Boolean)
		 *	Dim lbl As Label
		 *	lbl.Initialize("")
		 *	Activity.AddView(lbl, 0, 20dip, 100%x, 50dip)
		 *	Dim cs As CSBuilder
		 *	cs.Initialize.Size(20).Append("Click on underine word: ")
		 *	cs.Clickable("cs", 1).Underline.Append("One").Pop.Pop
		 *	cs.Append(", ").Clickable("cs", 2).Underline.Append("Two").PopAll
		 *	cs.EnableClickEvents(lbl)
		 *	lbl.Text = cs
		 *End Sub
		 *
		 *Sub cs_Click (Tag As Object)
		 *	Log($"You have clicked on word: ${Tag}"$)
		 *End Sub</code>
	 */
	public CSBuilder Clickable(final BA ba, final String EventName, final Object Tag) {
		final String eventName = EventName.toLowerCase(BA.cul) + "_click";
		return open(new ClickableSpan() {

			@Override
			public void onClick(View widget) {
				ba.raiseEventFromUI(getObject(), eventName, Tag);
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				
			}
			
		});
	}
	/**
	 * Starts an alignment span.
	 *Alignment - One of the following strings: ALIGN_NORMAL, ALIGN_OPPOSITE or ALIGN_CENTER.
	 */
	public CSBuilder Alignment(android.text.Layout.Alignment Alignment) {
		return open(new AlignmentSpan.Standard(Alignment));
	}
	/**
	 * Starts a bold span.
	 */
	public CSBuilder Bold() {
		return open(new StyleSpan(Typeface.BOLD));
	}
	@Hide
	public CSBuilder open(Object span) {
		spanOpenings().add(new SpanMark(span, getObject().length()));
		return this;
	}
	/**
	 * Closes the most recent span. All spans must be closed. You can call PopAll to close all open spans.
	 */
	public CSBuilder Pop() {
		LinkedList<SpanMark> marks = spanOpenings();
		SpanMark sm = marks.removeLast();
		sm.markEnd = getObject().length();
		marks.addFirst(sm);
		if (marks.getLast().markEnd != -1) {
			for (SpanMark sm2 : marks) {
				getObject().setSpan(sm2.span, sm2.markStart, sm2.markEnd, 0);
			}
			marks.clear();
		}
		
		return this;
	}
	/**
	 * Closes all open spans.
	 */
	public CSBuilder PopAll() {
		LinkedList<SpanMark> marks = spanOpenings();
		while (marks.size() > 0)
			Pop();
		return this;
	}
	/**
	 * Starts a foreground color span.
	 */
	public CSBuilder Color(int Color) {
		return open(new ForegroundColorSpan(Color));
	}
	/**
	 * Starts a background color span. 
	 */
	public CSBuilder BackgroundColor(int Color) {
		return open(new BackgroundColorSpan(Color));
	}
	/**
	 * Starts a text size span. Note that you should not use 'dip' units with text size dimensions.
	 */
	public CSBuilder Size(int Size) {
		return open(new AbsoluteSizeSpan(Size, true));
	}
	/**
	 * Starts a relative size span. The actual text size will be multiplied with the set Proportion.
	 */
	public CSBuilder RelativeSize(float Proportion) {
		return open(new RelativeSizeSpan(Proportion));
	}
	/**
	 * Starts a custom typeface span.
	 */
	public CSBuilder Typeface(Typeface Typeface) {
		return open(new CustomTypefaceSpan(Typeface));
	}
	/**
	 * Starts a strikethrough span.
	 */
	public CSBuilder Strikethrough() {
		return open(new StrikethroughSpan());
	}
	/**
	 * Starts a vertical alignment span.
	 */
	public CSBuilder VerticalAlign(@Pixel int Shift) {
		return open(new VerticalAlignedSpan(Shift));
	}
	/**
	 * Adds an image span. This method will add a space character as a placeholder for the image.
	 *Unlike the other methods you do not need to call Pop to close this span as it is closed automatically.
	 *Bitmap - The image.
	 *Width / Height - Image dimensions.
	 *Baseline - If true then the image will be aligned based on the baseline. Otherwise it will be aligned based on the lowest descender in the text.	
	 */
	public CSBuilder Image(Bitmap Bitmap, @Pixel int Width, @Pixel int Height, boolean Baseline) {
		BitmapDrawable bd = new BitmapDrawable(BA.applicationContext.getResources(), Bitmap);
		bd.setBounds(0, 0, Width, Height);
		return open(new ImageSpan(bd, Baseline ? DynamicDrawableSpan.ALIGN_BASELINE : DynamicDrawableSpan.ALIGN_BOTTOM)).Append("_").Pop();
	}
	/**
	 * Starts a scale X span. It horizontally scales the text.
	 */
	public CSBuilder ScaleX(float Proportion) {
		return open(new ScaleXSpan(Proportion));
	}
	/**
	 * This method should be called when using clickable spans.
	 */
	public void EnableClickEvents(TextView Label) {
		Label.setMovementMethod(LinkMovementMethod.getInstance());
	}
	/**
	 * Returns the number of characters.
	 */
	public int getLength() {
		return getObject().length();
	}
	/**
	 * Returns a string with the characters.
	 */
	public String ToString() {
		return getObject().toString();
	}
	@Hide
	@Override
	public String toString() {
		return ToString();
	}
	@Hide
	@Override
	public Object[] debug(int limit, boolean[] outShouldAddReflectionFields) {
		Object[] res = new Object[2 * 2];
		res[0] = "Length";
		res[1] = getLength();
		res[2] = "ToString";
		res[3] = ToString();
		outShouldAddReflectionFields[0] = true;
		return res;
	}
	
	@Hide
	public static class SpanMark {
		public final Object span;
		public final int markStart; 
		public int markEnd = -1;
		public SpanMark(Object span, int markStart) {
			this.span = span;
			this.markStart = markStart;
		}
		@Override
		public String toString() {
			return "" + span.getClass() + " " + markStart + " -> " + markEnd;
		}
	}
	@Hide
	public static class CustomTypefaceSpan extends MetricAffectingSpan
	{
	    private final Typeface typeface;

	    public CustomTypefaceSpan(final Typeface typeface)
	    {
	        this.typeface = typeface;
	    }

	    @Override
	    public void updateDrawState(final TextPaint drawState)
	    {
	        apply(drawState);
	    }

	    @Override
	    public void updateMeasureState(final TextPaint paint)
	    {
	        apply(paint);
	    }

	    private void apply(final Paint paint)
	    {
	        final Typeface oldTypeface = paint.getTypeface();
	        final int oldStyle = oldTypeface != null ? oldTypeface.getStyle() : 0;
	        final int fakeStyle = oldStyle & ~typeface.getStyle();

	        if ((fakeStyle & Typeface.BOLD) != 0)
	        {
	            paint.setFakeBoldText(true);
	        }

	        if ((fakeStyle & Typeface.ITALIC) != 0)
	        {
	            paint.setTextSkewX(-0.25f);
	        }

	        paint.setTypeface(typeface);
	    }
	}
	@Hide
	public static class VerticalAlignedSpan extends MetricAffectingSpan {
		   int shift;
		   public VerticalAlignedSpan(int shift) {
		     this.shift = shift;
		   }
		    @Override
		  public void updateDrawState(TextPaint tp) {
		  tp.baselineShift += shift;
		  }

		  @Override
		  public void updateMeasureState(TextPaint tp) {
		  tp.baselineShift += shift;
		  }
		}
	
}
