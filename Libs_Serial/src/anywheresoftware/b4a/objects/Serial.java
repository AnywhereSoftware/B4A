
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
 
 package anywheresoftware.b4a.objects;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.Map;

/**
 * The Serial library allows you to connect with other Bluetooth devices using RFCOMM, also named virtual serial port.
 *<b>This library requires Android 2.0 (API level 5) or above</b>.
 *The Serial object should be declared as a process global object.
 *After initializing the object you can connect to other devices by calling Connect with the target device MAC address.
 *This can be done by first getting the paired devices map. This map contains the friendly name and address of each paired device.
 *To allow other devices to connect to your device you should call Listen. When a connection is established the Connected event will be raised.
 *There is no problem with both listening to connections and trying to connect to a different device (this allows you to use the same application on two devices without defining a server and client).
 *A Serial object can handle a single connection. If a new connection is established, it will replace the previous one.
 *See this <link>tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6908-android-serial-tutorial.html</link> for more information.
 */
@Version(1.31f)
@ShortName("Serial")
@Permissions(values={"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"})
@Events(values={"Connected (Success As Boolean)"})
public class Serial implements CheckForReinitialize{
	private BluetoothAdapter blueAdapter;
	private String eventName;
	private BluetoothSocket socketClientConnecting;
	private volatile BluetoothSocket workingSocket;
	private volatile BluetoothServerSocket serverSocket;
	/**
	 * Initialized the object. You may want to call IsEnabled before trying to work with the object.
	 */
	public void Initialize(String EventName) {
		this.eventName = EventName.toLowerCase(BA.cul);
		blueAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	public boolean IsInitialized() {
		return blueAdapter != null;
	}
	/**
	 * Tests whether the Bluetooth is enabled.
	 */
	public boolean IsEnabled() {
		if (blueAdapter == null)
			return false;
		return blueAdapter.isEnabled();
	}
	/**
	 * Returns the current device MAC address.
	 */
	public String getAddress() {
		return blueAdapter.getAddress();
	}
	/**
	 * Returns the current device friendly name.
	 */
	public String getName() {
		return blueAdapter.getName();
	}
	/**
	 * Returns a map with the paired devices friendly names as keys and addresses as values.
	 *The following code shows a list of available devices and allows the user to connect to one:<code>
	 *Dim PairedDevices As Map
	 *PairedDevices = Serial1.GetPairedDevices
	 *Dim l As List
	 *l.Initialize
	 *For i = 0 To PairedDevices.Size - 1
	 *	l.Add(PairedDevices.GetKeyAt(i))
	 *Next
	 *Dim res As Int
	 *res = InputList(l, "Choose device", -1) 'show list with paired devices
	 *If res <> DialogResponse.CANCEL Then
	 *	Serial1.Connect(PairedDevices.Get(l.Get(res))) 'convert the name to mac address and connect
	 *End If</code>
	 */
	public Map GetPairedDevices() {
		Map m = new Map();
		m.Initialize();
		Set<BluetoothDevice> s = blueAdapter.getBondedDevices();
		for (BluetoothDevice b : s) {
			if (m.ContainsKey(b.getName())) {
				m.Put(b.getName() + " (" + b.getAddress() + ")", b.getAddress());
			}
			else {
				m.Put(b.getName(), b.getAddress());
			}
		}
		return m;
	}
	/**
	 * Tries to connect to a device with the given address. The connection is done in the background.
	 *The Connected event will be raised when the connection is ready (or fails).
	 *The UUID used for the connection is the default UUID: 00001101-0000-1000-8000-00805F9B34FB.
	 */
	public void Connect(final BA ba, String MacAddress) throws IOException {
		Connect2(ba, MacAddress, "00001101-0000-1000-8000-00805F9B34FB");
	}
	/**
	 * This method is a workaround for hardware devices that do not connect with Connect or Connect2.
	 *See this <link>issue|http://code.google.com/p/android/issues/detail?id=5427</link> for more information.
	 */
	public void Connect3(final BA ba, String MacAddress, int Port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		BluetoothDevice bd = blueAdapter.getRemoteDevice(MacAddress);
		Method m = BluetoothDevice.class.getMethod("createRfcommSocket", new Class[] { int.class });
		socketClientConnecting = (BluetoothSocket)m.invoke(bd, Port);
		afterConnection(ba);
	}
	/**
	 * Tries to connect to a device over an unencrypted connection.
	 *Admin - Object of type BluetoothAdmin.
	 *MacAddress - The address of the remote device.
	 *Port - RCOMM channel.
	 */
	public void ConnectInsecure(final BA ba, BluetoothAdmin Admin, String MacAddress, int Port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		BluetoothDevice bd = blueAdapter.getRemoteDevice(MacAddress);
		Method m = BluetoothDevice.class.getMethod("createInsecureRfcommSocket", int.class);
		socketClientConnecting = (BluetoothSocket)m.invoke(bd, Port);
		afterConnection(ba);
	}
	/**
	 * Tries to connect to a device with the given address and UUID. The connection is done in the background.
	 *The Connected event will be raised when the connection is ready (or fails).
	 */
	public void Connect2(final BA ba, String MacAddress, String UUID) throws IOException {
		BluetoothDevice bd = blueAdapter.getRemoteDevice(MacAddress);
		socketClientConnecting = bd.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
		afterConnection(ba);

	}
	private void afterConnection(final BA ba) {
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				try {
					socketClientConnecting.connect();
					synchronized (this) {
						workingSocket = socketClientConnecting;
						socketClientConnecting = null;

					}
					ba.raiseEventFromDifferentThread(Serial.this, Serial.this, 0, eventName + "_connected", false, new Object[] {true});
				} catch (Exception e) {
					if (workingSocket == null) {
						e.printStackTrace();
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(Serial.this, Serial.this, 0, eventName + "_connected", false, new Object[] {false});
					}
				}
			}

		}, this, 0);
	}
	/**
	 * Disconnects the connection (if such exists) and stops listening for new connections.
	 */
	public void Disconnect() throws IOException {
		safeClose(socketClientConnecting);
		StopListening();
		safeClose(workingSocket);
		socketClientConnecting = null;
		serverSocket = null;
		workingSocket = null;

	}
	private void safeClose(Closeable c) {
		if (c == null)
			return;
		for (int retries = 3;retries > 0;retries--) {
			try {
				c.close();
				break;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	/**
	 * Stops listening for incoming connections.
	 *This will not disconnect any active connection.
	 */
	public void StopListening() throws IOException {
		if (serverSocket != null) {
			BluetoothServerSocket b = serverSocket;
			serverSocket = null;
			safeClose(b);
		}
	}
	/**
	 * Starts listening for incoming connections using the default UUID.
	 *The Connected event will be raised when the connection is established.
	 *Nothing happens if the device already listens for connections.
	 */
	public void Listen(BA ba) throws IOException {
		Listen2("B4A", "00001101-0000-1000-8000-00805F9B34FB", ba);
	}
	/**
	 * Starts listening for incoming connections.
	 *The Connected event will be raised when the connection is established.
	 *Nothing happens if the device already listens for connections.
	 *Name - An arbitrary string that will be used for internal registration.
	 *UUID - The UUID defined for this record.
	 */
	public void Listen2(String Name, String UUID, BA ba) throws IOException {
		if (serverSocket != null)
			return;
		serverSocket = blueAdapter.listenUsingRfcommWithServiceRecord(Name, java.util.UUID.fromString(UUID));
		BA.submitRunnable(new ListenToConnection(ba), this, 1);
	}
	/**
	 * Starts listening for incoming unencrypted connections.
	 *Admin - An object of type BluetoothAdmin.
	 *Port - The RFCOMM channel.
	 */
	public void ListenInsecure(BA ba, BluetoothAdmin Admin, int Port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		if (serverSocket != null)
			return;
		Method m = BluetoothAdapter.class.getMethod("listenUsingInsecureRfcommOn", int.class);
		serverSocket = (BluetoothServerSocket)m.invoke(blueAdapter, Port);
		BA.submitRunnable(new ListenToConnection(ba), this, 1);
	}
	
	/**
	 * Returns the InputStream that is used to read data from the other device.
	 *Should be called after a connection is established.
	 */
	public InputStream getInputStream() throws IOException {
		return workingSocket.getInputStream();
	}
	/**
	 * Returns the OutputStream that is used to write data to the other device.
	 *Should be called after a connection is established.
	 */
	public OutputStream getOutputStream() throws IOException {
		return workingSocket.getOutputStream();
	}
	private class ListenToConnection implements Runnable {
		private BA ba;
		public ListenToConnection(BA ba) throws IOException {
			this.ba = ba;
		}
		@Override
		public void run() {
			while (true) {
				try {
					if (serverSocket == null)
						break;
					BluetoothSocket bs = serverSocket.accept();
					synchronized (Serial.this) {
						workingSocket = bs;
						if (socketClientConnecting != null)
							socketClientConnecting.close();
						socketClientConnecting = null;
					}
					ba.raiseEventFromDifferentThread(Serial.this, Serial.this, 0, eventName + "_connected", false, new Object[] {true});
				} catch (IOException e) {
					if (workingSocket == null && serverSocket != null) { //there is no connection
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(Serial.this, Serial.this, 0, eventName + "_connected", false, new Object[] {false});
					}
					try {
						if (serverSocket != null) {
							Thread.sleep(2000);
						}
					} catch (InterruptedException e1) {
						//
					}
				}
			}

		}
	}
	/**
	 * BluetoothAdmin allows you to administrate the Bluetooth adapter.
	 *Using this object you can enable or disable the adapter, monitor its state and discover devices in range.
	 *DiscoveryStarted and DiscoveryFinished events are raised when a discovery process starts or finishes.
	 *StateChanged event is raised whenever the adapter state changes. The new state and the previous state are passed.
	 *The values correspond to the STATE_xxxx constants.
	 *DeviceFound event is raised when a device is discovered. The device name and mac address are passed.
	 */
	@ShortName("BluetoothAdmin")
	@Permissions(values={"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.ACCESS_COARSE_LOCATION"})
	@Events(values={"StateChanged (NewState As Int, OldState As Int)",
			"DiscoveryStarted", "DiscoveryFinished",
	"DeviceFound (Name As String, MacAddress As String)"})
	public static class BluetoothAdmin {
		private BluetoothAdapter blueAdapter;
		private String eventName;
		public static final int STATE_OFF = 10;
		public static final int STATE_TURNING_ON = 11;
		public static final int STATE_ON = 12;
		public static final int STATE_TURNING_OFF = 13;
		@Hide
		public BluetoothDevice LastDiscoveredDevice;
		private Intent lastFoundIntent;
		/**
		 * Initializes the object and sets the subs that will handle the events.
		 */
		public void Initialize(final BA ba, String EventName) {
			this.eventName = EventName.toLowerCase(BA.cul);
			if (blueAdapter == null) {
				blueAdapter = BluetoothAdapter.getDefaultAdapter();
				BroadcastReceiver br = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, final Intent intent) {
						BA.handler.post(new BA.B4ARunnable() {
							@Override
							public void run() {
								String action = intent.getAction();
								if (action == null)
									return;
								if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
									ba.raiseEvent(BluetoothAdmin.this, eventName + "_discoverystarted");
								}
								else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
									ba.raiseEvent(BluetoothAdmin.this, eventName + "_discoveryfinished");
								else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
									ba.raiseEvent(BluetoothAdmin.this, eventName + "_statechanged", 
											intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1), intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1));
								}
								else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
									BluetoothDevice bd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
									if (bd != null) {
										String name = bd.getName();
										if (name == null)
											name = "";
										LastDiscoveredDevice = bd;
										lastFoundIntent = intent;
										ba.raiseEvent(BluetoothAdmin.this, eventName + "_devicefound", name, bd.getAddress());
									}
								}
							}
						});
					}

				};
				
				IntentFilter f = new IntentFilter();
				f.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
				f.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
				f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
				f.addAction(BluetoothDevice.ACTION_FOUND);
				BA.applicationContext.registerReceiver(br, f);
			}
		}
		/**
		 * Can be used inside the DeviceFound event to extract more data from the intent received. Returns an uninitialized object if no intent was received.
		 */
		public IntentWrapper getLastFoundIntent() {
			return (IntentWrapper) AbsObjectWrapper.ConvertToWrapper(new IntentWrapper(), lastFoundIntent);
		}
		/**
		 * Tests whether the object is initialized.
		 */
		public boolean IsInitialized() {
			return blueAdapter != null;
		}
		/**
		 * Turns on the Bluetooth adapter. The adapter will not be immediately ready. You should use the StateChanged event to find when it is enabled.
		 *This method returns False if the adapter cannot be enabled.
		 *<b>Always fails on Android 13+ with targetSdkVersion >= 13.</b>
		 */
		public boolean Enable() {
			return blueAdapter.enable();
		}
		/**
		 * Turns off the Bluetooth adapter. The adapter will not be immediately disabled. You should use the StateChanged event to monitor the adapter.
		 *This method returns False if the adapter cannot be disabled.
		 *<b>Always fails on Android 13+ with targetSdkVersion >= 13.</b>
		 */
		public boolean Disable() {
			return blueAdapter.disable();
		}
		/**
		 * Tests whether the Bluetooth adapter is enabled.
		 */
		public boolean IsEnabled() {
			return blueAdapter.isEnabled();
		}
		/**
		 * Cancels a discovery process.
		 *Returns False if the operation has failed.
		 */
		public boolean CancelDiscovery() {
			return blueAdapter.cancelDiscovery();
		}
		/**
		 * Starts a discovery process. You should handle DiscoveryStarted, DiscoveryFinished and DeviceFound events to get more information about the process.
		 *Returns False if the operation has failed.
		 */
		public boolean StartDiscovery() {
			return blueAdapter.startDiscovery();
		}

	}
}
