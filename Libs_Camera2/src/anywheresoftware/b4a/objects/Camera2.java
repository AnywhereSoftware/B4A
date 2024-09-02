
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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;

/**
 * Camera library based on Camera2 API. It should be used together with CamEx2 class.
 */
@Version(1.12f)
@ShortName("Camera2")
@Permissions(values={"android.permission.CAMERA"})
@Events(values={"PictureTaken (Data() As Byte)", "CameraState (Open As Boolean)", "CameraClosed", "SurfaceReady",
		"SessionConfigured (Success As Boolean)", "PreviewCaptureComplete (CaptureResult As Object)", "PreviewTaken (Image As Object)",
"CaptureComplete (CaptureResult As Object)"})
@DependsOn(values= {"kotlin-stdlib-1.6.10"})
public class Camera2 {
	@Hide
	public CameraManager cameraManager;
	@Hide
	public CameraDevice cameraDevice;
	@Hide
	public CameraCaptureSession captureSession;
	@Hide
	public ImageReader captureImageReader, previewImageReader;
	@Hide
	public Surface previewSurface;
	@Hide
	public int lastKnownOrientation;
	@Hide
	public MediaRecorder mediaRecorder;
	@Hide
	public Surface persistentSurface;
	private BA ba;
	private String eventName;
	public void Initialize(BA ba, String EventName) {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		cameraManager = (CameraManager) ba.applicationContext.getSystemService(Context.CAMERA_SERVICE);

	}
	/**
	 * Creates the surface view (TextureView) that will be used to display the preview frames.
	 *You need to add it to the layout and wait for the SurfaceReady event.
	 */
	public ConcreteViewWrapper CreateSurface(final BA ba) {

		final TextureView tv = new TextureView(ba.sharedProcessBA.activityBA.get().activity);
		tv.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surface,
					int width, int height) {
				configureTransform(tv);
				ba.raiseEventFromUI(Camera2.this, eventName + "_surfaceready");

			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
				return true;
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
					int width, int height) {
				configureTransform(tv);
			}

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surface) {

			}

		});
		return (ConcreteViewWrapper) AbsObjectWrapper.ConvertToWrapper(new ConcreteViewWrapper(), tv);
	}
	/**
	 * Opens the camera. The CameraState event will be raised.
	 */
	public void OpenCamera(String Id) throws CameraAccessException {
		cameraManager.openCamera(Id, new StateCallback() {

			@Override
			public void onDisconnected(CameraDevice camera) {
				BA.LogInfo("camera disconnected");
				ba.raiseEventFromUI(Camera2.this, eventName + "_camerastate", false);
				cameraDevice = null;
			}

			@Override
			public void onError(CameraDevice camera, int error) {
				ba.setLastException(new Exception("" + error));
				ba.raiseEventFromUI(Camera2.this, eventName + "_camerastate", false);
				cameraDevice = null;

			}

			@Override
			public void onOpened(CameraDevice camera) {
				cameraDevice = camera;
				ba.raiseEventFromUI(Camera2.this, eventName + "_camerastate", true);
			}

		}, null);
	}
	@Hide
	public int getRotation(Context c) {
		if (c instanceof Activity) {
			Activity a = (Activity) c;
			int rotation = a.getWindowManager().getDefaultDisplay().getRotation();
			lastKnownOrientation = rotation * 90;
		}
		return lastKnownOrientation;
	}
	private void configureTransform(TextureView v) {
		int orientation = getRotation(v.getContext());
		Matrix matrix = new Matrix();
		if (orientation % 180 == 90) {
			final int width = v.getWidth();
			final int height = v.getHeight();
			// Rotate the camera preview when the screen is landscape.
			matrix.setPolyToPoly(
					new float[]{
							0.f, 0.f, // top left
							width, 0.f, // top right
							0.f, height, // bottom left
							width, height, // bottom right
					}, 0,
					orientation == 90 ?
							// Clockwise
							new float[]{
							0.f, height, // top left
							0.f, 0.f, // top right
							width, height, // bottom left
							width, 0.f, // bottom right
					} : // mDisplayOrientation == 270
						// Counter-clockwise
						new float[]{
									width, 0.f, // top left
									width, height, // top right
									0.f, 0.f, // bottom left
									0.f, height, // bottom right
							}, 0,
							4);
		} else if (orientation == 180) {
			matrix.postRotate(180, v.getWidth() / 2, v.getHeight() / 2);
		}
		v.setTransform(matrix);
	}
	/**
	 * Returns the relevant camera id. Returns an empty string if not found.
	 */
	public String FindCameraId (boolean Front) throws CameraAccessException {
		int internalFacing = Front ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK;
		final String[] ids = cameraManager.getCameraIdList();
		if (ids.length == 0) { // No camera
			throw new RuntimeException("No camera available.");
		}
		for (String id : ids) {
			CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
			if (characteristics.get(CameraCharacteristics.LENS_FACING) == internalFacing) {
				return id;
			}
		}
		return "";
	}
	/**
	 * Returns the ids of the available cameras.
	 */
	public String[] getCameraIDs() throws CameraAccessException {
		return cameraManager.getCameraIdList();
	}
	/**
	 * Returns an object that holds the camera supported features.
	 */
	public Object GetCameraCharacteristics(String Id) throws CameraAccessException {
		return cameraManager.getCameraCharacteristics(Id);
	}
	/**
	 * Returns a list with the supported preview sizes.
	 */
	public List GetSupportedPreviewSizes(String Id) throws CameraAccessException {
		return Common.ArrayToList(((StreamConfigurationMap)GetScalerStreamConfiguration(Id)).getOutputSizes(SurfaceTexture.class));
	}
	/**
	 * Returns a list with the supported capture sizes.
	 */
	public List GetSupportedCaptureSizes(String Id) throws CameraAccessException {
		return Common.ArrayToList(((StreamConfigurationMap)GetScalerStreamConfiguration(Id)).getOutputSizes(ImageFormat.JPEG));
	}
	public List GetSupportedVideoSizes(String Id) throws CameraAccessException {
		return Common.ArrayToList(((StreamConfigurationMap)GetScalerStreamConfiguration(Id)).getOutputSizes(MediaRecorder.class));
	}
	@Hide
	public Object GetScalerStreamConfiguration(String Id) throws CameraAccessException {
		return cameraManager.getCameraCharacteristics(Id).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
	}
	public Object CreateMediaRecorder(CameraSizeWrapper VideoSize, String Dir, String FileName) {
		if (mediaRecorder != null)
			mediaRecorder.release();
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mediaRecorder.setOutputFile(anywheresoftware.b4a.objects.streams.File.Combine(Dir, FileName));
		mediaRecorder.setVideoEncodingBitRate(10000000);
		mediaRecorder.setVideoFrameRate(30);
		mediaRecorder.setVideoSize(VideoSize.getWidth(), VideoSize.getHeight());
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

		return mediaRecorder;
	}
	/**
	 * Starts a capture session. The SessionConfigured event will be raised.
	 */
	public void StartSession(TextureView Surface, CameraSizeWrapper PreviewSize, CameraSizeWrapper CaptureSize, int CaptureFormat, int PreviewFormat, boolean Video) throws CameraAccessException, IllegalStateException, IOException {
		if (captureSession != null) {
			captureSession.close();
			captureSession = null;
		}
		if (previewSurface != null)
			previewSurface.release();
		ArrayList<Surface> targets = new ArrayList<android.view.Surface>();
		if (Surface != null) {
			previewSurface = new Surface(Surface.getSurfaceTexture());
			Surface.getSurfaceTexture().setDefaultBufferSize(PreviewSize.getWidth(), PreviewSize.getHeight());
			targets.add(previewSurface);
		}
		for (ImageReader c : new ImageReader[] {captureImageReader, previewImageReader}) {
			if (c != null)
				c.close();
		}
		if (CaptureFormat != 0) {
			captureImageReader = ImageReader.newInstance(CaptureSize.getWidth(), CaptureSize.getHeight(), CaptureFormat, 2);
			captureImageReader.setOnImageAvailableListener(new ImageReaderListener(false), null);
			targets.add(captureImageReader.getSurface());
		}
		if (PreviewFormat != 0) {
			previewImageReader = ImageReader.newInstance(PreviewSize.getWidth(), PreviewSize.getHeight(), PreviewFormat, 8);
			previewImageReader.setOnImageAvailableListener(new ImageReaderListener(true), null);
			targets.add(previewImageReader.getSurface());
		}
		if (Video) {
			if (persistentSurface == null)
				persistentSurface = MediaCodec.createPersistentInputSurface();
			mediaRecorder.setInputSurface(persistentSurface);
			mediaRecorder.prepare();
			//			targets.add(mediaRecorder.getSurface());
			targets.add(persistentSurface);
		}

		cameraDevice.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {

			@Override
			public void onConfigureFailed(CameraCaptureSession session) {
				ba.raiseEventFromUI(Camera2.this, eventName + "_sessionconfigured", false);
			}

			@Override
			public void onConfigured(CameraCaptureSession session) {
				captureSession = session;
				ba.raiseEventFromUI(Camera2.this, eventName + "_sessionconfigured", true);
			}
			@Override
			public void onClosed(CameraCaptureSession session) {
				if (captureSession != null && captureSession.equals(session)) {
					captureSession = null;
				}
			}

		}, null);
	}
	@Hide
	public class ImageReaderListener implements ImageReader.OnImageAvailableListener{
		String eventsuffix;
		public ImageReaderListener(boolean preview) {
			this.eventsuffix = preview ? "_previewtaken" : "_picturetaken";

		}
		@Override
		public void onImageAvailable(ImageReader reader) {
			Image image = reader.acquireLatestImage();
			if (image == null) {
				return;
			}
			try {
				Image.Plane[] planes = image.getPlanes();
				if (planes.length > 0) {
					if (image.getFormat() == ImageFormat.YUV_420_888) {
						ba.raiseEventFromUI(Camera2.this, eventName + eventsuffix, image);
					} else {
						ByteBuffer buffer = planes[0].getBuffer();
						byte[] data = new byte[buffer.remaining()];
						buffer.get(data);
						ba.raiseEventFromUI(Camera2.this, eventName + eventsuffix, data);
					}
				}
			} catch (Exception e) {
				BA.LogError("Failed to get image.");
				e.printStackTrace();
			}
			image.close();
		}

	}
	/**
	 * Creates a preview request builder.
	 */
	public Object CreatePreviewBuilder() throws CameraAccessException {
		CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
		builder.addTarget(previewSurface);
		return builder;
	}
	public Object CreateVideoRequestBuilder() throws CameraAccessException {
		CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
		builder.addTarget(previewSurface);
		builder.addTarget(persistentSurface);
		//		builder.addTarget(mediaRecorder.getSurface());
		return builder;
	}
	/**
	 * Creates a still capture builder.
	 */
	public Object CreateCaptureBuilder() throws CameraAccessException {
		CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
		builder.addTarget(captureImageReader.getSurface());
		return builder;
	}
	/**
	 * Sets a repeating request (preview request). Clears any previous repeating request.
	 *Returns the CaptureRequest object.
	 */
	public Object SetRepeatingRequest(Object Builder) throws CameraAccessException {
		CaptureRequest req = ((CaptureRequest.Builder)Builder).build();
		captureSession.setRepeatingRequest(req, new CaptureCallback() {
			@Override
			public void onCaptureCompleted(CameraCaptureSession session,
					CaptureRequest request,android.hardware.camera2.TotalCaptureResult result) {
				ba.raiseEventFromUI(Camera2.this, eventName + "_previewcapturecomplete", result);
			}
		}, null);
		return req;
	}
	/**
	 * Adds a capture request.
	 */
	public Object AddCaptureRequest(final Object Builder) throws CameraAccessException {
		CaptureRequest req = ((CaptureRequest.Builder)Builder).build();
		captureSession.capture(req,  new CaptureCallback() {
			@Override
			public void onCaptureCompleted(CameraCaptureSession session,
					CaptureRequest request,android.hardware.camera2.TotalCaptureResult result) {
				ba.raiseEventFromUI(Builder, eventName + "_capturecomplete", result);
			}
		}, null);
		return req;
	}
	/**
	 * Cancels previous requests.
	 */
	public void AbortCaptures() throws CameraAccessException {
		captureSession.abortCaptures();
	}

	/**
	 * Stops the camera.
	 */
	public void Stop() {
		if (captureSession != null) {
			try {
				captureSession.abortCaptures();
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}
		if (mediaRecorder != null) {
			mediaRecorder.release();
		}
		for (AutoCloseable ac : new AutoCloseable[] {captureSession, captureImageReader, previewImageReader, cameraDevice}) {
			if (ac != null) {
				try {
					ac.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		captureSession = null;
		captureImageReader = null;
		cameraDevice = null;
		previewImageReader = null;
		mediaRecorder = null;
		try {
			if (persistentSurface != null)
				persistentSurface.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		persistentSurface = null;
	}
	public boolean getIsCameraOpen() {
		return cameraDevice != null;
	}
	@ShortName("CameraSize")
	public static class CameraSizeWrapper extends AbsObjectWrapper<Size> {
		public void Initialize(int Width, int Height) {
			setObject(new Size(Width, Height));
		}
		public int getWidth() {
			return getObject().getWidth();
		}
		public int getHeight() {
			return getObject().getHeight();
		}
	}
}
