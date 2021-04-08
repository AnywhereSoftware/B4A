
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;

/**
 * This library replaces the BLE library. It allows you to search for and connect to BLE devices.
 *It is supported by Android 4.3+ (API 18).
 */
@Version(1.39f)
@ShortName("BleManager2")
@Events(values={"StateChanged (State As Int)", "DeviceFound (Name As String, DeviceId As String, AdvertisingData As Map, RSSI As Double)",
		"Disconnected", "Connected (Services As List)", "DataAvailable (ServiceId As String, Characteristics As Map)",
		"WriteComplete (Characteristic As String, Status As Int)",
"RssiAvailable (Success As Boolean, RSSI As Double)", "MtuChanged (Success As Boolean, MTU As Int)"})
@Permissions(values={"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"})
public class BleManager2 {
	private String eventName;
	@Hide
	public BluetoothAdapter blueAdapter;
	private BA ba;
	@Hide
	public BluetoothGatt gatt;
	@Hide
	public LeScanCallback scanCallback;
	@Hide
	public final ConcurrentHashMap<String, BluetoothDevice> devices = new ConcurrentHashMap<String, BluetoothDevice>();
	@Hide
	public UUID notifyDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	private final ArrayList<BluetoothGattCharacteristic> charsToReadQueue = new ArrayList<BluetoothGattCharacteristic>();
	public static int STATE_POWERED_ON = BluetoothAdapter.STATE_ON, 
			STATE_POWERED_OFF = BluetoothAdapter.STATE_OFF,
			STATE_UNSUPPORTED = -9999;
	
