
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

import java.io.File;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.IOnActivityResult;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.phone.Phone.ContentChooser;
/**
 * The RingtoneManager object allows you to set or get the default ringtone.
 *It also provides access to the default ringtone picker.
 *The RingtoneResult event will be raised when the picker is closed with the Uri of the selected ringtone.
 *Note that an empty string will be returned if the "Silence" option was selected.
 *Example of playing the selected ringtone with MediaPlayer:
 *<code>
 *Sub Process_Globals
 *	Private rm As RingtoneManager
 *End Sub
 *
 *Sub Globals
 *
 *End Sub
 *
 *Sub Activity_Create(FirstTime As Boolean)
 *	rm.ShowRingtonePicker("rm", rm.TYPE_RINGTONE, True, "")
 *End Sub
 *
 *Sub rm_PickerResult (Success As Boolean, Uri As String)
 *	If Success Then
 *		If Uri = "" Then
 *			ToastMessageShow("Silent was chosen", True)
 *		Else
 *			rm.Play(Uri)
 *		End If
 *	Else
 *		ToastMessageShow("Error loading ringtone.", True)
 *	End If	
 *End Sub</code>
 */
@ShortName("RingtoneManager")
@Permissions(values={"android.permission.WRITE_SETTINGS"})
@Events(values={"PickerResult (Success As Boolean, Uri As String)"})
public class RingtoneManagerWrapper {
    public static final int TYPE_RINGTONE = 1;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_ALARM = 4;
    private IOnActivityResult ion;
    /**
     * Returns a string that represents the virtual content folder. This can be used to play a Ringtone with MediaPlayer.
     */
    public String GetContentDir() {
    	return anywheresoftware.b4a.objects.streams.File.ContentDir;
    }
  /**
   * This method no longer works due to restrictions in Android.
   */
    public String AddToMediaStore(String Dir, String FileName, String Title, boolean IsAlarm, boolean IsNotification, boolean IsRingtone, boolean IsMusic) {
    	File k = new File(Dir, FileName);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, Title);
        String mType = "audio/*";
        String mExtension = MimeTypeMap.getFileExtensionFromUrl(k.getAbsolutePath());
        if (mExtension != null) {
            mType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExtension);
        }
        values.put(MediaStore.MediaColumns.MIME_TYPE, mType);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, IsRingtone);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, IsNotification);
        values.put(MediaStore.Audio.Media.IS_ALARM, IsAlarm);
        values.put(MediaStore.Audio.Media.IS_MUSIC, IsMusic);
        
        return BA.applicationContext.getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath()), values).toString();
    }
    /**
     * Sets the default ringtone for the given type.
     *In order to get the Uri you should use AddToMediaStore (for new sounds) or ShowRingtonePicker (for existing sounds).
     */
	public void SetDefault(int Type, String Uri) {
		RingtoneManager.setActualDefaultRingtoneUri(BA.applicationContext, Type, android.net.Uri.parse(Uri));
	}
	/**
	 * Returns the Uri of the default ringtone of a specific type.
	 *Returns an empty string if no default is available.
	 *Use Play to play the ringtone._
	 */
	public String GetDefault(int Type) {
		Uri u = RingtoneManager.getDefaultUri(Type);
		if (u == null)
			return "";
		return u.toString();
	}
	/**
	 * Plays a ringtone Uri.
	 *Returns an object that can be passed to Stop, to stop playback.
	 */
	public Object Play(BA ba, String Uri) {
		Ringtone r = RingtoneManager.getRingtone(ba.context, android.net.Uri.parse(Uri));
		if (r != null)
			r.play();
		return r;
	}
	/**
	 * Stops playback of a previously played ringtone.
	 *Ringtone - the object returned from Play.
	 */
	public void Stop(Object Ringtone) {
		if (Ringtone != null)
			((Ringtone)Ringtone).stop();;
	}
	/**
	 * Deletes the given entry.
	 */
	public void DeleteRingtone(String Uri) {
		BA.applicationContext.getContentResolver().delete(android.net.Uri.parse(Uri), null, null);
	}
	/**
	 * Shows the ringtone picker activity.
	 *The PickerResult will be raised after the user selects a ringtone.
	 *EventName - Sets the sub that will handle the PickerResult event.
	 *Type - Defines the type(s) of sounds that will be listed. Multiple types can be set using Bit.Or.
	 *IncludeSilence - Whether to include the Silence option in the list.
	 *ChosenRingtone - The uri of the ringtone that will be selected when the dialog opens. Pass an empty string if not needed.
	 */
	public void ShowRingtonePicker(final BA ba, String EventName, int Type, boolean IncludeSilence, String ChosenRingtone) {
		final String eventName = EventName.toLowerCase(BA.cul);
		Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, Type);
		i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, IncludeSilence);
		Uri def = RingtoneManager.getDefaultUri(Type);
		if (def != null)
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, def);
		if (ChosenRingtone.length() > 0) {
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(ChosenRingtone));
		}
		ion = new IOnActivityResult() {
			@Override
			public void ResultArrived(int resultCode, Intent intent) {
				String uri = null;
				if (resultCode == Activity.RESULT_OK && intent != null) {
					Uri u = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					if (u == null) //silent
						uri = "";
					else
						uri = u.toString();
				}
				ion = null;
				if (uri != null) {
					ba.raiseEvent(RingtoneManagerWrapper.this, eventName + "_pickerresult", true, uri);
				}
				else {
					ba.raiseEvent(RingtoneManagerWrapper.this, eventName + "_pickerresult", false, "");
				}
			}
		};
		ba.startActivityForResult(ion, i);
	}
}
