
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
 
 package anywheresoftware.b4h.okhttp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.http.RequestLine;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.streams.File;

/**
 * OkHttpClient allows you to make Http requests. Instead of using OkHttpClient directly it is recommended to use <link>OkHttpUtil2|https://www.b4x.com/android/forum/threads/b4x-okhttputils2-with-wait-for.79345/</link> 
 *modules which are much simpler to use.
 */
@Events(values={"ResponseSuccess (Response As OkHttpResponse, TaskId As Int)",
"ResponseError (Response As OkHttpResponse, Reason As String, StatusCode As Int, TaskId As Int)"})
@DependsOn(values={"okhttp-4.9.0", "okio-2.8.0", "okhttp-urlconnection-4.9.3", "kotlin-stdlib-1.6.10"})
@ShortName("OkHttpClient")
@Permissions(values = {"android.permission.INTERNET"})
@Version(1.50f)
public class OkHttpClientWrapper {
	@Hide
	public OkHttpClient client;
	private String eventName;

	/**
	 * Initializes this object.
	 *IMPORTANT: this object should be declared in Sub Process_Globals.
	 *EventName - The prefix that will be used for ResponseSuccess and ResponseError events.
	 */
	public void Initialize(String EventName) {
		client = sharedInit(EventName).build();
		
	}
	/**
	 * Similar to Initialize, with one important difference. All SSL certificates will be automatically accepted.
	 *<b>This method should only be used when trying to connect to a server located in a secured network</b>.
	 */
	public void InitializeAcceptAll(String EventName) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		OkHttpClient.Builder builder = sharedInit(EventName);
		builder.hostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}

		});
		final SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { new NaiveTrustManager()}, new java.security.SecureRandom());
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
				TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init((KeyStore) null);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
			throw new IllegalStateException("Unexpected default trust managers:"
					+ Arrays.toString(trustManagers));
		}
		X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

		builder.sslSocketFactory(sslSocketFactory, trustManager);
		client = builder.build();

		

	}
	@Hide
	public OkHttpClient.Builder sharedInit(String EventName) {
		okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
		this.eventName = EventName.toLowerCase(BA.cul);
		setTimeout(builder, 30000);
		CookieManager cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		builder.cookieJar(new JavaNetCookieJar(cm));
		return builder;
	}
	public boolean IsInitialized() {
		return client != null;
	}
	static void setTimeout(OkHttpClient.Builder builder, int TimeoutMs) {
		builder.connectTimeout(TimeoutMs, TimeUnit.MILLISECONDS);
		builder.writeTimeout(TimeoutMs, TimeUnit.MILLISECONDS);
		builder.readTimeout(TimeoutMs, TimeUnit.MILLISECONDS);
	}


	/**
	 * Executes the OkHttpRequest asynchronously. ResponseSuccess or ResponseError events will be fired later.
	 *If there is a request with the same TaskId already running then this method will return False and the new request will not be submitted.
	 */
	public boolean Execute(final BA ba, final OkHttpRequest HttpRequest, 
			final int TaskId) throws IOException {
		return ExecuteCredentials(ba, HttpRequest, TaskId, null, null);
	}
	/**
	 * Same behavior as Execute. The UserName and Password will be used for Basic authentication and Digest authentication.
	 */
	public boolean ExecuteCredentials(final BA ba, final OkHttpRequest Request, final int TaskId,
			final String UserName, final String Password) {
		if (BA.isTaskRunning(this, TaskId))
			return false;
		Runnable runnable = new ExecuteHelper(ba, Request, TaskId, UserName, Password);
		BA.submitRunnable(runnable, this, TaskId);
		return true;

	}
	/**
	 * Returns null if there was a timeout
	 */
	private Response executeWithTimeout(final Runnable handler, OkHttpClient myClient, 
			Request req, final BA ba,final int TaskId) throws IOException {
		//try {
		return myClient.newCall(req).execute();
		//		} catch (ConnectionPoolTimeoutException cpte) {
		//			BA.handler.postDelayed(new Runnable() {
		//				@Override
		//				public void run() {
		//					ba.submitRunnable(handler, OkHttpClientWrapper.this, TaskId);
		//				}
		//			}, 2000);
		//		}
		//		return null;
	}
	class ExecuteHelper implements Runnable {
		private BA ba;
		private OkHttpRequest HttpRequest;
		private int TaskId;
		private String UserName, Password;
		public ExecuteHelper (final BA ba, final OkHttpRequest HttpRequest, 
				final int TaskId, final String UserName, final String Password) {
			this.ba = ba;
			this.HttpRequest = HttpRequest;
			this.TaskId = TaskId;
			this.UserName = UserName;
			this.Password = Password;
		}
		@Override
		public void run() {
			Response response = null;
			OkHttpResponse res = new OkHttpResponse();
			res.innerInitialize(OkHttpClientWrapper.this);
			try {
				OkHttpClient.Builder builder = client.newBuilder();
				setTimeout(builder, HttpRequest.timeout);
				Request req = HttpRequest.builder.build();
				boolean recoverable = !(req.body() instanceof PostPayload) || ((PostPayload)req.body()).data != null;
				if (UserName != null && UserName.length() > 0) {
					builder.authenticator(new B4AAuthenticator(UserName, Password));
					if (req.body() instanceof PostPayload) {
						if (!recoverable) {
							//need to send the credentials
							String credential = Credentials.basic(UserName, Password);
							req = req.newBuilder().header("Authorization", credential).build();
						}
					}
				}
				builder.retryOnConnectionFailure(recoverable);
				response = executeWithTimeout(this, builder.build(), req, ba, TaskId);
				if (response == null)
					return;
				res.response = response;
				if (response.isSuccessful() == false)
					throw new Exception();
				ba.raiseEventFromDifferentThread(client, OkHttpClientWrapper.this, TaskId,
						eventName + "_responsesuccess", true, new Object[] {res, TaskId});
			} catch (Exception e) {
				String reason;
				int statusCode;
				if (response != null) {
					statusCode = response.code();
					reason = response.message();
					if (reason == null)
						reason = "";
				} else {
					e.printStackTrace();
					reason = e.toString();
					statusCode = -1;
				}
				if (response != null) {
					try {
						res.errorMessage = response.body().string();
					} catch (Exception ee) {
						ee.printStackTrace();
					}

				} 
				ba.raiseEventFromDifferentThread(client, OkHttpClientWrapper.this, TaskId,
						eventName + "_responseerror", 
						false, new Object[] {res, reason, statusCode, TaskId});
			}
		}
	}
	@Hide
	public static class B4AAuthenticator implements Authenticator
	{
		public final String username, password;
		public B4AAuthenticator(String username, String password) {
			this.username = username;
			this.password = password;
		}
		@Override
		public Request authenticate(Route route, Response response)
				throws IOException {
			if (responseCount(response) >= 3) {
				return null; // If we've failed 3 times, give up.
			}
			String raw = response.header("WWW-Authenticate");
			if (raw == null)
				raw = "";
			String v = raw.toLowerCase(BA.cul);
			String credential;
			if (v.contains("digest")) {
				credential = handleDigest(response, raw);
			}
			else {
				credential = Credentials.basic(username, password);
				if (credential.equals(response.request().header("Authorization"))) {
					return null; 
				}
			}
			return response.request().newBuilder()
					.header("Authorization", credential)
					.build();
		}
		private static Pattern ptDigest;

		private String handleDigest(Response response, String raw) throws IOException {
			Request request = response.request();
			String methodName = request.method();
			String uri = RequestLine.INSTANCE.requestPath(request.url());
			if (ptDigest == null)
				ptDigest = Pattern.compile("(\\w+)=\\\"([^\"]+)\\\"");
			Matcher m = ptDigest.matcher(raw);
			HashMap<String, String> params = new HashMap<String, String>();
			while (m.find()) {
				params.put(m.group(1), m.group(2));
			}
			String nonce = params.get("nonce");
			String realm = params.get("realm");

			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException();
			}
			StringBuilder temp = new StringBuilder();
			temp.append(username).append(":").append(realm).append(":").append(password);
			byte[] binary = md.digest(temp.toString().getBytes("ISO-8859-1"));
			String md5a1 = encode(binary);
			String a2 = methodName + ":" + uri;
			boolean qopMissing = raw.contains("qop") == false;
			String md5a2 = encode(md.digest(a2.getBytes("ASCII")));
			String serverDigestValue;
			String NC = "00000001";
			String cnonce = null;
			if (qopMissing) {
				StringBuilder tmp2 = new StringBuilder();
				tmp2.append(md5a1);
				tmp2.append(':');
				tmp2.append(nonce);
				tmp2.append(':');
				tmp2.append(md5a2);
				serverDigestValue = tmp2.toString();
			} else {
				String qopOption = "auth";
				cnonce = encode(md.digest(Long.toString(System.currentTimeMillis()).getBytes("ASCII")));

				StringBuilder tmp2 = new StringBuilder();
				tmp2.append(md5a1);
				tmp2.append(':');
				tmp2.append(nonce);
				tmp2.append(':');
				tmp2.append(NC);
				tmp2.append(':');
				tmp2.append(cnonce);
				tmp2.append(':');
				tmp2.append(qopOption);
				tmp2.append(':');
				tmp2.append(md5a2); 
				serverDigestValue = tmp2.toString();
			}

			String serverDigest =
					encode(md.digest(serverDigestValue.getBytes("ASCII")));
			StringBuilder sb = new StringBuilder();
			sb.append("Digest ").append(param("username", username, true)).append(",")
			.append(param("realm", realm, true)).append(",")
			.append(param("nonce", nonce, true)).append(",")
			.append(param("uri", uri, true)).append(",");
			if (!qopMissing) {
				sb.append(param("qop", "auth", false)).append(",")
				.append(param("nc", NC, false)).append(",")
				.append(param("cnonce", cnonce, true)).append(",");
			}
			sb.append(param("response", serverDigest, true));
			String opaque = params.get("opaque");
			if (opaque != null)
				sb.append(",").append(param("opaque", opaque, true));
			return sb.toString();


		}  
		private String param(String key, String value, boolean quote) {
			return key + "=" + (quote ? "\"" : "") + value + (quote ? "\"" : "");
		}
		private static final char[] HEXADECIMAL = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 
			'e', 'f'
		};
		private static String encode(byte[] binaryData) {
			if (binaryData.length != 16) {
				return null;
			} 

			char[] buffer = new char[32];
			for (int i = 0; i < 16; i++) {
				int low = (binaryData[i] & 0x0f);
				int high = ((binaryData[i] & 0xf0) >> 4);
				buffer[i * 2] = HEXADECIMAL[high];
				buffer[(i * 2) + 1] = HEXADECIMAL[low];
			}

			return new String(buffer);
		}
		private int responseCount(Response response) {
			int result = 1;
			while ((response = response.priorResponse()) != null) {
				result++;
			}
			return result;
		}



	}

	@ShortName("OkHttpRequest")
	public static class OkHttpRequest  {
		int timeout = 30000;
		@Hide
		public Builder builder;
		@Hide
		public PostPayload pp;
		/**
		 * Initializes the request and sets it to be a Http Get method.
		 */
		public void InitializeGet(String URL) {
			builder = new Builder().url(URL).get();
		}
		/**
		 * Initializes the request and sets it to be a Http Head method.
		 */
		public void InitializeHead(String URL) {
			builder = new Builder().url(URL).head();
		}
		/**
		 * Initializes the request and sets it to be a Http Delete method.
		 */
		public void InitializeDelete(String URL) {
			builder = new Builder().url(URL).delete();
		}
		/**
		 * Initializes the request and sets it to be a Http Delete method with the given payload.
		 */
		public void InitializeDelete2(String URL, byte[] Data) {
			pp = PostPayload.createFromArray(Data);
			builder = new Builder().url(URL).delete(pp);
		}

		/**
		 * Initializes the request and sets it to be a Http Post method.
		 *The specified InputStream will be read and added to the request.
		 */
		public void InitializePost(String URL, InputStream InputStream, int Length) {
			pp = PostPayload.createFromStream(InputStream, Length);
			builder = new Builder().url(URL).post(pp);
		}
		/**
		 * Initializes the request and sets it to be a Http Post method.
		 *The specified Data array will be added to the request.
		 *Unlike InitializePost this method will enable the request to retry and send the data several times in case of IO errors.
		 */
		public void InitializePost2(String URL, byte[] Data) {
			pp = PostPayload.createFromArray(Data);
			builder = new Builder().url(URL).post(pp);
		}

		/**
		 * Initializes the request and sets it to be a Http Put method.
		 *The specified InputStream will be read and added to the request.
		 */
		public void InitializePut(String URL, InputStream InputStream, int Length) {
			pp = PostPayload.createFromStream(InputStream, Length);
			builder = new Builder().url(URL).put(pp);
		}
		/**
		 * Initializes the request and sets it to be a Http Put method.
		 *The specified Data array will be added to the request.
		 *Unlike InitializePost this method will enable the request to retry and send the data several times in case of IO errors.
		 */
		public void InitializePut2(String URL, byte[] Data) {
			pp = PostPayload.createFromArray(Data);
			builder = new Builder().url(URL).put(pp);
		}
		/**
		 * Initializes the request and sets it to be a Http Patch method.
		 *The specified InputStream will be read and added to the request.
		 */
		public void InitializePatch(String URL, InputStream InputStream, int Length) {
			pp = PostPayload.createFromStream(InputStream, Length);
			builder = new Builder().url(URL).patch(pp);
		}
		/**
		 * Initializes the request and sets it to be a Http Patch method.
		 *The specified Data array will be added to the request.
		 *Unlike InitializePost this method will enable the request to retry and send the data several times in case of IO errors.
		 */
		public void InitializePatch2(String URL, byte[] Data) {
			pp = PostPayload.createFromArray(Data);
			builder = new Builder().url(URL).patch(pp);
		}
		/**
		 * Sets the value of the header with the given name. If no such header exists then a new header will be added.
		 */
		public void SetHeader(String Name, String Value) {
			builder.addHeader(Name, Value);
		}
		/**
		 * Removes all headers with the given name.
		 */
		public void RemoveHeaders(String Name) {
			builder.removeHeader(Name);
		}
		/**
		 * Gets or sets the request timeout, measured in milliseconds. Default value is 30,000 (30 seconds).
		 */
		public int getTimeout() {
			return timeout;
		}
		public void setTimeout(int t) {
			timeout = t;
		}
		/**
		 * Sets the Mime header of the request.
		 *This method should only be used with requests that have a payload.
		 */
		public void SetContentType(String ContentType) {
			if (pp == null)
				throw new RuntimeException("Request does not support this method.");
			pp.contentType = ContentType;
		}
		/**
		 * Sets the encoding header of the request.
		 */
		public void SetContentEncoding(String Encoding) {
			builder.header("Content-Encoding", Encoding);
		}
	}
	@Hide
	public static class PostPayload extends RequestBody{
		public String contentType = "application/x-www-form-urlencoded";
		private long contentLength = -1;
		private Source source;
		public byte[] data;


		public static PostPayload createFromStream(InputStream input, int Length) {
			PostPayload pp = new PostPayload();
			pp.source = Okio.source(input);
			pp.contentLength = Length;
			return pp;

		}
		public static PostPayload createFromArray(byte[] data) {
			PostPayload pp = new PostPayload();
			pp.data = data;
			return pp;
		}
		@Override
		public MediaType contentType() {
			MediaType mt = MediaType.parse(contentType);
			return mt;
		}
		@Override
		public long contentLength() throws IOException {
			if (data != null)
				return data.length;
			return contentLength;
		}

		@Override
		public void writeTo(BufferedSink sink) throws IOException {
			if (data != null)
				sink.write(data);
			else
				sink.write(source, contentLength);

		}

	}
	/**
	 * An object that holds the response returned from the server.
	 *The object is passed in the ResponseSuccess event.
	 *You can choose to read the response synchronously or asynchronously.
	 *It is important to release this object when it is not used anymore by calling Release.
	 */
	@ShortName("OkHttpResponse")
	@Events(values={"StreamFinish (Success As Boolean, TaskId As Int)"})
	public static class OkHttpResponse {
		private OkHttpClientWrapper parent;
		String errorMessage = "";
		private void innerInitialize(OkHttpClientWrapper parent) {
			this.parent = parent;
		}
		@Hide
		public Response response;
		/**
		 * Returns a Map object with the response headers.
		 *Each elements is made of a key which is the header name and a value which is a list containing the values (one or more).
		 *Example:<code>
		 *Dim list1 As List
		 *list1 = response.GetHeaders.Get("Set-Cookie")
		 *For i = 0 To list1.Size - 1
		 *	Log(list1.Get(i))
		 *Next</code>
		 */
		public Map GetHeaders() {
			return convertHeaders(response.headers().toMultimap());
		}
		/**
		 * Returns the Content-Type header.
		 */
		public String getContentType() {
			return response.header("Content-Type", "");
		}
		/**
		 * Returns the Content-Encoding header.
		 */
		public String getContentEncoding() {
			return response.header("Content-Encoding", "");
		}
		/**
		 * Returns the server response as a string (for failed responses only).
		 */
		public String getErrorResponse() {
			return errorMessage;
		}
		/**
		 * Returns the response body length.
		 */
		public long getContentLength() throws IOException {
			return response.body().contentLength();
		}
		static Map convertHeaders(java.util.Map<String, List<String>> headers) {
			Map m = new Map();
			m.Initialize();
			for (Entry<String, List<String>> e : headers.entrySet()) {
				m.Put(e.getKey(), e.getValue());
			}
			return m;
		}
		/**
		 * Returns the response Http code.
		 *Returns -1 is the status code is not available.
		 */
		public int getStatusCode() {
			return response == null ? -1 : response.code();
		}
		/**
		 * Frees resources allocated for this object.
		 */
		public void Release() throws IOException {
			if (response != null && response.body() != null)
				Util.closeQuietly(response.body().source());
		}
		/**
		 * Asynchronously reads the response and writes it to the given OutputStream.
		 *If there is a request with the same TaskId already running then this method will return False, and the response object will be released.
		 *The StreamFinish event will be raised after the response has been fully read.
		 *EventName - The sub that will handle the StreamFinish event.
		 *Output - The stream from the server will be written to this stream.
		 *CloseOutput - Whether to close the specified output stream when done.
		 *TaskId - The task id given to this task.
		 *Example:<code>
		 *Sub Http_ResponseSuccess (Response As OkHttpResponse, TaskId As Int)
		 *	Response.GetAsynchronously("ImageResponse", _ 
		 *		File.OpenOutput(File.DirInternalCache, "image.jpg", False), True, TaskId)
		 *End Sub
		 *
		 *Sub ImageResponse_StreamFinish (Success As Boolean, TaskId As Int)
		 *	If Success = False Then
		 *		Msgbox(LastException.Message, "Error")
		 *		Return
		 *	End If
		 *	ImageView1.Bitmap = LoadBitmap(File.DirInternalCache, "image.jpg")
		 *End Sub</code>
		 */
		public boolean GetAsynchronously(final BA ba, final String EventName,final OutputStream Output, 
				final boolean CloseOutput, 
				final int TaskId) throws IOException {
			if (BA.isTaskRunning(parent, TaskId)) {
				Release();
				return false;
			}
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						File.Copy2(response.body().byteStream(), Output);
						if (CloseOutput)
							Output.close();
						ba.raiseEventFromDifferentThread(OkHttpResponse.this, parent, TaskId, 
								EventName.toLowerCase(BA.cul) + 
								"_streamfinish", true,  new Object[] {true, TaskId});
					} catch (IOException e) {
						ba.setLastException(e);
						if (CloseOutput)
							try {
								Output.close();
							} catch (IOException e1) {
								//nothing to see here...
							}
						ba.raiseEventFromDifferentThread(OkHttpResponse.this, parent, TaskId,
								EventName.toLowerCase(BA.cul) + 
								"_streamfinish", true, new Object[] {false, TaskId});
					}
					response.body().close();

				}
			};
			BA.submitRunnable(runnable, parent, TaskId);
			return true;
		}
	}
	private static class NaiveTrustManager implements X509TrustManager
	{
		public void checkClientTrusted ( X509Certificate[] cert, String authType )
				throws CertificateException 
				{
			//
				}

		public void checkServerTrusted ( X509Certificate[] cert, String authType ) 
				throws CertificateException 
				{
			//
				}


		public X509Certificate[] getAcceptedIssuers ()
		{
			return null; 
		}
	}


}