	public boolean IsInitialized() {
		return ba != null;
	}
	/**
	 * Initializes the object. The StateChanged event will be raised after this method with the current BLE state.
	 */
	public void Initialize(final BA ba, String EventName) {
		this.eventName = EventName.toLowerCase(BA.cul);
		blueAdapter = BluetoothAdapter.getDefaultAdapter();
		this.ba = ba;
		if (blueAdapter == null || BA.applicationContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) == false) {
			ba.raiseEventFromUI(this, eventName + "_statechanged", STATE_UNSUPPORTED);
			return;
		}
		BroadcastReceiver br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, final Intent intent) {
				BA.handler.post(new BA.B4ARunnable() {
					@Override
					public void run() {
						String action = intent.getAction();
						if (action == null)
							return;
						if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
							int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
							if (state == STATE_POWERED_OFF || state == STATE_POWERED_ON) {
								ba.raiseEvent(BleManager2.this, eventName + "_statechanged", state);
							}
						} else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
							IntentWrapper iw = new IntentWrapper();
							iw.setObject(intent);
							BA.Log(iw.toString());
							BA.Log(iw.ExtrasToString());
						}

					}
				});
			}

		};

		IntentFilter f = new IntentFilter();
		f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		f.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		BA.applicationContext.registerReceiver(br, f);
		Intent i = new Intent(BluetoothAdapter.ACTION_STATE_CHANGED);
		i.putExtra(BluetoothAdapter.EXTRA_STATE, blueAdapter.getState());
		br.onReceive(null, i);
	}
	/**
	 * Starts scanning for devices. The DeviceFound event will be raised when a device is found.
	 *ServiceUUIDs - A list (or array) with service uuids. Devices that don't advertise these uuids will not be discovered.
	 *Pass Null to discover all devices.
	 */
	public void Scan(List ServiceUUIDs) {
		Scan2(ServiceUUIDs, false);
	}
	/**
	 * Similar to Scan. If AllowDuplicates is true then the DeviceFound event will be raised whenever a packet is received.
	 */
	public void Scan2(List ServiceUUIDs,final boolean AllowDuplicates) {
		StopScan();
		devices.clear();
		scanCallback = new LeScanCallback() {

			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				if (!AllowDuplicates && devices.containsKey(device.getAddress()))
					return;
				devices.put(device.getAddress(), device);
				ba.raiseEventFromDifferentThread(BleManager2.this, null, 0, eventName + "_devicefound", false, 
						new Object[] {device.getName() == null ? "" : device.getName(), device.getAddress(), parseScanRecord(scanRecord), (double)rssi});
			}

		};
		boolean res;
		if (ServiceUUIDs == null || ServiceUUIDs.IsInitialized() == false || ServiceUUIDs.getSize() == 0)
			res =  blueAdapter.startLeScan(scanCallback);
		else {
			UUID[] u = new UUID[ServiceUUIDs.getSize()];
			for (int i = 0;i < u.length;i++)
				u[i] = UUID.fromString((String)ServiceUUIDs.Get(i));
			res = blueAdapter.startLeScan(u, scanCallback);
		}
		if (!res)
			throw new RuntimeException("Error starting scan.");
	}
	/**
	 * Reads the RSSI value of a connected device. The RssiAvailable event will be raised when the value is available.
	 */
	public void ReadRemoteRssi() {
		if (gatt != null)
			gatt.readRemoteRssi();
	}
	/**
	 * Requests to change the MTU size (packet size). The MtuChanged event will be raised.
	 *Should be called after a connection is established.
	 *Returns True if the request was sent successfully.
	 *Only available on Android 5+. Does nothing on older versions.
	 */
	public boolean RequestMtu (int MTU) {
		if (gatt == null || Build.VERSION.SDK_INT < 21)
			return false;
		return gatt.requestMtu(MTU);
	}
	/**
	 * Returns the current Bluetooth adapter state.
	 */
	public int getState() {
		if (blueAdapter == null || BA.applicationContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) == false)
			return STATE_UNSUPPORTED;
		else
			return blueAdapter.getState();
	}
	/**
	 * Returns a List with all the records with the specified type.
	 *This is useful when there could be several records with the same type. 
	 *Each item in the list is an array of bytes.
	 */
	public List GetRecordsFromAdvertisingData(Map AdvertisingData, int Key) {
		List res = new List();
		res.Initialize();
		byte[] scanRecord = (byte[]) AdvertisingData.Get(0);
		int index = 0;
		while (index < scanRecord.length) {
			int length = scanRecord[index++];
			if (length == 0) 
				break;

			int type = scanRecord[index];
			if (type == 0) 
				break;
			if (type == Key) {
				byte[] data = Arrays.copyOfRange(scanRecord, index+1, index+length);
				res.Add(data);
			}
			index += length;
		}
		return res;
	}
	private static Map parseScanRecord(byte[] scanRecord) {
		Map records = new Map();
		records.Initialize();

		int index = 0;
		while (index < scanRecord.length) {
			int length = 0xFF & scanRecord[index++];
			if (length == 0) 
				break;

			int type = scanRecord[index];
			if (type == 0) 
				break;

			byte[] data = Arrays.copyOfRange(scanRecord, index+1, index+length);
			records.Put(type, data);
			index += length;
		}
		records.Put(0, scanRecord);

		return records;
	}
	/**
	 * Stops scanning for new devices.
	 */
	public void StopScan() {
		if (scanCallback != null)
			blueAdapter.stopLeScan(scanCallback);
		scanCallback = null;


	}
	@Hide
	public boolean CreateBond(String DeviceId) {
		BluetoothDevice bd = this.devices.get(DeviceId);
		return bd.createBond();
	}
	/**
	 * Connects to a device with the given id. You can only connect to previously discovered devices.
	 *Note that the Disconnected event will be raised if the connection has failed.
	 */
	public void Connect(String DeviceId) {
		Connect2(DeviceId, true);
	}
	/**
	 * Similar to Connect. Allows you to disable auto connection.
	 */
	public void Connect2(String DeviceId, boolean AutoConnect) {
		charsToReadQueue.clear();
		BluetoothDevice bd = this.devices.get(DeviceId);
		if (bd == null)
			throw new RuntimeException("MacAddress not found. Make sure to call Scan before trying to connect.");

		bd.connectGatt(BA.applicationContext, AutoConnect, new GattCallback());
	}
	public void Disconnect() {
		if (gatt != null)
			gatt.disconnect();
		gatt = null;
	}

	/**
	 * Asynchronously reads all characteristics from the given service. The DataAvailable will be raised when the data is available.
	 */
	public void ReadData(String Service) {
		ReadData2(Service, null);
	}
	/**
	 * Asynchronously reads the value of the specified characteristic.
	 *The DataAvailable will be raised when the data of this characteristic is available.
	 */
	public void ReadData2(String Service, String Characteristic) {
		synchronized (charsToReadQueue) {
			boolean queueWasEmpty = charsToReadQueue.isEmpty();
			boolean atLeastOneReadable = false;
			BluetoothGattService ser = getService(Service);
			for (BluetoothGattCharacteristic chr : readableCharsFromService(ser)) {
				if (Characteristic == null || chr.getUuid().toString().equals(Characteristic)) {
					charsToReadQueue.add(chr);		
					atLeastOneReadable = true;
				}
			}
			if (atLeastOneReadable) {
				if (queueWasEmpty)
					gatt.readCharacteristic(charsToReadQueue.get(0));
			}
			else {
				if (Characteristic == null) {
					Map data = new Map();
					data.Initialize();
					for (BluetoothGattCharacteristic chr : ser.getCharacteristics()) {
						String uuid = chr.getUuid().toString();
						data.Put(uuid, new byte[0]);
					}
					ba.raiseEventFromDifferentThread(BleManager2.this, null, 0, eventName + "_dataavailable", false, 
							new Object[] {Service, data});
				}
				else {
					BA.LogInfo("No matching characteristic found.");
				}
			}

		}
	}
	/**
	 * Adds or removes a notification listener that monitor value changes.
	 *The DataAvailable event will be raised when the value of the characteristic changes.
	 *Returns True if successful.
	 *Service - The service id (as returned in the Connected event).
	 *Characteristic - The characteristic id.
	 *Notify - True to add a listener, false to remove it.
	 */
	public boolean SetNotify(String Service, String Characteristic, boolean Notify) {
		return setNotify(Service, Characteristic, Notify, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	}
	private boolean setNotify(String Service, String Characteristic, boolean Notify, byte[] descriptorValue) {
		BluetoothGattService ser = getService(Service);
		BluetoothGattCharacteristic chr = getChar(ser, Characteristic);
		if (!gatt.setCharacteristicNotification(chr, Notify))
			throw new RuntimeException("Error changing notification state: " + Characteristic);
		else {
			BluetoothGattDescriptor descriptor = chr.getDescriptor(notifyDescriptor);
			boolean res;
			if (Notify) {
				res = descriptor.setValue(descriptorValue);
				BA.Log("Setting descriptor. Success = " + res);
			}
			else {
				res = descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			}
			if (res) {
				boolean res2 = gatt.writeDescriptor(descriptor);
				BA.Log("writing descriptor: " + res2);
				return res2;
			}
			else {
				return false;
			}

		}
	}
	/**
	 * Similar to SetNotify. Sets the descriptor value to 2 (indication) instead of 1 (notification).
	 *Returns True if successful.
	 */
	public boolean SetIndication(String Service, String Characteristic, boolean Notify) {
		return setNotify(Service, Characteristic, Notify, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
	}
	/**
	 * Writes the data to the specified characteristic.
	 */
	public void WriteData(String Service, String Characteristic, byte[] Data) throws InterruptedException {
		BluetoothGattCharacteristic chr = getChar(getService(Service), Characteristic);
		chr.setValue(Data);
		int retries = 5;
		while (true) {
			if (!gatt.writeCharacteristic(chr)) {
				if (--retries <= 0)
					throw new RuntimeException("Error writing data to: " + Characteristic);
			}
			else
				break;
			BA.Log("retries: " + retries);
			Thread.sleep(150 * (5 - retries));
		}
	}
	/**
	 * Returns a numeric value from which you can find the properties of the specified characteristic.
	 */
	public int GetCharacteristicProperties(String Service, String Characteristic) {
		BluetoothGattCharacteristic chr = getChar(getService(Service), Characteristic);
		return chr.getProperties();
	}

	@Hide
	public BluetoothGattCharacteristic getChar(BluetoothGattService service, String Characteristic) {
		for (BluetoothGattCharacteristic chr : service.getCharacteristics()) {
			if (chr.getUuid().toString().equals(Characteristic))
				return chr;
		}
		throw new RuntimeException("Characterisic not found: " + Characteristic);
	}
	private ArrayList<BluetoothGattCharacteristic> readableCharsFromService(BluetoothGattService ser) {
		ArrayList<BluetoothGattCharacteristic> res = new ArrayList<BluetoothGattCharacteristic>();
		for (BluetoothGattCharacteristic chr : ser.getCharacteristics()) {
			if ((chr.getProperties()  & BluetoothGattCharacteristic.PROPERTY_READ) == BluetoothGattCharacteristic.PROPERTY_READ) {
				res.add(chr);
			}
		}
		return res;
	}
	@Hide
	public BluetoothGattService getService(String Service) {
		if (gatt == null)
			throw new RuntimeException("No device connected");
		for (BluetoothGattService s : gatt.getServices()) {
			if (s.getUuid().toString().equals(Service))
				return s;
		}
		throw new RuntimeException("Service not found");
	}

	class GattCallback extends BluetoothGattCallback {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				BleManager2.this.gatt = gatt;
				Common.Log("Discovering services.");
				gatt.discoverServices();
			}
			else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				if (gatt != null)
					gatt.close();
				ba.raiseEventFromDifferentThread(BleManager2.this,null, 0, eventName + "_disconnected", false, null);
			}

		}
		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			ba.raiseEventFromDifferentThread(BleManager2.this,null, 0, eventName + "_rssiavailable", false, 
					new Object[] {status == BluetoothGatt.GATT_SUCCESS, (double)rssi});
		}

		public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
			ba.raiseEventFromDifferentThread(BleManager2.this,null, 0, eventName + "_mtuchanged", false, 
					new Object[] {status == BluetoothGatt.GATT_SUCCESS, mtu});
		}


		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				Common.Log("Service discovery failed.");
				gatt.disconnect();
			}
			else {
				List services = new List();
				services.Initialize();
				for (BluetoothGattService s : gatt.getServices()) {
					services.Add(s.getUuid().toString());
				}
				ba.raiseEventFromDifferentThread(BleManager2.this,null, 0, eventName + "_connected", false, new Object[] {services});
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
				int status) {
			synchronized (charsToReadQueue) {
				if (charsToReadQueue.size() == 0 || charsToReadQueue.get(0) != characteristic) {
					//BA.LogInfo("Ignoring characteristic: " + characteristic.getUuid());
				} else {
					charsToReadQueue.remove(0);
					if (charsToReadQueue.size() == 0 || charsToReadQueue.get(0).getService() != characteristic.getService()) {
						Map data = new Map();
						data.Initialize();
						BluetoothGattService ser = getService(characteristic.getService().getUuid().toString());
						for (BluetoothGattCharacteristic chr : readableCharsFromService(ser)) {
							byte[] b = chr.getValue();
							data.Put(chr.getUuid().toString(), b == null ? new byte[0] : b);
						}
						for (BluetoothGattCharacteristic chr : ser.getCharacteristics()) {
							String uuid = chr.getUuid().toString();
							if (data.ContainsKey(uuid) == false)
								data.Put(uuid, new byte[0]);
						}
						ba.raiseEventFromDifferentThread(BleManager2.this, null, 0, eventName + "_dataavailable", false, 
								new Object[] {characteristic.getService().getUuid().toString(), data});
					}
				}
				if (charsToReadQueue.size() > 0)
					gatt.readCharacteristic(charsToReadQueue.get(0));
			}

		}


		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (characteristic == null || characteristic.getUuid() == null)
				return;
			ba.raiseEventFromDifferentThread(BleManager2.this, null, 0, eventName + "_writecomplete", false, 
					new Object[] {characteristic.getUuid().toString(), status});
		}


		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			//BA.Log("onCharacteristicChanged: " + characteristic.getUuid());
			Map data = new Map();
			data.Initialize();
			byte[] b = characteristic.getValue();
			data.Put(characteristic.getUuid().toString(), b == null ? new byte[0] : b);
			ba.raiseEventFromDifferentThread(BleManager2.this, null, 0, eventName + "_dataavailable", false, 
					new Object[] {characteristic.getService().getUuid().toString(), data});

		}


		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
				int status) {
		}

	}

}

