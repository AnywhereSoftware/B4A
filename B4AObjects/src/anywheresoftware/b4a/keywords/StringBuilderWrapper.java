package anywheresoftware.b4a.keywords;

import java.util.Map.Entry;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.B4aDebuggable;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

/**
 * StringBuilder is a mutable string, unlike regular strings which are immutable.
 *StringBuilder is especially useful when you need to concatenate many strings.
 *The following code demonstrates the performance boosting of StringBuilder:<code>
 *Dim start As Long
 *start = DateTime.Now
 *'Regular string
 *Dim s As String
 *For i = 1 To 5000
 *	s = s & i
 *Next
 *Log(DateTime.Now - start)
 *'StringBuilder
 *start = DateTime.Now
 *Dim sb As StringBuilder
 *sb.Initialize
 *For i = 1 To 5000
 *	sb.Append(i)
 *Next
 *Log(DateTime.Now - start)</code>
 *Tested on a real device, the first 'for loop' took about 20 seconds and the second took less then tenth of a second.
 *The reason is that the code: <code>s = s & i</code> creates a new string each iteration (strings are immutable).
 *The method <code>StringBuilder.ToString</code> converts the object to a string.
 */
@ShortName("StringBuilder")
public class StringBuilderWrapper extends AbsObjectWrapper<StringBuilder> implements B4aDebuggable{
	/**
	 * Initializes the object.
	 *Example:<code>
	 *Dim sb As StringBuilder
	 *sb.Initialize
	 *sb.Append("The value is: ").Append(SomeOtherVariable).Append(CRLF)</code>
	 */
	public void Initialize() {
		setObject(new StringBuilder());
	}
	/**
	 * Appends the specified text at the end.
	 *Returns the same object, so you can chain methods.
	 *Example:<code>
	 *sb.Append("First line").Append(CRLF).Append("Second line")</code>
	 */
	public StringBuilderWrapper Append(String Text) {
		getObject().append(Text);
		return this;
	}
	/**
	 * Converts the object to a string.
	 */
	public String ToString() {
		return getObject().toString();
	}
	/**
	 * Removes the specified characters.
	 *StartOffset - The first character to remove.
	 *EndOffset - The ending index. This character will not be removed.
	 */
	public StringBuilderWrapper Remove(int StartOffset, int EndOffset) {
		getObject().delete(StartOffset, EndOffset);
		return this;
	}
	/**
	 * Inserts the specified text at the specified offset.
	 */
	public StringBuilderWrapper Insert(int Offset, String Text) {
		getObject().insert(Offset, Text);
		return this;
	}
	/**
	 * Returns the number of characters.
	 */
	public int getLength() {
		return getObject().length();
	}
	@Hide
	@Override
	public String toString() {
		return getObjectOrNull() == null ? "null" : getObject().toString();
	}
	@Hide
	@Override
	public Object[] debug(int limit, boolean[] outShouldAddReflectionFields) {
		Object[] res = new Object[2 * 2];
		res[0] = "Length";
		res[1] = getLength();
		res[2] = "ToString";
		res[3] = toString();
		outShouldAddReflectionFields[0] = true;
		return res;
	}
}
