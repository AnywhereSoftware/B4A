package anywheresoftware.b4a.objects;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

import com.hoho.android.usbserial.driver.UsbId;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

/**
 *UsbSerial supports various popular chips that support serial emulation over a USB connection
 *and provides a common API to communicate with them all.
 *The devices recognised by this library include:
 *
 *Various Prolific PL2303 based USB serial devices.
 *
 *Various FTDI FT232 based devices including Arduinos that use FTDI USB serial chips.
 *The FTDI "status byte" bug on reading input in version 1.0 of this library is fixed.
 *
 *Various Arduinos that use an Atmel 8U2 or 16U2 programmed as a CDC ACM Virtual Comport.
 *USB versions of the Roboclaw controller which act as an CDC ACM Virtual Comport in mode 15. 
 *
 *Silicon Labs CP2102 and possibly others of that series of USB to UART Bridges.
 *
 *The above chips are all slave USB devices and need host capabilities in the Android device.
 *This usually implies that an On The Go (OTG) or "Camera" cable is needed to switch the
 *Android hardware into host mode. Note that access to the modem control signals is not implemented for
 *any of the above devices owing to the lack of technical data about the actual chips which the manufacturers
 *deem proprietary information made available only to their OEM customers.
 *
 *Supported devices are recongised by the combination of their VendorID and Product ID, a list of which is
 *precomiled into the library. A device that is compatible with one of the four device drivers listed above
 *but that is not recognised may be supported using the SetCustomDevice method. this is most likely to be useful
 *for devices implementing the USB CDC (Communications Device Class) ACM (Abstract Control Model) which is a
 *vendor-independent publicly documented protocol.
 *
 *For convenience this library also supports Android Accessory devices that implement the AOA protocol
 *(Android Open Accessory) using the same common API as the slave devices listed above.
 *
 *Accessories are host mode devices and can connect directly to an Android device using the same cable
 *that is used to connect the Android device to a PC for program development or direct file transfer.
 *
 *There is a bug in the Android USB Accessory handling when trying to reconnect to a disconnected Accessory.
 *In order to reliably connect to an Accessory with your program it is necessary to ensure that
 *the process of any previous instance of your program that communicated with the Arduino has been killed.
 *This is the reason why there is an Exit button that calls ExitApplication in the UsbSerial demo program 
 *that you should use to kill the program. It is also necessary before restarting the program
 *or downloading and running a modified version again to physically disconnect and reconnect the Accessory again
 *If it is an Arduino ADK then pressing its Reset button will also work and will maintain the Accessory permission.
 */
