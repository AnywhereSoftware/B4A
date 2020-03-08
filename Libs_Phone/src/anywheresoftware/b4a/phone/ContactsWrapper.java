
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

import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Contacts;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;

/**
 * Contacts object allows you to access the device stored contacts.
 *The following code finds all contacts named John (actually it will find all contacts which their name contains the string "john"),
 *and print their fields to the LogCat. It will also fetch the contact photo if it exists.
 *Example:<code>
 *Dim Contacts1 As Contacts
 *Dim listOfContacts As List
 *listOfContacts = Contacts1.FindByName("John", False)
 *For i = 0 To listOfContacts.Size - 1
 *	Dim Contact As Contact
 *	Contact = listOfContacts.Get(i)
 *	Log(Contact) 'will print the fields to the LogCat
 *	Dim photo As Bitmap
 *	photo = Contact.GetPhoto
 *	If photo <> Null Then Activity.SetBackgroundImage(photo)
 *	Dim emails As Map
 *	emails = Contact.GetEmails
 *	If emails.Size > 0 Then Log("Email addresses: " & emails)
 *	Dim phones As Map
 *	phones = Contact.GetPhones
 *	If phones.Size > 0 Then Log("Phone numbers: " & phones)
 *Next</code>
 */
@ShortName("Contacts")
@Permissions(values={"android.permission.READ_CONTACTS"})
public class ContactsWrapper {
	private static final String[] people_projection = {Contacts.People.TIMES_CONTACTED,
		Contacts.Phones.NUMBER, Contacts.People.LAST_TIME_CONTACTED,
		Contacts.People.DISPLAY_NAME, Contacts.People.NAME, Contacts.People.NOTES, Contacts.People.STARRED, BaseColumns._ID};
	
	/**
	 * Returns a List of Contact objects with all the contacts. This list can be very large.
	 */
	public List GetAll() {
		return getAllContacts(null, null);
	}
	/**
	 * Returns a List of Contact objects with all contacts matching the given name.
	 *Name - The name to search for.
	 *Exact - If True then only contacts with the exact name value (case sensitive) will return
	 *, otherwise all contacts names that include the Name string will return (case insensitive).
	 */
	public List FindByName(String Name, boolean Exact) {
		if (!Exact)
			return getAllContacts(Contacts.People.NAME + " LIKE ?", new String[] {"%" + Name + "%"});
		else
			return getAllContacts(Contacts.People.NAME + " = ?", new String[] {Name});
	}
	/**
	 * Returns a List of Contact objects with all contacts matching the given email.
	 *Email - The email to search for.
	 *Exact - If True then only contacts with the exact email address (case sensitive) will return
	 *, otherwise all contacts email addresses that include the Email string will return (case insensitive).
	 */
	public List FindByMail(String Email, boolean Exact) {
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
		Cursor crsr = cr.query(Contacts.ContactMethods.CONTENT_EMAIL_URI, new String[] {Contacts.ContactMethods.PERSON_ID,
				Contacts.ContactMethods.DATA}, Contacts.ContactMethods.DATA + sel, new String[] {args}, null);
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
		return getAllContacts(selection, null);
	}
	/**
	 * Returns the Contact with the specified Id.
	 * Returns Null if no matching contact found.
	 */
	public Contact GetById(int Id) {
		List l = getAllContacts(BaseColumns._ID + " = ?", new String[] {String.valueOf(Id)});
		if (l.getSize() == 0)
			return null;
		else
			return (Contact) l.Get(0);
	}
	private List getAllContacts(String selection, String[] args) {

		ContentResolver cr = BA.applicationContext.getContentResolver();
		Cursor crsr = cr.query(Contacts.People.CONTENT_URI, people_projection, selection, args, null);
		List l = new List();
		l.Initialize();
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		for (int col = 0;col < crsr.getColumnCount();col++) {
			m.put(crsr.getColumnName(col), col);
		}
		
		while (crsr.moveToNext()) {
			Contact contact = new Contact(
					crsr.getString(m.get(Contacts.People.DISPLAY_NAME)),
					crsr.getString(m.get(Contacts.Phones.NUMBER)),
					crsr.getInt(m.get(Contacts.People.STARRED)) > 0,
					crsr.getInt(m.get(BaseColumns._ID)),
					crsr.getString(m.get(Contacts.People.NOTES)),
					crsr.getInt(m.get(Contacts.People.TIMES_CONTACTED)),
					crsr.getLong(m.get(Contacts.People.LAST_TIME_CONTACTED)),
					crsr.getString(m.get(Contacts.People.NAME)));
			l.Add(contact);
		}
		crsr.close();
		return l;
	}

	/**
	 * Represents a single contact.
	 *The Contacts object should be used to get lists of Contact objects.
	 *EMAIL_x constants are the possible email types.
	 *PHONE_x constants are the possible phone types.
	 */
	@ShortName("Contact")
	public static class Contact {
        public static final int EMAIL_CUSTOM = 0;
        public static final int EMAIL_HOME = 1;
        public static final int EMAIL_WORK = 2;
        public static final int EMAIL_OTHER = 3;
        public static final int EMAIL_MOBILE = 4;
        
