package anywheresoftware.b4a.keywords;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;
import android.widget.Toast;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.CustomClass;
import anywheresoftware.b4a.BA.CustomClasses;
import anywheresoftware.b4a.BA.DesignerName;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Pixel;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ResumableSub;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.SubDelegator;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.BA.WaitForEvent;
import anywheresoftware.b4a.Msgbox;
import anywheresoftware.b4a.Msgbox.DialogResponse;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.keywords.constants.Colors;
import anywheresoftware.b4a.keywords.constants.Gravity;
import anywheresoftware.b4a.keywords.constants.KeyCodes;
import anywheresoftware.b4a.keywords.constants.TypefaceWrapper;
import anywheresoftware.b4a.objects.B4AException;
import anywheresoftware.b4a.objects.LabelWrapper;
import anywheresoftware.b4a.objects.PanelWrapper;
import anywheresoftware.b4a.objects.ServiceHelper;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.drawable.BitmapDrawable;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;
import anywheresoftware.b4a.objects.streams.File;
@CustomClasses(values = {
		@CustomClass(name = "Standard Class", fileNameWithoutExtension = "standard", priority = 1000),
		@CustomClass(name = "Custom View", fileNameWithoutExtension = "customview")
})
/**
 * These are the internal keywords.
 */
@ActivityObject
@Version(11.81f)
public class Common {
	static {
		System.out.println("common created.");
	}
	//BA named "mine" will be processBA.

	public static final boolean True = true;
	public static final boolean False = false;
	public static final Object Null = null;
	/**
	 * New line character. The value of Chr(10).
	 */
	public static final String CRLF = "\n";
	/**
	 * Tab character.
	 */
	public static final String TAB = "\t";
	/**
	 * Quote character. The value of Chr(34).
	 */
	public static final String QUOTE = "\"";
	/**
	 * PI constant.
	 */
	public static final double cPI = Math.PI;
	/**
	 * e (natural logarithm base) constant.
	 */
	public static final double cE = Math.E;
	/**
	 * Key codes constants.
	 */
	public static KeyCodes KeyCodes = null;
	/**
	 * Returns the device scale, which is DPI / 160.
	 *(DPI stands for dots per inch).
	 */
	public static float Density = BA.density;
	/**
	 * Colors related methods and constants.
	 */
	public static final Colors Colors = null;
	/**
	 * Gravity constants.
	 */
	public static final Gravity Gravity = null;
	/**
	 * Files related methods.
	 */
	public static final File File = null;
	/**
	 * Application related properties.
	 */
	public static final B4AApplication Application = null;
	/**
	 * Bitwise related methods.
	 */
	public static final Bit Bit = null;
	/**
	 * Typeface related methods.
	 */
	public static final TypefaceWrapper Typeface = null;
	/**
	 * Date and time related methods.
	 */
	public static final DateTime DateTime = null;
	/**
	 * Dialogs related constants.
	 */
	public static final anywheresoftware.b4a.keywords.constants.DialogResponse DialogResponse = null;
	/**
	 * Regular expressions related methods.
	 */
	public static final Regex Regex = null;
	private static Random random;
	/**
	 * Converts the specified number to a string. 
	 *The string will include at least Minimum Integers and at most Maximum Fractions digits.
	 *Example:<code>
	 *Log(NumberFormat(12345.6789, 0, 2)) '"12,345.68"
	 *Log(NumberFormat(1, 3 ,0)) '"001"</code>
	 */
	public static String NumberFormat(double Number, int MinimumIntegers, int MaximumFractions) {
		if (BA.numberFormat == null)
			BA.numberFormat = java.text.NumberFormat.getInstance(Locale.US);
		BA.numberFormat.setMaximumFractionDigits(MaximumFractions);
		BA.numberFormat.setMinimumIntegerDigits(MinimumIntegers);
		return BA.numberFormat.format(Number);
	}
	/**
	 * Converts the specified number to a string. 
	 *The string will include at least Minimum Integers, at most Maximum Fractions digits and at least Minimum Fractions digits.
	 *GroupingUsed - Determines whether to group every three integers.
	 *Example:<code>
	 *Log(NumberFormat2(12345.67, 0, 3, 3, false)) '"12345.670"</code>
	 */
	public static String NumberFormat2(double Number, int MinimumIntegers, int MaximumFractions, int MinimumFractions,
			boolean GroupingUsed) {
		if (BA.numberFormat2 == null)
			BA.numberFormat2 = java.text.NumberFormat.getInstance(Locale.US);
		BA.numberFormat2.setMaximumFractionDigits(MaximumFractions);
		BA.numberFormat2.setMinimumIntegerDigits(MinimumIntegers);
		BA.numberFormat2.setMinimumFractionDigits(MinimumFractions);
		BA.numberFormat2.setGroupingUsed(GroupingUsed);
		return BA.numberFormat2.format(Number);
	}
	/**
	 * Logs a message. The log can be viewed in the Logs tab.
	 */
	public static void Log(String Message) {
		BA.Log(Message);
	}
	private static int LogStub;
	@Hide
	public static void LogImpl(String line, String Message, int Color) {
		LogStub = (LogStub + 1) % 10;

		String prefix = Color == 0 ? "l" + LogStub + line : "L" + LogStub + line + "~" + Color;
		BA.addLogPrefix(prefix, Message);
	}
	/**
	 * Logs a message. The message will be displayed in the IDE with the specified color.
	 */
	public static void LogColor(String Message, int Color) {
		BA.addLogPrefix("c" + Color, Message);
	}

