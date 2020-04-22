///////////////////////////////////////////////////////////////////////////////
//
//   AutobahnJava - http://crossbar.io/autobahn
//
//   Copyright (c) Crossbar.io Technologies GmbH and contributors
//
//   Licensed under the MIT License.
//   http://www.opensource.org/licenses/mit-license.php
//
///////////////////////////////////////////////////////////////////////////////

package io.crossbar.autobahn.websocket;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import anywheresoftware.b4a.BA;



public class WebSocketConnection implements WebSocket {

	private static final boolean DEBUG = true;
	private static final String TAG = WebSocketConnection.class.getName();

	protected Handler mMasterHandler;

	protected WebSocketReader mReader;
	protected WebSocketWriter mWriter;
	protected HandlerThread mWriterThread;

	protected Socket mSocket;
	private URI mWsUri;
	private String mWsScheme;
	private String mWsHost;
	private int mWsPort;
	private String mWsPath;
	private String mWsQuery;
	private String[] mWsSubprotocols;
	private Map<String, String> mWsHeaders;

	private WebSocket.ConnectionHandler mWsHandler;

	protected WebSocketOptions mOptions;

	private boolean mActive;
	private boolean mPrevConnected;
	private boolean onCloseCalled;
	public TrustManager[] customTrustManager; //b4x

	/**
	 * Asynchronous socket connector.
	 */
	private class WebSocketConnector extends Thread {

		public void run() {
			Thread.currentThread().setName("WebSocketConnector");

			/*
			 * connect TCP socket
			 */
			try {
				if (mWsScheme.equals("wss")) {
					if (customTrustManager != null) {
						SSLContext context = SSLContext.getInstance("TLS");
						context.init(null, customTrustManager, /*SecureRandom*/ null);
						SSLSocketFactory ssf = context.getSocketFactory();
						mSocket = ssf.createSocket();
					} else {
						mSocket = SSLSocketFactory.getDefault().createSocket();
					}
				} else {
					mSocket = SocketFactory.getDefault().createSocket();
				}

				// the following will block until connection was established or
				// an error occurred!
				mSocket.connect(new InetSocketAddress(mWsHost, mWsPort), mOptions.getSocketConnectTimeout());

				// before doing any data transfer on the socket, set socket
				// options
				mSocket.setSoTimeout(mOptions.getSocketReceiveTimeout());
				mSocket.setTcpNoDelay(mOptions.getTcpNoDelay());

			} catch (Exception e) {
				e.printStackTrace();
				onClose(WebSocketConnectionHandler.CLOSE_CANNOT_CONNECT,
						e.getMessage());
				return;
			}

			if (isConnected()) {

				try {

					// create & start WebSocket reader
					createReader();

					// create & start WebSocket writer
					createWriter();

					// start WebSockets handshake
					WebSocketMessage.ClientHandshake hs = new WebSocketMessage.ClientHandshake(
							mWsHost + ":" + mWsPort);
					hs.mPath = mWsPath;
					hs.mQuery = mWsQuery;
					hs.mSubprotocols = mWsSubprotocols;
					hs.mHeaderList = mWsHeaders;
					mWriter.forward(hs);

					mPrevConnected = true;

				} catch (Exception e) {
					onClose(WebSocketConnectionHandler.CLOSE_INTERNAL_ERROR,
							e.getMessage());
				}
			} else {
				onClose(WebSocketConnectionHandler.CLOSE_CANNOT_CONNECT,
						"Could not connect to WebSocket server");
			}
		}
	}

	public WebSocketConnection() {
		if (DEBUG) Log.d(TAG, "created");

		// create WebSocket master handler
		createHandler();

		// set initial values
		mActive = false;
		mPrevConnected = false;
	}


	public void sendTextMessage(String payload) {
		mWriter.forward(new WebSocketMessage.TextMessage(payload));
	}


	public void sendRawTextMessage(byte[] payload) {
		mWriter.forward(new WebSocketMessage.RawTextMessage(payload));
	}


	public void sendBinaryMessage(byte[] payload) {
		mWriter.forward(new WebSocketMessage.BinaryMessage(payload));
	}


	public boolean isConnected() {
		return mSocket != null && mSocket.isConnected() && !mSocket.isClosed();
	}


