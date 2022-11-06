
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4a.objects.usb;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
import anywheresoftware.b4a.objects.streams.File.OutputStreamWrapper;

/**
 * UsbManager gives access to the connected Usb devices. It also holds the related constants.
 *This library requires Android SDK 12 or above (Android 3.1 or above).
 *You should configure Basic4android to use android.jar from android-12 or above.
 */
@Version(1.01f)
@ShortName("UsbManager")
public class UsbManagerWrapper {
	private UsbManager manager;
	public static final int USB_CLASS_APP_SPEC = 254;
	public static final int USB_CLASS_AUDIO = 1;
	public static final int USB_CLASS_CDC_DATA = 10;
	public static final int USB_CLASS_COMM = 2;
	public static final int USB_CLASS_CONTENT_SEC = 13;
	public static final int USB_CLASS_CSCID = 11;
	public static final int USB_CLASS_HID = 3;
	public static final int USB_CLASS_HUB = 9;
	public static final int USB_CLASS_MASS_STORAGE = 8;
	public static final int USB_CLASS_MISC = 239;
	public static final int USB_CLASS_PER_INTERFACE = 0;
	public static final int USB_CLASS_PHYSICA = 5;
	public static final int USB_CLASS_PRINTER = 7;
	public static final int USB_CLASS_STILL_IMAGE = 6;
	public static final int USB_CLASS_VENDOR_SPEC = 255;
	public static final int USB_CLASS_VIDEO = 14;
	public static final int USB_CLASS_WIRELESS_CONTROLLER = 224;
	public static final int USB_DIR_IN = 128;
	public static final int USB_DIR_OUT = 0;
	public static final int USB_ENDPOINT_DIR_MASK = 128;
	public static final int USB_ENDPOINT_NUMBER_MASK = 15;
	public static final int USB_ENDPOINT_XFERTYPE_MASK = 3;
	public static final int USB_ENDPOINT_XFER_BULK = 2;
	public static final int USB_ENDPOINT_XFER_CONTROL = 0;
	public static final int USB_ENDPOINT_XFER_INT = 3;
	public static final int USB_ENDPOINT_XFER_ISOC = 1;
	public static final int USB_INTERFACE_SUBCLASS_BOOT = 1;
	public static final int USB_SUBCLASS_VENDOR_SPEC = 255;
	public static final int USB_TYPE_CLASS = 32;
	public static final int USB_TYPE_MASK = 96;
	public static final int USB_TYPE_RESERVED = 96;
	public static final int USB_TYPE_STANDARD = 0;
	public static final int USB_TYPE_VENDOR = 64;
	
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	/**
	 * A complete working example with a tutorial is available <link>here|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/11289-android-usb-host-tutorial-adbtest.html</link>.
	 */
	public static void LIBRARY_DOC() {
		//
	}
	/**
	 * Initializes the object.
	 */
	public void Initialize() {
		manager = (UsbManager) BA.applicationContext.getSystemService(Context.USB_SERVICE);
	}
	/**
	 * Returns an array of UsbAccessories with all the connected USB accessories.
	 */
	public UsbAccessoryWrapper[] GetAccessories() {
		UsbAccessory[] h = manager.getAccessoryList();
		if (h == null)
			return new UsbAccessoryWrapper[0];
		UsbAccessoryWrapper[] uw = new UsbAccessoryWrapper[h.length];
		int i = 0;
		for (UsbAccessory u : h) {
			uw[i] = new UsbAccessoryWrapper();
			uw[i].accessory = u;
			i++;
		}
		return uw;
	}
	/**
	 * Returns an array of UsbDevices with all the connected USB devices.
	 */
	public UsbDeviceWrapper[] GetDevices() {
		HashMap<String, UsbDevice> h = manager.getDeviceList();
		UsbDeviceWrapper[] uw = new UsbDeviceWrapper[h.size()];
		int i = 0;
		for (UsbDevice u : h.values()) {
			uw[i] = new UsbDeviceWrapper();
			uw[i].setObject(u);
			i++;
		}
		return uw;
	}
	/**
	 * Connects to the given accessory
	 */
	public void OpenAccessory(UsbAccessoryWrapper Accessory) {
		Accessory.pfd = manager.openAccessory(Accessory.accessory);
	}
	/**
	 * Connects to the given device and claims exclusive access to the given interface.
	 *ForceClaim - Whether the system should disconnect kernel drivers if necessary.
	 */
	public UsbDeviceConnectionWrapper OpenDevice(UsbDevice Device, UsbInterface Interface, boolean ForceClaim) {
		UsbDeviceConnection u = manager.openDevice(Device);
		u.claimInterface(Interface, ForceClaim);
		UsbDeviceConnectionWrapper uu = new UsbDeviceConnectionWrapper();
		uu.connection = u;
		uu.usbInterface = Interface;
		return uu;
	}
	private int getIntentFlags() {
		int flags = 0;
		if (Build.VERSION.SDK_INT >= 31)
			flags |= 67108864 ; //FLAG_IMMUTABLE
		return flags;
	}
	/**
	 * Shows a dialog that asks the user to allow your application to access the USB accessory.
	 */
	public void RequestAccessoryPermission(UsbAccessoryWrapper Accessory) {
		
		manager.requestPermission(Accessory.accessory, PendingIntent.getBroadcast(BA.applicationContext, 0, new Intent(ACTION_USB_PERMISSION), getIntentFlags()));
	}
	/**
	 * Shows a dialog that asks the user to allow your application to access the USB device.
	 */
	public void RequestPermission(UsbDevice Device) {
		
		manager.requestPermission(Device, PendingIntent.getBroadcast(BA.applicationContext, 0, new Intent(ACTION_USB_PERMISSION), getIntentFlags()));
	}
	/**
	 * Tests whether your application has permission to access this accessory.
	 *Call RequestAccessoryPermission to receive such permission.
	 */
	public boolean HasAccessoryPermission(UsbAccessoryWrapper Accessory) {
		return manager.hasPermission(Accessory.accessory);
	}
	/**
	 * Tests whether your application has permission to access this device.
	 *Call RequestPermission to receive such permission.
	 */
	public boolean HasPermission(UsbDevice Device) {
		return manager.hasPermission(Device);
	}

