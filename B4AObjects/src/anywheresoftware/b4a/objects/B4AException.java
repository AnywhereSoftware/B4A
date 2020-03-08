package anywheresoftware.b4a.objects;

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
}
