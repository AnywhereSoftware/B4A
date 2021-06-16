
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.IOnActivityResult;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("FirebaseAuth")
@DependsOn(values={"com.google.firebase:firebase-auth", "com.google.android.gms:play-services-auth", "com.google.firebase:firebase-core"})
@Events(values={"SignedIn (User As FirebaseUser)", "TokenAvailable (User As FirebaseUser, Success As Boolean, TokenId As String)", "SignError (Error As Exception)"})
@Version(2.01f)
public class FirebaseAuthWrapper  {
	@Hide
	public FirebaseAuth auth;
	private IOnActivityResult ion;
	private boolean signOut;
	private String eventName;
	private BA firstBA;
	/**
	 * Initializes the object. The SignedIn event will be raised if there is already a signed in user.  
	 */
	public void Initialize(final BA ba, String EventName) {
		auth = FirebaseAuth.getInstance();
		eventName = EventName.toLowerCase(BA.cul);
		firstBA = ba;
		auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
				BA.Log("onAuthStateChanged: " + firebaseAuth);
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null)
					ba.raiseEventFromDifferentThread(FirebaseAuthWrapper.this, null, 0, eventName + "_signedin", false, new Object[] {AbsObjectWrapper.ConvertToWrapper(new FirebaseUserWrapper(), user)});
			}
		});
		
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
		.requestIdToken(BA.applicationContext.getResources().getString(GetResourceId("string", "default_web_client_id")))
		.requestEmail()
		.build();
		googleClient = new GoogleApiClient.Builder(ba.context)
		.addApi(Auth.GOOGLE_SIGN_IN_API, gso).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
		.build();
		googleClient.connect();
	}
	/**
	 * Returns the current signed in user. Returns an uninitialized object if there is no user.
	 */
	public FirebaseUserWrapper getCurrentUser() {
		return (FirebaseUserWrapper) AbsObjectWrapper.ConvertToWrapper(new FirebaseUserWrapper(), auth.getCurrentUser());
	}
	
	@Hide
	@Override
	public void onConnected(Bundle arg0) {
		if (signOut) {
			Auth.GoogleSignInApi.signOut(googleClient);
		}
		signOut = false;
		
		
	}
	@Hide
	@Override
	public void onConnectionSuspended(int arg0) {
		
	}

	/**
	 * Sign outs from Firebase and Google.
	 */
	public void SignOutFromGoogle() {
		auth.signOut();
		if (googleClient.isConnected()) {
			Auth.GoogleSignInApi.signOut(googleClient);
		}
		else {
			signOut = true;
			googleClient.connect();
		}
	}
	/**
	 * Start the sign in process.
	 */
	public void SignInWithGoogle(final BA ba) {
		final Activity act = ba.sharedProcessBA.activityBA.get().activity;
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleClient);
		BA.LogInfo("SignInWithGoogle called");

		ion = new IOnActivityResult() {

			@Override
			public void ResultArrived(int resultCode, Intent intent) {
				BA.LogInfo("SignInWithGoogle.ResultArrived");

				GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
				if (result.isSuccess()) {
					BA.LogInfo("ResultArrived Success");
					GoogleSignInAccount account = result.getSignInAccount();
					firebaseAuthWithGoogle(act, account);
				} else {
					BA.LogInfo("ResultArrived Error: " +  result.getStatus() + ", " + result.getStatus().getStatusMessage());
					firstBA.raiseEventFromDifferentThread(FirebaseAuthWrapper.this, null, 0, 
							eventName + "_signerror", false, new Object[] {AbsObjectWrapper.ConvertToWrapper(new B4AException(), new Exception(CommonStatusCodes.getStatusCodeString(result.getStatus().getStatusCode())))});
				}
			}

		};

		ba.startActivityForResult(ion, signInIntent);
	}
	/**
	 * Retrieves the token id. This token can be sent to your backend server. The server can use it to verify the user.
	 *The TokenAvailable event will be raised in the current module.
	 */
	public void GetUserTokenId(final BA ba, final FirebaseUserWrapper User, boolean ForceRefresh) {
		User.getObject().getIdToken(ForceRefresh).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {

			@Override
			public void onSuccess(GetTokenResult arg0) {
				ba.raiseEventFromDifferentThread(FirebaseAuthWrapper.this, null, 0, eventName + "_tokenavailable", false, new Object[] {User, true, arg0.getToken()});
			}
			
		}).addOnFailureListener(new OnFailureListener() {

			@Override
			public void onFailure(Exception arg0) {
				ba.raiseEventFromDifferentThread(FirebaseAuthWrapper.this, null, 0, eventName + "_tokenavailable", false, new Object[] {User, false, ""});
			}
			
		});
		
	}
	private void firebaseAuthWithGoogle(Activity act, GoogleSignInAccount acct) {
		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		auth.signInWithCredential(credential)
		.addOnCompleteListener(act, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(Task<AuthResult> task) {
				try {
				BA.LogInfo("firebaseAuthWithGoogle success: " + task.isSuccessful());
				if (task.isSuccessful())
					BA.LogInfo("result: " + task.getResult());
				else {
					BA.LogInfo("error: " + task.getException());
					firstBA.raiseEventFromDifferentThread(FirebaseAuthWrapper.this, null, 0, eventName + "_signerror", false, new Object[] {AbsObjectWrapper.ConvertToWrapper(new B4AException(), task.getException())});
				}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
	}
	private int GetResourceId(String Type, String Name) {
		return BA.applicationContext.getResources().getIdentifier(Name, Type, BA.packageName);
	}
	
	@ShortName("FirebaseUser")
	public static class FirebaseUserWrapper extends AbsObjectWrapper<FirebaseUser> {
		public String getEmail() {
			return BA.returnString(getObject().getEmail());
		}
		public String getDisplayName() {
			return getObject().getDisplayName();
		}
		public String getUid() {
			return getObject().getUid();
		}
		public String getPhotoUrl() {
			return getObject().getPhotoUrl() == null ? "" : getObject().getPhotoUrl().toString();
		}
		
	}
	

}