	/**
	 * Represents a Usb device.
	 */
	@ShortName("UsbDevice")
	public static class UsbDeviceWrapper extends AbsObjectWrapper<UsbDevice> {
		/**
		 * Gets the device name.
		 */
		public String getDeviceName() {
			return getObject().getDeviceName();
		}
		/**
		 * Gets the device id.
		 */
		public int getDeviceId() {
			return getObject().getDeviceId();
		}
		/**
		 * Gets the device class.
		 */
		public int getDeviceClass() {
			return getObject().getDeviceClass();
		}
		/**
		 * Gets the device subclass.
		 */
		public int getDeviceSubclass() {
			return getObject().getDeviceSubclass();
		}
		/**
		 * Gets the product id.
		 */
		public int getProductId() {
			return getObject().getProductId();
		}
		/**
		 * Gets the vendor id.
		 */
		public int getVendorId() {
			return getObject().getVendorId();
		}
		/**
		 * Gets the number of interfaces.
		 */
		public int getInterfaceCount() {
			return getObject().getInterfaceCount();
		}
		/**
		 * Gets the interface at the given index.
		 */
		public UsbInterface GetInterface(int Index) {
			return getObject().getInterface(Index);
		}
	}
	/**
	 * Represents a Usb interface on a specific device.
	 */
	@ShortName("UsbInterface")
	public static class UsbInterfaceWrapper extends AbsObjectWrapper<UsbInterface> {
		/**
		 * Gets the interface class.
		 */
		public int getInterfaceClass() {
			return getObject().getInterfaceClass();
		}
		/**
		 * Gets the interface protocol.
		 */
		public int getInterfaceProtocol() {
			return getObject().getInterfaceProtocol();
		}
		/**
		 * Gets the interface subclass.
		 */
		public int getInterfaceSubclass() {
			return getObject().getInterfaceSubclass();
		}
		/**
		 * Gets the number of endpoints available in this interface.
		 */
		public int getEndpointCount() {
			return getObject().getEndpointCount();
		}
		/**
		 * Gets the endpoint at the given index.
		 */
		public UsbEndpointWrapper GetEndpoint(int Index) {
			return (UsbEndpointWrapper) AbsObjectWrapper.ConvertToWrapper(new UsbEndpointWrapper(),getObject().getEndpoint(Index));
		}
	}
	/**
	 * Represents an endpoint in a specific interface.
	 */
	@ShortName("UsbEndpoint")
	public static class UsbEndpointWrapper extends AbsObjectWrapper<UsbEndpoint> {
		/**
		 * Gets the endpoint address.
		 */
		public int getAddress() {
			return getObject().getAddress();
		}
		/**
		 * Gets the endpoint attributes.
		 */
		public int getAttributes() {
			return getObject().getAttributes();
		}
		/**
		 * Gets the endpoint direction. Can be UsbManager.USB_DIR_IN or UsbManager.USB_DIR_OUT.
		 */
		public int getDirection() {
			return getObject().getDirection();
		}
		/**
		 * Gets the maximum packet size.
		 */
		public int getMaxPacketSize() {
			return getObject().getMaxPacketSize();
		}
		/**
		 * Gets the interval field.
		 */
		public int getInterval() {
			return getObject().getInterval();
		}
		/**
		 * Gets the endpoint number.
		 */
		public int getEndpointNumber() {
			return getObject().getEndpointNumber();
		}
		/**
		 * Gets the endpoint type.
		 */
		public int getType() {
			return getObject().getType();
		}
	}
	/**
	 * Represents a Usb accessory.
	 */
	@ShortName("UsbAccessory")
	public static class UsbAccessoryWrapper {
		UsbAccessory accessory;
		private ParcelFileDescriptor pfd;
		
