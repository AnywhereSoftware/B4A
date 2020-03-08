package anywheresoftware.b4a.keywords.constants;

import java.io.IOException;

import android.graphics.Typeface;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.TextViewWrapper;
import anywheresoftware.b4a.objects.streams.File;
/**
 * Typeface is a predefined object that holds the typeface styles and the default installed fonts.
 *Note that unlike most other predefined objects, you can declare new objects of this type.
 *Example:<code>
 *EditText1.Typeface = Typeface.DEFAULT_BOLD</code>
 */
@ShortName("Typeface")
public class TypefaceWrapper extends AbsObjectWrapper<Typeface>{
    public static final Typeface DEFAULT = Typeface.DEFAULT;
    public static final Typeface DEFAULT_BOLD = Typeface.DEFAULT_BOLD;
    public static final Typeface SANS_SERIF = Typeface.SANS_SERIF;
    public static final Typeface SERIF = Typeface.SERIF;
    public static final Typeface MONOSPACE = Typeface.MONOSPACE;
    
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;
    public static final int STYLE_BOLD_ITALIC = 3;
    /**
     * Returns a typeface with the specified style.
     *Example:<code>
     *Typeface.CreateNew(Typeface.MONOSPACE, Typeface.STYLE_ITALIC)</code>
     */
    public static Typeface CreateNew(Typeface Typeface1, int Style) {
    	return Typeface.create(Typeface1, Style);
    }
    /**
     * Loads a font file, that was added with the file manager.
     *Example:<code>
     *Dim MyFont As Typeface
     *MyFont = Typeface.LoadFromAssets("MyFont.ttf")
     *EditText1.Typeface = MyFont</code>
     */
    public static Typeface LoadFromAssets(String FileName) throws IOException {
    	if (File.virtualAssetsFolder != null)
    		return Typeface.createFromFile(new java.io.File(File.virtualAssetsFolder, 
    				File.getUnpackedVirtualAssetFile(FileName)));
    	return Typeface.createFromAsset(BA.applicationContext.getAssets(), FileName.toLowerCase(BA.cul));
    }
    //This will add the file to the project
    public static Typeface getFONTAWESOME() {
    	return TextViewWrapper.getTypeface(TextViewWrapper.fontAwesomeFile);
    }
    //This will add the file to the project
    public static Typeface getMATERIALICONS() {
    	return TextViewWrapper.getTypeface(TextViewWrapper.materialIconsFile);
    }
}
