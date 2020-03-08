
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
import android.media.SoundPool;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.streams.File;

/**
 * SoundPool holds a collection of short sounds which can be played with low latency.
 *Each sound has two Id values which you should work with. The first is the LoadId which is returned when loading the sound with Load.
 *The second is the PlayId which is returned when you call Play.
 *When working with SoundPool it is useful to watch the unfiltered LogCat for messages (for example when the sound is too long).
 */
@ShortName("SoundPool")
public class SoundPoolWrapper extends AbsObjectWrapper<SoundPool>{
	/**
	 * Initializes the SoundPool and sets the maximum number of simultaneous streams.
	 */
	public void Initialize(int MaxStreams) {
		setObject(new SoundPool(MaxStreams, AudioManager.STREAM_MUSIC, 0));
	}
	/**
	 * Loads a sound file and returns the sound LoadId.
	 *Example:<code>
	 *Dim LoadId As Int
	 *LoadId = SP.Load(File.DirAssets, "sound.wav")</code>
	 */
	public int Load(String Dir, String File) throws IOException {
		if (Dir.equals(anywheresoftware.b4a.objects.streams.File.getDirAssets())) {
			if (anywheresoftware.b4a.objects.streams.File.virtualAssetsFolder != null) {
				return Load(anywheresoftware.b4a.objects.streams.File.virtualAssetsFolder, 
						anywheresoftware.b4a.objects.streams.File.getUnpackedVirtualAssetFile(File));
			}
			return getObject().load(BA.applicationContext.getAssets().openFd(File.toLowerCase(BA.cul)), 1);
		}
		else {
			return getObject().load(anywheresoftware.b4a.objects.streams.File.Combine(Dir, File), 1);
		}
	}
	/**
	 * Plays the sound with the matching LoadId and returns the PlayId. Returns 0 if there was an error.
	 *LoadId - The value returned when loading the file.
	 *LeftVolume / RightVolume - The volume value (0 - 1)
	 *Priority - A priority value which you assign to this sound. The higher the value the higher the priority.
	 *When the number of simultaneous streams is higher than the value set in Initialize the lowest priority stream will be stopped.
	 *Loop - Number of times to repeat. Pass -1 to repeat indefinitely.
	 *Rate - Playback rate (0 - 2).
	 */
	public int Play(int LoadId, float LeftVolume, float RightVolume, int Priority, int Loop, float Rate) {
		return getObject().play(LoadId, LeftVolume, RightVolume, Priority, Loop, Rate);
	}
	/**
	 * Pauses the stream with the given PlayId.
	 */
	public void Pause(int PlayId) {
		getObject().pause(PlayId);
	}
	/**
	 * Resumes the stream with the given PlayId.
	 */
	public void Resume(int PlayId) {
		getObject().resume(PlayId);
	}
	/**
	 * Stops the stream with the given PlayId.
	 */
	public void Stop(int PlayId) {
		getObject().stop(PlayId);
	}
	/**
	 * Sets the volume of the stream with the given PlayId. Values are between 0 to 1.
	 */
	public void SetVolume(int PlayId, float Left, float Right) {
		getObject().setVolume(PlayId, Left, Right);
	}
	/**
	 * Unloads the stream with the given LoadId.
	 */
	public void Unload(int LoadId) {
		getObject().unload(LoadId);
	}
	/**
	 * Sets the rate of the stream with the given PlayId. Value is between 0 to 2. 
	 */
	public void SetRate(int PlayId, float Rate) {
		getObject().setRate(PlayId, Rate);
	}
	/**
	 * Releases all resources allocated to this object.
	 */
	public void Release() {
		getObject().release();
	}
}
