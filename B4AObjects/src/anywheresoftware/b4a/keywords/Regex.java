package anywheresoftware.b4a.keywords;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
/**
 * Regex is a predefined object that contains regular expressions methods.
 *All methods receive a 'pattern' string. This is the regular expression pattern.
 *More information about the regular expression engine can be found here: <link>Pattern Javadoc|http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html</link>
 *<link>Regular expression in Basic4android tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/7123-regular-expressions-tutorial.html</link>.
 */
public class Regex {
	
	private static LinkedHashMap<PatternAndOptions, Pattern> cachedPatterns;
	/**
	 * Changes ^ and $ to match the start and end of each line instead of the whole string.
	 */
	public static final int MULTILINE = Pattern.MULTILINE;
	/**
	 * Enables case insensitive matching.
	 */
	public static final int CASE_INSENSITIVE = Pattern.CASE_INSENSITIVE;
	private static Pattern getPattern(String pattern, int options) {
		if (cachedPatterns == null)
			cachedPatterns = new LinkedHashMap<PatternAndOptions, Pattern>();
		PatternAndOptions po = new PatternAndOptions(pattern, options);
		Pattern p = cachedPatterns.get(po);
		if (p == null) {
			p = Pattern.compile(pattern, options);
			cachedPatterns.put(po, p);
			if (cachedPatterns.size() > 50) {
				Iterator<Entry<PatternAndOptions, Pattern>> it = cachedPatterns.entrySet().iterator();
				for (int i = 0;i < 25 ;i++) {
					it.next();
					it.remove();
				}
			}
		}
		return p;
	}
	/**
	 * Tests whether the given text is a match for the given pattern.
	 *The whole text should match the pattern. Use Matcher if you look for a substring that matches the pattern.
	 *Example:<code>
	 *If Regex.IsMatch("\d\d\d", EditText1.Text) = False Then ...</code>
	 */
	public static boolean IsMatch(String Pattern, String Text) {
		return IsMatch2(Pattern, 0, Text);
	}
	/**
	 * Tests whether the given text is a match for the given pattern.
	 *Options - One or more pattern options. These options can be combined with Bit.Or.
	 */
	public static boolean IsMatch2(String Pattern, int Options, String Text) {
		return getPattern(Pattern, Options).matcher(Text).matches();
	}
	/**
	 * Replaces all the matches in the text based on the specified pattern and template.
	 *Example:<code>
	 *Log(Regex.Replace("\d", "1 2 3 4", "-$0-")) '-1- -2- -3- -4-</code>
	 */
	public static String Replace (String Pattern, String Text, String Template) {
		return Replace2(Pattern, 0, Text, Template);
	}
	/**
	 * Similar to Replace. Allows setting the regex options.
	 */
	public static String Replace2 (String Pattern, int Options, String Text, String Template) {
		return getPattern(Pattern, Options).matcher(Text).replaceAll(Template);
	}
	/**
	 * Splits the given text around matches of the pattern.
	 *Note that trailing empty matches will be removed.
	 *Example:<code>
	 *Dim components() As String
	 *components = Regex.Split(",", "abc,def,,ghi") 'returns: "abc", "def", "", "ghi"
	 *components = Regex.Split("\|", "abd|def||ghi") 'the pipe needs to be escaped as it has special meaning in Regex.</code> 
	 */
	public static String[] Split(String Pattern, String Text) {
		return Split2(Pattern, 0, Text);
	}
	/**
	 * Same as Split with the additional pattern options.
	 */
	public static String[] Split2(String Pattern, int Options, String Text) {
		return getPattern(Pattern, Options).split(Text);
	}
	/**
	 * Returns a Matcher object which can be used to find matches of the given pattern in the text.
	 *Example:<code>
	 *Dim text, pattern As String
	 *text = "This is an interesting sentence with two numbers: 123456 and 7890."
	 *pattern = "\d+" 'one or more digits
	 *Dim Matcher1 As Matcher
	 *Matcher1 = Regex.Matcher(pattern, text)
	 *Do While Matcher1.Find
	 *	Log("Found: " & Matcher1.Match)
	 *Loop</code>
	 */
	public static MatcherWrapper Matcher(String Pattern, String Text) {
		return Matcher2(Pattern, 0, Text);
	}
	/**
	 * Same as Matcher with the additional pattern options.
	 */
	public static MatcherWrapper Matcher2 (String Pattern, int Options, String Text) {
		MatcherWrapper mw = new MatcherWrapper();
		mw.setObject(getPattern(Pattern, Options).matcher(Text));
		return mw;
	}
	/**
	 * A Matcher object is used to search for a pattern in a string.
	 *<code>Regex.Matcher</code> returns a matcher object for a specific pattern and specific text.
	 *Example:<code>
	 *Dim text, pattern As String
	 *text = "This is an interesting sentence with two numbers: 123456 and 7890."
	 *pattern = "\d+" 'one or more digits
	 *Dim Matcher1 As Matcher
	 *Matcher1 = Regex.Matcher(pattern, text)
	 *Do While Matcher1.Find
	 *	Log("Found: " & Matcher1.Match)
	 *Loop</code>
	 */
	@ShortName("Matcher")
	public static class MatcherWrapper extends AbsObjectWrapper<Matcher> {
		
		/**
		 * Searches for the next substring that matches the pattern.
		 *Returns True if such a match was found.
		 *Example:<code>
		 *Dim text, pattern As String
		 *text = "This is an interesting sentence with two numbers: 123456 and 7890."
		 *pattern = "\d+" 'one or more digits
		 *Dim Matcher1 As Matcher
		 *Matcher1 = Regex.Matcher(pattern, text)
		 *Do While Matcher1.Find
		 *	Log("Found: " & Matcher1.Match)
		 *Loop</code>
		 */
		 
		public boolean Find() {
			return getObject().find();
		}
		/**
		 * Returns the value of the specified captured group.
		 *Group(0) returns the whole match.
		 */
		public String Group(int Index) {
			return getObject().group(Index);
		}
		/**
		 * Returns the number of capturing groups in the pattern.
		 *Note that the number returned does not include group(0) which is the whole match.
		 */
		public int getGroupCount() {
			return getObject().groupCount();
		}
		/**
		 * Returns the matched value. This is the same as calling Group(0).
		 */
		public String getMatch() {
			return getObject().group();
		}
		/**
		 * Returns the start offset of the specified captured group.
		 *Use GetStart(0) to get the start offset of the whole match.
		 */
		public int GetStart(int Index) {
			return getObject().start(Index);
		}
		/**
		 * Returns the end offset of the specified captured group.
		 *Use GetEnd(0) to get the end offset of the whole match.
		 */
		public int GetEnd(int Index) {
			return getObject().end(Index);
		}
	}
	private static class PatternAndOptions {
		public final String pattern;
		public final int options;
		public PatternAndOptions(String pattern, int options) {
			this.pattern = pattern;
			this.options = options;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + options;
			result = prime * result
					+ ((pattern == null) ? 0 : pattern.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PatternAndOptions other = (PatternAndOptions) obj;
			if (options != other.options)
				return false;
			if (pattern == null) {
				if (other.pattern != null)
					return false;
			} else if (!pattern.equals(other.pattern))
				return false;
			return true;
		}
		
	}
}
