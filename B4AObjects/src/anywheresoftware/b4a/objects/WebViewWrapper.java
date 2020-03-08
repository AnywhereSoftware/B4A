package anywheresoftware.b4a.objects;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;

/**
 * The WebView view uses the internal WebKit engine to display Html pages.
 *The page displayed can be an online page loaded with LoadUrl or a Html string loaded with LoadHtml.
 *The PageFinished event is raised after the page loads.
 *OverrideUrl is called before loading any Url. If this method returns True then the Url will not be loaded.
 *You can use this event as a way to handle click events in your code.
 *UserAndPasswordRequired event is raised when accessing a site that requires basic authentication.
 *You should return an array of strings with the username as the first element and password as the second element.
 *For example:<code>Return Array As String("someuser", "password123")</code>
 *Returning Null will cancel the request.
 *Sending incorrect credentials will cause this event to be raised again. 
 */
@ShortName("WebView")
@ActivityObject
@DontInheritEvents
@Permissions(values={"android.permission.INTERNET"})
@Events(values={"PageFinished (Url As String)", "OverrideUrl (Url As String) As Boolean",
		"UserAndPasswordRequired (Host As String, Realm As String) As String()"})
public class WebViewWrapper extends ViewWrapper<WebView>{

	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject) {
			setObject(new WebView(ba.context));
			getObject().getSettings().setJavaScriptEnabled(true);
			getObject().getSettings().setBuiltInZoomControls(true);
		}
		super.innerInitialize(ba, eventName, true);
		getObject().setWebViewClient(new WebViewClient() {
			@Override
            public void onPageFinished(WebView view, String url) {
            	ba.raiseEvent(getObject(), eventName + "_pagefinished", url);
            }
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Boolean b = (Boolean) ba.raiseEvent(getObject(), eventName + "_overrideurl", url);
				if (b != null)
					return b.booleanValue();
		        return false;
		    }
			 @Override
			public void onReceivedHttpAuthRequest(WebView view,
			            HttpAuthHandler handler, String host, String realm) {
				 Object o = ba.raiseEvent(getObject(), eventName + "_userandpasswordrequired", host, realm);
				 if (o == null) {
					 handler.cancel();
				 }
				 else {
					 String[] s = (String[]) o;
					 handler.proceed(s[0], s[1]);
				 }
			 }
			 
		});
		getObject().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });

	}
	
	/**
	 * Loads the given Url.
	 *Example:<code>
	 *WebView1.LoadUrl("http://www.google.com")</code>
	 */
	public void LoadUrl(String Url) {
		getObject().loadUrl(Url);
	}
	/**
	 * Loads the given Html.
	 *Example:<code>
	 *WebView1.LoadHtml("<html><body>Hello world!</body></html>")</code>
	 *You can use "file:///android_asset" to access files added with the file manager:
	 *<code>WebView1.LoadHtml("<html><body><img src='file:///android_asset/someimage.jpg'/></body></html>")</code>
	 *Note that files added with the file manager should be accessed with a lower cased name. 
	 */
	public void LoadHtml(String Html) {
		getObject().loadDataWithBaseURL("file:///", Html, "text/html", "UTF8", null);
	}
	/**
	 * Stops the current load.
	 */
	public void StopLoading() {
		getObject().stopLoading();
	}
	/**
	 * Returns the complete html page as a bitmap.
	 */
	public BitmapWrapper CaptureBitmap() {
		Picture pic = getObject().capturePicture();
		BitmapWrapper bw = new BitmapWrapper();
		bw.InitializeMutable(pic.getWidth(), pic.getHeight());
		CanvasWrapper cw = new CanvasWrapper();
		cw.Initialize2(bw.getObject());
		pic.draw(cw.canvas);
		return bw;
	}
	/**
	 * Returns the current Url.
	 */
	public String getUrl() {
		return getObject().getUrl();
	}
	/**
	 * Gets or sets whether JavaScript is enabled.
	 *JavaScript is enabled by default.
	 */
	public boolean getJavaScriptEnabled() {
		return getObject().getSettings().getJavaScriptEnabled();
	}
	public void setJavaScriptEnabled(boolean value) {
		getObject().getSettings().setJavaScriptEnabled(value);
	}
	/**
	 * Gets or sets whether the internal zoom feature is enabled.
	 *The zoom feature is enabled by default.
	 */
	public void setZoomEnabled(boolean v) {
		getObject().getSettings().setBuiltInZoomControls(v);
		if (Build.VERSION.SDK_INT >= 11)
			getObject().getSettings().setDisplayZoomControls(v);
	}
	public boolean getZoomEnabled() {
		return getObject().getSettings().getBuiltInZoomControls();
	}
	/**
	 * Zooms in or out according to the value of In.
	 *Returns true if zoom has changed.
	 */
	public boolean Zoom(boolean In) {
		if (In)
			return getObject().zoomIn();
		else
			return getObject().zoomOut();
	}
	/**
	 * Goes back to the previous Url.
	 */
	public void Back() {
		getObject().goBack();
	}
	/**
	 * Goes forward to the next Url.
	 */
	public void Forward() {
		getObject().goForward();
	}
	@Hide
	public static View build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{

		if (prev == null) {
			if (designer) {
				View v = new View((Context)tag);
				InputStream in = ((Context)tag).getAssets().open("webview.jpg");
				BitmapDrawable bd = new BitmapDrawable(in);
				in.close();
				v.setBackgroundDrawable(bd);
				prev = v;
			}
			else {
				WebView wv = ViewWrapper.buildNativeView((Context)tag, WebView.class, props, designer);
				wv.getSettings().setJavaScriptEnabled((Boolean)props.get("javaScriptEnabled"));
				wv.getSettings().setBuiltInZoomControls((Boolean)props.get("zoomEnabled"));
				if (Build.VERSION.SDK_INT >= 11)
					wv.getSettings().setDisplayZoomControls((Boolean)props.get("zoomEnabled"));
				prev = wv;
			}
		}
		ViewWrapper.build(prev, props, designer);
		return (View)prev;
	}

}
