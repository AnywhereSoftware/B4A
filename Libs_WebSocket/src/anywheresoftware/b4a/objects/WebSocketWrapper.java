
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

import io.crossbar.autobahn.websocket.WebSocketConnection;
import io.crossbar.autobahn.websocket.WebSocketException;
import io.crossbar.autobahn.websocket.WebSocketOptions;
import io.crossbar.autobahn.websocket.WebSocket.ConnectionHandler;

import java.io.IOException;

import javax.net.ssl.TrustManager;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.Map;

/**
 * Implementation of a WebSocket client.
 */
@Version(2.11f)
@ShortName("WebSocket")
@Events(values={"Connected", "Closed (Reason As String)", "TextMessage (Message As String)", "BinaryMessage (Data() As Byte)"})
@Permissions(values={"android.permission.INTERNET"})
public class WebSocketWrapper {
	private BA ba;
	private String eventName;
	@Hide
	public WebSocketOptions options;

	@Hide
	public WebSocketConnection wsc;
	public Map Headers;
	/**
	 * Initializes the object and sets the subs that will handle the events.
	 */
	public void Initialize(BA ba, String EventName) {
		eventName = EventName.toLowerCase(BA.cul);
		this.ba = ba;
		wsc = new WebSocketConnection();
		options = new WebSocketOptions();
		options.setSocketConnectTimeout(30000);
		Headers = new Map();
		Headers.Initialize();
		
	}
	/**
	 * Sets a custom SSL socket factory (custom factory is available in the Net library).
	 */
	public void SetCustomSSLTrustManager(Object TrustManager) {
		wsc.customTrustManager = (TrustManager[])TrustManager;
	}
	/**
	 * Tries to connect to the given Url. The Url should start with ws:// or wss://.
	 */
	@SuppressWarnings("unchecked")
	public void Connect(String Url) throws WebSocketException {
		
		wsc.connect(Url, null, new ConnectionHandler() {

			@Override
			public void onBinaryMessage(byte[] payload) {
				ba.raiseEvent(WebSocketWrapper.this, eventName + "_binarymessage", payload);
			}

			@Override
			public void onClose(int code, String reason) {
				if (wsc.isConnected())
					wsc.disconnect();
				ba.raiseEventFromDifferentThread(WebSocketWrapper.this, null, 0, eventName + "_closed", false, new Object[] {reason});
			}

			@Override
			public void onOpen() {
				ba.raiseEvent(WebSocketWrapper.this, eventName + "_connected");
			}

			@Override
			public void onRawTextMessage(byte[] payload) {
				
			}

			@Override
			public void onTextMessage(String payload) {
				ba.raiseEvent(WebSocketWrapper.this, eventName + "_textmessage", payload);
			}
			
		}, options, (java.util.Map)Headers.getObject());
	}
	/**
	 * Checks whether the connection is open.
	 */
	public boolean getConnected() {
		return wsc.isConnected();
	}
	/**
	 * Closes the connection.
	 */
	public void Close() {
		wsc.disconnect();
	}
	/**
	 * Sends a text message.
	 * 
	 */
	public void SendText(String Text) {
		wsc.sendTextMessage(Text);
	}
	/**
	 * Sends a binary message.
	 */
	public void SendBinary(byte[] Data) {
		wsc.sendBinaryMessage(Data);
	}
	
}
