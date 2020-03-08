
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

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.usb.UsbManagerWrapper.UsbEndpointWrapper;

/**
 *Represents a connection between the host and a client.
 *UsbDeviceConnection is created by calling UsbManager.OpenDevice.
 *Once connected you should call StartListening to start listening for completed requests.
 *Sending requests is done with UsbRequest.Queue.
 *The NewData event is raised when a request completes. The request is passed as a parameter.
 *You should call ContinueListening to allow the listener to listen to the next completed request (after another IN request is sent).
 *Calling StopListening will close the connection.
 *ControlTransfer method sends requests to endpoint zero which is the control endpoint.
 *ControlTransfer is a blocking method (unlike UsbRequest.Queue which is asynchronous).
 */
@Events(values={"NewData (Request As UsbRequest, InDirection As Boolean)"})
@ShortName("UsbDeviceConnection")
public class UsbDeviceConnectionWrapper {
	UsbDeviceConnection connection;
	UsbInterface usbInterface;
	private BA ba;
	private String eventName;
	private Thread readerThread;
	volatile boolean working;
	boolean waitingBeforeContinue;

	/**
	 * Tests whether the object was initialized.
	 */
	public boolean IsInitialized() {
		return connection != null;
	}
	/**
	 * Starts listening for completed requests. When such are available the NewData event will be raised.
	 *EventName - The name of the sub that will handle the events.
	 */
	public void StartListening(BA ba, String EventName) {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		readerThread = new Thread(new Reader());
		readerThread.setDaemon(true);
		working = true;
		readerThread.start();
	}
	/**
	 * Notifies the listener to continue listening for completed requests.
	 */
	public void ContinueListening() {
		if (connection == null || readerThread == null)
			return;
		synchronized (readerThread) {
			waitingBeforeContinue = false;
			readerThread.notify();
		}
	}
	/**
	 * Stops listening to requests and closes the connection.
	 */
	public void StopListening() {
		if (connection == null)
			return;
		working = false;
		if (readerThread != null && readerThread.isAlive()) {
			readerThread.interrupt();
		}
		readerThread = null;
		connection.releaseInterface(usbInterface);
		usbInterface = null;
		connection.close();
		connection = null;
	}
	
	/**
	 * Performs a control transaction on endpoint zero. Returns the number of bytes transferred.
	 *RequestType - The request type. It should combine USB_DIR_IN or USB_DIR_OUT to set the request direction.
	 *Request - Request Id.
	 *Value - Value field.
	 *Index - Index field.
	 *Buffer - Buffer for data portion. Pass Null if not needed.
	 *Length - The length of the data to send or receive.
	 *Timeout - Timeout in milliseconds.
	 */
	public int ControlTransfer(int RequestType,int Request,int Value,int Index,byte[] Buffer,int Length,int Timeout) {
		return connection.controlTransfer(RequestType, Request, Value, Index, Buffer, Length, Timeout);
	}
	/**
	 * Returns the raw descriptors as an array of bytes.
	 *<b>This method is only available in Android 3.2 or above.</b>It will return an empty array in Android 3.1.
	 */
	public byte[] GetRawDescriptors() {
		try {
			Method m = connection.getClass().getMethod("getRawDescriptors", (Class[])null);
			return (byte[]) m.invoke(connection, (Object[])null);
		} catch (Exception e) {
			return new byte[0];
		}
	}
	/**
	 * Sends a synchronous request.
	 *Endpoint - The endpoint for this transaction. The transfer direction is determined by this endpoint.
	 *Buffer - Buffer for data to send or receive.
	 *Length - The length of the data.
	 *Timeout - Request timeout in milliseconds.
	 */
	public int BulkTransfer (UsbEndpoint Endpoint, byte[] Buffer, int Length, int Timeout) {
		return connection.bulkTransfer(Endpoint, Buffer, Length, Timeout);
	}
	/**
	 * Closes the connection. StopListening also closes the connection.
	 *This method should only be used when the asynchronous listener was not started.
	 */
	public void CloseSynchronous() {
		if (connection == null)
			return;
		connection.close();
		connection = null;
	}
	/**
	 * Returns the connected device serial number.
	 */
	public String getSerial() {
		return connection.getSerial();
	}

	private class Reader implements Runnable {
		@Override
		public void run() {
			while (working) {

				UsbRequest req = null;
				try {
					req = connection.requestWait();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				if (req == null)
					continue;
				boolean in = req.getEndpoint().getDirection() == UsbConstants.USB_DIR_IN;
				waitingBeforeContinue = true;
				ba.raiseEventFromDifferentThread(UsbDeviceConnectionWrapper.this, null, 0, eventName + "_newdata", false, new Object[] {
						AbsObjectWrapper.ConvertToWrapper(new UsbRequestWrapper(), req), in});
				if (working == false)
					break;
				synchronized (readerThread) {
					while (working && waitingBeforeContinue) {
						try {
							readerThread.wait();
						} catch (InterruptedException e) {
							working = false;
						}
					}
				}
			}
			Common.Log("Reader quitting");
		}

	}
	/**
	 * This object represents a USB request packet.
	 * Queue method sends the request.
	 */
	@ShortName("UsbRequest")
	public static class UsbRequestWrapper extends AbsObjectWrapper<UsbRequest>{

		/**
		 * Initializes the request. The request will be binded to the given connection and endpoint.
		 *Note that for control transactions you should use UsbDeviceConnection.ControlTransfer.
		 */
		public void Initialize(UsbDeviceConnectionWrapper Connection, UsbEndpoint Endpoint) {
			UsbRequest ur = new UsbRequest();
			if (!ur.initialize(Connection.connection, Endpoint))
				throw new RuntimeException("Error initializing UsbRequest");
			ur.setClientData(new Object[] {"N/A", new byte[0]});
			setObject(ur);
		}
		/**
		 * Gets or sets an arbitrary string that can be used to identify the request.
		 */
		public String getName() {
			return (String)((Object[])getObject().getClientData())[0];
		}
		public void setName(String n) {
			((Object[])getObject().getClientData())[0] = n; 
		}
		/**
		 * Queues the request for sending. UsbDeviceConnection_NewData event will be raised when the transaction completes.
		 */
		public void Queue(byte[] Buffer, int Length) {
			((Object[])getObject().getClientData())[1] = Buffer;
			if (getObject().queue(ByteBuffer.wrap(Buffer), Length) == false)
				throw new RuntimeException("Error queuing request");
		}
		/**
		 * Returns the buffer associated with the request.
		 */
		public byte[] getBuffer() {
			return (byte[]) ((Object[])getObject().getClientData())[1];
		}
		public UsbEndpointWrapper getUsbEndpoint() {
			return (UsbEndpointWrapper)AbsObjectWrapper.ConvertToWrapper(new UsbEndpointWrapper(), getObject().getEndpoint());
		}
		@Hide
		@Override
		public String toString() {
			if (!IsInitialized())
				return super.toString();
			else
				return "Name=" + getName() + ", Buffer=" + Arrays.toString(getBuffer());
		}
	}

}
