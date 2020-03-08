
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

import java.util.HashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.sql.SQL.CursorWrapper;

/**
 * ContentResolver allows you to interact with other content providers.
 */
@ShortName("ContentResolver")
@DependsOn(values={"SQL"})
@Version(1.50f)
@Events(values={"QueryCompleted (Success As Boolean, Crsr As Cursor)",
		"InsertCompleted (Success As Boolean, Uri As Uri)",
		"UpdateCompleted (Success As Boolean, RowsAffected As Int)",
		"DeleteCompleted (Success As Boolean, RowsAffected As Int)",
		"ObserverChange (Uri As Uri)"})

public class ContentResolverWrapper {
	private String eventName;
	private HashMap<Uri, ContentObserver> observers = new HashMap<Uri, ContentObserver>();
	/**
	 * Initializes the object and sets the subs that will handle the asynchronous operations.
	 */
	public void Initialize(String EventName) {
		eventName = EventName.toLowerCase(BA.cul);
	}
	/**
	 * Queries the content provider.
	 *Uri - Content Uri.
	 *Project - An array of strings. The columns to return.
	 *Selection - The criteria.
	 *SelectionArgs - An array of strings that replace question marks in the selection string.
	 *SortOrder - The sorting column (or empty string if sorting is not required).
	 */
	public CursorWrapper Query(UriWrapper Uri, String[] Projection, String Selection, String[] SelectionArgs, String SortOrder) {
		CursorWrapper cw = new CursorWrapper();
		cw.setObject(BA.applicationContext.getContentResolver().query(Uri.getObject(), Projection, Selection, SelectionArgs, SortOrder));
		return cw;
	}
	public void QueryAsync(final BA ba, final UriWrapper Uri, final String[] Projection, final String Selection, final String[] SelectionArgs,final String SortOrder) {
		BA.submitRunnable(new Runnable() {
			@Override
			public void run() {
				try {
					CursorWrapper cw = Query (Uri, Projection, Selection, SelectionArgs, SortOrder);
					ba.raiseEventFromDifferentThread(ContentResolverWrapper.this, null, 0, eventName + "_querycompleted", true, new Object[] {true,cw});
				}
				catch (Exception e) {
					ba.setLastException(e);
					ba.raiseEventFromDifferentThread(ContentResolverWrapper.this, null, 0, eventName + "_querycompleted", true, new Object[] {false, new CursorWrapper()});
				}
			}

		},  null, 0);
	}
	/**
	 * Inserts a row.
	 *Uri - The content Uri.
	 *Values - The values to insert.
	 */
	public UriWrapper Insert(UriWrapper Uri, ContentValues Values) {
		UriWrapper uw = new UriWrapper();
		uw.setObject(BA.applicationContext.getContentResolver().insert(Uri.getObject(), Values));
		return uw;
	}
	/**
	 * Starts an asynchronous insert. The InsertCompleted event will be raised when operation completes.
	 */
	public void InsertAsync(final BA ba, final UriWrapper Uri, final ContentValues Values) {
		BA.submitRunnable(new Runnable() {
			UriWrapper uw;
			@Override
			public void run() {
				boolean success;
				try {
					uw = Insert(Uri, Values);
					success = true;
				}
				catch (Exception e) {
					uw = new UriWrapper();
					ba.setLastException(e);
					success = false;
				}
				ba.raiseEventFromDifferentThread(ContentResolverWrapper.this, null, 0, eventName + "_insertcompleted", true, new Object[] {success, uw});

			}

		},  null, 0);
	}
	/**
	 * Updates rows with the given values.
	 *Uri - Content Uri.
	 *Values - Values to update.
	 *Where - Selection criteria.
	 *SelectionArgs - An array of strings that replaces questions marks in the Where clause.
	 */
	public int Update(Uri Uri, ContentValues Values, String Where, String[] SelectionArgs) {
		return BA.applicationContext.getContentResolver().update(Uri, Values, Where, SelectionArgs);
	}
	/**
	 * Starts an asynchronous update. The UpdateCompleted event will be raised when operation completes.
	 */
	public void UpdateAsync(final BA ba, final Uri Uri, final ContentValues Values, final String Where, final String[] SelectionArgs) { 
		BA.submitRunnable(new Runnable() {
			int rows;
			@Override
			public void run() {
				boolean success;
				try {
					rows = Update(Uri, Values, Where, SelectionArgs);
					success = true;
				}
				catch (Exception e) {
					ba.setLastException(e);
					success = false;
				}
				ba.raiseEventFromDifferentThread(ContentResolverWrapper.this, null, 0, eventName + "_updatecompleted", true, new Object[] {success, rows});

			}

		},  null, 0);
	}
	/**
	 * Deletes rows based on the given criteria.
	 *Uri - Content Uri.
	 *Where - The selection criteria. Can include question marks.
	 *SelectionArgs - An array of strings that replace the question marks in the Where clause.
	 */
	public int Delete(Uri Uri, String Where, String[] SelectionArgs) {
		return BA.applicationContext.getContentResolver().delete(Uri, Where, SelectionArgs);
	}
	/**
	 * Starts an asynchronous delete. The DeleteCompleted event will be raised when operation completes.
	 */
	public void UpdateDelete(final BA ba, final Uri Uri, final String Where, final String[] SelectionArgs) { 
		BA.submitRunnable(new Runnable() {
			int rows;
			@Override
			public void run() {
				boolean success;
				try {
					rows = Delete(Uri, Where, SelectionArgs);
					success = true;
				}
				catch (Exception e) {
					ba.setLastException(e);
					success = false;
				}
				ba.raiseEventFromDifferentThread(ContentResolverWrapper.this, null, 0, eventName + "_deletecompleted", true, new Object[] {success, rows});

			}

		},  null, 0);
	}
	/**
	 * Registers a content observer. The ObserverChange event will be raised whenever there is a change related to the given Uri.
	 *Uri - The Uri to watch for changes.
	 *NotifyForDescendents - Whether to listen to changes related to descendant Uris.
	 *Example:<code>
	 *Sub Process_Globals
	 *	Private cr As ContentResolver
	 *End Sub
	 *
	 *Sub Service_Create
	 *	Dim uri As Uri
	 *	uri.Parse("content://com.android.contacts/contacts")
	 *	cr.Initialize("cr")
	 *	cr.RegisterObserver(uri, True)
	 *End Sub
	 *
	 *Sub cr_ObserverChange (Uri As Uri)
	 *	Log("Contacts provider has reported a change...")
	 *End Sub</code>
	 */
	public void RegisterObserver(BA ba, UriWrapper Uri, boolean NotifyForDescendents) {
		ContentObserver co = new ContentObserverWrapper(ba, eventName);
		observers.put(Uri.getObject(), co);
		BA.applicationContext.getContentResolver().registerContentObserver(Uri.getObject(), NotifyForDescendents, co);
	}
	public void UnregisterObserver(UriWrapper Uri) {
		ContentObserver co = observers.get(Uri.getObject());
		if (co == null)
			return;
		BA.applicationContext.getContentResolver().unregisterContentObserver(co);
	}

