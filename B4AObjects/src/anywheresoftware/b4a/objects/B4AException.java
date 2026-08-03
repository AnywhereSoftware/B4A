package anywheresoftware.b4a.objects;

import java.io.PrintWriter;
import java.io.StringWriter;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
/**
 * Holds a thrown exception.
 *You can access the last thrown exception by calling LastException.
 *For example:<code>
 *Try
 *   Dim in As InputStream
 *   in = File.OpenInput(File.DirInternal, "SomeMissingFile.txt")
 *   '...
 *Catch
 *   Log(LastException.Message)
 *End Try
 *If in.IsInitialized Then in.Close</code>
 */
@ShortName("Exception")
public class B4AException extends AbsObjectWrapper<Exception>{
	public String getMessage() {
		return getObject().toString();
	}
	/**
	 * Returns the stack trace as a string.
	 */
	public String getStackTrace() {
		Throwable e = getObject();
		 while (e.getCause() != null && e.getCause() != e) {
		        e = e.getCause();
		    }
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		getObject().printStackTrace(pw);
		return sw.toString();
	}
}
