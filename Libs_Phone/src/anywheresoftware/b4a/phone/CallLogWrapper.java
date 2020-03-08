
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
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.CallLog;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;

/**
 * CallLog allows you to browse the call logs.
 *Retrieved calls are always ordered by descending date.
 *Usage example:<code>
 *Dim Calls As List
 *Dim CallLog As CallLog
 *calls = CallLog.GetAll(10) 'Get the last 10 calls
 *For i = 0 To calls.Size - 1
 *	Dim c As CallItem
 *	c = calls.Get(i)
 *	Dim callType, name As String
 *	Select c.CallType
 *		Case c.TYPE_INCOMING
 *			callType="Incoming"
 *		Case c.TYPE_MISSED
 *			callType = "Missed"
 *		Case c.TYPE_OUTGOING
 *			callType = "Outgoing"
 *	End Select
 *	name = c.CachedName
 *	If name = "" Then name = "N/A"
 *	Log("Number=" & c.Number & ", Name=" & name _
 *		& ", Type=" & callType & ", Date=" & DateTime.Date(c.Date))
 *Next</code>
 *
 */
@ShortName("CallLog")
@Permissions(values={"android.permission.READ_CONTACTS", "android.permission.READ_CALL_LOG"})
public class CallLogWrapper {
	private static final String[] calls_projection = {CallLog.Calls.DATE, CallLog.Calls.TYPE, CallLog.Calls.DURATION,
		CallLog.Calls.NUMBER, CallLog.Calls._ID, CallLog.Calls.CACHED_NAME};
	/**
	 * Returns all calls ordered by date (descending) as a List of CallItems.
	 *Limit - Maximum number of CallItems to return. Pass 0 to return all items.
	 */
	public List GetAll(int Limit) {
		return getAllCalls(null, null, Limit);
	}
	/**
	 * Returns the CallItem with the specified Id.
	 *Returns Null if no matching CallItem found.
	 */
	public CallItem GetById(int Id) {
		List l = getAllCalls(BaseColumns._ID + " = ?", new String[] {String.valueOf(Id)}, 0);
		if (l.getSize() == 0)
			return null;
		else
			return (CallItem) l.Get(0);
	}
	/**
	 * Returns all CallItems with a date value larger than the specified value.
	 *Limit - Maximum number of items to return. Pass 0 to return all items.
	 */
	public List GetSince(long Date, int Limit) {
		return getAllCalls(CallLog.Calls.DATE  + " >= ?", new String[] {Long.toString(Date)}, Limit);
	}
	private List getAllCalls(String selection, String[] args, int limit) {

		ContentResolver cr = BA.applicationContext.getContentResolver();
		Cursor crsr = cr.query(CallLog.Calls.CONTENT_URI, calls_projection, selection, args, CallLog.Calls.DATE + " DESC");
		List l = new List();
		l.Initialize();
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		for (int col = 0;col < crsr.getColumnCount();col++) {
			m.put(crsr.getColumnName(col), col);
		}
		int i = 0;
		while (crsr.moveToNext()) {
			
			CallItem ci = new CallItem(crsr.getString(m.get(CallLog.Calls.NUMBER)),
					crsr.getInt(m.get(BaseColumns._ID)),
					crsr.getLong(m.get(CallLog.Calls.DURATION)),
					crsr.getInt(m.get(CallLog.Calls.TYPE)),
					crsr.getLong(m.get(CallLog.Calls.DATE)),
					crsr.getString(m.get(CallLog.Calls.CACHED_NAME)));
			
			l.Add(ci);
			if (limit > 0 && ++i >= limit)
				break;
		}
		crsr.close();
		return l;
	}

	/**
	 * Represents a single call in the call logs. See CallLog for more information.
	 */
	@ShortName("CallItem")
	public static class CallItem {
		public CallItem() {}
		public static final int TYPE_INCOMING = CallLog.Calls.INCOMING_TYPE;
		public static final int TYPE_OUTGOING = CallLog.Calls.OUTGOING_TYPE;
		public static final int TYPE_MISSED = CallLog.Calls.MISSED_TYPE;
		/**
		 * The call phone number.
		 */
		public String Number;
		/**
		 * The call internal id.
		 */
		public int Id = -1;
		/**
		 * The call duration in seconds.
		 */
		public long Duration;
		/**
		 * The call type. This value matches one of the TYPE constants.
		 */
		public int CallType;
		/**
		 * The call date measured as ticks.
		 */
		public long Date;
		/**
		 * Returns the cached name assigned to this call number at the time of call.
		 *Returns an empty string if no name was assigned.
		 */
		public String CachedName = "";
		CallItem(String number, int id, long duration, int type, long date, String name) {
			Number = number == null ? "" : number;
			Id = id;
			CallType = type;
			Duration = duration;
			Date = date;
			CachedName = name == null ? "" : name;
		}
		
		@Override
		@Hide
		public String toString() {
			return "Id=" + Id + ", Number=" + Number + ",CachedName=" + CachedName + 
				", Type=" + CallType + ", Date=" + Date + ", Duration=" + Duration;
		}
	}
}