	@ShortName("Uri")
	public static class UriWrapper extends AbsObjectWrapper<Uri> {
		/**
		 * Creates a new Uri from the given string.
		 */
		public void Parse(String UriString) {
			setObject(Uri.parse(UriString));
		}
		/**
		 * Creates a new Uri from the given parts.
		 */
		public void FromParts(String Scheme, String SSP, String Fragment) {
			setObject(Uri.fromParts(Scheme, SSP, Fragment));
		}
		/**
		 * Creates a new Uri by appending the path to the given Uri.
		 */
		public void WithAppendedPath(Uri BaseUri, String PathSegment) {
			setObject(Uri.withAppendedPath(BaseUri, PathSegment));
		}
		/**
		 * Creates a new Uri by appending the Id to the given Uri.
		 */
		public void WithAppendedId(Uri BaseUri, long Id) {
			setObject(ContentUris.withAppendedId(BaseUri, Id));
		}
		/**
		 * Returns the Id part of the current Uri.
		 */
		public long ParseId() {
			return ContentUris.parseId(getObject());
		}
	}
	/**
	 * Holds pairs of keys and values.
	 */
	@ShortName("ContentValues")
	public static class ContentValuesWrapper extends AbsObjectWrapper<ContentValues> {
		public void Initialize() {
			setObject(new ContentValues());
		}
		public void PutString(String Key, String Value) {
			getObject().put(Key, Value);
		}
		public void PutInteger(String Key, int Value) {
			getObject().put(Key, Value);
		}
		public void PutLong(String Key, Long Value) {
			getObject().put(Key, Value);
		}
		public void PutShort(String Key, Short Value) {
			getObject().put(Key, Value);
		}
		public void PutFloat(String Key, Float Value) {
			getObject().put(Key, Value);
		}
		public void PutDouble(String Key, Double Value) {
			getObject().put(Key, Value);
		}
		public void PutByte(String Key, Byte Value) {
			getObject().put(Key, Value);
		}
		public void PutBytes(String Key, byte[] Value) {
			getObject().put(Key, Value);
		}
		public void PutBoolean(String Key, boolean Value) {
			getObject().put(Key, Value);
		}
		public void PutNull(String Key) {
			getObject().putNull(Key);
		}
		public void Remove(String Key) {
			getObject().remove(Key);
		}
	}
	
	private static class ContentObserverWrapper extends ContentObserver {
		private BA ba;
		private String eventName;
		public ContentObserverWrapper(BA ba, String EventName) {
			super(BA.handler);
			this.ba = ba;
			this.eventName = EventName.toLowerCase(BA.cul);
		}
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}
		public void onChange(boolean selfChange, Uri uri) {
			ba.raiseEvent(this, eventName + "_observerchange", AbsObjectWrapper.ConvertToWrapper(new UriWrapper(), uri));
		}

	}
}