@ShortName("UsbSerial")
@Version(2.4f)
public class UsbSerial {
	// 2.0 	initial release
	//	
	//		Changes to usb-serial-for-android code marked by *ADDED, *REMOVED and *MOVED
	//		http://git.altlinux.org/people/manowar/public/usb-serial-for-android.git?p=usb-serial-for-android.git;a=blobdiff;f=UsbSerialLibrary/src/com/hoho/android/usbserial/driver/FtdiSerialDriver.java;h=e7f768356b9a82c574db31053bb06ad8d83d0fe7;hp=93c51539c9b0efcc984948a6330482a6ab60e6a2;hb=2da5aa26234a09b10469ed444cde887ff57bb601;hpb=9f24bf39a60d8fba24c3cf2fe0a3f6d4b4fb83f0
	//		modified FtDiSerialDriver.Read to read and set mMaxPacketSize instead of hard coding it
	//		modified FtDiSerialDriver.Read to call filterModemStatus to remove additional status bytes
	//		added VENDOR_PROLIFIC = 0x067B, PROLIFIC_PL2303 = 0x2303 and ATMEL_ROBOCLAW  = 0x2404 to UsbId : 3 lines
	//		added ATMEL_ROBOCLAW to CdcAcmSerialDriver.getSupportedDevices : 1 line
	//		added PROLIFIC_SERIAL to UsbSerialProber
	//		added new class ProlificSerialDriver to package com.hoho.android.usbserial.driver
	//
	//		Changes to original UsbSerial code
	//		added UsbPresent, HasPermission and RequestPermission which assume only one device is attached
	//		added SetParameters, which must be used after Open(), and the constants for SetParameters
	//		added DeviceInfo
	//		added Accessory support
	//	
	// 2.1	Changes to 2.0 UsbSerial code
	//		corrected parameters of SetParameters
	//		added UsbTimeout
	//
	// 2.2	Changes to 2.1 UsbSerial code
	//		added SetCustomDevice
	//
	//		Changes to usb-serial-for-android code marked by *ADDED, *REMOVED and *MOVED
	//		added DRIVER_CUSTOM, VENDOR_CUSTOM = 0 and PRODUCT_CUSTOM = 0 to UsbId : 3 lines
	//		added DRIVER_NONE = 0, DRIVER_PROLIFIC = 1, DRIVER_SILABS = 2, DRIVER_CDCACM = 3 and DRIVER_FTDI = 4 to UsbId : 5 lines
	//		added conditional block to getSupportedDevices in all four concrete drivers
	//
	// 2.3	Changes to 2.1 UsbSerial code
	//
	//		Changes to usb-serial-for-android code marked by *ADDED, *REMOVED and *MOVED
	//		Swapped endpoints if necessary in CdcAcmSerialDriver.Open
	//
	// 2.4	Changes to 2.3 UsbSerial code and driver
	//		added Use of multiple serial USB devices not just the first one
	//		moved a ":" in DeviceInfo
	// 		sb.append("VendorId  :").append(toHex(....
	//		to                  
	//		sb.append("VendorId : ").append(toHex(....
	//
	// 		*	// 2014-07-09 JeanLC-Mod 0.1	->
    // 		* Mod Files: 
    // 		* 1.- com/hoho/android/usbserial/driver/UsbSerialProber.java
    // 		* 2.- anywheresoftware/b4a/objects/UsbSerial.java
	//		Use:
	//		Dim usb1 As UsbSerial
	//		Dim usbN As UsbSerial
	//		For Device 1: usb1.UsbPresent(1) ,HasPermission(1) , DeviceInfo(1), Open(9600, 1) , RequestPermission(1)	
	//		For Device n: usbN.UsbPresent(n), HasPermission(n), DeviceInfo(n), Open(9600, n), RequestPermission(n)
	//		You have to analyze the incoming data or check DeviceInfo(n) to know what is connected to the USB. The order could change. 
	//		Changes to usb-serial-for-android code marked by *ADDED, *REMOVED, *MOVED and *MODDED Ver_2.4
	// 		*	// 2014-07-09 JeanLC-Mod 0.1	<-
	//

	private static final double version = 2.4;

	/**
	 *Returns the version of the library.
	 */
	public double getVersion()
	{
		return version;
	}

	/**
	 *This library is based on the usb-serial-for-android library with support added
	 *for Prolific PL2303  and Android Accessory (ADK) devices. 
	 *<link>https://code.google.com/p/usb-serial-for-android/|https://code.google.com/p/usb-serial-for-android/</link>
	 *
	 *Unlike version 1.0 of this library it does not need a separate usb-serial-for-android jar file as this
	 *is now incopoprated into the library.
	 *
	 *usb-serial-for-android and therefore also this library is licensed under the GNU Lesser General Public License v3.
	 *<link>http://www.gnu.org/licenses/lgpl.html|http://www.gnu.org/licenses/lgpl.html</link>
	 *Copies of both the General Public License and Lesser General Public License are in the provided archive.
	 */
	public void LIBRARY_DOC()
	{
	}

	private boolean accOpen = false;
	private boolean driverOpen = false;
	
    UsbAccessory accessory;
    private ParcelFileDescriptor pfd;
    
	@Hide
	public volatile UsbSerialDriver driver;
	@Hide
	public volatile UsbSerialPort port;
	private int TIMEOUT = 200;
		
