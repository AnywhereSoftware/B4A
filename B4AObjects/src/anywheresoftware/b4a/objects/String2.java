package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.BA.DesignerName;

/**
 * Strings are immutable in Basic4android, which means that you can change the value of a string variable but you cannot change the text stored in a string object.
 *So methods like SubString, Trim and ToLowerCase return a new string, <b>they do not change the value of the current string</b>.
 *Typical usage:<code>
 *Dim s As String
 *s = "some text"
 *s = s.Replace("a", "b")</code>
 *You can use StringBuilder if you need a mutable string.
 *Note that string literals are also string objects:<code>
 *Log(" some text ".Trim)</code>
 */
public abstract class String2 {
	/**
	 * Returns the length of this string.
	 */
	@DesignerName("Length")
	public abstract int length();
	/**
	 * Returns the index of the first occurrence of SearchFor string in the string.
	 *Returns -1 if SearchFor was not found.
	 */
	@DesignerName("IndexOf")
	public abstract int indexOf(String SearchFor) ;
	
	/**
	 * Returns the index of the first occurrence of SearchFor string in the string.
	 *Starts searching from the given Index.
	 *Returns -1 if SearchFor was not found.
	 */
	@DesignerName("IndexOf2")
	public abstract int indexOf(String SearchFor, int Index) ;
	/**
	 * Returns the index of the first occurrence of SearchFor string in the string.
	 *The search starts at the end of the string and advances to the beginning.
	 */
	@DesignerName("LastIndexOf")
	public abstract int lastIndexOf(String SearchFor);
	/**
	 * Returns the index of the first occurrence of SearchFor string in the string.
	 *The search starts at the given index and advances to the beginning.
	 */
	@DesignerName("LastIndexOf2")
	public abstract int lastIndexOf(String SearchFor, int Index);
	/**
	 * Returns a copy of the original string without any leading or trailing white spaces.
	 */
	@DesignerName("Trim")
	public abstract String trim();
	
	/**
	 * Returns a new string which is a substring of the original string.
	 *The new string will include the character at BeginIndex and will extend to the end of the string.
	 *
	 *Example:
	 *<code>"012345".SubString(2) 'returns "2345"</code>
	 */
	@DesignerName("SubString")
	public abstract String substring(int BeginIndex);
	
	/**
	 * Returns a new string which is a substring of the original string.
	 *The new string will include the character at BeginIndex and will extend to the character at EndIndex, not including the last character.
	 *
	 *Example:
	 *<code>"012345".SubString2(2, 4) 'returns "23"</code>
	 */
	@DesignerName("SubString2")
	public abstract String substring(int BeginIndex, int EndIndex);
	/**
	 * Lexicographically compares the two strings.
	 *Returns a value less than 0 if the current string precedes Other.
	 *Returns 0 if both strings are equal.
	 *Returns a value larger than 0 if the current string comes after Other.
	 *Note that upper case characters precede lower case characters.
	 *
	 *Examples:<code>
	 *"abc".CompareTo("da") ' < 0 
	 *"abc".CompareTo("Abc") ' > 0
	 *"abc".CompareTo("abca")' < 0 </code>
	 */
	@DesignerName("CompareTo")
	public abstract int compareTo(String Other);
	
	/**
	 * Returns true if both strings are equal ignoring their case.
	 */
	@DesignerName("EqualsIgnoreCase")
	public abstract boolean equalsIgnoreCase(String other);
	/**
	 * Returns the character at the given index.
	 */
	@DesignerName("CharAt")
	public abstract char charAt(int Index);
	
	/**
	 * Returns true if this string starts with the given Prefix.
	 */
	@DesignerName("StartsWith")
	public abstract boolean startsWith(String Prefix);
	
	/**
	 * Returns true if this string ends with the given Suffix.
	 */
	@DesignerName("EndsWith")
	public abstract boolean endsWith(String Suffix);
	
	/**
	 * Returns a new string resulting from the replacement of all the occurrences of Target with Replacement.
	 */
	@DesignerName("Replace")
	public abstract String replace(String Target, String Replacement);
	
	/**
	 * Returns a new string which is the result of lower casing this string.
	 */
	@DesignerName("ToLowerCase")
	public abstract String toLowerCase();
	/**
	 * Tests whether the string contains the given string parameter.
	 */
	@DesignerName("Contains")
	public abstract boolean contains(String SearchFor);
	/**
	 * Returns a new string which is the result of upper casing this string.
	 */
	@DesignerName("ToUpperCase")
	public abstract String toUpperCase();
	
	/**
	 * Encodes the string into a new array of bytes.
	 * Example:<code>
	 * Dim Data() As Byte
	 * Data = "Some string".GetBytes("UTF8")</code>
	 */
	@DesignerName("GetBytes")
	public abstract byte[] getBytes(String Charset);
	
	
}
