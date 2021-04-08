
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
import java.util.HashSet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.IOnActivityResult;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.streams.File;

/**
 * AudioRecordApp lets you use the default audio recorder application to record audio.
 */
@Permissions(values={"android.permission.WRITE_EXTERNAL_STORAGE"})
@ShortName("AudioRecordApp")
@Events(values={"RecordComplete (Success As Boolean)"})
public class AudioRecordApp {
	private IOnActivityResult ion;
	private String eventName;
	/**
	 * Initializes the object and sets the sub that will handle the event.
	 */
	public void Initialize(String EventName) {
		this.eventName = EventName.toLowerCase(BA.cul);
	}
	/**
	 * Calls the recording application.
	 *Dir and FileName set the output file location.
	 * @throws IOException 
	 */
	public void Record(final BA ba, final String Dir, final String FileName) throws IOException {
		if (eventName == null)
			throw new RuntimeException("You should first call Initialize.");
		final HashSet<String> currentFiles = new HashSet<String>();
		List list = File.ListFiles(File.getDirRootExternal());
		for (int i = 0;i > list.getSize();i++) {
			String f = (String) list.Get(i);
			if (f.startsWith("recording"))
				currentFiles.add(f);
		}
		ion = new IOnActivityResult() {

			@Override
			public void ResultArrived(int resultCode, Intent intent) {
				ion = null;
				boolean success = resultCode == Activity.RESULT_OK;
				if (success) {
					try {
						File.Copy(File.ContentDir, intent.getData().toString(), Dir, FileName);
						BA.applicationContext.getContentResolver().delete(Uri.parse(intent.getData().toString()), null, null);
						List list = File.ListFiles(File.getDirRootExternal());
						for (int i = 0;i < list.getSize();i++) {
							String f = (String) list.Get(i);
							if (f.startsWith("recording") && f.endsWith("3gpp")&& currentFiles.contains(f) == false) {
								File.Delete(File.getDirRootExternal(), f);
								break;
							}
						}

					} catch (IOException e) {
						e.printStackTrace();
						success = false;
					}
				}
				ba.raiseEvent(AudioRecordApp.this, eventName + "_recordcomplete", success);
			}

		};
		Intent i = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
		ba.startActivityForResult(ion, i);
	}
	/**
	 * VideoRecordApp lets you use the default video recorder application to record video.
	 *After initializing the object you should call Record to start recording.
	 *The RecordComplete event will be raised when record completes.
	 **/
	@ShortName("VideoRecordApp")
	@Events(values={"RecordComplete (Success As Boolean)"})
	public static class VideoRecordApp {
		private IOnActivityResult ion;
		private String eventName;
		/**
		 * Initializes the object and sets the sub that will handle the event.
		 */
		public void Initialize(String EventName) {
			this.eventName = EventName.toLowerCase(BA.cul);
		}
		/**
		 *<b>Will not work on Android 7+ devices.</b> Use Record3 instead.
		 */
		public void Record(final BA ba, final String Dir, final String FileName) {
			Record2(ba, Dir, FileName, -1);
		}
		/**
		 *<b>Will not work on Android 7+ devices.</b> Use Record3 instead.
		 */
		public void Record2(final BA ba, final String Dir, final String FileName, final int MaxLengthSeconds) {
			Record3(ba, Dir, FileName, MaxLengthSeconds, null);
		}
		/**
		 * Calls the recording app. The RecordComplete event will be raised.
		 *Dir / FileName - Video output file.
		 *MaxLengthSeconds - Sets a flag that requests the video duration to be limited. Pass -1 for no limits.
		 *Uri - FileProvider output file URI.
		 */
		public void Record3(final BA ba, final String Dir, final String FileName, final int MaxLengthSeconds, final Uri Uri) {
			if (eventName == null)
				throw new RuntimeException("You should first call Initialize.");
			
			ion = new IOnActivityResult() {

				@Override
				public void ResultArrived(int resultCode, final Intent intent) {
					ion = null;
					boolean success = resultCode == Activity.RESULT_OK;
					if (success) {
						ba.submitRunnable(new Runnable() {

							@Override
							public void run() {
								try {
									if (Uri == null) {
										String uri = intent.getData().toString();
										if (uri.startsWith("file://")) {
											if (uri.equals(Uri.parse("file://" + File.Combine(Dir, FileName)).toString()) == false) {
												File.Copy("", uri.substring(7), Dir, FileName);
											}
										} else {
											File.Copy(File.ContentDir, intent.getData().toString(), Dir, FileName);
											try {
												BA.applicationContext.getContentResolver().delete(Uri.parse(intent.getData().toString()), null, null);
											} catch (Exception ee) {
												System.out.println("failed to delete original video file.");
											}
										}
									}
									ba.raiseEventFromDifferentThread(VideoRecordApp.this, null, 0, eventName + "_recordcomplete", false, new Object[] {true});
								} catch (Exception e) {
									e.printStackTrace();
									ba.setLastException(e);
									ba.raiseEventFromDifferentThread(VideoRecordApp.this, null, 0, eventName + "_recordcomplete", false, new Object[] {false});
								}
							}

						}, null, 0);
					}
					else {
						ba.raiseEvent(VideoRecordApp.this, eventName + "_recordcomplete", false);
					}
				}

			};
			Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			if (Build.VERSION.SDK_INT >= 18) {
				if (Uri == null) {
					String dir = Dir;
					if (Dir != File.getDirRootExternal() && Dir != File.getDirDefaultExternal())
						dir = File.getDirDefaultExternal();
					i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + File.Combine(dir, FileName)));
				} else {
					i.putExtra(MediaStore.EXTRA_OUTPUT, Uri);
				}
			}

			if (MaxLengthSeconds > 0)
				i.putExtra("android.intent.extra.durationLimit", MaxLengthSeconds);
			ba.startActivityForResult(ion, i);
		}
	}
}