        public static final int PHONE_CUSTOM = 0;
        public static final int PHONE_HOME = 1;
        public static final int PHONE_MOBILE = 2;
        public static final int PHONE_WORK = 3;
        public static final int PHONE_FAX_WORK = 4;
        public static final int PHONE_FAX_HOME = 5;
        public static final int PHONE_PAGER = 6;
        public static final int PHONE_OTHER = 7;
        
		/**
		 * The displayed name. Equals to the Name if the Name is not empty, otherwise equals to the contacts first email address.
		 */
        public String DisplayName;
        /**
         * Primary phone number.
         */
		public String PhoneNumber = "";
		/**
		 * Whether this contact is a "favorite" contact.
		 */
		public boolean Starred;
		/**
		 * Internal Id.
		 */
		public int Id = -1;
		public String Notes;
		/**
		 * Number of times that this contact was contacted.
		 */
		public int TimesContacted;
		/**
		 * Last time that this contact was contacted. Value is a ticks value.
		 */
		public long LastTimeContacted;
		/**
		 * Contact name.
		 */
		public String Name;
		public Contact() {}
		Contact(String displayName, String phoneNumber, boolean starred,
				int id,  String notes,
				int timesContacted,
				long lastTimeContacted, String name) {
			DisplayName = displayName == null ? "" : displayName;
			PhoneNumber = phoneNumber == null ? "" : phoneNumber; 
			Starred = starred;
			Id = id;
			Notes = notes == null ? "" : notes;
			TimesContacted = timesContacted;
			LastTimeContacted = lastTimeContacted;
			Name = name == null ? "" : name;
		}
		/**
		 * Returns the contact photo or Null if there is no attached photo.
		 *This call executes an additional query.
		 */
		public BitmapWrapper GetPhoto() {
			if (Id == -1)
				throw new RuntimeException("Contact object should be set by calling one of the Contacts methods.");
			Uri u = Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.People.CONTENT_URI, Id), 
					Contacts.Photos.CONTENT_DIRECTORY);
			Cursor crsr = BA.applicationContext.getContentResolver().query(u, new String[] {Contacts.Photos.DATA}, null, null, null);
			BitmapWrapper bw = null;
			if (crsr.moveToNext()) {
				byte[] b = crsr.getBlob(0);
				if (b != null) {
					InputStreamWrapper isw = new InputStreamWrapper();
					isw.InitializeFromBytesArray(b, 0, b.length);
					bw = new BitmapWrapper();
					bw.Initialize2(isw.getObject());
				}
			}
			crsr.close();
			return bw;
		}
		/**
		 * Returns a Map with the contacts email addresses as keys and the email types as values.
		 *This call executes an additional query.
		 */
		public Map GetEmails() {
			if (Id == -1)
				throw new RuntimeException("Contact object should be set by calling one of the Contacts methods.");
			Uri u = Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.People.CONTENT_URI, Id), 
					Contacts.People.ContactMethods.CONTENT_DIRECTORY);
			Cursor crsr = BA.applicationContext.getContentResolver().query(u, new String[] {Contacts.ContactMethods.DATA,Contacts.ContactMethods.TYPE,
					Contacts.ContactMethods.KIND}, 
					Contacts.ContactMethods.KIND + " = " + Contacts.KIND_EMAIL, null, null);
			Map m = new Map(); m.Initialize();
			while (crsr.moveToNext()) {
				m.Put(crsr.getString(0), crsr.getInt(1));
			}
			crsr.close();
			return m;
		}
		/**
		 * Returns a Map with all the contacts phone numbers as keys and the phone types as values.
		 *This call executes an additional query.
		 */
		public Map GetPhones() {
			if (Id == -1)
				throw new RuntimeException("Contact object should be set by calling one of the Contacts methods.");
			Uri u = Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.People.CONTENT_URI, Id), 
					Contacts.People.Phones.CONTENT_DIRECTORY);
			Cursor crsr = BA.applicationContext.getContentResolver().query(u, new String[] {Contacts.PhonesColumns.NUMBER, Contacts.PhonesColumns.TYPE}, 
					null, null, null);
			Map m = new Map(); m.Initialize();
			while (crsr.moveToNext()) {
				m.Put(crsr.getString(0), crsr.getInt(1));
			}
			crsr.close();
			return m;
		}
		@Override
		@Hide
		public String toString() {
			return "DisplayName=" + DisplayName + ", PhoneNumber=" + PhoneNumber + 
			", Starred=" + Starred + ", Id=" + Id + ", Notes=" + Notes + ", TimesContacted=" + 
			TimesContacted + ", LastTimeContacted=" + LastTimeContacted + ", Name=" + Name;
		}
	}
}
