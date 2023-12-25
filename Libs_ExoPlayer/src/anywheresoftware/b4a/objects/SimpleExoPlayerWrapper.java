
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

import java.io.IOException;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Player.TimelineChangeReason;
import androidx.media3.common.Timeline;
import androidx.media3.common.Tracks;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.ConcatenatingMediaSource;
import androidx.media3.exoplayer.source.LoopingMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.streams.File;

/**
 * An advanced audio and video player. It supports more formats than MediaPlayer.
 *Can be used together with SimpleExoPlayerView.
 *<b>Should be a process global variable.</b>
 */
@SuppressWarnings("deprecation")
@ShortName("SimpleExoPlayer")
@Version(3.0f)
@DependsOn(values={
		"media3-common-1.2.0.aar",
		"media3-container-1.2.0.aar",
		"media3-database-1.2.0.aar",
		"media3-datasource-1.2.0.aar",
		"media3-decoder-1.2.0.aar",
		"media3-exoplayer-1.2.0.aar",
		"media3-exoplayer-dash-1.2.0.aar",
		"media3-exoplayer-hls-1.2.0.aar",
		"media3-exoplayer-rtsp-1.2.0.aar",
		"media3-exoplayer-smoothstreaming-1.2.0.aar",
		"media3-extractor-1.2.0.aar",
		"media3-ui-1.2.0.aar",
		"androidx.media:media", "androidx.recyclerview:recyclerview", 
		"guava-31.1-android.jar"})
@Permissions(values = {"android.permission.INTERNET"})
@Events(values = {"Complete", "Error (Message As String)", "Ready", "TrackChanged"})
public class SimpleExoPlayerWrapper  {
	@Hide
	public ExoPlayer player;
	@Hide
	public TrackSelector trackSelector;
	private int currentState;
	private String eventName;
	public void InitializeCustom (final BA ba, String EventName, Object NativePlayer) {
		eventName = EventName.toLowerCase(BA.cul);
		player = (ExoPlayer)NativePlayer;
		player.addListener(new Player.Listener() {

			public void onTimelineChanged(Timeline timeline, @TimelineChangeReason int reason) {}
			@Override
			public void onPlayerError(PlaybackException error) {
				ba.raiseEvent(SimpleExoPlayerWrapper.this, eventName + "_error", String.valueOf(error.getCause()));
			}

			@Override
			public void onPlayerStateChanged(boolean playWhenReady,
					int playbackState) {
				if (playbackState != currentState) {
					currentState = playbackState;
					if (currentState == Player.STATE_ENDED)
						ba.raiseEvent(SimpleExoPlayerWrapper.this, eventName + "_complete");
					else if (currentState == Player.STATE_READY)
						ba.raiseEvent(SimpleExoPlayerWrapper.this, eventName + "_ready");
				}
			}

			@Override
			public void onTracksChanged(Tracks tracks) {
				ba.raiseEventFromUI(SimpleExoPlayerWrapper.this, eventName + "_trackchanged");
			}

		});
	}
	/**
	 * Initializes the player.
	 */
	public void Initialize(final BA ba, String EventName) {
		trackSelector = new DefaultTrackSelector(ba.context);
		ExoPlayer.Builder builder = new ExoPlayer.Builder(ba.context);
		builder.setTrackSelector(trackSelector);
		InitializeCustom(ba, EventName, builder.build());
	}
	/**
	 * Concatenates multiple sources.
	 */
	public Object CreateListSource (List Sources) {
		MediaSource[] sources = new MediaSource[Sources.getSize()];
		for (int i = 0;i < Sources.getSize();i++)
			sources[i] = (MediaSource)Sources.Get(i);
		return new ConcatenatingMediaSource(sources);
	}
	/**
	 * Creates a local file source.
	 */
	public Object CreateFileSource (String Dir, String FileName) throws IOException {
		String path;
		if (Dir.equals(File.getDirAssets())) {
			if (File.virtualAssetsFolder != null) {
				path = "file://" + File.Combine(File.virtualAssetsFolder, File.getUnpackedVirtualAssetFile(FileName));
			} else {
				path = "asset:///" + FileName.toLowerCase(BA.cul);
			}
		}
		else {
			path = "file://" + File.Combine(Dir, FileName);
		}
		return CreateUriSource(path);
	}
	/**
	 * Creates a Uri source for non-streaming media resources.
	 */
	public Object CreateUriSource (String Uri) {
		return new ProgressiveMediaSource.Factory(createDefaultDataFactory()).createMediaSource(MediaItem.fromUri(android.net.Uri.parse(Uri)));
	}
	/**
	 * Creates a loop source. The child source will be played multiple times.
	 *Pass -1 to play it indefinitely.
	 */
	public Object CreateLoopSource (Object Source, int Count) {
		return new LoopingMediaSource((MediaSource) Source, Count > 0 ? Count : Integer.MAX_VALUE);
	}
	/**
	 * Creates a HLS (Http live streaming) source.
	 */
	public Object CreateHLSSource (String Uri)  {
		return new HlsMediaSource.Factory(createDefaultDataFactory()).createMediaSource(MediaItem.fromUri(android.net.Uri.parse(Uri)));
	}
	/**
	 * Creates a Dash (Dynamic Adaptive Streaming over Http) source.
	 */
	public Object CreateDashSource (String Uri) {
		return new DashMediaSource.Factory(createDefaultDataFactory()).createMediaSource(MediaItem.fromUri(android.net.Uri.parse(Uri)));
	}
	/**
	 * Creates a Rtsp source.
	 */
	public Object CreateRtspSource (String Uri) {
		return new RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(android.net.Uri.parse(Uri)));
	}
	/**
	 * Creates a Smooth Streaming source.
	 */
	public Object CreateSmoothStreamingSource (String Uri) {
		return new SsMediaSource.Factory(createDefaultDataFactory()).createMediaSource(MediaItem.fromUri(android.net.Uri.parse(Uri)));
	}
	@Hide
	public DefaultDataSource.Factory createDefaultDataFactory() {
		return new DefaultDataSource.Factory(BA.applicationContext);
	}
	
	/**
	 * Returns true if the player is currently playing.
	 */
	public boolean getIsPlaying() {
		return player.isPlaying();
	}
	/**
	 * Prepares the media source. The Ready event will be raised when the playback is ready. You can call play immediately after calling this method.
	 */
	public void Prepare (Object Source) {
		player.setMediaSource((MediaSource)Source);
		player.prepare();
	}
	/**
	 * Starts or resumes playback. If the source is currently loading then it will starting playing when ready.
	 */
	public void Play() {
		player.setPlayWhenReady(true);
	}
	/**
	 * Pauses the playback.
	 */
	public void Pause() {
		player.setPlayWhenReady(false);
	}
	/**
	 * Releases the player resources. The player needs to be initialized again before it can be used.
	 */
	public void Release() {
		player.release();
	}
	/**
	 * Gets or sets the current position (in milliseconds). Note that the Ready event will be raised after this call.
	 */
	public long getPosition() {
		return player.getCurrentPosition();
	}
	public void setPosition(int value) {
		player.seekTo(value);
	}
	/**
	 * Returns the resource duration (in milliseconds).
	 */
	public long getDuration() {
		return player.getDuration();
	}
	/**
	 * Gets or sets the volume (0 - 1).
	 */
	public float getVolume() {
		return player.getVolume();
	}
	public void setVolume(float f) {
		player.setVolume(f);
	}
	/**
	 * Returns the index of the window currently played.
	 */
	public int getCurrentWindowIndex() {
		return player.getCurrentMediaItemIndex();
	}
}