	/**
	 *Searches for a valid USB device or Accessory and tries to open it.
	 *Returns USB_DEVICE if a device was opened successfully.
	 *Returns USB_ACCESSORY if an accessory was opened successfully.
	 *Returns USB_NONE if neither a device nor an accessory was found.
	 *The BaudRate parameter is ignored if the connected device is an Accessory	
	 *
	 *ADDED: Ver_2.4 DevNum = 1 to n
	 *MODDED: Ver_2.4 Code changed to support DevNum
	 */
	public int Open(BA ba, int BaudRate, int DevNum) throws IOException {
		UsbManager manager = (UsbManager) BA.applicationContext.getSystemService(Context.USB_SERVICE);
		List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
		driver = drivers.size() > 0 ? drivers.get(Math.max(drivers.size(), DevNum) - 1) : null;
		if (driver != null) {
			port = driver.getPorts().get(0);
			port.open();
			port.setBaudRate(BaudRate);
			driverOpen = true;
			return USB_DEVICE;
		}
		UsbAccessory[] a = manager.getAccessoryList();
		if (a != null)
		{
			accessory = a[0];
			pfd = manager.openAccessory(accessory);
			accOpen = true;
			return USB_ACCESSORY;
		}		
		return USB_NONE;
	}
	
	/**
	 *Closes the USB device or Accessory.
	 *There seems to be a bug in the Android USB Accessory handling when trying to reconnect to a disconnected Accessory.
	 *In order to reliably connect to an Accessory with your program it is necessary to ensure that
	 *the process of any previous instance of your program that communicated with the Arduino has been killed.
	 *This is the reason why there is an Exit button that calls ExitApplication in the UsbSerial demo program 
	 *that you should use to kill the program. It is also necessary before restarting the program
	 *or downloading and running a modified version again to physically disconnect and reconnect the Accessory again
	 *If it is an Arduino ADK then pressing its Reset button will also work and will maintain the Accessry permission.
	 */
	public void Close() throws IOException {
		if (driver != null) {
			driver.close();
			driver = null;
		}
		if (accessory != null) {
			pfd.close();
			accessory = null;			
		}
		driverOpen = false;
		accOpen = false;
	}
	
	/**
	 *Returns an InputStream for working with AsyncStreams from the RandomAccessFile library.
	 */	
	public InputStream GetInputStream()
	{
		if (accOpen)
		{
			return new FileInputStream(pfd.getFileDescriptor());			
		}
		return new InputStream() {
			@Override
			public int read() throws IOException {
				throw new RuntimeException("This method is not supported.");
			}
			@Override
			public int read(byte b[]) throws IOException {
				try {
					return driver.read(b, TIMEOUT);
				} catch (RuntimeException e) {
					e.printStackTrace();
					throw e;
				}
			}
			@Override
			public void close() throws IOException {
				Close();
			}
		};
	}
	
