
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
 
 package anywheresoftware.b4a.objects.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.IntentWrapper;
import anywheresoftware.b4a.objects.collections.List;

/**
 * Allows you to connect two devices over a Wifi Direct connection.
 *See this <link>tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/30409-android-wifi-direct-tutorial.html</link> for more information.
 */
@Version(1.06f)
@Permissions(values={"android.permission.ACCESS_WIFI_STATE",
		"android.permission.CHANGE_WIFI_STATE",
		"android.permission.CHANGE_NETWORK_STATE",
		"android.permission.INTERNET",
"android.permission.ACCESS_NETWORK_STATE"})
@ShortName("WifiManager")
@Events(values={"EnabledChanged (Enabled As Boolean)", "PeersDiscovered (Success As Boolean, Devices As List)",
		"ConnectionChanged (Connected As Boolean, GroupOwnerIp As String)", "IntentReceived (Intent As Intent)"})
public class WifiManagerWrapper {
	private String eventName;
	private WifiP2pManager manager;
	private Channel channel;
	private final IntentFilter intentFilter = new IntentFilter();
	private BA ba;
	private static BroadcastReceiver receiver = null;
	/**
	 * Initializes the object. The EnabledChanged and ConnectionChanged events will be raised after this call.
	 */
	public void Initialize(BA ba, String EventName) {
		if (receiver != null)
			BA.applicationContext.unregisterReceiver(receiver);
		this.eventName = EventName.toLowerCase(BA.cul);
		this.ba = ba;
		manager = (WifiP2pManager) BA.applicationContext.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(BA.applicationContext, BA.applicationContext.getMainLooper(), null);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		receiver = new WifiReceiver();
		BA.applicationContext.registerReceiver(receiver, intentFilter);
		
	}
	/**
	 * Starts a discovery process. The PeersDiscovered event will be later raised.
	 */
	public void DiscoverPeers() {
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

			@Override
			public void onFailure(int arg0) {
				ba.raiseEvent(WifiManagerWrapper.this, eventName + "_peersdiscovered", false, null);
			}

			@Override
			public void onSuccess() {
			}
			
		});
	}
	/**
	 * Starts a connection process to the given address. The ConnectionChanged event will be raised.
	 */
	public void Connect(String MacAddress) {
		Connect2(MacAddress, false);
	}
	/**
	 * Similar to Connect. Setting the GroupOwner to True will ask the system to set the current device as the group owner.
	 */
	public void Connect2(String MacAddress, boolean GroupOwner) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = MacAddress;
		config.wps.setup = WpsInfo.PBC;
		if (GroupOwner)
			config.groupOwnerIntent = 15;
		manager.connect(channel, config, new WifiP2pManager.ActionListener() {

			@Override
			public void onFailure(int arg0) {
				ba.raiseEvent(WifiManagerWrapper.this, eventName + "_connectionchanged", false, "");
			}

			@Override
			public void onSuccess() {
			}
			
		});
	}
	/**
	 * Stops a discovery process.
	 */
	public void StopDiscovery() {
		manager.stopPeerDiscovery(channel, null);
	}
	/**
	 * Cancels a connection process.
	 */
	public void CancelConnections() {
		manager.cancelConnect(channel, null);
	}
	/**
	 * Calling this method will call the EnabledChanged and ConnectedChanged events to be raised with the last information.
	 */
	public void GetCurrentStatus() {
		BA.applicationContext.unregisterReceiver(receiver);
		BA.applicationContext.registerReceiver(receiver, intentFilter);
	}

	@Hide
	public class WifiReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();
			if (action == null)
				return;
			ba.raiseEvent(WifiManagerWrapper.this, eventName + "_intentreceived", AbsObjectWrapper.ConvertToWrapper(new IntentWrapper(), intent));
			if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
				ba.raiseEvent(WifiManagerWrapper.this, eventName + "_enabledchanged", intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) == 
					WifiP2pManager.WIFI_P2P_STATE_ENABLED);
			}
			else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
				manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

					@Override
					public void onPeersAvailable(WifiP2pDeviceList list) {
						List dev = new List(); dev.Initialize();
						for (WifiP2pDevice d : list.getDeviceList()) {
							dev.Add(d);
						}
						ba.raiseEvent(WifiManagerWrapper.this, eventName + "_peersdiscovered", true, dev);
					}
					
				});
				ba.raiseEvent(WifiManagerWrapper.this, eventName + "_peerschanged");
			}
			else if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
				NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

				if (networkInfo.isConnected()) {
					manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {

						@Override
						public void onConnectionInfoAvailable(WifiP2pInfo info) {
							
							if (info.groupFormed) {
								ba.raiseEvent(WifiManagerWrapper.this, eventName + "_connectionchanged", true, 
										info.isGroupOwner ? "127.0.0.1" : info.groupOwnerAddress.getHostAddress());
							}
							else {
								ba.raiseEvent(WifiManagerWrapper.this, eventName + "_connectionchanged", false, "");
							}
								
						}
						
					});
				}
				else {
					ba.raiseEvent(WifiManagerWrapper.this, eventName + "_connectionchanged", false, "");
				}
			}
		}

	}
	/**
	 * Represents a discovered wifi device.
	 */
	@ShortName("WifiDevice")
	public static class WifiDeviceWrapper extends AbsObjectWrapper<WifiP2pDevice> {
		/**
		 * Returns the device name.
		 */
		public String getName() {
			return getObject().deviceName;
		}
		/**
		 * Returns the device MAC address.
		 */
		public String getMacAddress() {
			return getObject().deviceAddress;
		}
		
	}



}
