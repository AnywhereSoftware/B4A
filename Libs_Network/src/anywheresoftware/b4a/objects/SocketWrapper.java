
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

/**
 * The Socket object is an endpoint for network communication.
 *If you are connecting to a server then you should initialize a Socket object and call Connect with the server address.
 *The Connected event will be raised when the connection is ready or if the connection has failed.
 *Sockets are also used by the server. Once a new incoming connection is established, the NewConnection event will be raised and an initialized Socket object will be passed as a parameter.
 *Once a socket is connected you should use its <code>InputStream</code> and <code>OutputStream</code> to communicate with the other machine.
 */
@ShortName("Socket")
@Version(1.54f)
@Events(values = {"Connected (Successful As Boolean)"})
@Permissions(values = {"android.permission.INTERNET"})
public class SocketWrapper implements CheckForReinitialize{
	@Hide
	public volatile Socket socket;
	private String eventName;
	/**
	 * Initializes a new socket.
	 */
	public void Initialize(String EventName) {
		socket = new Socket();
		this.eventName = EventName.toLowerCase(BA.cul);
	}
	/**
	 * Initializes a new SSL socket.
	 * EventName - Sets the sub that will handle the Connected event.
	 * KeystoreStream - An InputStream that points to an alternate keystore. Pass Null to use the default keystore.
	 *The keystore format should be BKS.
	 * Password - Custom keystore password.
	 */
	public void InitializeSSL(String EventName, InputStream KeyStoreStream, String Password) throws Exception{
		if (KeyStoreStream == null) {
			socket = SSLSocketFactory.getDefault().createSocket();
		} else {
			KeyStore ks = KeyStore.getInstance("BKS");
			ks.load(KeyStoreStream, Password.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
			socket = sslContext.getSocketFactory().createSocket();
		}
		this.eventName = EventName.toLowerCase(BA.cul);
	}
	/**
	 * Initializes a new SSL socket that accepts all certificates automatically. This method is less secure as the server certificate is not tested.
	 */
	public void InitializeSSLAcceptAll(String EventName) throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] {
				new X509TrustManager() {

					@Override
					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}
					
				}
				
		}, new SecureRandom());
		socket = sslContext.getSocketFactory().createSocket();
		this.eventName = EventName.toLowerCase(BA.cul);
	}
	/**
	 * The network library includes two objects for working with TCP: Socket, ServerSocket With Socket you can communicate with other devices and computers over TCP/IP.
	 *ServerSocket allows you to listen for incoming connections. Once a connection is established you will receive a Socket object that will be used for handling this specific connection.
	 *See the <link>Network tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/7001-android-network-tutorial.html</link>.
	 *It also includes two objects for working with UDP: UDPSocket and UDPPacket. See UDPSocket for more information. 
	 */
	public static void LIBRARY_DOC() {

	}
	/**
	 * Tests whether the object was initialized.
	 */
	public boolean IsInitialized() {
		return socket != null;
	}
	/**
	 * Resolves the host name and returns the IP address.
	 *<b>This method is deprecated and will not work properly on Android 4+ devices.</b>
	 */
	public String ResolveHost(String Host) throws UnknownHostException {
		return InetAddress.getByName(Host).getHostAddress();
	}
	/**
	 * Gets or sets the timeout of the socket's InputStream. Value is specified in milliseconds.
	 *By default there is no timeout.
	 */
	public int getTimeOut() throws SocketException {
		return socket.getSoTimeout();
	}
	public void setTimeOut(int value) throws SocketException {
		socket.setSoTimeout(value);
	}
	/**
	 * Tries to connect to the given address. The connection is done in the background.
	 *The Connected event will be raised when the connection is ready or if it has failed.
	 *Host - The host name or IP.
	 *Port - Port number.
	 *TimeOut - Connection timeout. Value is specified in milliseconds. Pass 0 to disable the timeout. 
	 */
	public void Connect(final BA ba, final String Host, final int Port, final int TimeOut) throws UnknownHostException {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				Socket mySocket = socket;
				try {
					InetAddress Address = InetAddress.getByName(Host);
					InetSocketAddress i = new InetSocketAddress(Address, Port);
					if (mySocket != socket)
						return;
					socket.connect(i, TimeOut);
					if (socket instanceof SSLSocket) {
						final SSLSocket ssocket = (SSLSocket)socket;
						ssocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {

							@Override
							public void handshakeCompleted(
									HandshakeCompletedEvent event) {
									BA.submitRunnable(new Runnable() {

										@Override
										public void run() {
											try {
												ssocket.getInputStream();
											} catch (IOException e) {
												ba.setLastException(e);
												ba.raiseEventFromDifferentThread(SocketWrapper.this,
														SocketWrapper.this, 0, eventName + "_connected", true, new Object[] {false});
												return;
											}
											ba.raiseEventFromDifferentThread(SocketWrapper.this,
													SocketWrapper.this, 0, eventName + "_connected", true, new Object[] {true});
											
										}
										
									}, null, 0);
								
							}
							
						});
						ssocket.startHandshake();
						
					}
					else {
						ba.raiseEventFromDifferentThread(SocketWrapper.this,
							SocketWrapper.this, 0, eventName + "_connected", true, new Object[] {true});
					}
				} catch (Exception e) {
					if (mySocket == socket) {
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(SocketWrapper.this,
								SocketWrapper.this, 0, eventName + "_connected", true, new Object[] {false});
					}
				}
			}

		};
		ba.submitRunnable(r, this, 0);
	}
	/**
	 * Returns the socket's InputStream which is used to read data.
	 */
	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}
	/**
	 * Returns the socket's OutputStream which is used to write data.
	 */
	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}
	/**
	 * Tests whether the socket is connected.
	 */
	public boolean getConnected() {
		return socket != null && socket.isConnected();
	}
	/**
	 * Closes the socket and the streams.
	 *It is safe to call this method multiple times.
	 */
	public void Close() throws IOException {
		if (socket != null) {
			final Socket s = socket;
			socket = null;
			Runnable r = new Runnable() {

				@Override
				public void run() {
					try {
						if (s != null) {
							if (!(s instanceof SSLSocket)) {
								if (!s.isInputShutdown() && !s.isClosed())
									s.shutdownInput();
								if (!s.isOutputShutdown() && !s.isClosed())
									s.shutdownOutput();
							}
							if (!s.isClosed())
								s.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			};
			if (socket instanceof SSLSocket) {
				BA.submitRunnable(r, null, 0);
			} else {
				r.run();
			}
			
		}
	}
	/**
	 * The ServerSocket object allows other machines to connect to this machine.
	 *The ServerSocket listens to a specific port. Once a connection arrives, the NewConnection event is raised with a Socket object.
	 *This Socket object should be used to communicate with this client. 
	 *You may call Listen again and receive more connections. A single ServerSocket can handle many connections.
	 *For each connection there should be one Socket object.
	 */
	@ShortName("ServerSocket")
	@Events(values={"NewConnection (Successful As Boolean, NewSocket As Socket)"})
	@Permissions(values = {"android.permission.INTERNET", "android.permission.ACCESS_WIFI_STATE",
			"android.permission.ACCESS_NETWORK_STATE"})
	public static class ServerSocketWrapper implements CheckForReinitialize{
		@Hide
		public volatile ServerSocket ssocket;
		private BA ba;
		private String eventName;
		/**
		 * Initializes the ServerSocket.
		 *Port - The port that the server will listen to. Note that you should call Listen to start listening. Port numbers lower than 1024 are restricted by the system.
		 *EventName - The event Sub prefix name.
		 */
		public void Initialize(BA ba, int Port, String EventName) throws IOException {
			this.ba = ba;
			this.eventName = EventName.toLowerCase(BA.cul);
			ssocket = new ServerSocket(Port);
		}
		/**
		 * Tests whether the object is initialized.
		 */
		public boolean IsInitialized() {
			return ssocket != null;
		}
		/**
		 * Returns the IP address of the wifi network.
		 *Returns "127.0.0.1" (localhost) if not connected.
		 */
		public String GetMyWifiIP() {
			String lh = "127.0.0.1";
			WifiManager wifiManager = (WifiManager) BA.applicationContext.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo != null) {
				int ip = wifiInfo.getIpAddress();
				if (ip != 0) {
					String ipString = String.format(BA.cul, "%d.%d.%d.%d",
							(ip & 0xff),
							(ip >> 8 & 0xff),
							(ip >> 16 & 0xff),
							(ip >> 24 & 0xff));
					return ipString;
				}
			}
			return lh;
		}
		/**
		 * Returns the server's IP. Will return "127.0.0.1" (localhost) if no other IP is found.
		 *This method will return the wifi network IP if it is available.
		 */
		public String GetMyIP() throws SocketException {
			String lh = "127.0.0.1";
			String res = GetMyWifiIP();
			if (res.equals(lh) == false)
				return res;
			Inet6Address ip6 = null;
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			if (en != null) {
				while (en.hasMoreElements()) {
					NetworkInterface ni = en.nextElement();
					Enumeration<InetAddress> en2 = ni.getInetAddresses();
					while (en2.hasMoreElements()) {
						InetAddress ia = en2.nextElement();
						if (!ia.isLoopbackAddress()) {
							if (ia instanceof Inet6Address) {
								if (ip6 == null)
									ip6 = (Inet6Address) ia;
							}
							else
								return ia.getHostAddress();
						}
					}
				}
			}
			if (ip6 != null)
				return ip6.getHostAddress();
			return lh;

		}
		/**
		 * Starts listening in the background for incoming connections.
		 *When a connection is established, the NewConnection event is raised. If the connection is successful a Socket object will be passed in the event.
		 *Calling Listen while the ServerSocket is listening will not do anything.
		 */
		public void Listen() throws IOException {
			if (ba.isTaskRunning(this, 0))
				return;
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						Socket s = ssocket.accept();
						SocketWrapper sw = new SocketWrapper();
						sw.socket = s;
						ba.raiseEventFromDifferentThread(ServerSocketWrapper.this,
								ServerSocketWrapper.this, 0, eventName + "_newconnection", true,
								new Object[] {true, sw});
					} catch (IOException e) {
						if (ssocket != null) {
							ba.setLastException(e);
							ba.raiseEventFromDifferentThread(ServerSocketWrapper.this,
									ServerSocketWrapper.this, 0, eventName + "_newconnection", true,
									new Object[] {false, null});
						}
					}
				}
			};
			ba.submitRunnable(r, this, 0);
		}
		/**
		 * Closes the ServerSocket. This will not close any other sockets.
		 *You should call Initialize if you want to use this object again.
		 */
		public void Close() throws IOException {
			if (ssocket != null) {
				ServerSocket sss = ssocket;
				ssocket = null;
				sss.close();
			}
		}
	}
	/**
	 * UDPSocket supports sending and receiving UDPPackets. Sending packets is done by calling the Send method.
	 *When a packet arrives the PacketArrived event is raised with the packet.
	 *This example sends a string message to some other machine. When a packet arrives it converts it to string and shows it:
	 *<code>
	 *Sub process_globals
	 *	Dim UDPSocket1 As UDPSocket
	 *End Sub
	 *
	 *Sub Globals
	 *
	 *End Sub
	 *Sub Activity_Create(FirstTime As Boolean)
	 *	If FirstTime Then
	 *		UDPSocket1.Initialize("UDP", 0, 8000)
	 *	End If
	 *	Dim Packet As UDPPacket
	 *	Dim data() As Byte
	 *	data = "Hello from Android".GetBytes("UTF8")
	 *	Packet.Initialize(data, "10.0.0.1", 5000)
	 *	UDPSocket1.Send(Packet)
	 *End Sub
	 *Sub UDP_PacketArrived (Packet As UDPPacket)
	 *	Dim msg As String
	 *	msg = BytesToString(Packet.Data, Packet.Offset, Packet.Length, "UTF8")
	 *	Msgbox("Message received: " & msg, "")
	 *End Sub</code>
	 */
	@ShortName("UDPSocket")
	@Permissions(values = {"android.permission.INTERNET"})
	@Events(values={"PacketArrived (Packet As UDPPacket)"})
	public static class UDPSocket implements CheckForReinitialize{
		private UDPReader reader;
		private DatagramSocket ds;
		/**
		 * Initializes the socket and starts listening for packets.
		 *EventName - The name of the Sub that will handle the events.
		 *Port - Local port to listen on. Passing 0 will cause the OS to choose an available port automatically.
		 *ReceiveBufferSize - The size of the receiving packet. Packets larger than this value will be truncated.
		 *Pass 0 if you do not want to receive any packets.
		 */
		public void Initialize(BA ba, String EventName, int Port, int ReceiveBufferSize) throws SocketException {
			Close();
			if (Port == 0)
				ds = new DatagramSocket();
			else
				ds = new DatagramSocket(Port);
			if (ReceiveBufferSize > 0) {
				reader = new UDPReader();
				reader.working = true;
				reader.socket = ds;
				reader.receiveLength = ReceiveBufferSize;
				reader.ba = ba;
				reader.eventName = EventName.toLowerCase(BA.cul);
				Thread t = new Thread(reader);
				t.setDaemon(true);
				t.start();
			}
		}
		/**
		 * Tests whether this object is initialized.
		 */
		public boolean IsInitialized() {
			return ds != null && !ds.isClosed();
		}
		/**
		 * Gets the local port that this socket listens to.
		 */
		public int getPort() {
			return ds.getLocalPort();
		}
		
		/**
		 * Returns the network broadcast address.
		 *Note that the loopback broadcast address is 127.255.255.255.
		 */
		public String GetBroadcastAddress() throws SocketException {
			Enumeration<NetworkInterface> interfaces =
					NetworkInterface.getNetworkInterfaces();
			for (boolean ip6 : new boolean[] {false, true}) {
				while (interfaces.hasMoreElements()) {
					NetworkInterface networkInterface = interfaces.nextElement();
					if (networkInterface.isLoopback())
						continue;    
					for (InterfaceAddress interfaceAddress :
						networkInterface.getInterfaceAddresses()) {
						InetAddress broadcast = interfaceAddress.getBroadcast();
						if (broadcast == null)
							continue;
						if (broadcast instanceof Inet6Address == ip6)
							return broadcast.getHostAddress();
					}
				}
			}
			return "";
		}
		
		/**
		 * Sends a Packet. The packet will be sent in the background (asynchronously).
		 */
		public void Send(final UDPPacket Packet) throws IOException {
			BA.submitRunnable(new Runnable() {

				@Override
				public void run() {
					try {
						Packet.getObject().packet.setSocketAddress(new InetSocketAddress(Packet.getObject().host, Packet.getObject().port));
						ds.send(Packet.getObject().packet);
					} catch (Exception e) {
						BA.LogError(e.toString());
						throw new RuntimeException(e);
					}
				}

			}, this, 1);
		}

		/**
		 * Closes the socket.
		 */
		public void Close() {
			if (reader != null)
				reader.working = false;
			if (ds != null)
				ds.close();
			reader = null;
			ds = null;
		}
		@Override
		public String toString() {
			if (ds == null)
				return "Not initialized";
			return "Port=" + getPort();
		}
		private static class UDPReader implements Runnable {
			volatile boolean working;
			DatagramSocket socket;
			int receiveLength;
			BA ba;
			String eventName;
			@Override
			public void run() {
				while (working) {
					try {
						DatagramPacket p = new DatagramPacket(new byte[receiveLength], receiveLength);
						socket.receive(p);
						UDPPacket u = new UDPPacket();
						u.setObject(new MyDatagramPacket("", 0, p));
						ba.raiseEventFromDifferentThread(null, null, 0, eventName + "_packetarrived", false, new Object[] {u});
					} catch (IOException e) {
						if (working) {
							try {
								e.printStackTrace();
								Thread.sleep(100);
							} catch (InterruptedException e1) {
							}
						}
					}
				}
			}
		}
		/**
		 * A packet of data that is being sent or received.
		 *To send a packet call one of the Initialize methods and then send the packet by passing it to UDPSocket.Send.
		 *When a packet arrives you can get the data in the packet from the available properties.
		 */
		@ShortName("UDPPacket")
		public static class UDPPacket extends AbsObjectWrapper<MyDatagramPacket> {
			/**
			 * Initializes the packet and makes it ready for sending.
			 *Data - The data that will be send.
			 *Host - The target host name or IP address.
			 *Port - The target port.
			 */
			public void Initialize(byte[] Data, String Host, int Port) throws SocketException {
				Initialize2(Data, 0, Data.length, Host, Port);
			}
			/**
			 * Similar to Initialize. The data sent is based on the Offset and Length values.
			 */
			public void Initialize2(byte[] Data, int Offset, int Length, String Host, int Port) throws SocketException {

				DatagramPacket d = new DatagramPacket(Data, Offset, Length);
				MyDatagramPacket m = new MyDatagramPacket(Host, Port, d);
				setObject(m);
			}
			/**
			 * Gets the length of available bytes in the data. This can be shorter than the array length.
			 */
			public int getLength() {
				return getObject().packet.getLength();
			}
			/**
			 * Gets the data array received.
			 */
			public byte[] getData() {
				return getObject().packet.getData();
			}
			/**
			 * Gets the offset in the data array where the available data starts.
			 */
			public int getOffset() {
				return getObject().packet.getOffset();
			}
			/**
			 * Gets the port of the sending machine.
			 */
			public int getPort() {
				return getObject().packet.getPort();
			}
			/**
			 *<b>This method is deprecated and will not work properly on Android 4+ device.</b>
			 *Use HostAddress instead.
			 */
			public String getHost() {
				return getObject().packet.getAddress().getHostName();
			}
			/**
			 * Gets the IP address of the sending machine.
			 */
			public String getHostAddress() {
				return getObject().packet.getAddress().getHostAddress();
			}
			@Override
			public String toString() {
				if (getObjectOrNull() == null)
					return super.toString();
				return "Length=" + getLength() + ", Offset=" + getOffset() + ", Host=" + getHost() + ", Port=" + getPort();
			}
		}
		@Hide
		public static class MyDatagramPacket {
			public final String host;
			public final int port;
			public final DatagramPacket packet;
			public MyDatagramPacket(String host, int port, DatagramPacket packet) {
				this.host = host;
				this.port = port;
				this.packet = packet;
			}
		}
	}
}
