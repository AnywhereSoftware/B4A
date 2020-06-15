
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
 
 package anywheresoftware.b4a.obejcts;

import java.util.Locale;

import android.speech.tts.TextToSpeech;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

/**
 * Synthesizes text to speech and plays it.
 *After initializing the object you should wait for the Ready event.
 *See this <link>example|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/7043-android-text-speech-example.html</link>.
 */
@Version(1.01f)
@ShortName("TTS")
@Events(values={"Ready (Success As Boolean)"})
public class TTS extends AbsObjectWrapper<TextToSpeech> {
	/**
	 * Initializes the object.
	 *The Ready event will be raised when the text to speech engine is ready.
	 *EventName - The Sub that will handle the Ready event.
	 */
	public void Initialize(final BA ba, final String EventName) {
		TextToSpeech tts = new TextToSpeech(ba.context, new TextToSpeech.OnInitListener() {

			@Override
			public void onInit(int status) {
				ba.raiseEventFromUI(null, EventName.toLowerCase(BA.cul) + "_ready", status == TextToSpeech.SUCCESS);
			}
			
		});
		setObject(tts);
	}
	/**
	 * Speaks the given text.
	 *ClearQueue - If True then all waiting texts are dismissed and the new text is spoken.
	 *Otherwise the new text is added to the queue.
	 */
	public void Speak (String Text, boolean ClearQueue) {
		int r = getObject().speak(Text, ClearQueue ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, null);
		if (r != TextToSpeech.SUCCESS)
			throw new RuntimeException("Error speaking text.");
	}
	/**
	 * Stops speaking any currently playing text (and dismisses texts in the queue).
	 */
	public void Stop() {
		getObject().stop();
	}
	/**
	 * Sets the pitch value. Default is 1.
	 *Example: <code>TTS1.Pitch = 1.5</code>
	 */
	public void setPitch(float value) {
		getObject().setPitch(value);
	}
	/**
	 * Sets the speech rate. Default is 1.
	 *Example: <code>TTS1.SpeechRate = 0.5</code>
	 */
	public void setSpeechRate(float value) {
		getObject().setSpeechRate(value);
	}
	/**
	 * Sets the spoken language.
	 *Language - Language code. Two lowercase letters.
	 *Country - Country code. Two uppercase letters. Pass an empty string if not needed.
	 *Returns True if a matching language is available. The country value will be ignored if the language code matches and the country code does not match.
	 */
	public boolean SetLanguage(String Language, String Country) {
		Locale l;
		if (Country.length() > 0)
			l = new Locale(Language, Country);
		else
			l = new Locale(Language);
		int r = getObject().setLanguage(l);
		return r >= 0;
	}
	/**
	 * Releases any resources related to this object. You will need to initialize the object again before use.
	 *Note that it is safe to call this method with an uninitialized object.
	 */
	public void Release() {
		if (IsInitialized()) {
			getObject().shutdown();
			setObject(null);
		}
	}
}
