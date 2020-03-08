package anywheresoftware.b4a.keywords.constants;

import anywheresoftware.b4a.BA.ShortName;
/**
 * A predefined object containing color constants.
 *For example: <code>Activity.Color = Colors.Green</code>
 */
public class Colors {
    public static final int Black       = 0xFF000000;
    public static final int DarkGray      = 0xFF444444;
    public static final int Gray        = 0xFF888888;
    public static final int LightGray      = 0xFFCCCCCC;
    public static final int White       = 0xFFFFFFFF;
    public static final int Red         = 0xFFFF0000;
    public static final int Green       = 0xFF00FF00;
    public static final int Blue        = 0xFF0000FF;
    public static final int Yellow      = 0xFFFFFF00;
    public static final int Cyan       = 0xFF00FFFF;
    public static final int Magenta     = 0xFFFF00FF;
    public static final int Transparent = 0;

    /**
     * Returns an integer value representing the color built from the three components.
     *Each component should be a value between 0 to 255 (inclusive)
     *Alpha is implicitly set to 255 (opaque).
     */
	public static int RGB(int R, int G, int B) {
		return android.graphics.Color.rgb(R, G, B);
	}
	/**
     * Returns an integer value representing the color built from the three components and with the specified alpha value.
     *Each component should be a value between 0 to 255 (inclusive)
     *Alpha - A value between 0 to 255 where 0 is fully transparent and 255 is fully opaque.
     */
	public static int ARGB(int Alpha, int R, int G, int B) {
		return android.graphics.Color.argb(Alpha, R, G, B);
	}
}
