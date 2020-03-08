package anywheresoftware.b4a.keywords;

import java.io.DataInputStream;
import java.io.IOException;

import anywheresoftware.b4a.ConnectorUtils;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

/**
 * Holds values related to the display.
 *You can get the values of the the current display by calling <code>GetDeviceLayoutValues</code>.
 *For example:<code>
 *Dim lv As LayoutValues
 *lv = GetDeviceLayoutValues
 *Log(lv) 'will print the values to the log</code>
 *<code>Activity.LoadLayout</code> and <code>Panel.LoadLayout</code> return a LayoutValues object with the values of the
 *chosen layout variant.  
 */
@ShortName("LayoutValues")
public class LayoutValues {
	/**
	 * The device scale value which is equal to 'dots per inch' / 160.
	 */
	public float Scale;
	/**
	 * The display width (pixels).
	 */
	public int Width;
	/**
	 * The display height (pixels).
	 */
	public int Height;
	/**
	 * Returns the approximate screen size.
	 */
	public double getApproximateScreenSize() {
		if (Scale == 0)
			throw new RuntimeException("Scale = 0");
		return Math.sqrt(Math.pow(Width / Scale, 2) + Math.pow(Height / Scale, 2)) / 160;
	}
	@Hide
	public static LayoutValues readFromStream(DataInputStream din) throws IOException {
		LayoutValues lv = new LayoutValues();
		lv.Scale = Float.intBitsToFloat(ConnectorUtils.readInt(din));
		lv.Width = ConnectorUtils.readInt(din);
		lv.Height = ConnectorUtils.readInt(din);
		return lv;
	}
	@Hide
	public float calcDistance(LayoutValues device) {
		float fixedScale = device.Scale / Scale;
		float w = Width * fixedScale;
		float h = Height * fixedScale;
		if (w > device.Width * 1.2)
			return Float.MAX_VALUE;
		if (h > device.Height * 1.2)
			return Float.MAX_VALUE;
		if (w > device.Width)
			w += 50;
		if (h > device.Height)
			h += 50;
		float sameOrientation = Math.signum(w - h) == Math.signum(device.Width - device.Height) ? 0 : 100; //larger value => larger distance
		return Math.abs(w - device.Width) + Math.abs(h - device.Height) + 100 * Math.abs(Scale - device.Scale) + sameOrientation;
	}
	@Override
	public String toString() {
		return ""+  Width + " x " + Height
        + ", scale = " + Scale + " (" + (int)(Scale * 160) + " dpi)";
	}
	
}