	/**
	 *Returns the object that raised the event.
	 *Only valid while inside the event sub and before calls to Sleep or Wait For.
	 *Example:<code>
	 *Sub Button_Click
	 * Dim b As B4XView = Sender
	 * b.Text = "I've been clicked"
	 *End Sub</code>
	 */
	public static Object Sender(BA ba) {
		return ba.getSender();
	}
	/**
	 * Inverts the value of the given boolean.
	 */
	public static boolean Not(boolean Value) {
		return !Value;
	}
	/**
	 * Sets the random seed value. 
	 *This method can be used for debugging as it allows you to get the same results each time.
	 */
	public static void RndSeed(long Seed) {
		if (random == null)
			random = new Random(Seed);
		else
			random.setSeed(Seed);
	}
	/**
	 * Returns a random integer between Min (inclusive) and Max (exclusive).
	 */
	public static int Rnd(int Min, int Max)
	{
		if (random == null)
			random = new Random();
		return Min + random.nextInt(Max - Min);
	}
	/**
	 * Returns the absolute value.
	 */
	public static double Abs(double Number) {
		return Math.abs(Number);
	}
	@Hide
	public static int Abs(int Number) {
		return Math.abs(Number);
	}
	/**
	 * Returns the larger number between the two numbers.
	 */
	public static double Max(double Number1, double Number2) {
		return Math.max(Number1, Number2);
	}
	@Hide
	public static double Max(int Number1, int Number2) {
		return Math.max(Number1, Number2);
	}
	/**
	 * Returns the smaller number between the two numbers.
	 */
	public static double Min(double Number1, double Number2) {
		return Math.min(Number1, Number2);
	}
	@Hide
	public static double Min(int Number1, int Number2) {
		return Math.min(Number1, Number2);
	}
	/**
	 * Calculates the trigonometric sine function. Angle measured in radians.
	 */
	public static double Sin(double Radians) {
		return Math.sin(Radians);
	}
	/**
	 * Calculates the trigonometric sine function. Angle measured in degrees.
	 */
	public static double SinD(double Degrees) {
		return Math.sin(Degrees / 180 * Math.PI);
	}
	/**
	 * Calculates the trigonometric cosine function. Angle measured in radians.
	 */
	public static double Cos(double Radians) {
		return Math.cos(Radians);
	}
	/**
	 * Calculates the trigonometric cosine function. Angle measured in degrees.
	 */
	public static double CosD(double Degrees) {
		return Math.cos(Degrees / 180 * Math.PI);
	}
	/**
	 * Calculates the trigonometric tangent function. Angle measured in radians.
	 */
	public static double Tan(double Radians) {
		return Math.tan(Radians);
	}
	/**
	 * Calculates the trigonometric tangent function. Angle measured in degrees.
	 */
	public static double TanD(double Degrees) {
		return Math.tan(Degrees / 180 * Math.PI);
	}
	/**
	 * Returns the Base value raised to the Exponent power.
	 */
	public static double Power(double Base, double Exponent) {
		return Math.pow(Base, Exponent);
	}
	/**
	 * Returns the positive square root.
	 */
	public static double Sqrt(double Value) {
		return Math.sqrt(Value);
	}
	/**
	 * Returns the angle measured with radians.
	 */
	public static double ASin(double Value) {
		return Math.asin(Value);
	}
	/**
	 * Returns the angle measured with degrees.
	 */
	public static double ASinD(double Value) {
		return Math.asin(Value) / Math.PI * 180;
	}
	/**
	 * Returns the angle measured with radians.
	 */
	public static double ACos(double Value) {
		return Math.acos(Value);
	}
	/**
	 * Returns the angle measured with degrees.
	 */
	public static double ACosD(double Value) {
		return Math.acos(Value) / Math.PI * 180;
	}
	/**
	 * Returns the angle measured with radians.
	 */
	public static double ATan(double Value) {
		return Math.atan(Value);
	}
	/**
	 * Returns the angle measured with degrees.
	 */
	public static double ATanD(double Value) {
		return Math.atan(Value) / Math.PI * 180;
	}
	/**
	 * Returns the angle measured with radians.
	 */
	public static double ATan2(double Y, double X) {
		return Math.atan2(Y, X);
	}
	/**
	 * Returns the angle measured with degrees.
	 */
	public static double ATan2D(double Y, double X) {
		return Math.atan2(Y, X) / Math.PI * 180;
	}

