
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

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import android.content.pm.PackageManager.NameNotFoundException;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.B4AApplication;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.streams.File;

/**
 * An advanced audio and video player. It supports more formats than MediaPlayer.
 *Can be used together with SimpleExoPlayerView.
 *<b>Should be a process global variable.</b>
 */
@ShortName("SimpleExoPlayer")
@Version(1.52f)
@DependsOn(values={"exoplayer-2.13.3.aar", 
		"exoplayer-common-2.13.3.aar", 
		"exoplayer-core-2.13.3.aar", 
		"exoplayer-dash-2.13.3.aar", 
		"exoplayer-extractor-2.13.3.aar", 
		"exoplayer-hls-2.13.3.aar", 
		"exoplayer-smoothstreaming-2.13.3.aar", 
		"exoplayer-ui-2.13.3.aar", 
		"extension-rtmp-2.13.3.aar", 
		"exoplayer_desugar.jar", "androidx.media:media", "androidx.recyclerview:recyclerview", 
		"guava-30.1.1.WithoutListenable.jar", "com.google.guava:listenablefuture"})
@Permissions(values = {"android.permission.INTERNET"})
@Events(values = {"Complete", "Error (Message As String)", "Ready", "TrackChanged"})
public class SimpleExoPlayerWrapper  {
	@Hide
	public SimpleExoPlayer player;
	@Hide
	public TrackSelector trackSelector;
	private int currentState;
	private String eventName;
	public void InitializeCustom (final BA ba, String EventName, Object NativePlayer) {
		eventName = EventName.toLowerCase(BA.cul);
		player = (SimpleExoPlayer)NativePlayer;
		player.addListener(new Player.EventListener() {

			@Override
			public void onLoadingChanged(boolean isLoading) {
			}

			@Override
			public void onPlayerError(ExoPlaybackException error) {
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
			public void onPlaybackParametersChanged(
					PlaybackParameters playbackParameters) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRepeatModeChanged(int repeatMode) {
				
			}

			@Override
			public void onTracksChanged(TrackGroupArray trackGroups,
					TrackSelectionArray trackSelections) {
				ba.raiseEventFromUI(SimpleExoPlayerWrapper.this, eventName + "_trackchanged");
			}

			@Override
			public void onPositionDiscontinuity(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSeekProcessed() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onShuffleModeEnabledChanged(boolean arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTimelineChanged(Timeline arg0, Object arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onIsPlayingChanged(boolean arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPlaybackSuppressionReasonChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTimelineChanged(Timeline arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onEvents(Player arg0, com.google.android.exoplayer2.Player.Events arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onExperimentalOffloadSchedulingEnabledChanged(boolean offloadSchedulingEnabled) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onExperimentalSleepingForOffloadChanged(boolean arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onMediaItemTransition(MediaItem mediaItem, int reason) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onIsLoadingChanged(boolean isLoading) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPlaybackStateChanged(int state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStaticMetadataChanged(java.util.List<Metadata> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	/**
	 * Initializes the player.
	 */
	public void Initialize(final BA ba, String EventName) {
		AdaptiveTrackSelection.Factory videoTrackSelectionFactory =
		    new AdaptiveTrackSelection.Factory();
		trackSelector =
		    new DefaultTrackSelector(videoTrackSelectionFactory);
		player = ExoPlayerFactory.newSimpleInstance(BA.applicationContext, trackSelector);
		InitializeCustom(ba, EventName, player);
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
		return new ProgressiveMediaSource.Factory(createDefaultDataFactory()).createMediaSource(android.net.Uri.parse(Uri));
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
		return new HlsMediaSource.Factory(createDefaultDataFactory()).createMediaSource(android.net.Uri.parse(Uri));
	}
	/**
	 * Creates a Dash (Dynamic Adaptive Streaming over Http) source.
	 */
	public Object CreateDashSource (String Uri) {
		return new DashMediaSource.Factory(createDefaultDataFactory()).createMediaSource(android.net.Uri.parse(Uri));
	}
	/**
	 * Creates a Smooth Streaming source.
	 */
	public Object CreateSmoothStreamingSource (String Uri) {
		return new SsMediaSource.Factory(createDefaultDataFactory()).createMediaSource(android.net.Uri.parse(Uri));
	}
	@Hide
	public DefaultDataSourceFactory createDefaultDataFactory() {
		try {
			return new DefaultDataSourceFactory(BA.applicationContext, Util.getUserAgent(BA.applicationContext, B4AApplication.getLabelName()));
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
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
		player.prepare((MediaSource)Source);
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
		return player.getCurrentWindowIndex();
	}
}