	private void closeReaderThread(boolean waitForQuit) {
		if (mReader != null) {
			mReader.quit();
			if (waitForQuit) {
				try {
					mReader.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			if (DEBUG) Log.d(TAG, "mReader already NULL");
		}
	}

	private void closeWriterThread() {
		if (mWriter != null) {
			//mWriterThread.getLooper().quit();
			mWriter.forward(new WebSocketMessage.Quit());
			try {
				mWriterThread.join();
			} catch (InterruptedException e) {
				if (DEBUG) e.printStackTrace();
			}
			//mWriterThread = null;
		} else {
			if (DEBUG) Log.d(TAG, "mWriter already NULL");
		}
	}


	private void failConnection(int code, String reason) {
		if (DEBUG) Log.d(TAG, "fail connection [code = " + code + ", reason = " + reason);

		closeReaderThread(false);

		closeWriterThread();

		if (isConnected()) {
			closeSocket(); //b4x
			
			//mTransportChannel = null;
		} else {
			if (DEBUG) Log.d(TAG, "mTransportChannel already NULL");
		}

		closeReaderThread(true);

		onClose(code, reason);

		if (DEBUG) Log.d(TAG, "worker threads stopped");
	}


	public void connect(String wsUri, WebSocket.ConnectionHandler wsHandler) throws WebSocketException {
		connect(wsUri, null, wsHandler, new WebSocketOptions(), null);
	}


	public void connect(String wsUri, WebSocket.ConnectionHandler wsHandler, WebSocketOptions options) throws WebSocketException {
		connect(wsUri, null, wsHandler, options, null);
	}


	public void connect(String wsUri, String[] wsSubprotocols, WebSocket.ConnectionHandler wsHandler, WebSocketOptions options, Map<String, String> headers) throws WebSocketException {

		// don't connect if already connected .. user needs to disconnect first
		//
		if (isConnected()) {
			throw new WebSocketException("already connected");
		}

		// parse WebSockets URI
		//
		try {
			mWsUri = new URI(wsUri);

			if (!mWsUri.getScheme().equals("ws") && !mWsUri.getScheme().equals("wss")) {
				throw new WebSocketException("unsupported scheme for WebSockets URI");
			}

			mWsScheme = mWsUri.getScheme();

			if (mWsUri.getPort() == -1) {
				if (mWsScheme.equals("ws")) {
					mWsPort = 80;
				} else {
					mWsPort = 443;
				}
			} else {
				mWsPort = mWsUri.getPort();
			}

			if (mWsUri.getHost() == null) {
				throw new WebSocketException("no host specified in WebSockets URI");
			} else {
				mWsHost = mWsUri.getHost();
			}

			if (mWsUri.getRawPath() == null || mWsUri.getRawPath().equals("")) {
				mWsPath = "/";
			} else {
				mWsPath = mWsUri.getRawPath();
			}

			if (mWsUri.getRawQuery() == null || mWsUri.getRawQuery().equals("")) {
				mWsQuery = null;
			} else {
				mWsQuery = mWsUri.getRawQuery();
			}

		} catch (URISyntaxException e) {

			throw new WebSocketException("invalid WebSockets URI");
		}

		mWsSubprotocols = wsSubprotocols;
		mWsHeaders = headers;
		mWsHandler = wsHandler;

		// make copy of options!
		mOptions = new WebSocketOptions(options);

		// set connection active
		mActive = true;

		// reset value
		onCloseCalled = false;

		// use async connector on short-lived background thread
		new WebSocketConnector().start();
	}


	public void disconnect() {
		// Close the writer thread here but delay the closing of reader thread
		// as we need to have active connection to be able to process the response
		// of this close request.
		if (mWriter != null) {
			mWriter.forward(new WebSocketMessage.Close(1000));
		} else {
			if (DEBUG) Log.d(TAG, "could not send Close .. writer already NULL");
		}
		onCloseCalled = false;
		mActive = false;
		mPrevConnected = false;
	}

	/**
	 * Reconnect to the server with the latest options
	 *
	 * @return true if reconnection performed
	 */
	public boolean reconnect() {
		if (!isConnected() && (mWsUri != null)) {
			new WebSocketConnector().start();
			return true;
		}
		return false;
	}

	/**
	 * Perform reconnection
	 *
	 * @return true if reconnection was scheduled
	 */
	protected boolean scheduleReconnect() {
		/**
		 * Reconnect only if:
		 *  - connection active (connected but not disconnected)
		 *  - has previous success connections
		 *  - reconnect interval is set
		 */
		int interval = mOptions.getReconnectInterval();
		boolean need = mActive && mPrevConnected && (interval > 0);
		if (need) {
			if (DEBUG) Log.d(TAG, "Reconnection scheduled");
			mMasterHandler.postDelayed(new Runnable() {

				public void run() {
					if (DEBUG) Log.d(TAG, "Reconnecting...");
					reconnect();
				}
			}, interval);
		}
		return need;
	}

	/**
	 * Common close handler
	 *
	 * @param code   Close code.
	 * @param reason Close reason (human-readable).
	 */
	private void onClose(int code, String reason) {
		boolean reconnecting = false;

		if ((code == ConnectionHandler.CLOSE_CANNOT_CONNECT) ||
				(code == ConnectionHandler.CLOSE_CONNECTION_LOST)) {
			reconnecting = scheduleReconnect();
		}


		if (mWsHandler != null) {
			try {
				if (reconnecting) {
					mWsHandler.onClose(ConnectionHandler.CLOSE_RECONNECT, reason);
				} else {
					mWsHandler.onClose(code, reason);
				}
			} catch (Exception e) {
				if (DEBUG) e.printStackTrace();
			}
			//mWsHandler = null;
		} else {
			if (DEBUG) Log.d(TAG, "mWsHandler already NULL");
		}
		onCloseCalled = true;
	}

	private void closeAndCleanup() {
		// Close the reader thread but don't wait for it to quit because
		// the blocking call to BufferedInputStream.read() can take a
		// a few seconds in some cases to unblock. We call this method later
		// a few lines below *after* we close the socket, because as soon as
		// the Socket.close() is called, BufferedInputStream.read() throws.
		// So this gives us quick cleaning of resources.
		closeReaderThread(false);
		closeWriterThread();
		closeSocket(); //b4x
		closeReaderThread(true);
		onCloseCalled = false;
	}

	//b4x change
	private void closeSocket() {
		if (mSocket != null) {
			final Socket s = mSocket;
			mSocket = null;
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
			if (mSocket instanceof SSLSocket) {
				BA.submitRunnable(r, null, 0);
			} else {
				r.run();
			}
		}
	}


	/**
	 * Create master message handler.
	 */
	protected void createHandler() {

		mMasterHandler = new Handler(Looper.getMainLooper()) {

			public void handleMessage(Message msg) {
				// We have received the closing handshake and replied to it, discard
				// anything received after that.
				if (onCloseCalled) {
					if (DEBUG) Log.d(TAG, "onClose called already, ignore message.");

					return;
				}

				if (msg.obj instanceof WebSocketMessage.TextMessage) {

					WebSocketMessage.TextMessage textMessage = (WebSocketMessage.TextMessage) msg.obj;

					if (mWsHandler != null) {
						mWsHandler.onTextMessage(textMessage.mPayload);
					} else {
						if (DEBUG) Log.d(TAG, "could not call onTextMessage() .. handler already NULL");
					}

				} else if (msg.obj instanceof WebSocketMessage.RawTextMessage) {

					WebSocketMessage.RawTextMessage rawTextMessage = (WebSocketMessage.RawTextMessage) msg.obj;

					if (mWsHandler != null) {
						mWsHandler.onRawTextMessage(rawTextMessage.mPayload);
					} else {
						if (DEBUG) Log.d(TAG, "could not call onRawTextMessage() .. handler already NULL");
					}

				} else if (msg.obj instanceof WebSocketMessage.BinaryMessage) {

					WebSocketMessage.BinaryMessage binaryMessage = (WebSocketMessage.BinaryMessage) msg.obj;

					if (mWsHandler != null) {
						mWsHandler.onBinaryMessage(binaryMessage.mPayload);
					} else {
						if (DEBUG) Log.d(TAG, "could not call onBinaryMessage() .. handler already NULL");
					}

				} else if (msg.obj instanceof WebSocketMessage.Ping) {

					WebSocketMessage.Ping ping = (WebSocketMessage.Ping) msg.obj;
					if (DEBUG) Log.d(TAG, "WebSockets Ping received");

					// reply with Pong
					WebSocketMessage.Pong pong = new WebSocketMessage.Pong();
					pong.mPayload = ping.mPayload;
					mWriter.forward(pong);

				} else if (msg.obj instanceof WebSocketMessage.Pong) {

					@SuppressWarnings("unused")
					WebSocketMessage.Pong pong = (WebSocketMessage.Pong) msg.obj;

					if (DEBUG) Log.d(TAG, "WebSockets Pong received");

				} else if (msg.obj instanceof WebSocketMessage.Close) {

					WebSocketMessage.Close close = (WebSocketMessage.Close) msg.obj;

					final int crossbarCloseCode = (close.mCode == 1000) ? ConnectionHandler.CLOSE_NORMAL : ConnectionHandler.CLOSE_CONNECTION_LOST;

					if (close.mIsReply) {
						if (DEBUG) Log.d(TAG, "WebSockets Close received (" + close.mCode + " - " + close.mReason + ")");
						closeAndCleanup();
						onClose(crossbarCloseCode, close.mReason);
					} else if (mActive) {
						// We have received a close frame, lets clean.
						closeReaderThread(false);
						mWriter.forward(new WebSocketMessage.Close(1000, true));
						mActive = false;
					} else {
						if (DEBUG) Log.d(TAG, "WebSockets Close received (" + close.mCode + " - " + close.mReason + ")");
						// we've initiated disconnect, so ready to close the channel
						closeAndCleanup();
						onClose(crossbarCloseCode, close.mReason);
					}

				} else if (msg.obj instanceof WebSocketMessage.ServerHandshake) {

					WebSocketMessage.ServerHandshake serverHandshake = (WebSocketMessage.ServerHandshake) msg.obj;

					if (DEBUG) Log.d(TAG, "opening handshake received");

					if (serverHandshake.mSuccess) {
						if (mWsHandler != null) {
							mWsHandler.onOpen();
							if (DEBUG) Log.d(TAG, "onOpen() called, ready to rock.");
						} else {
							if (DEBUG) Log.d(TAG, "could not call onOpen() .. handler already NULL");
						}
					}

				} else if (msg.obj instanceof WebSocketMessage.ConnectionLost) {

					@SuppressWarnings("unused")
					WebSocketMessage.ConnectionLost connnectionLost = (WebSocketMessage.ConnectionLost) msg.obj;
					failConnection(WebSocketConnectionHandler.CLOSE_CONNECTION_LOST, "WebSockets connection lost");

				} else if (msg.obj instanceof WebSocketMessage.ProtocolViolation) {

					@SuppressWarnings("unused")
					WebSocketMessage.ProtocolViolation protocolViolation = (WebSocketMessage.ProtocolViolation) msg.obj;
					failConnection(WebSocketConnectionHandler.CLOSE_PROTOCOL_ERROR, "WebSockets protocol violation");

				} else if (msg.obj instanceof WebSocketMessage.Error) {

					WebSocketMessage.Error error = (WebSocketMessage.Error) msg.obj;
					failConnection(WebSocketConnectionHandler.CLOSE_INTERNAL_ERROR, "WebSockets internal error (" + error.mException.toString() + ")");

				} else if (msg.obj instanceof WebSocketMessage.ServerError) {

					WebSocketMessage.ServerError error = (WebSocketMessage.ServerError) msg.obj;
					failConnection(WebSocketConnectionHandler.CLOSE_SERVER_ERROR, "Server error " + error.mStatusCode + " (" + error.mStatusMessage + ")");

				} else {

					processAppMessage(msg.obj);

				}
			}
		};
	}


	protected void processAppMessage(Object message) {
	}


	/**
	 * Create WebSockets background writer.
	 */
	protected void createWriter() throws IOException {

		mWriterThread = new HandlerThread("WebSocketWriter");
		mWriterThread.start();
		mWriter = new WebSocketWriter(mWriterThread.getLooper(), mMasterHandler, mSocket, mOptions);

		if (DEBUG) Log.d(TAG, "WS writer created and started");
	}


	/**
	 * Create WebSockets background reader.
	 */
	protected void createReader() throws IOException {

		mReader = new WebSocketReader(mMasterHandler, mSocket, mOptions, "WebSocketReader");
		mReader.start();

		if (DEBUG) Log.d(TAG, "WS reader created and started");
	}
}
