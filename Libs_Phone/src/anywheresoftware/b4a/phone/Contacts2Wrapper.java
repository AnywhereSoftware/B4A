
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
 
 package anywheresoftware.b4a.phone;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;
import anywheresoftware.b4a.phone.ContactsWrapper.Contact;

/**
 *Contacts2 object allows you to access the device stored contacts. This type is based on a new API supported by Android 2.0 and above.
 *This type supersedes Contacts type. 
 *The following code finds all contacts named John (actually it will find all contacts which their name contains the string "john"),
 *and print their fields to the LogCat. It will also fetch the contact photo if it exists.
 *Example:<code>
 *Dim Contacts2 As Contacts2
 *Dim listOfContacts As List
 *listOfContacts = Contacts2.FindByName("John", False, True, True)
 *For i = 0 To listOfContacts.Size - 1
 *    Dim Contact As Contact
 *    Contact = listOfContacts.Get(i)
 *    Log(Contact) 'will print the fields to the LogCat
 *    Dim photo As Bitmap
 *    photo = Contact.GetPhoto
 *    If photo <> Null Then Activity.SetBackgroundImage(photo)
 *    Dim emails As Map
 *    emails = Contact.GetEmails
 *    If emails.Size > 0 Then Log("Email addresses: " & emails)
 *    Dim phones As Map
 *    phones = Contact.GetPhones
 *    If phones.Size > 0 Then Log("Phone numbers: " & phones)
 *Next </code>
 */
@ShortName("Contacts2")
@Permissions(values={"android.permission.READ_CONTACTS"})
@Events(values={"Complete (ListOfContacts As List)"})
public class Contacts2Wrapper {
	private static final String[] people_projection = {ContactsContract.Contacts.TIMES_CONTACTED, 
		ContactsContract.Contacts.LAST_TIME_CONTACTED,
		ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.STARRED, BaseColumns._ID,
		ContactsContract.Contacts.PHOTO_ID};
	private static final String[] phone_projection = {ContactsContract.CommonDataKinds.Phone.IS_PRIMARY, ContactsContract.CommonDataKinds.Phone.NUMBER, 
		ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
	/**
	 * Returns a List of Contact objects with all the contacts. This list can be very large.
	 */
	public List GetAll(boolean IncludePhoneNumber, boolean IncludeNotes) {
		return getAllContacts(null, null, IncludePhoneNumber, IncludeNotes);
	}
	/**
	 * Returns the Contact with the specified Id.
	 *Returns Null if no matching contact found.
	 *IncludePhoneNumber - Whether to fetch the default phone number.
	 *IncludeNotes - Whether to fetch the notes field.
	 */
	public Contact GetById(int Id, boolean IncludePhoneNumber, boolean IncludeNotes) {
		List l = getAllContacts(BaseColumns._ID + " = ?", new String[] {String.valueOf(Id)}, IncludePhoneNumber, IncludeNotes);
		if (l.getSize() == 0)
			return null;
		else
			return (Contact) l.Get(0);
	}
	/**
	 * Returns a List of Contact objects with all contacts matching the given email.
	 *Email - The email to search for.
	 *Exact - If True then only contacts with the exact email address (case sensitive) will return
	 *, otherwise all contacts email addresses that include the Email string will return (case insensitive).
	 *
	 *IncludePhoneNumber - Whether to fetch the default phone number.
	 *IncludeNotes - Whether to fetch the notes field.
	 */
	public List FindByMail(String Email, boolean Exact, boolean IncludePhoneNumber, boolean IncludeNotes) {
		ContentResolver cr = BA.applicationContext.getContentResolver();
		String sel, args;
		if (!Exact) {
			sel = " LIKE ?";
			args = "%" + Email + "%";
		}
		else {
			sel = " = ?";
			args = Email;
		}
		Cursor crsr = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[] {ContactsContract.Data.CONTACT_ID},
				ContactsContract.Data.DATA1 + sel, new String[] {args}, null);
		StringBuilder sb = new StringBuilder();
		while (crsr.moveToNext()) {
			for (int i = 0;i < crsr.getColumnCount();i++) {
				sb.append(crsr.getString(0)).append(",");
			}
		}
		int count = crsr.getCount();
		crsr.close();
		if (count == 0) {
			List l = new List();
			l.Initialize();
			return l;
		}
		sb.setLength(sb.length() - 1);
		String selection = BaseColumns._ID +  " IN (" + sb.toString() + ")";
		return getAllContacts(selection, null, IncludePhoneNumber, IncludeNotes);
	}
	/**
	 * Returns a List of Contact objects with all contacts matching the given name.
	 *Name - The name to search for.
	 *Exact - If True then only contacts with the exact name value (case sensitive) will return
	 *, otherwise all contacts names that include the Name string will return (case insensitive).
	 *
	 *IncludePhoneNumber - Whether to fetch the default phone number.
	 *IncludeNotes - Whether to fetch the notes field.
	 */
	public List FindByName(String Name, boolean Exact, boolean IncludePhoneNumber, boolean IncludeNotes) {
		if (!Exact)
			return getAllContacts(ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?", new String[] {"%" + Name + "%"}, IncludePhoneNumber, IncludeNotes);
		else
			return getAllContacts(ContactsContract.Contacts.DISPLAY_NAME + " = ?", new String[] {Name}, IncludePhoneNumber, IncludeNotes);
	}
	/**
	 * This method is an asynchronous version of GetContactsByQuery. Once the list is ready the Complete event will be raised.
	 *The EventName parameter sets the sub that will handle this event.
	 */
	public void GetContactsAsync(final BA ba, final String EventName, final String Query, final String[] Arguments, 
			final boolean IncludePhoneNumber, final boolean IncludeNotes) {
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				List res = GetContactsByQuery(Query, Arguments, IncludePhoneNumber, IncludeNotes);
				ba.raiseEventFromDifferentThread(this, this, 0, EventName.toLowerCase(BA.cul) + "_complete", true, new Object[] {res});
			}
			
		}, this, 0);
	}
	/**
	 * Returns a list of contacts based on the specified query and arguments.
	 *Query - The SQL query. Pass an empty string to return all contacts.
	 *Arguments - An array of strings used for parameterized queries. Pass Null if not needed.
	 *IncludePhoneNumber - Whether to fetch the phone number for each contact.
	 *IncludeNotes - Whether to fetch the notes field for each contact.
	 */
	public List GetContactsByQuery(String Query, String[] Arguments, boolean IncludePhoneNumber, boolean IncludeNotes) {
		return getAllContacts(Query.length() == 0 ? null : Query, Arguments, IncludePhoneNumber, IncludeNotes);
	}
	
	private List getAllContacts(String selection, String[] args, boolean includePhone, boolean includeNotes) {

		ContentResolver cr = BA.applicationContext.getContentResolver();
		Cursor crsr = cr.query(ContactsContract.Contacts.CONTENT_URI, people_projection, selection, args, null);
		List l = new List();
		l.Initialize();
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		for (int col = 0;col < crsr.getColumnCount();col++) {
			m.put(crsr.getColumnName(col), col);
		}

		while (crsr.moveToNext()) {
			String phoneNumber = "";
			String notes = "";
			int id = crsr.getInt(m.get(BaseColumns._ID));
			if (includePhone && crsr.getInt(m.get(ContactsContract.Contacts.HAS_PHONE_NUMBER)) != 0) {
				Cursor phones = BA.applicationContext.getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, phone_projection,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, null, null); 
				while (phones.moveToNext()) { 
					phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					if (phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) != 0)
						break;
				} 
				phones.close(); 
			}
			if (includeNotes) {
				Cursor notesC = BA.applicationContext.getContentResolver().query(
						ContactsContract.Data.CONTENT_URI, new String[] {ContactsContract.CommonDataKinds.Note.NOTE},
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id + 
						" AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[] {ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE}, null);
				while (notesC.moveToNext()) {
					notes = notesC.getString(0);
				}
				notesC.close();
			}
			Contact contact = new Contact2(
					crsr.getString(m.get(ContactsContract.Contacts.DISPLAY_NAME)),
					phoneNumber,
					crsr.getInt(m.get(ContactsContract.Contacts.STARRED)) > 0,
					id,
					notes,
					crsr.getInt(m.get(ContactsContract.Contacts.TIMES_CONTACTED)),
					crsr.getLong(m.get(ContactsContract.Contacts.LAST_TIME_CONTACTED)),
					crsr.getString(m.get(ContactsContract.Contacts.DISPLAY_NAME)),
					crsr.getInt(m.get(ContactsContract.Contacts.PHOTO_ID)));
			l.Add(contact);
		}
		crsr.close();
		return l;
	}
	
