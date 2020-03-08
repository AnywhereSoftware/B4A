
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
 
 package anywheresoftware.b4a.audio;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.MediaPlayerWrapper;

/**
 * MediaPlayerStream is similar to MediaPlayer. Unlike MediaPlayer which plays local files MediaPlayerStream plays audio streams
 *which are available online. Another difference between the objects is that in this case the Load method is asynchronous.
 *Only when the file is ready, the StreamReady event will be fired and you can start playing.
 *According to the native documentation the online resource must support progressive download.
 *Example:<code>
	 *Sub Process_Globals
	 *	Dim mp As MediaPlayerStream
	 *End Sub
	 *
	 *Sub Globals
	 *
	 *End Sub
	 *
	 *Sub Activity_Create(FirstTime As Boolean)
	 *	If FirstTime Then
	 *		mp.Initialize("mp")
	 *	End If
	 *	mp.Load("http://www...")
	 *End Sub
	 *Sub mp_StreamReady
	 *	Log("starts playing")
	 *	mp.Play
	 *End Sub
	 *Sub mp_StreamError (ErrorCode As String, ExtraData As Int)
	 *	Log("Error: " & ErrorCode & ", " & ExtraData)
	 *	ToastMessageShow("Error: " & ErrorCode & ", " & ExtraData, True)
	 *End Sub
	 *Sub mp_StreamBuffer(Percentage As Int)
	 *	Log(Percentage)
	 *End Sub</code>
 */
@Permissions(values = {"android.permission.INTERNET"})
@Events(values={"StreamReady", "StreamError (ErrorCode As String, ExtraData As Int)", "StreamBuffer(Percentage As Int)", "Complete"})
@ShortName("MediaPlayerStream")
public class MediaPlayerStreamWrapper extends MediaPlayerWrapper{
	
	/**
	 * Hidden.
	 */
	@Hide
	@Override
	public void Initialize2(final BA ba, String EventName) {
		
	}
	/**
	 * Initializes the object.
	 *EventName - Name of Subs that will handle the events.
	 */
	public void Initialize(final BA ba, String EventName) throws IllegalArgumentException, IllegalStateException, IOException {
		super.Initialize2(ba, EventName);
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				String error;
				switch (what) {
				case 100:
					error = "MEDIA_ERROR_SERVER_DIED";
					break;
				case 200:
					error = "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
					break;
				default:
					error = "MEDIA_ERROR_UNKNOWN";
				}
				ba.raiseEvent(MediaPlayerStreamWrapper.this, eventName + "_streamerror", error, extra);
				return false;
			}
			
		});
		mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				ba.raiseEvent(MediaPlayerStreamWrapper.this, eventName + "_streambuffer", percent);
			}
			
		});
		
	}
	/**
	 * Starts loading the resource from the given URL.
	 *StreamReady event will be raised when the stream is ready.
	 */
	public void Load(final BA ba, String URL) throws IllegalArgumentException, IllegalStateException, IOException {
		mp.reset();
		mp.setDataSource(URL);
		mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				ba.raiseEvent(MediaPlayerStreamWrapper.this, eventName + "_streamready");
			}
			
		});
		mp.prepareAsync();
	}
	/**
	 * Hidden
	 */
	@Hide
	@Override
	public int getPosition() {
		return mp.getCurrentPosition();
	}
	/**
	 * Hidden
	 */
	@Hide
	@Override
	public void setPosition(int value) {
		mp.seekTo(value);
	}
	/**
	 * Sets the playing volume for each channel. The value should be between 0 to 1.
	 */
	@Override
	public void SetVolume(float Right, float Left) {
		mp.setVolume(Right, Left);
	}
}