	public static double Logarithm(double Number, double Base) {
		return Math.log(Number) / Math.log(Base);
	}
	/**
	 * Returns the closest long number to the given number. 
	 */
	public static long Round(double Number) {
		return Math.round(Number);
	}
	/**
	 * Rounds the given number and leaves up to the specified number of fractional digits.
	 */
	public static double Round2(double Number, int DecimalPlaces) {
		double shift = Math.pow(10, DecimalPlaces);
		return Math.round(Number * shift) / shift;
	}
	/**
	 * Returns the largest double that is smaller or equal to the specified number and is equal to an integer.
	 */
	public static double Floor(double Number) {
		return Math.floor(Number);
	}
	/**
	 * Returns the smallest double that is greater or equal to the specified number and is equal to an integer.
	 */
	public static double Ceil(double Number) {
		return Math.ceil(Number);
	}
	/**
	 * Returns the unicode code point of the given character or first character in string.
	 */
	public static int Asc(char Char) {
		return (int) Char;
	}
	/**
	 * Returns the character that is represented by the given unicode value.
	 */
	public static char Chr(int UnicodeValue) {
		return (char)UnicodeValue;
	}
	/**
	 * <b>DoEvents is deprecated.</b> It can lead to stability issues.
	 *Consider using Sleep(0) instead.
	 */
	@RaisesSynchronousEvents
	public static void DoEvents() {
		Msgbox.sendCloseMyLoopMessage();
		Msgbox.waitForMessage(false, true);

	}
	/**
	 * Shows a quick little message that goes out automatically.
	 *Message - The text message to show.
	 *LongDuration - If true then shows the message for a long period, otherwise shows the message for a short period.
	 */
	public static void ToastMessageShow(CharSequence Message, boolean LongDuration) {
		Toast.makeText(BA.applicationContext, Message, LongDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}
	/**
	 * Shows a modal message box with the specified message and title.
	 *The dialog will show one OK button.
	 *<b>It is recommended to use MsgboxAsync instead.</b>
	 *Note that services cannot show dialogs.
	 */
	@RaisesSynchronousEvents
	public static void Msgbox(CharSequence Message, CharSequence Title, BA ba) {
		Msgbox2 (Message, Title, "OK", "", "", null, ba);
	}
	/**
	 * Shows a modal message box with the specified message and title.
	 * <b>It is recommended to use Msgbox2Async instead.</b>
	 *Message - The dialog message.
	 **Title - The dialog title.
	 *Positive - The text to show for the "positive" button. Pass "" to hide the button.
	 *Cancel - The text to show for the "cancel" button. Pass "" to hide the button.
	 *Negative - The text to show for the "negative" button. Pass "" to hide the button.
	 *Icon - A bitmap that will be drawn near the title. Pass Null to hide hide the icon.
	 *Returns one of the DialogResponse values.
	 *Example:<code>
	 *Dim result As Int
	 *result = Msgbox2("This is the message", "This is the title", "Good", "", "Bad", LoadBitmap(File.DirAssets, "smiley.gif"))
	 *If result = DialogResponse.Positive Then ...
	 *</code>
	 */
	@RaisesSynchronousEvents
	public static int Msgbox2(CharSequence Message, CharSequence Title, String Positive, String Cancel, String Negative, Bitmap Icon, BA ba) {
		anywheresoftware.b4a.Msgbox.DialogResponse dr = new DialogResponse(false);
		Msgbox.msgbox(createMsgboxAlertDialog(Message, Title, Positive, Cancel, Negative, Icon, ba, dr), false);
		return dr.res;
	}
	/**
	 * Shows a non-modal message box with the specified message and title.
	 *The dialog will show one OK button.
	 *Note that services cannot show dialogs.
	 *You can use Wait For to wait for the Msgbox_Result event, if you want to continue the code flow after the dialog is dismissed.
	 *Example:<code>
	 *MsgboxAsync("Hello world", "This is the title")</code>
	 */
	public static void MsgboxAsync(CharSequence Message, CharSequence Title, BA mine) {
		Msgbox2Async (Message, Title, "OK", "", "", null, mine, true);
	}
	/**
	 * Shows a non-modal message box with the specified message and title. The Msgbox_Result event will be raised with the result.
	 *Message - The dialog message.
	 *Title - The dialog title.
	 *Positive - The text to show for the "positive" button. Pass "" to hide the button.
	 *Cancel - The text to show for the "cancel" button. Pass "" to hide the button.
	 *Negative - The text to show for the "negative" button. Pass "" to hide the button.
	 *Icon - A bitmap that will be drawn near the title. Pass Null to hide hide the icon.
	 *Cancelable - If true then the dialog can be canceled by clicking on the back key or outside the dialog.
	 *The object returned can be used as the Sender Filter parameter of Wait For.
	 *Example:<code>
	 *Msgbox2Async("Question?", "Title", "Yes", "Cancel", "No", Null, False)
	 *Wait For Msgbox_Result (Result As Int)
	 *If Result = DialogResponse.POSITIVE Then
	 *	'...
	 *End If</code>
	 */
	public static Object Msgbox2Async(CharSequence Message, CharSequence Title, String Positive, String Cancel, String Negative, BitmapWrapper Icon,
			final BA mine, boolean Cancelable) {
		AlertDialog ad = createMsgboxAlertDialog(Message, Title, Positive, Cancel, Negative, Icon == null ? null : Icon.getObjectOrNull(), 
				mine.sharedProcessBA.activityBA.get(), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mine.raiseEvent(dialog, "msgbox_result", which);
			}

		});
		ad.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mine.raiseEvent(dialog, "msgbox_result", anywheresoftware.b4a.keywords.constants.DialogResponse.CANCEL);
			}

		});
		return showAndTrackDialog(ad, Cancelable);
	}
	private static AlertDialog createMsgboxAlertDialog(CharSequence Message, CharSequence Title, String Positive, String Cancel, String Negative, Bitmap Icon, BA ba, OnClickListener listener) {
		AlertDialog.Builder b = new AlertDialog.Builder(ba.context);
		b.setTitle(Title).setMessage(Message);
		if (Positive.length() > 0)
			b.setPositiveButton(Positive, listener);
		if (Negative.length() > 0)
			b.setNegativeButton(Negative, listener);
		if (Cancel.length() > 0)
			b.setNeutralButton(Cancel, listener);
		if (Icon != null) {
			BitmapDrawable bd = new BitmapDrawable();
			bd.Initialize(Icon);
			b.setIcon(bd.getObject());
		}
		return b.create();

	}
	/**
	 * Shows a modal dialog with a list of items and radio buttons. Pressing on an item will close the dialog.
	 *Returns the index of the selected item or DialogResponse.Cancel if the user pressed on the back key.
	 *It is recommended to use InputListAsync instead.
	 *List - Items to display.
	 *Title - Dialog title.
	 *CheckedItem - The index of the item that will first be selected. Pass -1 if no item should be preselected.
	 */
	@RaisesSynchronousEvents
	public static int InputList(List Items, CharSequence Title, int CheckedItem, BA ba) {
		DialogResponse dr = new DialogResponse(true);
		Msgbox.msgbox(createInputList(Items, Title, CheckedItem, ba, dr), false);
		return dr.res;
	}
	/**
	 * Shows a non-modal dialog with a list of items and radio buttons. Pressing on an item will close the dialog.
	 *Returns the index of the selected item or DialogResponse.Cancel if the user pressed on the back key.
	 *The InputList_Result event will be raised with the result.
	 *List - Items to display.
	 *Title - Dialog title.
	 *CheckedItem - The index of the item that will first be selected. Pass -1 if no item should be preselected.
	 *Cancelable - If true then the dialog can be canceled by clicking on the back key or outside the dialog.
	 *The object returned can be used as the Sender Filter parameter of Wait For.
	 *Example: <code>
	 *Dim options As List = Array("Red", "Green", "Blue")
	 *InputListAsync(options, "Select Color", 0, False)
	 *Wait For InputList_Result (Index As Int)
	 *If Index <> DialogResponse.CANCEL Then
	 *	Log("Selected color: " & options.Get(Index))
	 *End If</code>
	 */
	public static Object InputListAsync(List Items, CharSequence Title, int CheckedItem, final BA mine, boolean Cancelable) {
		AlertDialog ad = createInputList(Items, Title, CheckedItem, mine.sharedProcessBA.activityBA.get(), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				mine.raiseEvent(dialog, "inputlist_result", which);
			}

		});
		ad.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mine.raiseEvent(dialog, "inputlist_result", anywheresoftware.b4a.keywords.constants.DialogResponse.CANCEL);
			}

		});
		return showAndTrackDialog(ad, Cancelable);
	}

	private static AlertDialog createInputList(List Items, CharSequence Title, int CheckedItem, BA ba, OnClickListener listener) {
		AlertDialog.Builder b = new AlertDialog.Builder(ba.context);
		CharSequence[] items = new CharSequence[Items.getSize()];
		for (int i = 0;i < Items.getSize();i++) {
			Object o = Items.Get(i);
			if (o instanceof CharSequence)
				items[i] = (CharSequence)o;
			else
				items[i] = String.valueOf(o);
		}
		b.setSingleChoiceItems(items, CheckedItem, listener);
		b.setTitle(Title);
		return b.create();
	}
	@Hide
	public static Dialog showAndTrackDialog(Dialog ad, boolean Cancelable) {
		ad.setCancelable(Cancelable);
		ad.setCanceledOnTouchOutside(Cancelable);
		ad.show();
		Msgbox.trackAsyncDialog(ad);
		return ad;
	}

	/**
	 * Shows a modal dialog with a list of items and checkboxes. The user can select multiple items.
	 *The dialog is closed by pressing on the "Ok" button. The InputMap_Result event will be raised.
	 *It is recommended to use InputMapAsync instead.
	 *The items displayed are the map keys. Items with a value of True will be checked.
	 *When the user checks or unchecks an item, the related item value gets updated.
	 *Items - A map object with the items as keys and their checked state as values.

	 */
	@RaisesSynchronousEvents
	public static void InputMap(final Map Items, CharSequence Title, BA ba) {

		DialogResponse dr = new DialogResponse(false);
		Msgbox.msgbox(createInputMap(Items, Title, ba, dr), false);
	}
	/**
	 * Shows a non-modal dialog with a list of items and checkboxes. The user can select multiple items.
	 *The dialog is closed by pressing on the "Ok" button.
	 *The object returned can be used as the Sender Filter parameter of Wait For.
	 *The items displayed are the map keys. Items with a value of True will be checked.
	 *When the user checks or unchecks an item, the related item value gets updated.
	 *Items - A map object with the items as keys and their checked state as values.
	 *Cancelable - If true then the dialog can be closed by clicking on the back key or outside the dialog (the changes will not be canceled).
	 *Example: <code>
	 *Dim m As Map = CreateMap("Item #1": True, "Item #2": True, "Item #3": False)
	 *InputMapAsync(m, "Select items", True)
	 *Wait For InputMap_Result
	 *Log(m)</code>
	 */
	public static Object InputMapAsync(final Map Items, CharSequence Title, final BA mine, boolean Cancelable) {
		AlertDialog ad = createInputMap(Items, Title, mine.sharedProcessBA.activityBA.get(), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mine.raiseEvent(dialog, "inputmap_result");
			}

		});
		ad.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mine.raiseEvent(dialog, "inputmap_result");
			}

		});
		return showAndTrackDialog(ad, Cancelable);
	}
	private static AlertDialog createInputMap(final Map Items, CharSequence Title, BA ba, OnClickListener listener) {
		AlertDialog.Builder b = new AlertDialog.Builder(ba.context);
		final CharSequence[] items = new CharSequence[Items.getSize()];
		boolean[] checked = new boolean[Items.getSize()];
		int i = 0;
		for (Entry<Object, Object> e : ((Map.MyMap)Items.getObject()).entrySet()) {
			if (e.getKey() instanceof String == false)
				throw new RuntimeException("Keys must be strings.");
			items[i] = (String)e.getKey();
			Object o = e.getValue();
			if (o instanceof Boolean)
				checked[i] = (Boolean)o;
			else
				checked[i] = Boolean.parseBoolean(String.valueOf(o));
			i++;
		}
		b.setMultiChoiceItems(items, checked, new OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				if (isChecked) {
					Items.Put(items[which], true);
				}
				else {
					Items.Put(items[which], false);
				}
			}
		});
		b.setTitle(Title);
		b.setPositiveButton("Ok", listener);
		return b.create();
	}
	/**
	 * Shows a modal dialog with a list of items and checkboxes. The user can select multiple items.
	 *The dialog is closed by pressing on the "Ok" button.
	 *Returns a list with the indices of the selected items. The list is sorted.
	 *Returns an empty list if the user has pressed on the back key.
	 */
	@RaisesSynchronousEvents
	public static List InputMultiList(List Items, CharSequence Title, BA ba) {
		AlertDialog.Builder b = new AlertDialog.Builder(ba.context);
		CharSequence[] items = new CharSequence[Items.getSize()];
		for (int i = 0;i < Items.getSize();i++) {
			Object o = Items.Get(i);
			if (o instanceof CharSequence)
				items[i] = (CharSequence)o;
			else
				items[i] = String.valueOf(o);
		}
		DialogResponse dr = new DialogResponse(false);
		final List result = new List();
		result.Initialize();
		b.setMultiChoiceItems(items, null, new OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				if (isChecked) {
					result.Add(which);
				}
				else {
					int i = result.IndexOf(which);
					result.RemoveAt(i);
				}
			}

		});
		b.setTitle(Title);
		b.setPositiveButton("Ok", dr);
		Msgbox.msgbox(b.create(), false);
		if (dr.res != Common.DialogResponse.POSITIVE) {
			result.Clear();
		}
		else
			result.Sort(true);
		return result;
	}
	/**
	 * Shows a dialog with a circular spinning bar and the specified text.
	 *Unlike Msgbox and InputList methods, the code will not block.
	 *You should call ProgressDialogHide to remove the dialog.
	 *The dialog will also be removed if the user presses on the Back key.
	 */
	public static void ProgressDialogShow(BA ba, CharSequence Text) {
		ProgressDialogShow2(ba, Text, true);
	}
	/**
	 * Shows a dialog with a circular spinning bar and the specified text.
	 *Unlike Msgbox and InputList methods, the code will not block.
	 *You should call ProgressDialogHide to remove the dialog.
	 *Cancelable - Whether the user can dismiss the dialog by pressing on the Back key.
	 */
	public static void ProgressDialogShow2(BA ba, CharSequence Text, boolean Cancelable) {
		ProgressDialogHide();
		Msgbox.pd = new WeakReference<ProgressDialog>(
				ProgressDialog.show(ba.context, "", Text, true, Cancelable));
	}
	/**
	 * Hides a visible progress dialog. Does not do anything if no progress dialog is visible.
	 */
	public static void ProgressDialogHide() {
		Msgbox.dismissProgressDialog();
	}
	/**
	 * Returns a string representing the object's java type.
	 */
	public static String GetType(Object object) {
		return object.getClass().getName();
	}
	/**
	 * Returns true if ToolName equals B4A.
	 */
	public static boolean IsDevTool(String ToolName) {
		return ToolName.toLowerCase(BA.cul).equals("b4a");
	}

	/**
	 * Scales the value, which represents a specific length on a default density device (Density = 1.0),
	 *to the current device.
	 *For example, the following code will set the width value of this button to be the same physical size
	 *on all devices.
	 *Button1.Width = DipToCurrent(100)
	 *
	 *Note that a shorthand syntax for this method is available. Any number followed by the string 'dip'
	 *will be converted in the same manner (no spaces are allowed between the number and 'dip').
	 *So the previous code is equivalent to:
	 *Button1.Width = 100dip 'dip -> density independent pixel
	 */
	public static int DipToCurrent(int Length)
	{
		return (int)(Density * Length);
	}
	/**
	 *Returns the actual size of the given percentage of the activity width.
	 *Example:
	 *Button1.Width = PerXToCurrent(50) 'Button1.Width = 50% * Activity.Width
	 *
	 *A shorthand syntax for this method is available. Any number followed by the string '%x'
	 *will be converted in the same manner (no spaces are allowed between the number and '%x').
	 *So the previous code is equivalent to:
	 *Button1.Width = 50%x
	 */
	public static int PerXToCurrent(float Percentage, BA ba)
	{
		return (int) (Percentage / 100f * ba.vg.getWidth());
	}
	/**
	 *Returns the actual size of the given percentage of the activity height.
	 *Example:
	 *Button1.Height = PerYToCurrent(50) 'Button1.Height = 50% * Activity.Height
	 *
	 *A shorthand syntax for this method is available. Any number followed by the string '%y'
	 *will be converted in the same manner (no spaces are allowed between the number and '%y').
	 *So the previous code is equivalent to:
	 *Button1.Height = 50%y
	 */
	public static int PerYToCurrent(float Percentage, BA ba)
	{
		return (int) (Percentage / 100f * ba.vg.getHeight());
	}
	/**
	 * Tests whether the specified string can be safely parsed as a number.
	 */
	public static boolean IsNumber(String Text){
		try {
			Double.parseDouble(Text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	/**
	 * Returns the last exception that was caught (if such exists).
	 */
	public static B4AException LastException(BA ba) {
		B4AException e = new B4AException();
		e.setObject(ba.getLastException());
		return e;
	}
	/**
	 * Returns the device LayoutValues.
	 *
	 *Example:<code>
	 *Log(GetDeviceLayoutValues)</code>
	 */
	public static LayoutValues GetDeviceLayoutValues(BA ba) {
		DisplayMetrics dm = BA.applicationContext.getResources().getDisplayMetrics();
		LayoutValues deviceValues;
		deviceValues = new LayoutValues();
		deviceValues.Scale = dm.density;
		deviceValues.Width = dm.widthPixels;
		deviceValues.Height = dm.heightPixels;	
		return deviceValues;
	}

	/**
	 * Starts an activity or brings it to front if it already exists.
	 *The target activity will be started once the program is free to process its message queue.
	 *Activity can be a string with the target activity name or it can be the actual activity.
	 *After this call the current activity will be paused and the target activity will be resumed.
	 *This method can also be used to send Intents objects to the system.
	 *Note that you should usually not call StartActivity from a Service.
	 *Example: StartActivity (Activity2)
	 */
	public static void StartActivity(BA mine, Object Activity) throws ClassNotFoundException {
		//this will be a processBA
		Intent i = getComponentIntent(mine, Activity);
		BA activityBA = null;
		if (mine.sharedProcessBA.activityBA != null)
			activityBA = mine.sharedProcessBA.activityBA.get();
		if (activityBA != null) {
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			activityBA.context.startActivity(i);
		}
		else {
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mine.context.startActivity(i);
		}


	}
	/**
	 * Starts the given service. The service will be first created if it was not started before.
	 *The target service will be started once the program is free to process its message queue.
	 *Note that you cannot show a Msgbox after this call and before the service starts.
	 *Service - The service module or the service name.
	 *Example:<code>
	 *StartService(SQLService)</code>
	 */
	public static void StartService(final BA mine, final Object Service) throws ClassNotFoundException {
		if (BA.shellMode) {
			BA.handler.post(new BA.B4ARunnable() {
				public void run() {
					try {
						StartServiceImpl(mine, Service);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
		else {
			BA.handler.post(new Runnable() {
				@Override
				public void run() {
					Msgbox.isDismissing = false;
				}
			});
			StartServiceImpl(mine, Service);
			Msgbox.isDismissing = true; //no msgbox and debugging are allowed till the service starts
		}
	}
	private static void StartServiceImpl(BA mine, Object Service) throws ClassNotFoundException {
		Intent in = getComponentIntent(mine, Service);
		try {
			mine.context.startService(in);
		} catch (IllegalStateException i) {
			if (Build.VERSION.SDK_INT >= 26) {
				BA.LogInfo("Service started in the background. Trying to start again in foreground mode.");
				in.putExtra(ServiceHelper.FOREGROUND_KEY, true);
				mine.context.startForegroundService(in);
			} else {
				throw new RuntimeException(i);
			}
		}
	}
	/**
	 * Schedules the given service to start at the given time.
	 *Service - The service module. Pass Me when calling from a service module that schedules itself.
	 *Time - The time to start the service. If this time has already past the service will be started now.
	 *The actual delivery time might change to reduce battery usage. Use StartServiceAtExact if the exact time is important.
	 *DuringSleep - Whether to start the service when the device is sleeping. If set to false and the device is sleeping
	 *at the specified time, the service will be started when the device wakes up.
	 *Setting DuringSleep to True can have a large impact on the battery usage.
	 *StartServiceAt can be used to schedule a repeating task. You should call it under Service_Start to schedule the next task.
	 *This call cancels previous scheduled tasks (for the same service).
	 *Example:<code>
	 *StartServiceAt(SQLService, DateTime.Now + 30 * 1000, false) 'will start after 30 seconds.</code>
	 */
	public static void StartServiceAt(BA mine, Object Service, long Time, boolean DuringSleep) throws ClassNotFoundException {
		AlarmManager am = (AlarmManager) BA.applicationContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = createPendingIntentForAlarmManager(mine, Service);
		if (Build.VERSION.SDK_INT >= 23 && DuringSleep)
			am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Time, pi);
		else
			am.set(DuringSleep ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC, Time, pi);
	}
	/**
	 * Same as StartServiceAt for Android versions up to 4.3 (API 18).
	 *On Android 4.4+ it forces the OS to start the service at the exact time. This method will have a larger impact on the battery compared
	 *to StartServiceAt and should only be used in cases where it is important for the service to start at the exact time.
	 */
	public static void StartServiceAtExact(BA mine, Object Service, long Time, boolean DuringSleep) throws Exception {
		AlarmManager am = (AlarmManager) BA.applicationContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = createPendingIntentForAlarmManager(mine, Service);
		if (Build.VERSION.SDK_INT >= 23 && DuringSleep)
			am.setExactAndAllowWhileIdle (AlarmManager.RTC_WAKEUP, Time, pi);
		else if (Build.VERSION.SDK_INT >= 19)
			am.setExact(DuringSleep ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC, Time, pi);
		else
			am.set(DuringSleep ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC, Time, pi);
	}
	private static PendingIntent createPendingIntentForAlarmManager(BA mine, Object Service) throws ClassNotFoundException {
		Intent in = new Intent(BA.applicationContext, getComponentClass(mine, Service, true));
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= 31)
			flags |= 33554432; //FLAG_MUTABLE
		return PendingIntent.getBroadcast(mine.context, 1, in,
				flags);
	}
	/**
	 * Cancels previously scheduled tasks for this service.
	 */
	public static void CancelScheduledService(BA mine, Object Service) throws ClassNotFoundException {
		AlarmManager am = (AlarmManager) BA.applicationContext.getSystemService(Context.ALARM_SERVICE);
		am.cancel(createPendingIntentForAlarmManager(mine, Service));
	}
	@Hide
	public static Class<?> getComponentClass(BA mine, Object component, boolean receiver) throws ClassNotFoundException {
		Class<?> resClass = null;
		if (component instanceof Class<?>) { //default case
			resClass = (Class<?>) component;
		}
		else if (component == null || component.toString().length() == 0) {
			resClass = Class.forName(mine.className);
		}
		else if (component instanceof String) {
			resClass = Class.forName(BA.packageName + "." + 
					((String)component).toLowerCase(BA.cul));
		}
		if (resClass == null)
			return null;
		if (receiver) {
			String serviceName = resClass.getName().substring(resClass.getName().lastIndexOf(".") + 1);
			resClass = Class.forName(resClass.getName() + "$" + serviceName + "_BR");
		}
		return resClass;
	}
	@Hide
	public static Intent getComponentIntent(BA mine, Object component) throws ClassNotFoundException {
		Intent i;
		Class<?> cls = getComponentClass(mine, component, false);
		if (cls != null)
			i = new Intent(mine.context, cls);
		else
			i = (Intent)component;

		return i;
	}
	/**
	 * Stops the given service. Service_Destroy will be called. Call StartService afterwards will first create the service.
	 *Service - The service module or service name. Pass Me to stop the current service (from the service module).
	 *Example:<code>
	 *StopService(SQLService)</code>
	 */
	public static void StopService(BA mine, Object Service) throws ClassNotFoundException {
		mine.context.stopService(getComponentIntent(mine, Service));
	}
	/**
	 * Tests whether the object includes the specified method.
	 *Returns false if the object was not initialized or not an instance of a user class.
	 */
	public static boolean SubExists(BA mine, Object Object, String Sub) throws IllegalArgumentException, SecurityException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		if (Object == null)
			return false;
		BA ba = getComponentBA(mine, Object);
		if (ba == null)
			return false;
		return ba.subExists(Sub.toLowerCase(BA.cul));
	}

	//must match code in Mesharsher.writeMethod
	/**
	 * Calls the given sub. CallSub can be used to call a sub which belongs to a different module.
	 *However the sub will only be called if the other module is not paused. In that case an empty string will be returned.
	 *You can use IsPaused to test whether a module is paused.
	 *This means that one activity cannot call a sub of a different activity. As the other activity will be paused for sure.
	 *CallSub allows an activity to call a service sub or a service to call an activity sub.
	 *CallSub can also be used to call subs in the current module. Pass Me as the component in that case.
	 *Note that it is not possible to call subs of code modules.
	 *To avoid issues with obfuscation, it is recommended to include an underscore in the target sub name.
	 *Example:<code>
	 *CallSub(Main, "RefreshData")</code>
	 */
	@DesignerName("CallSub")
	@RaisesSynchronousEvents
	public static Object CallSubNew(BA mine, Object Component, String Sub) throws Exception{
		return CallSub4(false, mine, Component, Sub, null);
	}
	/**
	 * Similar to CallSub. Calls a sub with a single argument.
	 */
	@DesignerName("CallSub2")
	@RaisesSynchronousEvents
	public static Object CallSubNew2(BA mine, Object Component, String Sub, Object Argument) throws Exception {
		return CallSub4(false, mine, Component, Sub, new Object[] {Argument});
	}
	/**
	 * Similar to CallSub. Calls a sub with two arguments.
	 */
	@DesignerName("CallSub3")
	@RaisesSynchronousEvents
	public static Object CallSubNew3(BA mine, Object Component, String Sub, Object Argument1, Object Argument2) throws Exception {
		return CallSub4(false,mine, Component, Sub, new Object[] {Argument1, Argument2});
	}
	@Hide
	public static Object CallSubDebug(BA mine, Object Component, String Sub) throws Exception {
		return Class.forName("anywheresoftware.b4a.debug.Debug").getDeclaredMethod("CallSubNew", BA.class, Object.class, String.class)
				.invoke(null, mine, Component, Sub);
	}
	@Hide
	public static Object CallSubDebug2(BA mine, Object Component, String Sub, Object Argument) throws Exception {
		return Class.forName("anywheresoftware.b4a.debug.Debug").getDeclaredMethod("CallSubNew2", BA.class, Object.class, String.class, Object.class)
				.invoke(null, mine, Component, Sub, Argument);
	}
	@Hide
	public static Object CallSubDebug3(BA mine, Object Component, String Sub, Object Argument1, Object Argument2) throws Exception {
		return Class.forName("anywheresoftware.b4a.debug.Debug").getDeclaredMethod("CallSubNew3", BA.class, Object.class, String.class, Object.class, Object.class)
				.invoke(null, mine, Component, Sub, Argument1, Argument2);
	}

	private static Object CallSub4(boolean old, BA mine, Object Component, String Sub, Object[] Arguments) throws Exception {
		Object o = null;
		if (Component instanceof SubDelegator) {
			o = ((SubDelegator)Component).callSub(Sub, mine.eventsTarget, Arguments);
			if (o != SubDelegator.SubNotFound) {
				//we got result
				if (o != null && o instanceof ObjectWrapper) {
					return ((ObjectWrapper<?>)o).getObject();
				}
				return o;
			}
			else {
				o = null;
			}
		}
		BA ba = getComponentBA(mine, Component);
		if (ba != null) {
			boolean isTargetClass = Component instanceof B4AClass;
			//for classes we allow even when the context is paused (it is less critical).
			o = ba.raiseEvent2(mine.eventsTarget, isTargetClass /*allow during pause*/ ,
					Sub.toLowerCase(BA.cul), isTargetClass /*throw error*/, Arguments);
		}
		if (old) {
			if (o == null)
				o = "";
			return String.valueOf(o);
		}
		if (o != null && o instanceof ObjectWrapper) {
			return ((ObjectWrapper<?>)o).getObject();
		}
		return o;
	}
	/**
	 *CallSubDelayed is a combination of StartActivity, StartService and CallSub.
	 *Unlike CallSub which only works with currently running components, CallSubDelayed will first start the target component if needed.
	 *CallSubDelayed can also be used to call subs in the current module. Instead of calling these subs directly, a message will be sent to the message queue.
	 *The sub will be called when the message is processed. This is useful in cases where you want to do something "right after" the current sub (usually related to UI events).
	 *Note that if you call an Activity while the whole application is in the background (no visible activities), the sub will be executed once the target activity is resumed.
	 */
	public static void CallSubDelayed(BA mine, Object Component, String Sub) {
		CallSubDelayed4(mine, Component, Sub, null);
	}
	/**
	 * Similar to CallSubDelayed. Calls a sub with a single argument.
	 */
	public static void CallSubDelayed2(BA mine, Object Component, String Sub, Object Argument) {
		CallSubDelayed4(mine, Component, Sub, new Object[] {Argument});
	}
	/**
	 * Similar to CallSubDelayed. Calls a sub with two arguments.
	 */
	public static void CallSubDelayed3(BA mine, Object Component, String Sub, Object Argument1, Object Argument2) {
		CallSubDelayed4(mine, Component, Sub, new Object[] {Argument1, Argument2});
	}
	private static void CallSubDelayed4(final BA mine, final Object Component, final String Sub,
			final Object[] Arguments) {

		final Runnable runnable = new Runnable() {
			int retries = 5;

			@Override
			public void run() {
				try {
					final BA ba = getComponentBA(mine, Component);
					final Object sender = mine.eventsTarget;
					if (ba == null || ba.isActivityPaused()) {

						if (Component instanceof B4AClass) {
							Log("Object context is paused. Ignoring CallSubDelayed: " + Sub);
							return;
						}
						Intent i = getComponentIntent(mine, Component);
						ComponentName cn = i.getComponent();
						if (cn == null) {
							Log("ComponentName = null");
							return;
						}
						Class<?> cls = Class.forName(cn.getClassName());
						Field f = cls.getDeclaredField("mostCurrent");
						f.setAccessible(true);
						if (f.get(null) == null && retries == 5) { //mostCurrent = null => need to start
							if (Activity.class.isAssignableFrom(cls)) {
								//check whether any activity is visible.
								if (BA.isAnyActivityVisible())
									StartActivity(mine, Component);
								else
									retries = 0; //send message to message queue immediately
							} else if (Service.class.isAssignableFrom(cls)) {
								StartService(mine, Component);
							}
						}
						if (--retries > 0)
							BA.handler.postDelayed(this, 100);
						else {
							//this can happen when the user turns off the phone
							//the activity will not be able to resume.
							if (ba != null) {
								Runnable msg = new Runnable() {
									@Override
									public void run() {
										ba.raiseEvent2(sender, true, Sub.toLowerCase(BA.cul), true, Arguments);
									}
								};
								ba.addMessageToPausedMessageQueue("CallSubDelayed - " + Sub, msg);
							}
							else {
								BA.addMessageToUninitializeActivity(cn.getClassName(), Sub.toLowerCase(BA.cul), sender, Arguments);
							}

						}
					}
					else {
						if (BA.shellMode) 
							ba.raiseEventFromDifferentThread(sender, null, 0, Sub.toLowerCase(BA.cul), false, Arguments);
						else
							ba.raiseEvent2(sender, true, Sub.toLowerCase(BA.cul), false, Arguments);
					}						
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		};
		if (!BA.shellMode)
			BA.handler.post(runnable);
		else {
			BA.handler.post(new BA.B4ARunnable() {
				@Override
				public void run() {
					runnable.run();
				}
			});
		}

	}
	/**
	 * Tests whether the given component is paused. Will also return true for components that were not started yet.
	 *Example:<code>
	 *If IsPaused(Main) = False Then CallSub(Main, "RefreshData")</code>
	 */
	public static boolean IsPaused(BA mine, Object Component) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		BA ba = getComponentBA(mine, Component);
		return ba == null || ba.isActivityPaused();
	}
	@Hide
	public static BA getComponentBA(BA mine, Object Component) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		Class<?> c;
		if (Component instanceof Class<?>)
			c = (Class<?>) Component;
		else if (Component instanceof B4AClass) {
			return ((B4AClass)Component).getBA();
		}
		else if (Component == null || Component.toString().length() == 0)
			return mine;
		else
			c = Class.forName(BA.packageName + "." + ((String)Component).toLowerCase(BA.cul));
		return (BA) c.getField("processBA").get(null);
	}
	/**
	 * Creates a new String by copying the characters from the array.
	 *Copying starts from StartOffset and the number of characters copied equals to Length.
	 */
	public static String CharsToString(char[] Chars, int StartOffset, int Length) {
		return new String(Chars, StartOffset, Length);
	}
	/**
	 * Decodes the given bytes array as a string.
	 *Data - The bytes array.
	 *StartOffset - The first byte to read.
	 *Length - Number of bytes to read.
	 *CharSet - The name of the character set.
	 *Example:<code>
	 *Dim s As String
	 *s = BytesToString(Buffer, 0, Buffer.Length, "UTF-8")</code>
	 */
	public static String BytesToString(byte[] Data, int StartOffset, int Length, String CharSet) throws UnsupportedEncodingException {
		return new String(Data, StartOffset, Length, CharSet);
	}
	@Hide
	public static Map createMap(Object[] data) {
		Map m = new Map();
		m.Initialize();
		for (int i = 0;i < data.length;i+=2) {
			m.Put(data[i], data[i + 1]);
		}
		return m;
	}
	@Hide
	public static List ArrayToList(Object[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		list.setObject(Arrays.asList(Array));
		return list;
	}
	@Hide
	public static List ArrayToList(int[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Integer.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(long[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Long.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(float[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Float.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(double[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Double.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(boolean[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Boolean.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(short[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Short.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(byte[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Byte.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	/**
	 * Tests whether a background task, submitted by the container object and with the specified id, is running.
	 */
	public static boolean IsBackgroundTaskRunning(BA ba, Object ContainerObject, int TaskId) {
		return ba.isTaskRunning(ContainerObject, TaskId);
	}
	/**
	 *Loads the bitmap.
	 *Note that the Android file system is case sensitive.
	 *You should consider using LoadBitmapSample if the image size is large.
	 *The actual file size is not relevant as images are usually stored compressed.
	 *Example:<code>
	 *Activity.SetBackgroundImage(LoadBitmap(File.DirAssets, "SomeFile.jpg"))</code>
	 */
	public static BitmapWrapper LoadBitmap(String Dir, String FileName) throws IOException {
		BitmapWrapper bw = new BitmapWrapper();
		bw.Initialize(Dir, FileName);
		return bw;
	}
	/**
	 *Loads the bitmap.
	 *The decoder will subsample the bitmap if MaxWidth or MaxHeight are smaller than the bitmap dimensions.
	 *This can save a lot of memory when loading large images.
	 *<b>In most cases it is better to use LoadBitmapResize.</b>
	 *Example:<code>
	 *Activity.SetBackgroundImage(LoadBitmapSample(File.DirAssets, "SomeFile.jpg", Activity.Width, Activity.Height))</code>
	 */
	public static BitmapWrapper LoadBitmapSample(String Dir, String FileName, @Pixel int MaxWidth, @Pixel int MaxHeight) throws IOException {
		BitmapWrapper bw = new BitmapWrapper();
		bw.InitializeSample(Dir, FileName, MaxWidth, MaxHeight);
		return bw;
	}
	/**
	 * Loads the bitmap and sets its size.
	 *The bitmap scale will be the same as the device scale.
	 *Unlike LoadBitmapSample which requires the container Gravity to be set to FILL, LoadBitmapResize provides better results when the Gravity is set to CENTER. 
	 *Example:<code>
	 *Dim bd As BitmapDrawable = Activity.SetBackgroundImage(LoadBitmapResize(File.DirAssets, "SomeFile.jpg", 100%x, 100%y, True))
	 *bd.Gravity = Gravity.CENTER</code>
	 *Or:<code>
	 *Activity.SetBackgroundImage(LoadBitmapResize(File.DirAssets, "SomeFile.jpg", 100%x, 100%y, True)).Gravity = Gravity.CENTER</code>
	 */
	public static BitmapWrapper LoadBitmapResize(String Dir, String FileName, @Pixel int Width, @Pixel int Height, boolean KeepAspectRatio) throws IOException {
		BitmapWrapper bw = new BitmapWrapper();
		bw.InitializeResize(Dir, FileName, Width, Height, KeepAspectRatio);
		return bw;
	}
	/**
	 * Internal keyword used by the Smart String literal.
	 */
	public static String SmartStringFormatter(String Format, Object Value) {
		//format lower cased
		if (Format.length() == 0)
			return BA.ObjectToString(Value);
		if (Format.equals("date"))
			return DateTime.Date(BA.ObjectToLongNumber(Value));
		else if (Format.equals("datetime")) {
			long l = BA.ObjectToLongNumber(Value);
			return DateTime.Date(l) + " " + DateTime.Time(l);
		}
		else if (Format.equals("time"))
			return DateTime.Time(BA.ObjectToLongNumber(Value));
		else if (Format.equals("xml")) {
			StringBuilder sb = new StringBuilder();
			String s = String.valueOf(Value);
			for (int i = 0;i < s.length();i++) {
				char c = s.charAt(i);
				switch (c) {
				case '\"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&#39;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				default:
					sb.append(c);
					break;
				}
			}
			return sb.toString();
		}
		else {
			int i = Format.indexOf(".");
			int minInts, maxFracs;
			if (i > -1) {
				minInts = Integer.parseInt(Format.substring(0, i));
				maxFracs = Integer.parseInt(Format.substring(i + 1));
			}
			else {
				minInts = Integer.parseInt(Format);
				maxFracs = Integer.MAX_VALUE;
			}

			try {
				return NumberFormat(BA.ObjectToNumber(Value), minInts, maxFracs);
			} catch (Exception e) {
				return "NaN";
			}

		}
	}

	/**
	 * Creates a single dimension array of the specified type.
	 *The syntax is: Array [As type] (list of values).
	 *If the type is ommitted then an array of objects will be created.
	 *Example:<code>
	 *Dim Days() As String
	 *Days = Array As String("Sunday", "Monday", ...)</code>
	 */
	public static void Array() {

	}
	/**
	 * Creates a Map with the given key / value pairs.
	 *The syntax is: CreateMap (key1: value1, key2: value2, ...)
	 *Example: <code>
	 *Dim m As Map = CreateMap("January": 1, "February": 2)</code>
	 */
	public static void CreateMap() {

	}
	/**
	 * Single line:
	 *If condition Then true-statement [Else false-statement]
	 *Multiline:
	 *If condition Then
	 * statement
	 *Else If condition Then
	 * statement
	 *...
	 *Else
	 * statement
	 *End If
	 */
	public static void If() {

	}
	/**
	 * Any exception thrown inside a try block will be caught in the catch block.
	 *Call LastException to get the caught exception.
	 *Syntax:
	 *Try
	 * ...
	 *Catch
	 * ...
	 *End Try
	 */
	public static void Try() {

	}
	/**
	 * Any exception thrown inside a try block will be caught in the catch block.
	 *Call LastException to get the caught exception.
	 *Syntax:
	 *Try
	 * ...
	 *Catch
	 * ...
	 *End Try
	 */
	public static void Catch() {

	}
	/**
	 * Declares a variable.
	 *Syntax:
	 *Declare a single variable:
	 *Dim variable name [As type] [= expression]
	 *The default type is String.
	 *
	 *Declare multiple variables. All variables will be of the specified type.
	 *Dim [Const] variable1 [= expression], variable2 [= expression], ..., [As type]
	 *Note that the shorthand syntax only applies to Dim keyword.
	 *Example:<code>Dim a = 1, b = 2, c = 3 As Int</code>
	 *
	 *Declare an array:
	 *Dim variable(Rank1, Rank2, ...) [As type]
	 *Example:<code>Dim Days(7) As String</code>
	 *The actual rank can be omitted for zero length arrays.
	 */
	public static void Dim() {

	}
	/**
	 * Loops while the condition is true.
	 * Syntax:
	 * Do While condition
	 *  ...
	 * Loop
	 */
	public static void While() {

	}
	/**
	 * Loops until the condition is true.
	 * Syntax:
	 * Do Until condition
	 *  ...
	 * Loop
	 */
	public static void Until() {

	}
	/**
	 * Syntax:
	 *For variable = value1 To value2 [Step interval]
	 * ...
	 *Next
	 *If the iterator variable was not declared before it will be of type Int.
	 *
	 *Or:
	 *For Each variable As type In collection
	 * ...
	 *Next
	 *Examples:<code>
	 *For i = 1 To 10
	 * Log(i) 'Will print 1 to 10 (inclusive).
	 *Next
	 *For Each n As Int In Numbers 'an array
	 * Sum = Sum + n
	 *Next
	 *</code>
	 *Note that the loop limits will only be calculated once before the first iteration.
	 */
	public static void For() {

	}
	/**
	 * Declares a structure.
	 *Can only be used inside sub Globals or sub Process_Globals.
	 *Syntax:
	 *Type type-name (field1, field2, ...)
	 *Fields include name and type.
	 *Example:<code>
	 *Type MyType (Name As String, Items(10) As Int)
	 *Dim a, b As MyType
	 *a.Initialize
	 *a.Items(2) = 123</code>
	 */
	public static void Type() {

	}
	/**
	 * Returns from the current sub and optionally returns the given value.
	 *Syntax: Return [value]
	 */
	public static void Return() {

	}
	/**
	 * Declares a sub with the parameters and return type.
	 *Syntax: Sub name [(list of parameters)] [As return-type]
	 *Parameters include name and type.
	 *The lengths of arrays dimensions should not be included.
	 *Example:<code>
	 *Sub MySub (FirstName As String, LastName As String, Age As Int, OtherValues() As Double) As Boolean
	 * ...
	 *End Sub</code>
	 *In this example OtherValues is a single dimension array.
	 *The return type declaration is different than other declarations as the array parenthesis follow the type and not
	 *the name (which does not exist in this case).
	 */
	public static void Sub() {

	}
	/**
	 * Exits the most inner loop.
	 *Note that Exit inside a Select block will exit the Select block.
	 */
	public static void Exit() {

	}
	/**
	 * Stops executing the current iteration and continues with the next one.
	 */
	public static void Continue() {

	}
	/**
	 * Compares a single value to multiple values.
	 *Example:<code>
	 *Dim value As Int
	 *value = 7
	 *Select value
	 *	Case 1
	 *		Log("One")
	 *	Case 2, 4, 6, 8
	 *		Log("Even")
	 *	Case 3, 5, 7, 9
	 *		Log("Odd larger than one")
	 *	Case Else
	 *		Log("Larger than 9")
	 *End Select</code>
	 */
	public static void Select() {

	}
	/**
	 * Tests whether the object is of the given type.
	 *Example:<code>
	 *For i = 0 To Activity.NumberOfViews - 1
	 *  If Activity.GetView(i) Is Button Then
	 *   Dim b As Button
	 *   b = Activity.GetView(i)
	 *   b.Color = Colors.Blue
	 *  End If
	 *Next</code>
	 */
	public static void Is() {

	}
	/**
	 * Immediately ends the application and stops the process.
	 *Most applications should not use this method and prefer Activity.Finish which lets the OS decide when the process is killed.
	 */
	public static void ExitApplication() {
		System.exit(0);
	}
	/**
	 * Creates a RemoteViews object based on the layout file. The compiler will generate the required XML files based on the parameters.
	 *See the widgets tutorial for more information: <link>Widgets tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/10166-android-home-screen-widgets-tutorial-part-i.html</link>.
	 *Note that all parameters must be strings or numbers as they are read by the compiler.
	 *LayoutFile - The widget layout file.
	 *EventName - Sets the subs that will handle the RemoteViews events.
	 *UpdateIntervalMinutes - Sets the update interval. Pass 0 to disable automatic updates. Minimum value is 30 (or 0).
	 *WidgetName - The name of the widget as appears in the widgets list.
	 *CenterWidget - Whether to center the widget.
	 */
	public static RemoteViews ConfigureHomeWidget (String LayoutFile, String EventName, int UpdateIntervalMinutes, String WidgetName, 
			boolean CenterWidget) {
		return null;
	}
	/**
	 * For classes: returns a reference to the current instance.
	 *For activities and services: returns a reference to an object that can be used with CallSub, CallSubDelayed and SubExists keywords.
	 *Cannot be used in code modules.
	 */
	public static Object Me(BA ba) {
		return null;
	}
	/**
	 * Pauses the current sub execution and resumes it after the specified time.
	 */
	public static void Sleep(int Milliseconds) {

	}
	/**
	 * Inline If - returns TrueValue if Condition is True and False otherwise. Only the relevant expression is evaluated. 
	 */
	public static Object IIf (boolean Condition, Object TrueValue, Object FalseValue) {
		return null;
	}
	@Hide
	public static void Sleep(final BA ba, final ResumableSub rs, int Milliseconds) {
		BA.handler.postDelayed(new BA.B4ARunnable() {

			@Override
			public void run() {
				if (ba == null) {
					BA.LogError("Sleep failed to resume (ba = null)");
					return;
				}
				boolean isActivity = ba.processBA != null;
				if (isActivity) {
					if (ba.processBA.sharedProcessBA.activityBA == null || ba != ba.processBA.sharedProcessBA.activityBA.get()) {//activity recreated
						BA.LogInfo("Sleep not resumed (context destroyed): " + rs.getClass().getName());
						return;
					}

				}
				if (ba.isActivityPaused()) {
					if (isActivity)
						ba.processBA.addMessageToPausedMessageQueue("sleep", this);
					else
						BA.LogInfo("Sleep not resumed (context is paused): " + rs.getClass().getName());
					return;
				}
				try {
					rs.resume(ba, null);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		}, Milliseconds);
	}
	@Hide
	public static void WaitFor(String SubName, BA ba, ResumableSub rs, Object SenderFilter) {
		if (ba.waitForEvents == null)
			ba.waitForEvents = new HashMap<String, LinkedList<WaitForEvent>>();
		Object o;
		if (SenderFilter instanceof ObjectWrapper)
			o = ((ObjectWrapper<?>)SenderFilter).getObject();
		else
			o = SenderFilter;
		if (o instanceof ResumableSub) {
			ResumableSub rsSenderFilter = (ResumableSub)o;
			if (rsSenderFilter.completed)
				throw new RuntimeException("Resumable sub already completed");
			rsSenderFilter.waitForBA = ba;

		}
		LinkedList<WaitForEvent> ll = ba.waitForEvents.get(SubName);
		if (ll == null) {
			ll = new LinkedList<BA.WaitForEvent>();
			ba.waitForEvents.put(SubName, ll);
		}
		boolean added = false;
		Iterator<WaitForEvent> it = ll.iterator();
		while (it.hasNext()) {
			WaitForEvent wfe = it.next();
			if (added == false && ((o == null && wfe.noFilter()) || (o != null && o == wfe.senderFilter.get()))) {
				added = true;
				wfe.rs = rs;
			} else if (wfe.cleared()) {
				it.remove();
			}
		}
		if (added == false) {
			WaitForEvent wfe = new WaitForEvent(rs, o);
			if (wfe.noFilter())
				ll.addLast(wfe);
			else
				ll.addFirst(wfe);
		}
	}
	@Hide
	public static void ReturnFromResumableSub(final ResumableSub rs, final Object returnValue) {
		BA.handler.post(new Runnable() {

			@Override
			public void run() {
				rs.completed = true;
				if (rs.waitForBA != null)
					rs.waitForBA.raiseEvent(rs, "complete", returnValue);

			}
		});

	}
	/**
	 * This object is returned from a call to a non-void resumable sub.
	 *You can use it as the sender filter parameter and wait for the Complete event. 
	 */
	@ShortName("ResumableSub")
	public static class ResumableSubWrapper extends AbsObjectWrapper<ResumableSub> {
		/**
		 * Tests whether the resumable sub has already completed.
		 */
		public boolean getCompleted() {
			return getObject().completed;
		}
	}
	@Hide
	public interface DesignerCustomView {
		void DesignerCreateView(PanelWrapper base, LabelWrapper lw, anywheresoftware.b4a.objects.collections.Map props);
		void _initialize(BA ba, Object activityClass, String EventName);

	}

}

