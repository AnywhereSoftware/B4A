
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;

/**
 * The camera object allows you to access the device cameras.
 *This library is supported by Android 1.6+.
 *If possible it is recommended to work with CameraEx class that wraps this object and adds many features.
 *The CameraEx class requires Android 2.3+.
 */
@ActivityObject
@Permissions(values={"android.permission.CAMERA"})
@ShortName("Camera")
@Version(2.21f)
@DependsOn(values= {"kotlin-stdlib-1.6.10"})
@Events(values={"Ready (Success As Boolean)", "PictureTaken (Data() As Byte)",
"Preview (Data() As Byte)", "FocusDone (Success As Boolean)"})
public class CameraW {
	@Hide
	public volatile Camera camera;
	private static HashMap<Integer, Camera> closingCameras = new HashMap<Integer, Camera>();
	private SurfaceView sv;
	private String eventName;
	private BA ba;
	private AtomicInteger readyCount = new AtomicInteger();
	private static volatile int liveCameraId = 100;
	/**
	 * Initializes the back camera.
	 *Panel - The preview images will be displayed on the panel.
	 *EventName - Events subs prefix.
	 *The Ready event will be raised when the camera has finished opening.
	 */
	public void Initialize(final BA ba, ViewGroup Panel, String EventName) throws InterruptedException {
		shared(ba, Panel, EventName, -1);
	}
	/**
	 * Same as Initialize. CameraId is the id of the hardware camera.
	 *<b>This method is only available from Android 2.3+.</b>
	 */
	public void Initialize2(final BA ba, ViewGroup Panel, String EventName, int CameraId) throws InterruptedException {
		shared(ba, Panel, EventName, CameraId);
	}
	private void shared(final BA ba, ViewGroup Panel, String EventName, final int CameraId) throws InterruptedException {
		synchronized (closingCameras) {
			liveCameraId = CameraId;
			this.ba = ba;
			readyCount.set(0);
			this.eventName = EventName.toLowerCase(BA.cul);
			if (Panel.getChildCount() == 0) {
				sv = new SurfaceView(ba.context);
				anywheresoftware.b4a.BALayout.LayoutParams lp = new anywheresoftware.b4a.BALayout.LayoutParams(0, 0,
						Panel.getLayoutParams().width, Panel.getLayoutParams().height);
				Panel.addView(sv, lp);
				sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				sv.getHolder().setFixedSize(Panel.getLayoutParams().width, Panel.getLayoutParams().height);
				sv.getHolder().addCallback(new SurfaceHolder.Callback() {

					@Override
					public void surfaceChanged(SurfaceHolder holder, int format,
							int width, int height) {
					}

					@Override
					public void surfaceCreated(SurfaceHolder holder) {
						if (readyCount.addAndGet(1) == 2) {
							ba.raiseEvent(null, eventName + "_ready", true);
						}

					}

					@Override
					public void surfaceDestroyed(SurfaceHolder holder) {

					}

				});
			}
			else {
				sv = (SurfaceView)Panel.getChildAt(0);	
			}

		}
		ba.submitRunnable(new Runnable() {

			@Override
			public void run() {
				synchronized (closingCameras) {
					try {
						camera = closingCameras.remove(CameraId);
						releaseCameras(true);
						if (camera == null) {
							if (CameraId == -1)
								camera = Camera.open();
							else {
								Method m = Camera.class.getMethod("open", int.class);
								camera = (Camera)m.invoke(null, CameraId);
							}
							closingCameras.put(CameraId, camera);
							if (sv == null) {
								releaseCameras(true);
								return;
							}
						}
						else {
							closingCameras.put(CameraId, camera);
						}
						BA.handler.post(new Runnable() {
							//main thread
							final Camera currentCamera = camera;
							@Override
							public void run() {
								synchronized (closingCameras) {
									if (ba.isActivityPaused()) {
										releaseCameras(true);
										return;
									}
									if (camera == null || sv == null || currentCamera != camera)
										return;
									if (readyCount.addAndGet(1) == 2) {

										ba.raiseEvent(CameraW.this,eventName +  "_ready", true);
									}
									if (ba.subExists(eventName + "_preview")) {
										camera.setPreviewCallback(new Camera.PreviewCallback() {

											@Override
											public void onPreviewFrame(byte[] data,
													Camera camera) {
												ba.raiseEvent(null, eventName + "_preview", data);
											}

										});
									}
								}
							}

						});


					}
					catch (Exception e) {
						e.printStackTrace();
						ba.setLastException(e);
						Common.Log(e.toString());
						ba.raiseEventFromDifferentThread(null, CameraW.this, -1,eventName +  "_ready", false, new Object[] {false});
						releaseCameras(true);
					}
				}
			}

		}, this, -1);
	}
	/**
	 * Starts displaying the preview images.
	 */
	public void StartPreview() throws IOException, InterruptedException {
		Thread.sleep(50);
		camera.setPreviewDisplay(sv.getHolder());
		camera.startPreview();
	}
	/**
	 * Stops displaying the preview images.
	 */
	public void StopPreview() {
		if (camera != null)
			camera.stopPreview();
	}
	/**
	 * Starts auto-focus function. The FocusDone event will be raised when the operation completes.
	 *You can check whether the "auto" focus mode is supported with CameraEx class.
	 */
	public void AutoFocus() {
		camera.autoFocus(new AutoFocusCallback() {

			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				ba.raiseEvent(this, eventName + "_focusdone", success);
			}
			
		});
	}
	/**
	 * Cancels the auto-focus operation. Does nothing if no such operation is in progress.
	 */
	public void CancelAutoFocus() throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		try {
			Method m = Camera.class.getMethod("cancelAutoFocus");
			m.invoke(camera);
		} catch (NoSuchMethodException e) {
			//ignore
		}
	}
	/**
	 * Releases the camera object and allows other processes to access the camera.
	 */
	public void Release() throws InterruptedException {
		liveCameraId = 100;
		synchronized (closingCameras) {
			if (camera != null && closingCameras.values().contains(camera)) {
				camera.setPreviewCallback(null);
				camera.stopPreview();
			}
			releaseCameras(false);
			if (sv != null) {
				ViewGroup vg = (ViewGroup) sv.getParent();
				if (vg != null)
					vg.removeView(sv);
				sv = null;
			}
		}
	}
	private void releaseCameras(boolean now)  {

		synchronized (closingCameras) {
			CloseCamera cc = new CloseCamera(now == false);
			if (now) {
				cc.run();
			}
			else {
				Thread t = new Thread(cc);
				t.start();
			}
		}

	}
	private static class CloseCamera implements Runnable {
		private final boolean sleep;
		private static int ccCounter = 0;
		private int myCounter;
		public CloseCamera(boolean sleep) {
			this.sleep = sleep;
			ccCounter++;
			myCounter = ccCounter;
		}
		@Override
		public void run() {
			try {
				if (sleep) {
					Thread.sleep(5000);
					if (myCounter != ccCounter)
						return;
				}
				synchronized (closingCameras) {
					Camera liveCamera = null;
					for (Entry<Integer, Camera> kvp : closingCameras.entrySet()) {
						if (kvp.getKey() == liveCameraId) {
							liveCamera = kvp.getValue();
							continue;
						}
						kvp.getValue().release();
						Thread.sleep(100);
					}
					closingCameras.clear();
					if (liveCamera != null)
						closingCameras.put(liveCameraId, liveCamera);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Takes a picture. When the picture is ready, the PictureTaken event will be raised.
	 *You should not call TakePicture while another picture is currently taken.
	 *The preview images are stopped after calling this method. You can call StartPreview to restart the preview images.
	 */
	public void TakePicture() {
		camera.takePicture(null , null, new Camera.PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				ba.raiseEventFromUI(null, eventName + "_picturetaken", data);
			}

		});
	}

}
