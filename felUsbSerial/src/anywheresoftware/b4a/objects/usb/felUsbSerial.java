
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

import java.lang.reflect.InvocationTargetException;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface.UsbReadCallback;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.usb.UsbManagerWrapper.UsbDeviceWrapper;


@ShortName("felUsbSerial")
@Events(values={"DataAvailable (Buffer() As Byte)"})
@Version(1.12f)
public class felUsbSerial extends AbsObjectWrapper<UsbSerialDevice>{
	@Hide
	public UsbManager usbManager;
	public static final int DATA_BITS_5 = 5;
	public static final int DATA_BITS_6 = 6;
	public static final int DATA_BITS_7 = 7;
	public static final int DATA_BITS_8 = 8;
	
	public static final int STOP_BITS_1 = 1;
	public static final int STOP_BITS_15 = 3;
	public static final int STOP_BITS_2 = 2;
	
	public static final int PARITY_NONE = 0;
	public static final int PARITY_ODD = 1;
	public static final int PARITY_EVEN = 2;
	public static final int PARITY_MARK = 3;
	public static final int PARITY_SPACE = 4;
	
	public static final int FLOW_CONTROL_OFF = 0;
	public static final int FLOW_CONTROL_RTS_CTS= 1;
	public static final int FLOW_CONTROL_DSR_DTR = 2;
	public static final int FLOW_CONTROL_XON_XOFF = 3;
	/**
	 * Internal read buffer size. Default value is 16 * 1024. Changes should be done before calling Initialize.
	 */
	public static int BUFFER_READ_SIZE = 16 * 1024;
	/**
	 * Internal write buffer size. Default value is 16 * 1024. Changes should be done before calling Initialize.
	 */
    public static int BUFFER_WRITE_SIZE = 16 * 1024;
	private String eventName;
	/**
	 * Initializes and opens the usb device.
	 *EventName - Sets the sub that will handle the DataAvailable event.
	 *Device - The UsbDevice previously found with UsbManager.
	 *InterfaceIndex - The interface index. Pass -1 to choose automatically.
	 */
	public void Initialize(String EventName, UsbDevice Device, int InterfaceIndex) throws Exception{
		Initialize2(EventName, Device, InterfaceIndex, null);
	}
	/**
	 * Similar to Initialize. This method lets you explicitly choose the serial class.
	 *ClassName - One of the following strings: "CDCSerialDevice", "CH34xSerialDevice", "CP2102SerialDevice",
	 *	"FTDISerialDevice", "PL2303SerialDevice". 
	 */
	public void Initialize2(String EventName, UsbDevice Device, int InterfaceIndex, String ClassName) throws Exception {
		usbManager = (UsbManager) BA.applicationContext.getSystemService(Context.USB_SERVICE);
		eventName = EventName.toLowerCase(BA.cul);
		UsbDeviceConnection connection = usbManager.openDevice(Device);
		if (ClassName == null)
			setObject(UsbSerialDevice.createUsbSerialDevice(Device, connection, InterfaceIndex));
		else {
			setObject((UsbSerialDevice) Class.forName("com.felhr.usbserial." + ClassName).getConstructor(UsbDevice.class, UsbDeviceConnection.class, int.class).newInstance(Device, connection, InterfaceIndex));
		}
		if (getObjectOrNull() == null)
			throw new RuntimeException("UsbDevice is not supported.");
		getObject().debug(true);
		if (!getObject().open()) {
			throw new RuntimeException("Error opening serial device.");
		}
	}
	public void setBaudRate(int rate) {
		getObject().setBaudRate(rate);
	}
	/**
	 * One of the DATA_BITS constants.
	 */
	public void setDataBits(int i) {
		getObject().setDataBits(i);
	}
	/**
	 * One of the STOP_BITS constants.
	 */
	public void setStopBits(int i) {
		getObject().setStopBits(i);
	}
	/**
	 * One of the PARITY constants.
	 */
	public void setParity(int i) {
		getObject().setParity(i);
	}
	/**
	 * One of the FLOW_CONTROL constants.
	 *Note that this method is only supported with CP2102 and FTDI chips.
	 */
	public void setFlowControl(int i) {
		getObject().setFlowControl(i);
	}
	/**
	 * Whether to print debug information (default is true).
	 */
	public void setDebugMode(boolean b) {
		getObject().debug(b);
	}
	/**
	 * Starts listening for incoming data. The DataAvailable event will be raised whenever new data is available.
	 */
	public void StartReading(final BA ba) {
		final Object sender = getObject();
		getObject().read(new UsbReadCallback() {

			@Override
			public void onReceivedData(byte[] data) {
				ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_dataavailable", true, new Object[] {data});
			}
			
		});
	}
	public void Write(byte[] Data) {
		getObject().write(Data);
	}
	public void Close() {
		if (IsInitialized())
			getObject().close();
	}
}
