package anywheresoftware.b4a.objects;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.streams.File;

/**
 * The MediaPlayer can be used to play audio files.
 *See the <link>media player tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6591-mediaplayer-tutorial.html</link> for more information.
 *NOTE: The media player should be declared as a <b>process</b> global object.
 *The Complete event is raised when playback completes. It will only be raised if you initialize the object with Initialize2. 
 */
@Events(values={"Complete"})
@ShortName("MediaPlayer")
public class MediaPlayerWrapper {
	protected String eventName;
	@Hide
	public MediaPlayer mp;
	/**
	 * Initializes the object.
	 *You should use Initialize2 if you want to handle the Complete event.
	 *Example:<code>
	 *Dim MP As MediaPlayer 'should be done in Sub Process_Globals
	 *MP.Initialize2("MP")
	 *MP.Load(File.DirAssets, "SomeFile.mp3")
	 *MP.Play</code>
	 */
	public void Initialize() throws IllegalArgumentException, IllegalStateException, IOException {
		mp = new MediaPlayer();
	}
	public boolean IsInitialized() {
		return mp != null;
	}
	/**
	 * Similar to Initialize2. Complete event will be raised when play back completes.
	 * EventName - The Sub that will handle the Complete event.
	 */
	public void Initialize2(final BA ba, String EventName) throws IllegalArgumentException, IllegalStateException, IOException {
		Initialize();
		this.eventName = EventName.toLowerCase(BA.cul);
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				ba.raiseEvent(MediaPlayerWrapper.this, eventName + "_complete");
			}
			
		});
	}
	/**
	 * Loads an audio file and prepares it for playing.
	 */
	public void Load(String Dir, String FileName) throws IllegalArgumentException, IllegalStateException, IOException {
		mp.reset();
		loadAfterReset(Dir, FileName);
	}

	private void loadAfterReset(String Dir, String FileName) throws IllegalArgumentException, IllegalStateException, IOException {
		if (Dir.equals(File.getDirAssets())) { 
			if (File.virtualAssetsFolder != null) {
				loadAfterReset(anywheresoftware.b4a.objects.streams.File.virtualAssetsFolder, File.getUnpackedVirtualAssetFile(FileName));
				return;
			}
			else {
				AssetFileDescriptor fd = BA.applicationContext.getAssets().openFd(FileName.toLowerCase(BA.cul));
				if (fd.getDeclaredLength() < 0) {
					mp.setDataSource(fd.getFileDescriptor());
				} else {
					mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
				}
			}
		}
		else if (Dir.startsWith(File.getDirInternal()) ||
				Dir.equals(File.getDirInternalCache())) {
			FileInputStream fileInputStream = new FileInputStream(new java.io.File(Dir, FileName));
			mp.setDataSource(fileInputStream.getFD());

		}
		else if (Dir.equals(File.ContentDir)) {
			AssetFileDescriptor fd = BA.applicationContext.getContentResolver().openAssetFileDescriptor(Uri.parse(FileName), "r");
			if (fd.getDeclaredLength() < 0) {
				mp.setDataSource(fd.getFileDescriptor());
			} else {
				mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
			}
		}
		else {
			mp.setDataSource(new java.io.File(Dir, FileName).toString());
		}
		mp.prepare();
	}
	/**
	 * Gets or sets whether the media player will restart playing automatically.
	 */
	public boolean getLooping() {
		return mp.isLooping();
	}
	public void setLooping(boolean value) {
		mp.setLooping(value);
	}
	/**
	 * Starts (or resumes) playing the loaded audio file.
	 */
	public void Play() {
		mp.start();
	}
	/**
	 * Stops playing. You must call Load before trying to play again.
	 */
	public void Stop() {
		mp.reset();
	}
	/**
	 * Pauses playback. You can resume playback from the current position by calling Play.
	 */
	public void Pause() {
		mp.pause();
	}
	/**
	 * Returns the total duration of the loaded file (in milliseconds).
	 */
	public int getDuration() {
		return mp.getDuration();
	}
	/**
	 * Gets or sets the current position (in milliseconds).
	 */
	public int getPosition() {
		return mp.getCurrentPosition();
	}
	public void setPosition(int value) {
		mp.seekTo(value);
	}
	/**
	 * Sets the playing volume for each channel. The value should be between 0 to 1.
	 */
	public void SetVolume(float Left, float Right) {
		mp.setVolume(Left, Right);
	}
	/**
	 * Returns true if the media player is currently playing.
	 */
	public boolean IsPlaying() {
		return mp.isPlaying();
	}
	/**
	 * Releases all resources allocated by the media player.
	 */
	public void Release() {
		mp.release();
	}
	
}
