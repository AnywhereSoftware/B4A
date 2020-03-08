
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

import android.media.JetPlayer;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;

@Version(1.71f)
@ShortName("JetPlayer")
@Events(values={"QueuedSegmentsCountChanged (Count As Int)", "CurrentUserIdChanged (UserId As Int, RepeatCount As Int)"})
public class JetPlayerWrapper extends AbsObjectWrapper<JetPlayer>{
	/**
	 * Initializes the object and sets the Subs that will handle the JetPlayer events.
	 */
	public void Initialize(final BA ba, String EventName) {
		JetPlayer jp = JetPlayer.getJetPlayer();
		setObject(jp);
		jp.clearQueue();
		final String e = EventName.toLowerCase(BA.cul);
		jp.setEventListener(new JetPlayer.OnJetEventListener() {

			@Override
			public void onJetEvent(JetPlayer player, short segment, byte track,
					byte channel, byte controller, byte value) {
				
			}

			@Override
			public void onJetNumQueuedSegmentUpdate(JetPlayer player,
					int nbSegments) {
				ba.raiseEvent(getObject(), e + "_queuedsegmentscountchanged", nbSegments);
			}

			@Override
			public void onJetPauseUpdate(JetPlayer player, int paused) {
				
			}

			@Override
			public void onJetUserIdUpdate(JetPlayer player, int userId,
					int repeatCount) {
				ba.raiseEvent(getObject(), e + "_currentuseridchanged", userId, repeatCount);
			}
			
		});
	}
	/**
	 * Loads a jet file.
	 */
	public void LoadFile(String Dir, String File) throws IOException {
		boolean result;
		if (Dir.equals(anywheresoftware.b4a.objects.streams.File.getDirAssets())) {
			if (anywheresoftware.b4a.objects.streams.File.virtualAssetsFolder != null) {
				LoadFile(anywheresoftware.b4a.objects.streams.File.virtualAssetsFolder, 
						anywheresoftware.b4a.objects.streams.File.getUnpackedVirtualAssetFile(File));
				return;
			}
			result = getObject().loadJetFile(BA.applicationContext.getAssets().openFd(File.toLowerCase(BA.cul)));
		}
		else {
			 result = getObject().loadJetFile(anywheresoftware.b4a.objects.streams.File.Combine(Dir, File));
		}
		if (!result) {
			throw new IOException("Error loading Jet file.");
		}
	}
	/**
	 * Closes the resources related to the loaded file.
	 */
	public void CloseFile() {
		if (IsInitialized())
			getObject().closeJetFile();
	}
	/**
	 * Clears the segments queue.
	 */
	public void ClearQueue() {
		getObject().clearQueue();
	}
	/**
	 * Adds a segment to the queue. No more than 3 segments are allowed.
	 *SegmentNum - The segment identifier.
	 *LibNum - The index of the sound bank associated with this segment. Pass -1 if there is no sound bank.
	 *RepeatCount - Number of times the segment will be repeated. 0 means that it will be played once. Pass -1 to repeat indefinitely.
	 *Transpose - The pitch transition. Should be between -12 to 12.
	 *MuteArray - An array of booleans that sets the mute value of each track. The array length must be equal to MaxTracks value.
	 *UserId - An id given to this segment. When the current segment changes the CurrentUserIdChanged event is raised with this id
	 *(assuming that the id of the previous segment was different). 
	 */
	public void QueueSegment(int SegmentNum, int LibNum,
			int RepeatCount, int Transpose, boolean[] MuteArray, byte UserId) {
		if (getObject().queueJetSegmentMuteArray(SegmentNum, LibNum,
				RepeatCount, Transpose, MuteArray, UserId) == false)
			throw new RuntimeException("Error queuing segment.");
	}
	/**
	 * Sets the tracks mute state.
	 *MuteArray - An array of booleans that sets the mute state of each track. The array length must be equal to MaxTracks value.
	 *Sync - If false the change will be applied as soon as possible, otherwise the change will be applied at the start of the next segment or next repeat.
	 */
	public void SetMute(boolean[] MuteArray, boolean Sync) {
		if (!getObject().setMuteArray(MuteArray, Sync))
			throw new RuntimeException("Error setting mute state.");
	}
	/**
	 * Similar to SetMute but only changes the state of a single track.
	 */
	public void SetTrackMute(int Track, boolean Mute, boolean Sync) {
		if (!getObject().setMuteFlag(Track, Mute, Sync))
			throw new RuntimeException("Error setting mute state.");
	}
	/**
	 * Starts playing the segments queue.
	 */
	public void Play() {
		if (!getObject().play())
			throw new RuntimeException("Error playing file.");
	}
	/**
	 * Pauses playback.
	 */
	public void Pause() {
		if (!getObject().pause()) {
			throw new RuntimeException("Error pausing.");
		}
	}
	/**
	 * Returns the maximum number of simultaneous tracks.
	 */
	public int getMaxTracks() {
		return getObject().getMaxTracks();
	}
	/**
	 * Releases all resources allocated for the JetPlayer.
	 */
	public void Release() {
		getObject().release();
	}
}
