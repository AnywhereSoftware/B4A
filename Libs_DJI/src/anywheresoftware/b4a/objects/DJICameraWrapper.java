package anywheresoftware.b4a.objects;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;

@ShortName("DJICamera")
@Events(values={"FrameReceived (Data() As Byte)"})
public class DJICameraWrapper {
	@Hide
	public Camera camera;
	private BA ba;
	private String eventName;
	@Hide
	public DJICodecManager mCodecManager;
	@Hide
	public VideoFeeder videoFeeder;
	private TextureView mVideoSurface;
	public void Initialize(BA ba, String EventName, DJIAircraftWrapper Aircraft) {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		this.camera = Aircraft.getObject().getCamera();
		videoFeeder = VideoFeeder.getInstance();

	}
	public boolean IsInitialized() {
		return this.camera != null && this.camera.isConnected();
	}


	/**
	 * Creates the view that will show the camera feed.
	 */
	public ConcreteViewWrapper CreateVideoView() {
		mVideoSurface = new TextureView(ba.context);
		mVideoSurface.setSurfaceTextureListener(new SurfaceTextureListener() {

			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surface,
					int width, int height) {
				if (mCodecManager == null) {
					mCodecManager = new DJICodecManager(ba.context, surface, width, height);
				}

			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
				if (mCodecManager != null) {
					mCodecManager.cleanSurface();
					mCodecManager = null;
				}
				return false;
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
					int width, int height) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surface) {
				// TODO Auto-generated method stub

			}

		});

		ConcreteViewWrapper c = new ConcreteViewWrapper();
		c.setObject(mVideoSurface);

		return c;
	}

}
