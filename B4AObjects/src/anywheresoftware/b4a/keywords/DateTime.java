package anywheresoftware.b4a.keywords;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.TimeFormatException;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
/**
 * Date and time related methods.
 *DateTime is a predefined object. You should not declare it yourself.
 *Date and time values are stored as ticks. Ticks are the number of milliseconds since January 1, 1970 00:00:00 UTC.
 *<b>This value is too large to be stored in an Int variable. It should only be stored in a Long variable.</b>
 *The methods <code>DateTime.Date</code> and <code>DateTime.Time</code> convert the ticks value to a string.
 *You can get the current time with <code>DateTime.Now</code>.
 *Example:<code>
 *Dim now As Long
 *now = DateTime.Now
 *Msgbox("The date is: " & DateTime.Date(now) & CRLF & _
 *	"The time is: " & DateTime.Time(now), "")</code>
 */
@Events(values={"TimeChanged"})
public class DateTime {
	public static final long TicksPerSecond = 1000L;
	public static final long TicksPerMinute = 60 * TicksPerSecond;
	public static final long TicksPerHour = TicksPerMinute * 60;
	public static final long TicksPerDay = TicksPerHour * 24;
	private java.util.Date date;
	private static DateTime _instance;
	private static TimeZone zeroTimeZone = new SimpleTimeZone(0, "13256");

