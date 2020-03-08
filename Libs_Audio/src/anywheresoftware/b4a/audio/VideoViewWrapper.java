
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

import java.io.File;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.MediaPlayerWrapper;
import anywheresoftware.b4a.objects.ViewWrapper;

/**
 * VideoView is a view that allows you to play video media inside your application.
 *The VideoView optionally shows a media controller when the user touches the view.
 *The Completed event is raised when playback is completed.
 *Simple example of using VideoView:<code>
	 *Sub Globals
	 *	Dim vv As VideoView
	 *End Sub
	 *Sub Activity_Create(FirstTime As Boolean)
	 *	vv.Initialize("vv")
	 *	Activity.AddView(vv, 10dip, 10dip, 250dip, 250dip)
	 *	vv.LoadVideo(File.DirRootExternal, "somefile.mp4")
	 *	vv.Play
	 *End Sub
	 *Sub vv_Complete
	 *	Log("Playing completed")
	 *End Sub</code>
 *
 */
@DontInheritEvents
@ShortName("VideoView")
@ActivityObject
@Events(values={"Complete"})
public class VideoViewWrapper extends ViewWrapper<VideoView>{
	/**
	 * Initialize the objects and sets the name of the subs that will handle the events.
	 */
	@Override
	public void Initialize(final BA ba, String EventName) {
		super.Initialize(ba, EventName);
	}
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new VideoView(ba.context));
		super.innerInitialize(ba, eventName, true);
		setMediaControllerEnabled(true);
		getObject().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				ba.raiseEvent(getObject(), eventName + "_complete");
			}
			
		});
	}
	/**
	 * Loads a video file and prepares it for playing.
	 *It is not possible to load files from the assets folder.
	 *Advanced: you can pass "http" to the Dir parameter and then a full URL (including http) to the FileName.
	 *In this case the online video will be streamed. Note that you need to add the INTERNET permission for this to work.
	 */
	public void LoadVideo(String Dir, String FileName) {
		if (Dir.equals(anywheresoftware.b4a.objects.streams.File.getDirAssets())) { 
			throw new RuntimeException("Cannot load video from assets folder.");
		}
		else if (Dir.equals(anywheresoftware.b4a.objects.streams.File.ContentDir)) {
			getObject().setVideoPath(FileName);
		}
		else if (Dir.equals("http")) {
			getObject().setVideoPath(FileName);
		}
		else {
			getObject().setVideoPath(new File(Dir, FileName).toString());
		}
	}
	/**
	 * Starts or resumes playing.
	 */
	public void Play() {
		getObject().start();
	}
	/**
	 * Pauses the playback.
	 */
	public void Pause() {
		getObject().pause();
	}
	/**
	 * Stops the playback.
	 */
	public void Stop() {
		getObject().stopPlayback();
	}
	/**
	 * Tests whether the video is currently playing.
	 */
	public boolean IsPlaying() {
		return getObject().isPlaying();
	}
	/**
	 * Gets or sets the playing position (in milliseconds).
	 */
	public int getPosition() {
		return getObject().getCurrentPosition();
	}
	public void setPosition(int v) {
		getObject().seekTo(v);
	}
	/**
	 * Gets the video duration in milliseconds.
	 */
	public int getDuration() {
		return getObject().getDuration();
	}
	/**
	 * Sets whether the media controller is enabled. It is enabled by default.
	 *Note that the media player gets attached to the VideoView parent.
	 */
	public void setMediaControllerEnabled (boolean v) {
		getObject().setMediaController(v ? new MediaController(ba.context) : null);
	}
	@Override
	public String toString() {
		if (getObjectOrNull() == null)
			return super.toString();
		if (getObject().isPlaying() == false)
			return "Not playing";
		else
			return "Playing, Position=" + getPosition() + ", Duration=" + getDuration();
	}
	

}
