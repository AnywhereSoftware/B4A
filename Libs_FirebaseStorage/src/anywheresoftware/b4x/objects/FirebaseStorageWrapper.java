
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sun.awt.geom.AreaOp.AddOp;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask.TaskSnapshot;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.streams.File;

@DependsOn(values={"com.google.firebase:firebase-storage", "com.google.firebase:firebase-auth"})
@Version(2.00f)
@ShortName("FirebaseStorage")
@Events(values={"UploadCompleted (ServerPath As String, Success As Boolean)",
		"DownloadCompleted (ServerPath As String, Success As Boolean)", 
		"MetadataCompleted (Metadata As StorageMetadata, Success As Boolean)",
		"DeleteCompleted (ServerPath As String, Success As Boolean)"})
public class FirebaseStorageWrapper {
	@Hide
	public FirebaseStorage fs;
	private String eventName;
	private String bucket;
	/**
	 * Initializes the object.
	 * Bucket - The url from Firebase console (ex: gs://yourapp.appspot.com)
	 */
	public void Initialize(String EventName, String Bucket) {
		fs = FirebaseStorage.getInstance();
		this.eventName = EventName.toLowerCase(BA.cul);
		this.bucket = Bucket;
		if (bucket.endsWith("/"))
			bucket = bucket.substring(0, bucket.length() - 1);
	}
	/**
	 * Reads the data from the file and uploads it to the specified ServerPath.
	 *The UploadCompleted event will be raised in the current module.
	 */
	public void UploadFile(final BA ba, String Dir, String FileName, final String ServerPath) throws IOException {
		UploadStream(ba, File.OpenInput(Dir, FileName).getObject(), ServerPath);
	}
	/**
	 * Reads the data from the input stream and uploads it to the specified ServerPath.
	 *The UploadCompleted event will be raised in the current module.
	 */
	public void UploadStream(final BA ba, final InputStream InputStream, final String ServerPath) {
		StorageReference ref = fs.getReferenceFromUrl(bucket + ServerPath);
		UploadTask task = ref.putStream(InputStream);
		task.addOnFailureListener(new OnFailureListener()  {

			@Override
			public void onFailure(Exception e) {
				try {
					InputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				ba.setLastException(e);
				ba.raiseEventFromDifferentThread(FirebaseStorageWrapper.this, null, 0, eventName + "_uploadcompleted", false, new Object[] {ServerPath, false});
			}
			
		});
		task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				try {
					InputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				ba.raiseEventFromDifferentThread(FirebaseStorageWrapper.this, null, 0, eventName + "_uploadcompleted", false, new Object[] {ServerPath, true});
			}
			
		});
	}
	/**
	 * Downloads the remote resource and writes it to the specified file.
	 *The DownloadCompleted event will be raised in the current module.
	 */
	public void DownloadFile(final BA ba, final String ServerPath, String Dir, String FileName) throws FileNotFoundException {
		DownloadStream(ba, ServerPath, File.OpenOutput(Dir, FileName, false).getObject());
	}
	/**
	 * Downloads the remote resource and writes it to the OutputStream.
	 *The DownloadCompleted event will be raised in the current module.
	 */
	public void DownloadStream(final BA ba, final String ServerPath, final OutputStream OutputStream) {
		StorageReference ref = fs.getReferenceFromUrl(bucket + ServerPath);
		StreamDownloadTask task = ref.getStream(new StreamDownloadTask.StreamProcessor() {

			@Override
			public void doInBackground(
					com.google.firebase.storage.StreamDownloadTask.TaskSnapshot arg0,
					InputStream in) throws IOException {
				File.Copy2(in, OutputStream);
				OutputStream.close();
			}
			
		});
		task.addOnFailureListener(new OnFailureListener()  {

			@Override
			public void onFailure(Exception e) {
				ba.setLastException(e);
				ba.raiseEventFromDifferentThread(FirebaseStorageWrapper.this, null, 0, eventName + "_downloadcompleted", false, new Object[] {ServerPath, false});
			}
			
		});
		task.addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {

			@Override
			public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
				ba.raiseEventFromDifferentThread(FirebaseStorageWrapper.this, null, 0, eventName + "_downloadcompleted", false, new Object[] {ServerPath, true});
			}
			
		});
	}
	/**
	 * Retrieves the metadata of the remote resource. The MetadataCompleted event will be raised in the current module.
	 */
	public void GetMetadata(final BA ba, final String ServerPath) {
		StorageReference ref = fs.getReferenceFromUrl(bucket + ServerPath);
		ref.getMetadata().addOnFailureListener(new OnFailureListener()  {

			@Override
			public void onFailure(Exception e) {
				ba.setLastException(e);
				ba.raiseEventFromDifferentThread(FirebaseStorageWrapper.this, null, 0, eventName + "_metadatacompleted", false, new Object[] {new StorageMetadataWrapper(), false});
			}
			
		}).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {

			@Override
			public void onSuccess(StorageMetadata arg0) {
				ba.raiseEventFromDifferentThread(FirebaseStorageWrapper.this, null, 0, eventName + "_metadatacompleted", false, 
						new Object[] {AbsObjectWrapper.ConvertToWrapper(new StorageMetadataWrapper(), arg0), true});
			}
			
		});
	}
	/**
	 * Deletes the remote resource. The DeleteCompleted event will be raised in the current module.
	 */
	public void DeleteFile (final BA ba, final String ServerPath) {
		StorageReference ref = fs.getReferenceFromUrl(bucket + ServerPath);
		ref.delete().addOnFailureListener(new OnFailureListener() {

			@Override
			public void onFailure(Exception e) {
				ba.setLastException(e);
				ba.raiseEventFromDifferentThread(FirebaseStorageWrapper.this, null, 0, 
						eventName + "_deletecompleted", false, new Object[] {ServerPath, false});
				
			}
			
		}).addOnSuccessListener(new OnSuccessListener<Void>() {

			@Override
			public void onSuccess(Void arg0) {
				ba.raiseEventFromDifferentThread(FirebaseStorageWrapper.this, null, 0, 
						eventName + "_deletecompleted", false, new Object[] {ServerPath, true});
			}
			
		});
	}
	@ShortName("StorageMetadata")
	public static class StorageMetadataWrapper extends AbsObjectWrapper<StorageMetadata> {
		/**
		 * Returns the resource size in bytes.
		 */
		public long getSize() {
			return getObject().getSizeBytes();
		}
		/**
		 * Returns the last updated time as ticks.
		 */
		public long getTimestamp() {
			return getObject().getUpdatedTimeMillis();
		}
		/**
		 * Returns the resource name.
		 */
		public String getName() {
			return getObject().getName();
		}
		/**
		 * Returns the resource path.
		 */
		public String getPath() {
			return getObject().getPath();
		}
	}
}