	/**
	 * Returns an OutputStream for working with AsyncStreams from the RandomAccessFile library.
	 */
	public OutputStream GetOutputStream()
	{
		if (accOpen)
		{
			return new FileOutputStream(pfd.getFileDescriptor());
		}
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw new RuntimeException("This method is not supported.");
			}
			@Override
			public void write(byte b[]) throws IOException {
				try {
					driver.write(b, TIMEOUT);
				} catch (RuntimeException e) {
					e.printStackTrace();
					throw e;
				}
			}
			@Override
			public void close() throws IOException {
				Close();
			}
		};
	}

	/**
	 *Tests whether your application has permission to access this device or Accessory.
	 *Call RequestPermission to receive such permission.
	 *Returns True if the user already has permission.
	 *
	 *ADDED: Ver_2.4 DevNum = 1 to n, DevCount (Count devices)
	 *MODDED: Ver_2.4 Code changed to support DevNum and DevCount
	 */
	public boolean HasPermission(int DevNum)
	{
		int DevCount = 1;			
		UsbManager manager = (UsbManager) BA.applicationContext.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> h = manager.getDeviceList();
		if (h.size() > 0)
		{
			for (UsbDevice u : h.values())
			{
				if(DevCount++ == DevNum){				
					return manager.hasPermission(u);
				}				
			}
		}
		UsbAccessory[] a = manager.getAccessoryList();
		if (a != null)
		{
			return manager.hasPermission(a[0]);
		}		
		return false;
	}

	/**
	 *Shows a dialog that asks the user to allow your application to access the USB device or Accessory.
	 *Note that this dialog is non-modal so your code that invokes it will carry on running and not wait for the dialog to close.
	 *ADDED: Ver_2.4 DevNum = 1 to n
	 *MODDED: Ver_2.4 Code changed to support DevNum
	 */
	public void RequestPermission(int DevNum)
	{
		int DevCount = 1;			
		UsbManager manager = (UsbManager) BA.applicationContext.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> h = manager.getDeviceList();
		if (h.size() > 0)
		{
			for (UsbDevice u : h.values())
			{
				if(DevCount++ == DevNum){		
					manager.requestPermission(u, PendingIntent.getBroadcast(BA.applicationContext, 0, new Intent(
							"com.android.example.USB_PERMISSION"), 0));
					return;
				}
			}
		}
		UsbAccessory[] a = manager.getAccessoryList();
		if (a != null)
		{
			manager.requestPermission(a[0], PendingIntent.getBroadcast(BA.applicationContext, 0, new Intent(
					"com.android.example.USB_PERMISSION"), 0));
		}
	}

	/**
	 *Sets the parameters of the serial port.
	 *This must be called after the permission is obtained and the port has been opened.
	 *This method is ignored if the connected device is an Accessory which doesn't need these parameters.
	 *
	 *baudRate baud rate is an integer, for example 115200.
	 *dataBits is one of DATABITS_5, DATABITS_6, DATABITS_7, or DATABITS_8.
	 *stopBits is one of STOPBITS_1, STOPBITS_1_5, or STOPBITS_2.
	 *parity is one of PARITY_NONE, PARITY_ODD, PARITY_EVEN, PARITY_MARK, or PARITY_SPACE.
	 */
	public void SetParameters(int baudRate, int dataBits, int stopBits, int parity) throws Exception
	{
		if (driverOpen)
			driver.getPorts().get(0).setParameters(baudRate, dataBits, stopBits, parity);
	}
	
	public final int STOPBITS_1 = UsbSerialPort.STOPBITS_1;
	public final int STOPBITS_1_5 = UsbSerialPort.STOPBITS_1_5;
	public final int STOPBITS_2 = UsbSerialPort.STOPBITS_2;
	
	public final int DATABITS_5 = UsbSerialPort.DATABITS_5;
	public final int DATABITS_6 = UsbSerialPort.DATABITS_6;
	public final int DATABITS_7 = UsbSerialPort.DATABITS_7;
	public final int DATABITS_8 = UsbSerialPort.DATABITS_8;
	
	public final int PARITY_EVEN = UsbSerialPort.PARITY_EVEN;
	public final int PARITY_MARK = UsbSerialPort.PARITY_MARK;
	public final int PARITY_NONE = UsbSerialPort.PARITY_NONE;
	public final int PARITY_ODD = UsbSerialPort.PARITY_ODD;
	public final int PARITY_SPACE = UsbSerialPort.PARITY_SPACE;
	
	public final int USB_NONE = 0;
	public final int USB_DEVICE = 1;
	public final int USB_ACCESSORY = 2;
	
	
	/**
	 *Checks is there is a supported device or Accessory connected to the USB port.
	 *Returns USB_DEVICE if a supported slave device is present
	 *Returns USB_ACCESSORY if an Accessory that supports is present
	 *Returns USB_NONE if neither a recognised device nor an accessory was found.
	 *
	 *ADDED: Ver_2.4 DevNum = 1 to n
	 *MODDED: Ver_2.4 Code changed to support DevNum
	 */
	public int UsbPresent(int DevNum)
	{		
		UsbManager manager = (UsbManager) BA.applicationContext.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> h = manager.getDeviceList();
		if (h.size() > 0)
		{	
			if(h.size() >= DevNum){		
				return USB_DEVICE;
			}			
		}
		UsbAccessory[] a = manager.getAccessoryList();
		if (a != null)
		{
			return USB_ACCESSORY;
		}
		return USB_NONE;

	}
	
	/**
	 *Returns a multi-line string containing the details for the connected device.
	 *You need to have obtained permission before calling this method. 
	 *
	 *ADDED: Ver_2.4 DevNum = 1 to n
	 *MODDED: Ver_2.4 Code changed to support DevNum
	 */
	public String DeviceInfo(int DevNum) throws Exception
	{
		int DevCount = 1;	
		StringBuilder sb = new StringBuilder();
		byte[] rawdescs;
		UsbManager manager = (UsbManager) BA.applicationContext.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> h = manager.getDeviceList();
		if (h.size() == 0)
			return "No device found. Is an Accessory connected?";
		UsbDevice usb = null;
		UsbDeviceConnection conn;
		UsbInterface iface;
		for (UsbDevice u : h.values())
		{
			if(DevCount++ == DevNum){		
				usb = u;
				break;
			}
		}
		iface = usb.getInterface(0);
		conn = manager.openDevice(usb);		
		conn.claimInterface(iface, true);
		rawdescs = conn.getRawDescriptors();
				
		int idx = rawdescs[14];	// Manufacturer string index at byte 14
		byte[] buffer = new byte[rawdescs[7]]; // maximum packet size
		conn.controlTransfer(128, 6, 0x0300+idx, 0, buffer, buffer.length, 100);
		sb.append("Manufacturer : ").append(bytes0ToString(buffer, idx)).append('\n');
		
		idx = rawdescs[15];	// Product string index at byte 15
		buffer = new byte[rawdescs[7]]; // maximum packet size
		conn.controlTransfer(128, 6, 0x0300+idx, 0, buffer, buffer.length, 100);
		sb.append("Product : ").append(bytes0ToString(buffer, idx)).append('\n');
		
		idx = rawdescs[16];	// Serial string index at byte 16
		buffer = new byte[rawdescs[7]]; // maximum packet size
		conn.controlTransfer(128, 6, 0x0300+idx, 0, buffer, buffer.length, 100);
		sb.append("Serial : ").append(bytes0ToString(buffer, idx)).append('\n');
		
		sb.append('\n');
		sb.append("DeviceName : ").append(usb.getDeviceName()).append('\n');
		sb.append("DeviceClass : ").append(usbClass(usb.getDeviceClass())).append('\n');
		sb.append("DeviceSubClass : ").append(usb.getDeviceSubclass()).append('\n');
		sb.append("Device ID : ").append(toHex(usb.getDeviceId())).append('\n');
		sb.append("ProductId : ").append(toHex(usb.getProductId())).append('\n');
		sb.append("VendorId : ").append(toHex(usb.getVendorId())).append('\n');
		sb.append('\n');
		for (int i = 0; i < usb.getInterfaceCount(); i++)
		{
			UsbInterface ifce = usb.getInterface(i);
			sb.append("  B4aInterfaceNumber : ").append(i).append('\n');
			sb.append("  InterfaceClass : ").append(usbClass(ifce.getInterfaceClass())).append('\n');
			sb.append("  InterfaceSubClass : ").append(ifce.getInterfaceSubclass()).append('\n');
			sb.append("  InterfaceProtocol : ").append(ifce.getInterfaceProtocol()).append('\n');
			sb.append('\n');
			UsbEndpoint endpoint;
			for (int j = 0; j < ifce.getEndpointCount(); j++)
			{
				endpoint = ifce.getEndpoint(j);
				sb.append("    EndpointNumber : ").append(endpoint.getEndpointNumber()).append('\n');
				sb.append("    EndpointDirection : ").append(usbDirection(endpoint.getDirection())).append('\n');
				sb.append("    EndpointType : ").append(usbEndpoint(endpoint.getType())).append('\n');
				sb.append("    EndpointAttribute : ").append(endpoint.getAttributes()).append('\n');
				sb.append("    EndpointInterval : ").append(endpoint.getInterval()).append('\n');
				sb.append("    EndpointMaxPacketSize : ").append(endpoint.getMaxPacketSize()).append('\n');
				sb.append('\n');
			}
		}		
		conn.close();
		return sb.toString();
	}
	

	/**
	 *This is exposed for dignostic purpose. It sets the timeout that USB reads and writes wait.
	 *The default value of 200mS will probably not need to be changed in normal use.
	 *If you seem to be losing data at high baud rates try reducing or increasing this value
	 */
	public int getUsbTimeout() 
	{ return TIMEOUT; }
	public void setUsbTimeout(int mSecs)
	{ TIMEOUT = mSecs; }
	
	
	public final int DRIVER_PROLIFIC = UsbId.PROLIFIC_PL2303;
	public final int DRIVER_SILABS = UsbId.SILABS_CP2102;
	public final int DRIVER_CDCACM = UsbId.ST_CDC;
	public final int DRIVER_FTDI = UsbId.FTDI_FT2232H;
	
	/**
	 *If a device might be supported by an existing driver in this library but is not recognised
	 *then it can be added by this method.
	 *
	 *The driverID parameter can be one of
	 *
	 *DRIVER_PROLIFIC for a device that is compatible with the Prolific PL2303.
	 *DRIVER_SILABS for a device that is compatible with the Silicon Labs CP2102
	 *DRIVER_CDCACM for a device that is compatible with the CDC ACM model.
	 *DRIVER_FTDI for a device that is compatible with the FTDI FT232.
	 *
	 *DRIVER_NONE can be used in the unlikely event of needing to unrecongise a device
	 *
	 *The vendorID and productID parameters are those of the device in question.
	 */
	public void SetCustomDevice(int driverID, int vendorID, int productID)
	{
		UsbId.DRIVER_CUSTOM = driverID;
		UsbId.PRODUCT_CUSTOM = productID;
		UsbId.VENDOR_CUSTOM = vendorID;		
	}
	
	
	
	// Private methods
	
  	private String bytes0ToString(byte[] data, int idx) throws Exception
	{
		String str= "";
		if (idx == 0 || data[0] == 0)  // index is 0 or zero length descriptor, should be byte count
			return "not available";
		if (data[1] != 3)
			return "bad descriptor type " + (((int)data[1]) & 0xff);
		str = new String(data, 0, data.length, "UTF-16LE");		
		return str.substring(1);
	}
	
	private String toHex(int number)
	{
		return "0x" + Integer.toHexString(number).toUpperCase();
	}
	
	private String usbDirection(int direction)
	{
		if (direction == 0)
			return "out";
		return "In";
	}
	
	private String usbEndpoint(int eptype)
	{
		switch(eptype)
		{
			case 0: return "USB_ENDPOINT_XFER_CONTROL (control)";
			case 1: return "USB_ENDPOINT_XFER_ISOC (isochronous )";
			case 2: return "USB_ENDPOINT_XFER_BULK (bulk)";
			case 3: return "USB_ENDPOINT_XFER_INT (interrupt)";
			default: return "Unknown end point type " +  eptype;
		}
	}

	private String usbClass(int usbclass)
	{
		switch(usbclass)
		{
			case 0: return "USB_CLASS_PER_INTERFACE (per-interface basis)";
			case 1: return "USB_CLASS_AUDIO (audio)";
			case 2: return "USB_CLASS_COM (communication device)";
			case 3: return "USB_CLASS_COMM (human interface";
			case 6: return "USB_CLASS_STILL_IMAGE (camera)";
			case 5: return "USB_CLASS_PHYSICA (physical)";
			case 7: return "USB_CLASS_PRINTER (printer)";
			case 8: return "USB_CLASS_MASS_STORAGE (mass storage)";
			case 9: return "USB_CLASS_HUB (hub)";
			case 10: return "USB_CLASS_CDC_DATA(CDC device)";
			case 11: return "USB_CLASS_CSCID (smart card)";
			case 13: return "USB_CLASS_CONTENT_SEC (content security)";
			case 14: return "USB_CLASS_VIDEO (video)";
			case 224: return "USB_CLASS_WIRELESS_CONTROLLER (wireless controller)";
			case 239: return "USB_CLASS_MISC (miscellaneous";
			case 254: return "USB_CLASS_APP_SPEC (application specific)";
			case 255: return "USB_CLASS_VENDOR_SPEC (vendor specific)";
			default: return "Unknown class " + usbclass;
		}
	}
	

	
}
