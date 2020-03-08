
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;


/**
 * Allows to configure the device as a BLE peripheral device. Other central devices can connect to this device.
 *Only supported by some devices running Android 5+. Make sure to check IsPeripheralSupported property.
 */
@Version(1.12f)
@ShortName("BlePeripheral2")
@Events(values={"Start (Success As Boolean)", "Subscribe (DeviceId As String)", "Unsubscribe (DeviceId As String)", "NewData (DeviceId As String, Data() As Byte)"})
public class BlePeripheral2 {
	@Hide
	public BluetoothAdapter blueAdapter;
	@Hide
	public BluetoothGattServer gattServer;
	private BA ba;
	private String eventName;
	@Hide
	public BluetoothLeAdvertiser advertiser;
	@Hide
	public final ConcurrentHashMap<String, BluetoothDevice> connectedDevices = new ConcurrentHashMap<String, BluetoothDevice>();
	@Hide
	public BluetoothGattCharacteristic charRead, charWrite;
	private Map manufacturerData = new Map();
	@Hide
	public AdvertiseCallback advertiseCallback;
	/**
	 * Initializes the object.
	 */
	public void Initialize(BA ba, String EventName, BleManager2 Ble) {
		blueAdapter = Ble.blueAdapter;
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		advertiser = blueAdapter.getBluetoothLeAdvertiser();
	}
	/**
	 * Checks whether peripheral mode is supported.
	 */
	public boolean getIsPeripheralSupported() {
		return blueAdapter.isMultipleAdvertisementSupported() &&
		blueAdapter.isOffloadedFilteringSupported() &&
		blueAdapter.isOffloadedScanBatchingSupported() && advertiser != null;
	}
	/**
	 * Gets or sets the manufacturer specific data that will be advertised.
	 *Each item in the map should have a positive int number as the key and an array of bytes as the value.
	 */
	public void setManufacturerData(Map Map) {
		manufacturerData.setObject(Map.getObject());
	}
	public Map getManufacturerData() {
		return manufacturerData;
	}
	public void Close() {
		if (advertiseCallback != null) {
			advertiser.stopAdvertising(advertiseCallback);
			advertiseCallback = null;
		}
		gattServer.clearServices();
		gattServer.close();
	}
	/**
	 * Starts advertising. The name will be set as the device Bluetooth name. Pass an empty string to keep the current name.
	 *The Start event will be raised.
	 */
	public void Start(String Name) {
		AdvertiseSettings settings = new AdvertiseSettings.Builder()
		.setConnectable(true)
		.build();
		Start2(Name, settings);
	}
	/**
	 * Similar to Start. Allows overriding the default settings.
	 */
	public void Start2(String Name, AdvertiseSettings Settings) {
		if (Name.length() > 0)
			blueAdapter.setName(Name);
		AdvertiseData.Builder builder = new AdvertiseData.Builder()
		.setIncludeDeviceName(true)
		.setIncludeTxPowerLevel(true)
		.addServiceUuid(new ParcelUuid(longUUID("0001")));
		if (manufacturerData.IsInitialized()) {
			for (Entry<Object, Object> e : manufacturerData.getObject().entrySet()) {
				builder.addManufacturerData(((Number)e.getKey()).intValue(), (byte[])e.getValue());
			}
		}
		AdvertiseData advertiseData = builder.build();
		AdvertiseData scanResponse = new AdvertiseData.Builder()
		.addServiceUuid(new ParcelUuid(longUUID("0001")))
		.build();
		advertiseCallback = new AdvertiseCallback() {
			@Override
			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
				ba.raiseEventFromUI(BlePeripheral2.this, eventName + "_start", true);
			}
			@Override
			public void onStartFailure(int errorCode) {
				ba.setLastException(new Exception("code: " + errorCode));
				ba.raiseEventFromUI(BlePeripheral2.this, eventName + "_start", true);
			}
		};
		advertiser.startAdvertising(Settings, advertiseData, scanResponse,  advertiseCallback);
		BluetoothManager manager = (BluetoothManager) BA.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
		gattServer = manager.openGattServer(ba.context, new BluetoothGattServerCallback() {
			@Override
			public void onConnectionStateChange(BluetoothDevice device, int status,
					int newState) {
				if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					removeDevice(device);
				}

			}
			@Override
			public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
					int offset, BluetoothGattCharacteristic characteristic) {
				//BA.Log("onCharacteristicReadRequest: " + characteristic.getUuid());
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.getValue() == null ? new byte[8] : characteristic.getValue());
			}
			@Override
			public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
					int offset, BluetoothGattDescriptor descriptor) {
				//BA.Log("onDescriptorReadRequest: " + descriptor.getUuid());
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, connectedDevices.contains(device.getAddress()) ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[] {0, 0});
			}
			@Override
			public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
					BluetoothGattDescriptor descriptor,
					boolean preparedWrite, boolean responseNeeded,
					int offset,  byte[] value) {
				//BA.Log("onDescriptorWriteRequest: " + descriptor.getUuid());
				if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE))
					addDevice(device);
				else
					removeDevice(device);
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
			}
			@Override
			public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
					BluetoothGattCharacteristic characteristic,
					boolean preparedWrite, boolean responseNeeded,
					int offset, byte[] value) {
				if (characteristic.getUuid().equals(charWrite.getUuid())) {
					ba.raiseEventFromDifferentThread(BlePeripheral2.this, null, 0, eventName + "_newdata", false, new Object[]{device.getAddress(), value});
				}
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
			}

		});
		BluetoothGattService service = new BluetoothGattService(longUUID("0001"), BluetoothGattService.SERVICE_TYPE_PRIMARY);
		charRead = new BluetoothGattCharacteristic(longUUID("1001"), BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
		charWrite = new BluetoothGattCharacteristic(longUUID("1002"), BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
		charRead.addDescriptor(new BluetoothGattDescriptor(longUUID("2902"), BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
		service.addCharacteristic(charRead);
		service.addCharacteristic(charWrite);
		gattServer.addService(service);
			

	}
	/**
	 * Writes data to subscribed devices.
	 *Centrals - Target devices. Pass Null to send to all subscribed devices.
	 */
	public void Write(List Centrals, byte[] Data) {
		charRead.setValue(Data);
		for (BluetoothDevice bd : connectedDevices.values()) {
			if (Centrals.IsInitialized() == false || Centrals.IndexOf(bd.getAddress()) > -1)
				gattServer.notifyCharacteristicChanged(bd, charRead, false);
			
		}
	}
	private void addDevice(BluetoothDevice device) {
		if (device == null)
			return;
		connectedDevices.put(device.getAddress(), device);
		ba.raiseEventFromDifferentThread(BlePeripheral2.this, null, 0, eventName + "_subscribe", false, new Object[] {device.getAddress()});
	}
	private void removeDevice(BluetoothDevice device) {
		if (device == null)
			return;
		BluetoothDevice bd = connectedDevices.remove(device.getAddress());
		if (bd != null) {
			ba.raiseEventFromDifferentThread(BlePeripheral2.this, null, 0, eventName + "_unsubscribe", false, new Object[] {bd.getAddress()});
		}
	}
	private java.util.UUID longUUID(String shortName) {
		return UUID.fromString("0000" + shortName + "-0000-1000-8000-00805f9b34fb");

	}
}
