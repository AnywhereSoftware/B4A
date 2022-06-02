package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;


import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.realname.AircraftBindingState;
import dji.common.realname.AircraftBindingState.AircraftBindingStateListener;
import dji.common.realname.AppActivationState;
import dji.common.realname.AppActivationState.AppActivationStateListener;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.base.BaseProduct.ComponentKey;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.DJISDKManager.SDKManagerCallback;
import dji.sdk.useraccount.UserAccountManager;

@ShortName("DJISDKManager")
@Version(4.80f)
@DependsOn(values={"dji-sdk-4.16.aar",  "utmiss-1.2.1.aar", "gson-2.8.2.jar", "library-networkrtk-helper-2.0.2.aar", "wire-runtime-2.2.0.jar", "bcprov-jdk15on-1.57", "bcpkix-jdk15on-1.57", "okio-2.8.0",
	"disruptor-3.3.9", "lost-3.0.4.aar", "eventbus-3.0.0", "androidx.constraintlayout:constraintlayout", "androidx.appcompat:appcompat", "androidx.core:core", "androidx.constraintlayout:constraintlayout", "androidx.recyclerview:recyclerview",
	"androidx.lifecycle:lifecycle-extensions", "androidx.annotation:annotation", "rxandroid-2.1.0.aar", "subsampling-scale-image-view-3.10.0.aar"})
@Events(values={"RegisteredResult (Success As Boolean, ErrorMessage As String)", "ActivationStateChanged (State As String)",
		"ProductConnected (AircraftData As Object)", "ProductDisconnected", "BindingStateChanged (State As String)"})
	public class DJISDKManagerWrapper  {
	@Hide
	public DJISDKManager manager;
	private BA ba;
	private String eventName;
	private boolean activationListenerAdded;
	public boolean IsInitialized() {
		return manager != null;
	}
	/**
	 * Initializes the DJI SDK. The RegisteredResult event will be raised.
	 *Note that an internet connection is required on the first run.
	 */
	public void Initialize(final BA ba, String EventName) {
		manager = DJISDKManager.getInstance();
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		DJISDKManager.getInstance().registerApp(ba.context, new SDKManagerCallback() {
			
			@Override
			public void onRegister(DJIError paramDJIError) {
				ba.raiseEventFromDifferentThread(DJISDKManagerWrapper.this, null, 0, eventName + "_registeredresult", true, 
						new Object[] {paramDJIError == DJISDKError.REGISTRATION_SUCCESS,
						paramDJIError.getDescription()});
				if (activationListenerAdded == false && manager.getAppActivationManager() != null) {
					activationListenerAdded = true;
					manager.getAppActivationManager().addAppActivationStateListener(new AppActivationStateListener() {

						@Override
						public void onUpdate(AppActivationState arg0) {
							ba.raiseEventFromDifferentThread(DJISDKManagerWrapper.this, null, 0, eventName + "_activationstatechanged", true, new Object[] {arg0.toString()});
						}

					});
					manager.getAppActivationManager().addAircraftBindingStateListener(new AircraftBindingStateListener() {
						
						@Override
						public void onUpdate(AircraftBindingState arg0) {
							if (arg0 == AircraftBindingState.UNBOUND) {
								Thread.dumpStack();
							}
							ba.raiseEventFromDifferentThread(DJISDKManagerWrapper.this, null, 0, eventName + "_bindingstatechanged", true,new Object[] {arg0.toString()});
						}
					});
				}
				
			}
			
			@Override
			public void onProductDisconnect() {
				ba.raiseEventFromDifferentThread(DJISDKManagerWrapper.this, null, 0, eventName + "_productdisconnected", true, null);
				
			}
			
			@Override
			public void onProductConnect(BaseProduct arg0) {
				ba.raiseEventFromDifferentThread(DJISDKManagerWrapper.this, null, 0, eventName + "_productconnected", true, new Object[] {arg0});
				
			}
			
			@Override
			public void onInitProcess(DJISDKInitEvent arg0, int arg1) {
				
			}
			
			@Override
			public void onDatabaseDownloadProgress(long arg0, long arg1) {
				
			}
			
			@Override
			public void onComponentChange(ComponentKey arg0, BaseComponent arg1, BaseComponent arg2) {
				BA.LogInfo("onComponentChange: " + arg0);

				
			}

			@Override
			public void onProductChanged(BaseProduct arg0) {
				BA.LogInfo("onProductChanged: " + arg0);
				
			}
		}); 


	}
	public String getSdkVersion() {
		return manager.getSDKVersion();
	}

	/**
	 * Tries to connect to the product. The ProductChanged event will be raised.
	 */
	public void StartConnectionToProduct() {
		manager.startConnectionToProduct();
	}
	public boolean getRegistered() {
		return manager.hasSDKRegistered();
	}
	/**
	 * Returns the activation state.
	 * One of the following values: NOT_SUPPORTED, LOGIN_REQUIRED, ACTIVATED, UNKNOWN
	 * Returns an empty string if the SDK was not registered.
	 */
	public String getActivationState() {
		if (manager.getAppActivationManager() == null)
			return "";
		return String.valueOf(manager.getAppActivationManager().getAppActivationState());
	}
	/**
	 * Returns the aircraft binding state.
	 * One of the following values: NOT_SUPPORTED, INITIAL, BOUND, NOT_REQUIRED, UNBOUND , UNBOUND_BUT_CANNOT_SYNC,  UNKNOWN
	 * Returns an empty string if the SDK was not registered.
	 */
	public String getAircraftBindingState() {
		if (manager.getAppActivationManager() == null)
			return "";
		return String.valueOf(manager.getAppActivationManager().getAircraftBindingState());
	}
	@ShortName("DJIUserManager")
	public static class DJIUserManager extends DJIBaseComponentWrapper<UserAccountManager> {
		public void Initialize(final BA ba, String EventName) {
			setObject(UserAccountManager.getInstance());
			super.initialize(ba, EventName);
		}
		public Object Login() {
			B4ACompletionCallback cc = new B4ACompletionCallback();
			getObject().logIntoDJIUserAccount(getBA().context, cc);
			return cc;
		}
		public Object Logout() {
			B4ACompletionCallback cc = new B4ACompletionCallback();
			getObject().logoutOfDJIUserAccount(cc);
			return cc;
		}
		public String getState() {
			return String.valueOf(getObject().getUserAccountState());
		}

	}

}
