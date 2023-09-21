
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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;

/**
 * Plays a "beep" sound with the given duration and frequency.
 *Example:<code>
 *Dim b As Beeper
 *b.Initialize(300, 500)
 *b.Beep</code>
 */
@ShortName("Beeper")
public class Beeper {
    private AudioTrack at;
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
	 * Initializes the object with the given duration measured in milliseconds and the given frequency measured in Hertz.
	 *The music volume channel will be used.
	 */
	public void Initialize(int Duration, int Frequency) {
		Initialize2(Duration, Frequency, VOLUME_MUSIC);
	}
	/**
	 * Similar to Initialize. Allows you to set the voice channel.
	 */
    public void Initialize2(int Duration, int Frequency, int VoiceChannel) {
    	double duration = Duration / 1000d;
    	final int sampleRate = 8000;
    	int numSamples = (int) (sampleRate * duration);
    	int freqOfTone = Frequency;
    	byte[] generatedSnd = new byte[2 * numSamples];
    	 // fill out the array
    	int idx = 0;
        for (int i = 0; i < numSamples; ++i) {
            double d = Math.sin(2 * Math.PI * i / ((float)sampleRate/freqOfTone));
            final short val = (short) ((d * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        at = new AudioTrack(VoiceChannel,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples * 2,
                AudioTrack.MODE_STATIC);
        
        at.write(generatedSnd, 0, generatedSnd.length);
    }
    /**
     * Plays the sound.
     */
    public void Beep() {
    	BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				if (at.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
					at.stop();
					at.reloadStaticData();
				}
				at.play();
			}
    		
    	}, this, 1);
    }
    /**
     * Releases the resources used by this beeper.
     */
    public void Release() {
    	if (at != null) {
    		at.release();
    	}
    }

}
