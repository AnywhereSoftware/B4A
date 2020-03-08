
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
 
 package anywheresoftware.b4a.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
/**
 * HttpClient allows you to make Http requests. Instead of using HttpClient directly it is recommended to use <link>HttpUtil2|http://www.basic4ppc.com/forum/showthread.php?p=109068</link> 
 *modules which are much simpler to use.
 */
@Events(values={"ResponseSuccess (Response As HttpResponse, TaskId As Int)",
"ResponseError (Response As HttpResponse, Reason As String, StatusCode As Int, TaskId As Int)"})
@ShortName("HttpClient")
@Permissions(values = {"android.permission.INTERNET"})
@Version(1.36f)
public class HttpClientWrapper implements CheckForReinitialize {
	private static final int maxConnectionToRoute = 5;
	@Hide
	public DefaultHttpClient client;
	/**
	 * The HTTP library allows you to communicate with web services and to download resources from the web.
	 *As network communication can be slow and fragile this library handles the requests and responses in the background and raises events when a task is ready.
	 */
	public static void LIBRARY_DOC() {
		//
	}
	private String eventName;
	/**
	 * Initializes this object.
	 *IMPORTANT: this object should be declared in Sub Process_Globals.
	 *EventName - The prefix that will be used for ResponseSuccess and ResponseError events.
	 */
	public void Initialize(String EventName) throws ClientProtocolException, IOException {
		initializeShared(EventName, SSLSocketFactory.getSocketFactory());
	}
	private void initializeShared(String EventName, SSLSocketFactory ssl) {
		eventName = EventName.toLowerCase(BA.cul);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
				new Scheme("http",PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(
				new Scheme("https", ssl, 443));
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRoute() {
			@Override
			public int getMaxForRoute(HttpRoute route) {
				return maxConnectionToRoute;
			}

		});
		ConnManagerParams.setTimeout(params, 100);
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

		client = new DefaultHttpClient(cm, params);
	}
	/**
	 * Similar to Initialize, with one important difference. All SSL certificates will be automatically accepted.
	 *<b>This method should only be used when trying to connect to a server located in a secured network</b>.
	 */
	public void InitializeAcceptAll(String EventName) throws KeyManagementException, NoSuchAlgorithmException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
		SSLContext context = SSLContext.getInstance ("TLS");
		context.init( new KeyManager[0], tm, new SecureRandom( ) );
		Constructor<SSLSocketFactory> c = SSLSocketFactory.class.getConstructor(javax.net.ssl.SSLSocketFactory.class);
		SSLSocketFactory ssl = c.newInstance(context.getSocketFactory());
		ssl.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		initializeShared(EventName, ssl);
		
	}
	public boolean IsInitialized() {
		return client != null;
	}
	
	/**
	 * Sets the value of the parameter with the given name.
	 */
	public void SetHttpParameter(String Name, Object Value) {
		client.getParams().setParameter(Name, Value);
	}
	/**
	 * Sets the proxy to use for the connections.
	 *Host - Proxy host name or IP.
	 *Port - Proxy port.
	 *Scheme - Scheme name. Usually "http".
	 */
	public void SetProxy(String Host, int Port, String Scheme) {
		HttpHost hh = new HttpHost(Host, Port, Scheme);
		client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, hh);
	}
	/**
	 * Sets the proxy to use for the connections, with the required credentials.
	 */
	public void SetProxy2(String Host, int Port, String Scheme, String Username, String Password) {
		HttpHost hh = new HttpHost(Host, Port, Scheme);
		client.getCredentialsProvider().setCredentials(new AuthScope(Host, Port), 
				new UsernamePasswordCredentials(Username, Password));
		client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, hh);
	}
	/**
	 * Executes the HttpRequest asynchronously. ResponseSuccess or ResponseError events will be fired later.
	 *Note that in many cases the Response object passed in ResponseError event will be Null.
	 *If there is a request with the same TaskId already running then this method will return False and the new request will not be submitted.
	 */
	public boolean Execute(final BA ba, final HttpUriRequestWrapper HttpRequest, 
			final int TaskId) throws ClientProtocolException, IOException {
		return ExecuteCredentials(ba, HttpRequest, TaskId, null, null);
	}
	/**
	 * Same behavior as Execute. The UserName and Password will be used for Basic or Digest authentication.
	 *Digest authentication is supported for GET requests and repeatable POST requests (requests with payloads based on an array of bytes).
	 */
	public boolean ExecuteCredentials(final BA ba, final HttpUriRequestWrapper HttpRequest, 
			final int TaskId, final String UserName, final String Password) throws ClientProtocolException, IOException {
		if (ba.isTaskRunning(this, TaskId))
			return false;
		Runnable runnable = new ExecuteHelper(ba, HttpRequest, TaskId, UserName, Password);
		ba.submitRunnable(runnable, this, TaskId);
		return true;
	}

	class ExecuteHelper implements Runnable {
		private BA ba;
		private HttpUriRequestWrapper HttpRequest;
		private int TaskId;
		private String UserName, Password;
		public ExecuteHelper (final BA ba, final HttpUriRequestWrapper HttpRequest, 
				final int TaskId, final String UserName, final String Password) {
			this.ba = ba;
			this.HttpRequest = HttpRequest;
			this.TaskId = TaskId;
			this.UserName = UserName;
			this.Password = Password;
		}
		@Override
		public void run() { //will be run by the thread pool
			HttpResponse response = null;
			try {
				//for unrepeatable entities we use Basic ahead of the request.
				if (HttpRequest.req instanceof HttpEntityEnclosingRequestBase && UserName != null && UserName.length() > 0) {
					HttpEntityEnclosingRequestBase base = (HttpEntityEnclosingRequestBase) HttpRequest.req;
					if (base.getEntity() != null && base.getEntity().isRepeatable() == false) {
						UsernamePasswordCredentials credentials = new
						UsernamePasswordCredentials(UserName, Password);
						BasicScheme scheme = new BasicScheme();
						Header authorizationHeader = scheme.authenticate(credentials, HttpRequest.req);
						HttpRequest.req.addHeader(authorizationHeader);
					}
				}
				response = executeWithTimeout(this, HttpRequest.req, ba, TaskId);
				if (response == null)
					return;
				if (response.getStatusLine().getStatusCode() == 401 && UserName != null && UserName.length() > 0) {
					boolean basic = false;
					boolean digest = false;
					Header challenge = null;
					for (Header h : response.getHeaders("WWW-Authenticate")) {
						String v = h.getValue().toLowerCase(BA.cul);
						if (v.contains("basic")) {
							basic = true;
						}
						else if (v.contains("digest")) {
							digest = true;	
							challenge = h;
						}

					}
					UsernamePasswordCredentials credentials = new
					UsernamePasswordCredentials(UserName, Password);
					if (response.getEntity() != null)
						response.getEntity().consumeContent();
					if (digest) {
						DigestScheme ds = new DigestScheme();
						ds.processChallenge(challenge);
						HttpRequest.req.addHeader(ds.authenticate(credentials, HttpRequest.req));
						response = executeWithTimeout(this, HttpRequest.req, ba, TaskId);
						if (response == null)
							return;
					}
					else if (basic) {
						BasicScheme scheme = new BasicScheme();
						Header authorizationHeader = scheme.authenticate(credentials, HttpRequest.req);
						HttpRequest.req.addHeader(authorizationHeader);
						response = executeWithTimeout(this, HttpRequest.req, ba, TaskId);
						if (response == null)
							return;
					}
				}
				if (response.getStatusLine().getStatusCode() / 100 != 2) {

					throw new Exception();
				}
				HttpResponeWrapper res = new HttpResponeWrapper();
				res.innerInitialize(HttpClientWrapper.this);
				res.response = response;
				ba.raiseEventFromDifferentThread(client, HttpClientWrapper.this, TaskId,
						eventName + "_responsesuccess", true, new Object[] {res, TaskId});
			} catch (Exception e) {
				String reason;
				int statusCode;
				if (response != null) {
					reason = response.getStatusLine().getReasonPhrase();
					statusCode = response.getStatusLine().getStatusCode();
				}
				else {
					e.printStackTrace();
					reason = e.toString();
					statusCode = -1;
				}
				Method m = ba.htSubs.get(eventName + "_responseerror");
				boolean shouldClose = true;
				if (m != null) {
					Object[] args;
					if (m.getParameterTypes().length == 4 || BA.shellMode) {
						HttpResponeWrapper res  = null;
						if (response != null) {
							res = new HttpResponeWrapper();
							res.innerInitialize(HttpClientWrapper.this);
							res.response = response;
							try {
								response.setEntity(new ByteArrayEntity(EntityUtils.toByteArray(response.getEntity())));
							} catch (Exception ee) {
								ee.printStackTrace();
							}
						}
						args = new Object[] {res, reason, statusCode, TaskId};
					}
					else
						args = new Object[] {reason, statusCode, TaskId};
					shouldClose = false;
					ba.raiseEventFromDifferentThread(client, HttpClientWrapper.this, TaskId,
							eventName + "_responseerror", 
							false, args);
				}
				if (shouldClose && response != null && response.getEntity() != null) {
					try {
						response.getEntity().consumeContent();
					} catch (IOException e1) {
						Log.w("B4A", e1);
					}
				}
			}
		}

	}
	/**
	 * Returns null if there was a timeout
	 */
	private HttpResponse executeWithTimeout(final Runnable handler, HttpUriRequest req, final BA ba,final int TaskId) throws ClientProtocolException, IOException {
		try {
			HttpResponse response = client.execute(req);
			return response;
		} catch (ConnectionPoolTimeoutException cpte) {
			BA.handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					ba.submitRunnable(handler, HttpClientWrapper.this, TaskId);
				}
			}, 2000);
		}
		return null;
	}
	/**
	 * Holds the target URL and other data sent to the web server.
	 *The initial time out is set to 30000 milliseconds (30 seconds).
	 */
	@ShortName("HttpRequest")
	public static class HttpUriRequestWrapper {
		private boolean POST;
		private AbstractHttpEntity entity;
		@Hide
		public HttpRequestBase req; //accessed with reflection from OAuth
		/**
		 * Initializes the request and sets it to be a Http Get method.
		 */
		public void InitializeGet(String URL) {
			req = new HttpGet(URL);
			POST = false;
			sharedInit();
		}
		/**
		 * Initializes the request and sets it to be a Http Head method.
		 */
		public void InitializeHead(String URL) {
			req = new HttpHead(URL);
			POST = false;
			sharedInit();
		}
		/**
		 * Initializes the request and sets it to be a Http Delete method.
		 */
		public void InitializeDelete(String URL) {
			req = new HttpDelete(URL);
			POST = false;
			sharedInit();
		}

		/**
		 * Initializes the request and sets it to be a Http Post method.
		 *The specified InputStream will be read and added to the request.
		 */
		public void InitializePost(String URL, InputStream InputStream, int Length) {
			HttpPost post = new HttpPost(URL);
			req = post;
			entity = new InputStreamEntity(InputStream, Length);
			post.setEntity(entity);
			entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
			POST = true;
			sharedInit();
		}
		/**
		 * Initializes the request and sets it to be a Http Put method.
		 *The specified InputStream will be read and added to the request.
		 */
		public void InitializePut(String URL, InputStream InputStream, int Length) {
			HttpPut post = new HttpPut(URL);
			req = post;
			entity = new InputStreamEntity(InputStream, Length);
			post.setEntity(entity);
			entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
			POST = true;
			sharedInit();
		}
		/**
		 * Initializes the request and sets it to be a Http Post method.
		 *The specified Data array will be added to the request.
		 *Unlike InitializePost this method will enable the request to retry and send the data several times in case of IO errors.
		 */
		public void InitializePost2(String URL, byte[] Data) {
			HttpPost post = new HttpPost(URL);
			req = post;
			entity = new ByteArrayEntity(Data); //byte entity!
			post.setEntity(entity);
			entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
			POST = true;
			sharedInit();
		}
		/**
		 * Initializes the request and sets it to be a Http Put method.
		 *The specified Data array will be added to the request.
		 */
		public void InitializePut2(String URL, byte[] Data) {
			HttpPut post = new HttpPut(URL);
			req = post;
			entity = new ByteArrayEntity(Data); //byte entity!
			post.setEntity(entity);
			entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
			POST = true;
			sharedInit();
		}
		private void sharedInit() {
			setTimeout(30000);
		}

		/**
		 * Sets the Mime header of the request.
		 *This method should only be used with Post or Put requests.
		 */
		public void SetContentType(String ContentType) {
			if (!POST)
				throw new RuntimeException("Only Post / Put requests support this method.");
			entity.setContentType(ContentType);
		}
		/**
		 *Sets the encoding header of the request.
		 *This method should only be used with Post or Put requests.
		 */
		public void SetContentEncoding(String Encoding) {
			if (!POST)
				throw new RuntimeException("Only Post / Put requests support this method.");
			entity.setContentEncoding(Encoding);
		}
		/**
		 * Sets the request timeout measured in milliseconds.
		 */
		public void setTimeout(int Timeout) {
			HttpConnectionParams.setConnectionTimeout(req.getParams(), Timeout);
			HttpConnectionParams.setSoTimeout(req.getParams(), Timeout);
		}
		/**
		 * Sets the value of the first header with the given name. If no such header exists then a new header will be added.
		 */
		public void SetHeader(String Name, String Value) {
			req.setHeader(Name, Value);
		}
		/**
		 * Removes all headers with the given name.
		 */
		public void RemoveHeaders(String Name) {
			req.removeHeaders(Name);
		}

	}
	/**
	 * An object that holds the response returned from the server.
	 *The object is passed in the ResponseSuccess event.
	 *You can choose to read the response synchronously or asynchronously.
	 *It is important to release this object when it is not used anymore by calling Release.
	 */
	@Events(values={"StreamFinish (Success As Boolean, TaskId As Int)"})
	@ShortName("HttpResponse")
	public static class HttpResponeWrapper {
		private HttpClientWrapper parent;
		private HttpResponse response;
		private void innerInitialize(HttpClientWrapper parent) {
			this.parent = parent;
		}
		/**
		 *<b>This method is deprecated and will not work properly on Android 4+ device.</b>
		 *Use GetAsynchronously instead.
		 */
		public InputStreamWrapper GetInputStream() throws IllegalStateException, IOException {
			InputStreamWrapper isw = new InputStreamWrapper();
			isw.setObject(response.getEntity().getContent());
			return isw;
		}
		/**
		 *<b>This method is deprecated and will not work properly on Android 4+ device.</b>
		 *Use GetAsynchronously instead.
		 */
		public String GetString(String DefaultCharset) throws ParseException, IOException {
			if (response.getEntity() == null)
				return "";
			return EntityUtils.toString(response.getEntity(), DefaultCharset);
		}
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
			return convertHeaders(response.getAllHeaders());
		}
		@SuppressWarnings("unchecked")
		static Map convertHeaders(Header[] headers) {
			Map m = new Map();
			m.Initialize();
			for (Header h : headers) {
				List<Object> l = (List<Object>) m.Get(h.getName());
				if (l == null) {
					anywheresoftware.b4a.objects.collections.List ll = new anywheresoftware.b4a.objects.collections.List();
					ll.Initialize();
					l = ll.getObject();
					m.Put(h.getName(), l);
				}
				l.add(h.getValue());
			}
			return m;
		}
		/**
		 * Returns the content type header.
		 */
		public String getContentType() {
			return response.getEntity().getContentType().getValue();
		}
		/**
		 * Frees resources allocated for this object.
		 */
		public void Release() throws IOException {
			if (response != null && response.getEntity() != null)
				response.getEntity().consumeContent();
		}
		/**
		 * Returns the content encoding header.
		 */
		public String getContentEncoding() {
			return response.getEntity().getContentEncoding().getValue();
		}
		/**
		 * Returns the content length header.
		 */
		public long getContentLength() {
			return response.getEntity().getContentLength();
		}
		/**
		 * Returns the response Http code.
		 */
		public int getStatusCode() {
			return response.getStatusLine().getStatusCode();
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
		 *Sub Http_ResponseSuccess (Response As HttpResponse, TaskId As Int)
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
			if (ba.isTaskRunning(parent, TaskId)) {
				Release();
				return false;
			}

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					boolean abortConnection = false;
					try {
						response.getEntity().writeTo(Output);
						if (CloseOutput)
							Output.close();
						ba.raiseEventFromDifferentThread(response, parent, TaskId, 
								EventName.toLowerCase(BA.cul) + 
								"_streamfinish", true,  new Object[] {true, TaskId});
					}
					catch (IOException e) {
						abortConnection = true;
						ba.setLastException(e);
						if (CloseOutput)
							try {
								Output.close();
							} catch (IOException e1) {
								//nothing to see here...
							}
							ba.raiseEventFromDifferentThread(response, parent, TaskId,
									EventName.toLowerCase(BA.cul) + 
									"_streamfinish", true, new Object[] {false, TaskId});
					}
					try { 
						if (abortConnection && response.getEntity() instanceof ConnectionReleaseTrigger)
							((ConnectionReleaseTrigger)response.getEntity()).abortConnection();
						else {
							response.getEntity().consumeContent();
						}

					} catch (IOException e) {
						Log.w("B4A", e);
					}
				}
			};
			ba.submitRunnable(runnable, parent, TaskId);
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