	private static DateTime getInst() {
		if (_instance == null)
			_instance = new DateTime();
		return _instance;
	}
	private Calendar cal;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;
	private TimeZone timeZone;
	private static boolean listenToTimeZone = false;
	private static long lastTimeSetEvent;
	private DateTime() {
		cal = Calendar.getInstance(Locale.US);
		dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateFormat.setLenient(false);
		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setLenient(false);
		date = new Date(0);
		timeZone = TimeZone.getDefault();
	}
	/**
	 * Creates a dynamic broadcast receiver that listens to the "time-zone changed" event and "time set" event.
	 *By calling this method the time-zone will update automatically when the device time-zone changes.
	 *DateTime_TimeChanged event will be raised when the time-zone changes or when the time is set.
	 */
	public static void ListenToExternalTimeChanges(final BA ba) {
		if (listenToTimeZone)
			return;
		listenToTimeZone = true;
		BroadcastReceiver br = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.hasExtra("time-zone")) {
					String id = intent.getStringExtra("time-zone");
					setTimeZoneInternal(TimeZone.getTimeZone(id));
				}
				if (DateTime.getNow() - lastTimeSetEvent > 100)
					ba.raiseEventFromDifferentThread(null,null, 0, "datetime_timechanged", false, null);
				lastTimeSetEvent = DateTime.getNow();

			}
		};
		IntentFilter fil = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
		fil.addAction(Intent.ACTION_TIME_CHANGED);
		BA.applicationContext.registerReceiver(br, fil);
	}
	/**
	 * Gets the current time as ticks (number of milliseconds since January 1, 1970). 
	 */
	public static long getNow() {
		return System.currentTimeMillis();
	}
	/**
	 * Returns a string representation of the date (which is stored as ticks).
	 *The date format can be set with the DateFormat keyword.
	 *Example:<code>
	 *Log("Today is: " & DateTime.Date(DateTime.Now))</code>
	 */
	public static String Date(long Ticks) {
		DateTime d = getInst();
		d.date.setTime(Ticks);
		return d.dateFormat.format(d.date);
	}
	/**
	 * Returns a string representation of the time (which is stored as ticks).
	 *The time format can be set with the TimeFormat keyword.
	 *Example:<code>
	 *Log("The time now is: " & DateTime.Time(DateTime.Now))</code>
	 */
	public static String Time(long Ticks) {
		DateTime d = getInst();
		d.date.setTime(Ticks);
		return d.timeFormat.format(d.date);
	}
	/**
	 * Gets or sets the format used to parse time strings.
	 *See this page for the supported patterns: <link>formats|http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html</link>.
	 *The default pattern is HH:mm:ss (23:45:12 for example). <b>HH not hh</b>.
	 */
	public static String getTimeFormat() {
		return getInst().timeFormat.toPattern();
	}
	public static void setTimeFormat(String Pattern) {
		getInst().timeFormat.applyPattern(Pattern);
	}
	/**
	 * Gets or sets the format used to parse date strings.
	 *See this page for the supported patterns: <link>formats|http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html</link>.
	 *The default pattern is MM/dd/yyyy (04/23/2002 for example). <b>MM not mm</b>.
	 */
	public static String getDateFormat() {
		return getInst().dateFormat.toPattern();
	}
	public static void setDateFormat(String Pattern) {
		getInst().dateFormat.applyPattern(Pattern);
	}
	/**
	 * Parses the given date string and returns its ticks representation.
	 *An exception will be thrown if parsing fails.
	 *Example:<code>
	 *Dim SomeTime As Long
	 *SomeTime = DateTime.DateParse("02/23/2007")</code>
	 */
	public static long DateParse(String Date) throws ParseException {
		return getInst().dateFormat.parse(Date).getTime();
	}
	/**
	 * Returns the default date format based on the device selected language.
	 */
	public static String getDeviceDefaultDateFormat() {
		SimpleDateFormat sdf = (SimpleDateFormat)DateFormat.getDateInstance();
		return sdf.toPattern();
	}
	/**
	 * Returns the default time format based on the device selected language.
	 */
	public static String getDeviceDefaultTimeFormat() {
		SimpleDateFormat sdf = (SimpleDateFormat)DateFormat.getTimeInstance();
		return sdf.toPattern();
	}
	/**
	 * Parses the given time string and returns its ticks representation.
	 *Note that the returned value date will be today.
	 */
	public static long TimeParse(String Time) throws ParseException {
		SimpleDateFormat tf = getInst().timeFormat;
		tf.setTimeZone(zeroTimeZone);
		long time = 0;
		try {
			time = tf.parse(Time).getTime(); //no time zone here
		} finally {
			tf.setTimeZone(getInst().timeZone);
		}
		long offsetInMinutes = Math.round(getTimeZoneOffset() * 60);
		long dayStartInUserTimeZone = System.currentTimeMillis() + offsetInMinutes * DateTime.TicksPerMinute;
		dayStartInUserTimeZone = dayStartInUserTimeZone - (dayStartInUserTimeZone % TicksPerDay);
		dayStartInUserTimeZone -= offsetInMinutes * DateTime.TicksPerMinute;
		return dayStartInUserTimeZone + (time % TicksPerDay);
	}
	
	/**
	 * Parses the given date and time strings and returns the ticks representation.
	 */
	public static long DateTimeParse(String Date, String Time) throws ParseException {
		SimpleDateFormat df = getInst().dateFormat;
		SimpleDateFormat tf = getInst().timeFormat;
		df.setTimeZone(zeroTimeZone);
		tf.setTimeZone(zeroTimeZone);
		try {
			//no time zone here
			long dd = DateParse(Date);
			long tt = tf.parse(Time).getTime(); 
			long total = dd + tt;
			int endShift, startShift;
			endShift = (int) (GetTimeZoneOffsetAt(total) * TicksPerHour);
			total = total - endShift;
			startShift = (int)(GetTimeZoneOffsetAt(total) * TicksPerHour);
			total = total + (endShift - startShift);
			return total;
		} finally {
			tf.setTimeZone(getInst().timeZone);
			df.setTimeZone(getInst().timeZone);
		}

	}
	/**
	 * Sets the application time zone. This setting affect the conversions of dates to ticks value and vice versa (device default settings are not changed).
	 */
	public static void SetTimeZone(double OffsetHours) {
		setTimeZoneInternal(new SimpleTimeZone((int)Math.round(OffsetHours * 3600 * 1000), ""));
	}
	@Hide
	public static void SetTimeZone(int OffsetHours) {
		setTimeZoneInternal(new SimpleTimeZone(OffsetHours * 3600 * 1000, ""));
	}
	private static void setTimeZoneInternal(TimeZone tz) {
		getInst().timeZone = tz; 
		getInst().cal.setTimeZone(getInst().timeZone);
		getInst().dateFormat.setTimeZone(getInst().timeZone);
		getInst().timeFormat.setTimeZone(getInst().timeZone);
	}
	/**
	 * Returns the current offset measured in hours from UTC.
	 */
	public static double getTimeZoneOffset() {
		return getInst().timeZone.getOffset(System.currentTimeMillis()) / (3600d * 1000);
	}
	/**
	 * Returns the offset measured in hours from UTC at the specified date (offset can change due to daylight saving settings).
	 */
	public static double GetTimeZoneOffsetAt(long Date) {
		double d = getInst().timeZone.getOffset(Date) / (3600d * 1000);
		return d;
	}
	/**
	 * Returns the year component from the ticks value.
	 */
	public static int GetYear(long Ticks) {
		getInst().cal.setTimeInMillis(Ticks);
		return getInst().cal.get(Calendar.YEAR);
	}
	/**
	 * Returns the month of year component from the ticks value.
	 *Values are between 1 to 12.
	 */
	public static int GetMonth(long Ticks) {
		getInst().cal.setTimeInMillis(Ticks);
		return getInst().cal.get(Calendar.MONTH) + 1; //+1 !
	}
	/**
	 * Returns the day of month component from the ticks value.
	 *Values are between 1 to 31.
	 */
	public static int GetDayOfMonth(long Ticks) {
		getInst().cal.setTimeInMillis(Ticks);
		return getInst().cal.get(Calendar.DAY_OF_MONTH);
	}
	/**
	 * Returns the day of year component from the ticks value.
	 *Values are between 1 to 366.
	 */
	public static int GetDayOfYear(long Ticks) {
		getInst().cal.setTimeInMillis(Ticks);
		return getInst().cal.get(Calendar.DAY_OF_YEAR);
	}
	/**
	 * Returns the day of week component from the ticks value.
	 *Values are between 1 to 7, where 1 means Sunday.
	 *You can use the AHLocale library if you need to change the first day.
	 */
	public static int GetDayOfWeek(long Ticks) {
		getInst().cal.setTimeInMillis(Ticks);
		return getInst().cal.get(Calendar.DAY_OF_WEEK);
	}
	/**
	 * Returns the hour of day component from the ticks value.
	 *Values are between 0 to 23.
	 */
	public static int GetHour(long Ticks) {
		getInst().cal.setTimeInMillis(Ticks);
		return getInst().cal.get(Calendar.HOUR_OF_DAY);
	}
	/**
	 * Returns the seconds within a minute component from the ticks value.
	 *Values are between 0 to 59.
	 */
	public static int GetSecond(long Ticks) {
		getInst().cal.setTimeInMillis(Ticks);
		return getInst().cal.get(Calendar.SECOND);
	}
	/**
	 * Returns the minutes within a hour component from the ticks value.
	 *Values are between 0 to 59.
	 */
	public static int GetMinute(long Ticks) {
		getInst().cal.setTimeInMillis(Ticks);
		return getInst().cal.get(Calendar.MINUTE);
	}
	/**
	 * Returns a ticks value which is the result of adding the specified time spans to the given ticks value.
	 *Pass negative values if you want to subtract the values.
	 *Example:<code>
	 *Dim Tomorrow As Long
	 *Tomorrow = DateTime.Add(DateTime.Now, 0, 0, 1)
	 *Log("Tomorrow date is: " & DateTime.Date(Tomorrow))</code>
	 */
	public static long Add(long Ticks, int Years, int Months, int Days) {
		Calendar c = getInst().cal;
		c.setTimeInMillis(Ticks);
		c.add(Calendar.YEAR, Years);
		c.add(Calendar.MONTH, Months);
		c.add(Calendar.DAY_OF_YEAR, Days);
		return c.getTimeInMillis();
	}
}
