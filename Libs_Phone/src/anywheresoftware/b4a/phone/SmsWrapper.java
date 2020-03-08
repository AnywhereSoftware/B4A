
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

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;

/**
 * Provides access to the stored SMS messages.
 *Note that you can use PhoneSms to send SMS messages.
 *Example of printing all messages from the last week:<code>
 *Dim SmsMessages1 As SmsMessages
 *Dim List1 As List
 *List1 = SmsMessages1.GetAllSince(DateTime.Add(DateTime.Now, 0, 0, -7))
 *For i = 0 To List1.Size - 1
 *	Dim Sms As Sms
 *	Sms = List1.Get(i)
 *	Log(Sms)
 *Next</code>
 */
@ShortName("SmsMessages")
@Permissions(values={"android.permission.READ_SMS"})
public class SmsWrapper {
	public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_INBOX  = 1;
    public static final int TYPE_SENT   = 2;
    public static final int TYPE_DRAFT  = 3;
    public static final int TYPE_OUTBOX = 4;
    public static final int TYPE_FAILED = 5;
    public static final int TYPE_QUEUED = 6;
	private static final String[] projection = {BaseColumns._ID, "thread_id",
		"address", "read", "type", "body", "person", "date"};
	/**
	 * Returns a list with all messages of the given type. The type should be one of the type constants.
	 */
	public List GetByType(int Type) {
		return get("type = ?", new String[]{String.valueOf(Type)});
	}
	public List GetByMessageId(int Id) {
		return get(BaseColumns._ID + " = ?", new String[] {String.valueOf(Id)});
	}
	/**
	 * Returns a list with all messages with the given ThreadId.
	 */
	public List GetByThreadId(int ThreadId) {
		return get("thread_id = ?", new String[]{String.valueOf(ThreadId)});
	}
	/**
	 * Returns a list with all messages received from the person with the given id.
	 */
	public List GetByPersonId(int PersonId) {
		return get("person = ?", new String[]{String.valueOf(PersonId)});
	}
	/**
	 * Returns all unread messages.
	 */
	public List GetUnreadMessages() {
		return get("read = 0", null);
	}
	/**
	 * Returns all stored messages.
	 */
	public List GetAll() {
		return get(null, null);
	}
	/**
	 * Returns all messages since the given date.
	 */
	public List GetAllSince(long Date) {
		return get("date >= ?", new String[] {String.valueOf(Date)});
	}
	/**
	 * Returns all messages between the given dates. Start value is inclusive and end value is exclusive.
	 */
	public List GetBetweenDates(long StartDate, long EndDate) {
		return get("date >= ? AND date < ?", new String[] {String.valueOf(StartDate), String.valueOf(EndDate)});
	}
	private List get(String selection, String[] args) {
		Cursor crsr = BA.applicationContext.getContentResolver().query(
				Uri.parse("content://sms"), projection, selection, args, "date DESC");
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		for (int col = 0;col < crsr.getColumnCount();col++) {
			m.put(crsr.getColumnName(col), col);
		}
		List l = new List();
		l.Initialize();
		while (crsr.moveToNext()) {
			String personId = crsr.getString(m.get("person"));
			Sms s = new Sms(
					crsr.getInt(m.get(BaseColumns._ID)),
					crsr.getInt(m.get("thread_id")),
					personId == null ? -1 : Integer.parseInt(personId),
					crsr.getLong(m.get("date")),
					crsr.getInt(m.get("read")) > 0,
					crsr.getInt(m.get("type")),
					crsr.getString(m.get("body")),
					crsr.getString(m.get("address")));
			l.Add(s);
		}
		crsr.close();
		return l;
	}
	/**
	 * Represents an SMS message.
	 *SMS messages are retrieved using SmsMessages object.
	 */
	@ShortName("Sms")
	public static class Sms {

		/**
		 * Message internal id.
		 */
		public int Id;
		/**
		 * Thread id.
		 */
		public int ThreadId;
		/**
		 * The id of the person who sent the message.
		 *Will be -1 if this data is missing.
		 *You can find more information about this person by calling <code>Contacts.GetById</code>.
		 */
		public int PersonId;
		/**
		 * The date of this message.
		 */
		public long Date;
		/**
		 * Whether this message has been read.
		 */
		public boolean Read;
		/**
		 * The message type. One of the SmsMessages constant values.
		 */
		public int Type;
		/**
		 * Message body.
		 */
		public String Body;
		/**
		 * The message address.
		 */
		public String Address;
		@Hide
		public Sms(int id, int threadId, int personId, long date, boolean read,
				int type, String body, String address) {
			Id = id;
			ThreadId = threadId;
			PersonId = personId;
			Date = date;
			Read = read;
			Type = type;
			Body = body;
			Address = address;
		}
		public Sms() {
			
		}
		
		@Hide
		@Override
		public String toString() {
			return "Id=" + Id + ", ThreadId=" + ThreadId + ", PersonId=" + PersonId + 
			", Date=" + Date + ", Read=" + Read + ", Type=" + Type  + 
			", Body=" + Body + ", Address=" + Address;
		}
	}
	
}