//	private void printRow(Cursor c, String title) {
//		Common.Log("****** " + title);
//		for (int i = 0;i < c.getColumnCount();i++) {
//			Common.Log(c.getColumnName(i) + "=" + c.getString(i));
//		}
//	}
	protected static class Contact2 extends Contact{
		private int photoId;
		Contact2(String displayName, String phoneNumber, boolean starred,
				int id,  String notes,
				int timesContacted,
				long lastTimeContacted, String name, int photoId) {
			super(displayName, phoneNumber, starred, id, notes, timesContacted, lastTimeContacted, name);
			this.photoId = photoId;
		}
		
		@Override
		public BitmapWrapper GetPhoto() {
			Cursor photo = BA.applicationContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, BaseColumns._ID + "=" + photoId, null, null);
			BitmapWrapper bw = null;
			if (photo.moveToNext()) {
				byte[] b = photo.getBlob(photo.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
				if (b != null) {
					bw = new BitmapWrapper();
					ByteArrayInputStream in = new ByteArrayInputStream(b);
					bw.Initialize2(in);
				}
			}
			photo.close();
			return bw;
		}
		@Override
		public Map GetEmails() {
			Cursor emails = BA.applicationContext.getContentResolver().query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[] {ContactsContract.Data.DATA1, ContactsContract.Data.DATA2}, 
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + Id, null, null); 
			Map m = new Map(); m.Initialize();
			while (emails.moveToNext()) {
				m.Put(emails.getString(0), emails.getInt(1));
			}
			emails.close();
			return m;
		}
		@Override
		public Map GetPhones() {
			Cursor phones = BA.applicationContext.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[] {ContactsContract.Data.DATA1, ContactsContract.Data.DATA2},
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ Id, null, null); 
			Map m = new Map(); m.Initialize();
			while (phones.moveToNext()) {
				m.Put(phones.getString(0), phones.getInt(1));
			}
			phones.close();
			return m;
		}
	}
}