		/**
		 *Closes the accessory.
		 *The accessory input and output streams should be individually closed first. 
		 */
		public void Close() throws Exception {
			pfd.close();
		}		
		/**
		 *Gets the input stream for the accessory.
		 *When reading data from an accessory ensure that the buffer that you use is
		 *big enough to store the USB packet data. The Android accessory protocol supports
		 *packet buffers up to 16384 bytes, so you can choose to always declare your buffer
		 *to be of this size for simplicity.
		 */
		public InputStreamWrapper getInputStream()
		{
			FileInputStream in = new FileInputStream(pfd.getFileDescriptor());
	        InputStreamWrapper i = new InputStreamWrapper();
	        i.setObject(in);
        	return i;
		}
		/**
		 * Gets the output stream for the accessory.
		 */
		public OutputStreamWrapper getOutputStream()
		{
			FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());			
	        OutputStreamWrapper o = new OutputStreamWrapper();
	        o.setObject(out);
	        return o;
		}			
		/**
		 * Gets the description of the accessory.
		 */
		public String getDescription() {
			return accessory.getDescription();
		}
		/**
		 * Gets the manufacturer of the accessory.
		 */
		public String getManufacturer() {
			return accessory.getManufacturer();
		}
		/**
		 * Gets the model name of the accessory.
		 */
		public String getModel() {
			return accessory.getModel();
		}
		/**
		 * Gets the unique serial number for the accessory.
		 */
		public String getSerial() {
			return accessory.getSerial();
		}
		/**
		 * Gets the URI for the website of the accessory.
		 */
		public String getUri() {
			return accessory.getUri();
		}
		/**
		 * Gets the version of the accessory.
		 */
		public String getVersion() {
			return accessory.getVersion();
		}
	}

}
