
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;

@Events(values={"RecordBuffer (Data() As Byte)", "PlaybackComplete", "Error"})
@ShortName("AudioStreamer")
@Permissions(values={"android.permission.RECORD_AUDIO"})
public class AudioStreamer {

    AudioRecord audioRecord;
    private Recorder recorder;
    private Player player;
    private Thread recThread, playThread;
    private byte[] recordBuffer;
    private BA ba;
    private String eventName;
    private AudioTrack track;
    private int playSize;
    /**
	 * Phone ring channel.
	 */
	public static final int VOLUME_RING = AudioManager.STREAM_RING;
	/**
	 * Alarms channel.
	 */
	public static final int VOLUME_ALARM = AudioManager.STREAM_ALARM;
	/**
	 * Music channel.
	 */
	public static final int VOLUME_MUSIC = AudioManager.STREAM_MUSIC;
	/**
	 * Notifications channel.
	 */
	public static final int VOLUME_NOTIFICATION = AudioManager.STREAM_NOTIFICATION;
	/**
	 * System sounds channel.
	 */
	public static final int VOLUME_SYSTEM = AudioManager.STREAM_SYSTEM;
	/**
	 * Voice calls channel.
	 */
	public static final int VOLUME_VOICE_CALL = AudioManager.STREAM_VOICE_CALL;
	
	/**
	 * Initializes the object.
	 *EventName - Sets the subs that will handle the events.
	 *SampleRate - Sample rate in Hz. Common values: 44100, 22050 and 11025.
	 *Mono - True for mono false for stereo.
	 *Encoding - 8 for 8 bit or 16 for 16 bit. <b>Only 16 bit is supported for now.</b>
	 *VolumeChannel - The output channel. One of the VOLUME constants. 
	 */
	public void Initialize(BA ba, String EventName, int SampleRate, boolean Mono, int Encoding, int VolumeChannel) {
		Initialize2(ba, AudioSource.MIC, EventName, SampleRate, Mono, Encoding, VolumeChannel);
	}
	/**
	 * Similar to Initialize. Allows you to set the audio source.
	 *The values are listed <link>here|http://developer.android.com/reference/android/media/MediaRecorder.AudioSource.html</link>.
	 */
	public void Initialize2(BA ba, int AudioSource, String EventName, int SampleRate, boolean Mono, int Encoding, int VolumeChannel) {
		this.ba = ba;
		eventName = EventName.toLowerCase(BA.cul);
		int channelConfig = Mono ? AudioFormat.CHANNEL_CONFIGURATION_MONO : AudioFormat.CHANNEL_CONFIGURATION_STEREO;
		int audioFormat = Encoding == 8 ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
		int bufferSize = AudioRecord.getMinBufferSize(SampleRate,channelConfig, audioFormat);
        recordBuffer = new byte[bufferSize];
        audioRecord = new AudioRecord(AudioSource, 
        		SampleRate,channelConfig, audioFormat,bufferSize);
        playSize = AudioTrack.getMinBufferSize(SampleRate, channelConfig, audioFormat);
        playSize = Math.max(playSize, bufferSize);
        track = new AudioTrack(VolumeChannel, SampleRate, channelConfig, audioFormat, playSize, AudioTrack.MODE_STREAM); 
	}
	
	/**
	 * Returns the player buffer size. This is the maximum size that can be written at once.
	 */
	public int getPlayerBufferSize() {
		return playSize;
	}
	/**
	 * Starts recording. The RecordBuffer event will be raised during the record.
	 */
	public void StartRecording() {
		StopRecording();
		recorder = new Recorder();
		recThread = new Thread(recorder);
		audioRecord.startRecording();
		recThread.setDaemon(true);
		recThread.start();
		
	}
	/**
	 * Stops recording.
	 */
	public void StopRecording() {
		if (recorder != null) {
			recorder.working = false;
			recThread.interrupt();
			audioRecord.stop();
		}
		recorder = null;
	}
	/**
	 * Starts playing. You should call Write to write the PCM data while playing is in progress.
	 */
	public void StartPlaying() {
		StopPlaying();
		player = new Player();
		track.play();
		playThread = new Thread(player);
		playThread.setDaemon(true);
		playThread.start();
	}
	/**
	 * Stops playing.
	 */
	public void StopPlaying() {
		if (player != null) {
			player.working = false;
			if (Thread.currentThread() != playThread)
				playThread.interrupt();
			track.stop();
		}
		player = null;
	}
	/**
	 * Writes the data to the player queue. The array size must be smaller than PlayerBufferSize.
	 *Returns False if the internal queue is full. In that case the data was not written.
	 *Writing Null to the queue will stop the player when the message is processed and then raise the PlaybackComplete event.
	 */
	public boolean Write(byte[] Data) throws InterruptedException {
		if (player != null)
			return player.put(Data);
		else
			return false;
	}
	class Recorder implements Runnable {
		volatile boolean working = true;
		@Override
		public void run() {
			try {
			while (working) {
				int bufferRead = audioRecord.read(recordBuffer, 0, recordBuffer.length);
				if (!working)
					return;
				byte[] data = new byte[bufferRead];
				System.arraycopy(recordBuffer, 0, data, 0, bufferRead);
				ba.raiseEventFromDifferentThread(null, null, 0, eventName + "_recordbuffer", false, new Object[] {data});
			} 
			} catch (Exception e) {
				e.printStackTrace();
				ba.setLastException(e);
				ba.raiseEventFromDifferentThread(null, null, 0, eventName + "_error", false, null);
			}
		}
	}
	class Player implements Runnable {
		volatile boolean working = true;
		private final byte[] STOP = new byte[0];
		private final ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(10000);
		@Override
		public void run() {
			while (working) {
				byte[] data;
				try {
					data = queue.take();
					if (data == STOP) {
						StopPlaying();
						ba.raiseEventFromDifferentThread(null, null, 0, eventName + "_playbackcomplete", false, new Object[] {});
						return;
					}
					track.write(data, 0, data.length);
				} catch (InterruptedException e) {
					working = false;
				}
				
			}
		}
		public boolean put(byte[] data) throws InterruptedException {
			if (data == null)
				return queue.offer(STOP);
			return queue.offer(data);
		}
		
	}
}
