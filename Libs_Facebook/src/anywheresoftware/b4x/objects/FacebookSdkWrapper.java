
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
 
 package anywheresoftware.b4x.objects;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import android.app.Activity;
import android.content.Intent;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.IOnActivityResult;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common.DesignerCustomView;
import anywheresoftware.b4a.objects.B4AException;
import anywheresoftware.b4a.objects.FirebaseAuthWrapper;
import anywheresoftware.b4a.objects.LabelWrapper;
import anywheresoftware.b4a.objects.PanelWrapper;
import anywheresoftware.b4a.objects.collections.Map;

@Version(2.00f)
@ShortName("FacebookSdk")
@Permissions(values={"android.permission.INTERNET"})
@DependsOn(values={"facebook-core-14.1.1.aar", "facebook-login-14.1.1.aar",  "facebook-common-14.1.1.aar", "androidx.cardview:cardview"})
@Events(values= {"SignError (Error As Exception)"})
public class FacebookSdkWrapper {
	@Hide
	public FacebookSdk sdk;
	private HashMap<Integer, WeakReference<IOnActivityResult>> onActivityResultMap;
	private IOnActivityResult ion;
	private CallbackManager callback;
	private FirebaseAuth auth;
	@Hide
	LoginManager loginManager;
	private String[] permissions = new String[] {"email", "public_profile"};
	private BA ba;
	private String eventName;
	/**
	 * Use Initialize2 instead.
	 */
	public void Initialize(BA ba) throws Exception {
		Initialize2(ba, "");
	}
	
	public void Initialize2(BA ba, String EventName) throws Exception {
		if (FacebookSdk.isInitialized() == false) {
			FacebookSdk.sdkInitialize(BA.applicationContext);
			AppEventsLogger.activateApp(BA.applicationContext);
		}
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		loginManager = LoginManager.getInstance();
		callback = CallbackManager.Factory.create();
		this.auth = FirebaseAuth.getInstance();
		loginManager.registerCallback(callback, new FacebookCallback<LoginResult>() {

			@Override
			public void onCancel() {
				BA.Log("Facebook cancel");
				FacebookSdkWrapper.this.ba.raiseEventFromDifferentThread(FacebookSdkWrapper.this, null, 0, 
						eventName + "_signerror", false, new Object[] {AbsObjectWrapper.ConvertToWrapper(new B4AException(), new Exception("cancel"))});
			}

			@Override
			public void onError(FacebookException arg0) {
				BA.Log("Facebook error: " + arg0);
				FacebookSdkWrapper.this.ba.raiseEventFromDifferentThread(FacebookSdkWrapper.this, null, 0, 
						eventName + "_signerror", false, new Object[] {AbsObjectWrapper.ConvertToWrapper(new B4AException(), arg0)});
			}

			@Override
			public void onSuccess(LoginResult arg0) {
				BA.Log("Facebook success: " + arg0);
				handleFacebookAccessToken(arg0.getAccessToken());
			}

		});

	}
	private void fillRequests() {
		WeakReference<IOnActivityResult> wion = new WeakReference<IOnActivityResult>(ion);
		for (int i = 0;i < 1;i++) {
			onActivityResultMap.put(FacebookSdk.getCallbackRequestCodeOffset() + i, wion);
		}
	}
	/**
	 * Logs out from the signed in Facebook account;
	 */
	public void SignOut() {
		FirebaseAuth.getInstance().signOut();
		LoginManager.getInstance().logOut();
	}
	/**
	 * Starts the sign in process. This method should be called from an Activity.
	 *The SignError event will be raised in the module where FacebookSDK was initialized.
	 */
	public void SignIn(BA ba) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		BA.LogInfo("Facebook - SignIn");
		BA.SharedProcessBA sba = ba.sharedProcessBA;
		Field f = sba.getClass().getDeclaredField("onActivityResultMap");
		f.setAccessible(true);
		if (f.get(sba) == null)
			f.set(sba, new HashMap<Integer, WeakReference<IOnActivityResult>>());
		onActivityResultMap = (HashMap<Integer, WeakReference<IOnActivityResult>>) f.get(sba);
		ion = new IOnActivityResult() {

			@Override
			public void ResultArrived(int resultCode, Intent intent) {
				BA.LogInfo("Facebook - ResultArrived: " + intent);
				callback.onActivityResult(FacebookSdk.getCallbackRequestCodeOffset(), resultCode, intent);
			}
		};
		fillRequests();
		loginManager.logInWithReadPermissions(ba.sharedProcessBA.activityBA.get().activity, Arrays.asList(permissions));
	}
	private void handleFacebookAccessToken(AccessToken token) {
		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		BA.Log("signInWithCredential start");
		auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

			@Override
			public void onComplete(Task<AuthResult> arg0) {
				BA.Log("signInWithCredential complete: " + arg0.isSuccessful());
				if (arg0.isSuccessful() == false) { 
					BA.Log("error: " + arg0.getException());
					ba.raiseEventFromDifferentThread(FacebookSdkWrapper.this, null, 0, 
							eventName + "_signerror", false, new Object[] {AbsObjectWrapper.ConvertToWrapper(new B4AException(), arg0.getException())});
				}
				
			}
			
		});

	}
}