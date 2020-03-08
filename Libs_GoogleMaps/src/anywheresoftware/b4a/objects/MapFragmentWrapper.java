
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

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.location.Location;
import android.view.ViewGroup;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.DesignerProperties;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.Property;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common.DesignerCustomView;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;


@ShortName("MapFragment")
@ActivityObject
@DependsOn(values={"com.google.android.gms:play-services-maps"})
@Events(values={"Ready", 
		"CameraChange (Position As CameraPosition)"
		, "Click (Point As LatLng)"
		, "LongClick (Point As LatLng)"
		, "MarkerClick (SelectedMarker As Marker) As Boolean 'Return True to consume the click"})
		@Permissions(values={"android.permission.INTERNET",
				"com.google.android.providers.gsf.permission.READ_GSERVICES", "android.permission.ACCESS_COARSE_LOCATION",
				"android.permission.ACCESS_FINE_LOCATION", "$PACKAGE$.permission.MAPS_RECEIVE", "android.permission.ACCESS_NETWORK_STATE"})
	@Version(2.50f)
	@DesignerProperties(values={
			@Property(key="MapType", displayName = "Map Type", fieldType="String", defaultValue="NORMAL", list="NORMAL|SATELLITE|TERRAIN"),
			@Property(key="TrafficEnabled", displayName = "Traffic Enabled", fieldType="Boolean", defaultValue="False", description="Whether to show the traffic layer."),
			@Property(key="BuildingsEnabled", displayName = "Building Enabled", fieldType="Boolean", defaultValue="False", description="Whether to show the buildings layer."),
			@Property(key="auto1CompassEnabled", displayName = "Compass Enabled", fieldType="Boolean", defaultValue="True"),
			@Property(key="auto1MyLocationButtonEnabled", displayName = "MyLocation Button Enabled", fieldType="Boolean", defaultValue="True"),
			@Property(key="auto1RotateGesturesEnabled", displayName = "Rotate Gestures Enabled", fieldType="Boolean", defaultValue="True"),
			@Property(key="auto1ScrollGesturesEnabled", displayName = "Scroll Gestures Enabled", fieldType="Boolean", defaultValue="True"),
			@Property(key="auto1TiltGesturesEnabled", displayName = "Tilt Gestures Enabled", fieldType="Boolean", defaultValue="True"),
			@Property(key="auto1ZoomControlsEnabled", displayName = "Zoom Controls Enabled", fieldType="Boolean", defaultValue="True"),
			@Property(key="auto1ZoomGesturesEnabled", displayName = "Zoom Gestures Enabled", fieldType="Boolean", defaultValue="True")
		
	})
	public class MapFragmentWrapper implements DesignerCustomView{
		private BA ba;
		MapFragment mf;
		private String eventName;
		private GoogleMapWrapper gmap;
		private Map props;
		/**
		 * This library allows you to show maps from Google Maps service in your application.
		 *It requires Android 4+ (API level 14+).
		 *Please see the <link>tutorial|https://www.b4x.com/android/forum/threads/google-maps.63930/</link> for more information about the setup process.
		 */
		public static void LIBRARY_DOC() {
			//
		}
		/**
		 * Tests whether Google Play Services area available.
		 */
		public boolean IsGooglePlayServicesAvailable(BA ba) {
			return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ba.context) == ConnectionResult.SUCCESS;
		}
		/**
		 * Should not be used.
		 */
		public String GetOpenSourceLicenseInfo(BA ba) {
			return "";
			//return GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(ba.context);
		}
		private static int id = 10000;
		@Override
		public void DesignerCreateView(PanelWrapper base, LabelWrapper lw, Map props) {
			this.props = props;
			base.getObject().setId(++id);
			Initialize(ba, eventName, base.getObject());
		}
		@Hide
		@Override
		public void _initialize(BA ba, Object activityClass, String EventName) {
			this.ba = ba;
			this.eventName = EventName;
			
		}
		/**
		 * Initializes this object and adds the map to the given parent.
		 */
		public void Initialize(final BA ba, String EventName, ViewGroup Parent) {
			this.ba = ba;
			this.eventName = EventName.toLowerCase(BA.cul);
			mf = (MapFragment) ba.activity.getFragmentManager().findFragmentById(Parent.getId());
			FragmentTransaction ft = ba.activity.getFragmentManager().beginTransaction();
			if (mf != null) {
				ft.remove(mf);
			}
			mf = new MapFragment();
			ft.add(Parent.getId(), mf);
			ft.commitAllowingStateLoss();
			mf.getMapAsync(new OnMapReadyCallback() {
	
				@Override
				public void onMapReady(final GoogleMap gm) {
					gmap = (GoogleMapWrapper) AbsObjectWrapper.ConvertToWrapper(new GoogleMapWrapper(),gm);
					if (ba.subExists(eventName + "_camerachange")) {
						gm.setOnCameraChangeListener(new OnCameraChangeListener() {
	
							@Override
							public void onCameraChange(CameraPosition arg0) {
								ba.raiseEvent(gm, eventName + "_camerachange", 
										AbsObjectWrapper.ConvertToWrapper(new CameraPositionWrapper(), arg0));
							}
	
						});
					}
					if (ba.subExists(eventName + "_click")) {
						gm.setOnMapClickListener(new OnMapClickListener() {
	
							@Override
							public void onMapClick(LatLng arg0) {
								ba.raiseEvent(gm, eventName + "_click", 
										AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), arg0));
							}
	
						});
					}
					if (ba.subExists(eventName + "_longclick")) {
						gm.setOnMapLongClickListener(new OnMapLongClickListener() {
							@Override
							public void onMapLongClick(LatLng arg0) {
								ba.raiseEvent(gm, eventName + "_longclick", 
										AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), arg0));
							}
						});
					}
					if (ba.subExists(eventName + "_markerclick")) {
						gm.setOnMarkerClickListener(new OnMarkerClickListener() {
	
							@Override
							public boolean onMarkerClick(Marker arg0) {
								Boolean b = (Boolean) ba.raiseEvent(gm, eventName + "_markerclick", AbsObjectWrapper.ConvertToWrapper(new MarkerWrapper(), arg0));
								return b == null ? false : b.booleanValue();
	
							}
	
						});
					}
					if (props != null) {
						
					}
					try {
						ba.raiseEventFromDifferentThread(MapFragmentWrapper.this, null, 0, eventName + "_ready", false, null);
						gm.setBuildingsEnabled((Boolean)props.Get("BuildingsEnabled"));
						gm.setTrafficEnabled((Boolean)props.Get("TrafficEnabled"));
						gm.setMapType(GoogleMapWrapper.class.getDeclaredField("MAP_TYPE_" + (String)props.Get("MapType")).getInt(null));
						UiSettings settings = gm.getUiSettings();
						for (Object k : props.getObject().keySet()) {
							String key = (String)k;
							if (key.startsWith("auto1")) {
								Boolean b = (Boolean)props.Get(key);
								
									settings.getClass().getDeclaredMethod("set" + key.substring(5), boolean.class).invoke(settings, b);
								
							}
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
	
			});
	
		}
		/**
		 * Returns the GoogleMap object. You should check that the object returned is initialized.
		 *It will not be initialized if there was any error.
		 *The map is only available after the Ready event fires.
		 */
		public GoogleMapWrapper GetMap() {
			if (gmap == null) {
				BA.LogError("Map is not ready yet.");
				return new GoogleMapWrapper();
			}
			return gmap;
	
		}

	@ShortName("GoogleMap")
	public static class GoogleMapWrapper extends AbsObjectWrapper<GoogleMap> {
		public static final float HUE_RED = 0.0F;
		public static final float HUE_ORANGE = 30.0F;
		public static final float HUE_YELLOW = 60.0F;
		public static final float HUE_GREEN = 120.0F;
		public static final float HUE_CYAN = 180.0F;
		public static final float HUE_AZURE = 210.0F;
		public static final float HUE_BLUE = 240.0F;
		public static final float HUE_VIOLET = 270.0F;
		public static final float HUE_MAGENTA = 300.0F;
		public static final float HUE_ROSE = 330.0F;
		public static final int MAP_TYPE_NONE = 0;
		public static final int MAP_TYPE_NORMAL = 1;
		public static final int MAP_TYPE_SATELLITE = 2;
		public static final int MAP_TYPE_TERRAIN = 3;
		public static final int MAP_TYPE_HYBRID = 4;

		public void Clear() {
			getObject().clear();
		}
		/**
		 * Moves the camera to the new position. The movement is animated.
		 */
		public void AnimateCamera(CameraPosition NewPosition) {
			getObject().animateCamera(CameraUpdateFactory.newCameraPosition(NewPosition));
		}
		/**
		 * Immediately moves the camera to the new position.
		 */
		public void MoveCamera(CameraPosition NewPosition) {
			getObject().moveCamera(CameraUpdateFactory.newCameraPosition(NewPosition));
		}
		/**
		 * Returns the current camera position.
		 */
		public CameraPositionWrapper getCameraPosition() {
			return (CameraPositionWrapper)AbsObjectWrapper.ConvertToWrapper(
					new CameraPositionWrapper(), getObject().getCameraPosition());
		}
		/**
		 * Adds a marker to the map.
		 *This method returns a Marker object which you can further customize.
		 */
		public MarkerWrapper AddMarker(double Lat, double Lon, String Title) {
			Marker m = getObject().addMarker(new MarkerOptions().position(new LatLng(Lat, Lon))
					.title(Title));
			return (MarkerWrapper)AbsObjectWrapper.ConvertToWrapper(new MarkerWrapper(), m);
		}
		/**
		 * Similar to AddMarker. The last parameter sets the marker's hue color. It should be
		 *one of the HUE constants.
		 */
		public MarkerWrapper AddMarker2(double Lat, double Lon, String Title, float HueColor) {
			Marker m = getObject().addMarker(new MarkerOptions().position(new LatLng(Lat, Lon))
					.title(Title).icon(BitmapDescriptorFactory.defaultMarker(HueColor)));
			return (MarkerWrapper)AbsObjectWrapper.ConvertToWrapper(new MarkerWrapper(), m);
		}
		/**
		 * Similar to AddMarker. The last parameter sets the marker icon.
		 */
		public MarkerWrapper AddMarker3(double Lat, double Lon, String Title, Bitmap Bitmap) {
			Marker m = getObject().addMarker(new MarkerOptions().position(new LatLng(Lat, Lon))
					.title(Title).icon(BitmapDescriptorFactory.fromBitmap(Bitmap)));
			return (MarkerWrapper)AbsObjectWrapper.ConvertToWrapper(new MarkerWrapper(), m);
		}
		/**
		 * Adds a Polyline. This method returns a Polyline object which you should use to set the points.
		 */
		public PolylineWrapper AddPolyline() {
			Polyline p = getObject().addPolyline(new PolylineOptions());
			return (PolylineWrapper)AbsObjectWrapper.ConvertToWrapper(new PolylineWrapper(), p);
		}
		/**
		 * Gets or sets the map type. The value should be one of the MAP_TYPE constants. 
		 */
		public int getMapType() {
			return getObject().getMapType();
		}
		public void setMapType(int v) {
			getObject().setMapType(v);
		}
		/**
		 * Gets or sets whether the device location will be marked.
		 */
		public boolean getMyLocationEnabled() {
			return getObject().isMyLocationEnabled();
		}
		public void setMyLocationEnabled(boolean v) {
			getObject().setMyLocationEnabled(v);
		}
		/**
		 * Gets or sets whether traffic data is drawn on the map (if such data is available).
		 */
		public void setTrafficEnabled(boolean v) {
			getObject().setTrafficEnabled(v);
		}
		public boolean getTrafficEnabled() {
			return getObject().isTrafficEnabled();
		}
		/**
		 * Returns the current location. Will return an uninitialized object if the location is not available.
		 */
		public LatLngWrapper getMyLocation() {
			Location l = getObject().getMyLocation();
			LatLngWrapper lw = new LatLngWrapper();
			if (l == null)
				return lw;
			LatLng ll = new LatLng(l.getLatitude(), l.getLongitude());
			lw.setObject(ll);
			return lw;
		}
		/**
		 * Returns a MapUiSettings object which you can use to configure the user interface.
		 */
		public UiSettingsWrapper GetUiSettings() {
			return (UiSettingsWrapper)AbsObjectWrapper.ConvertToWrapper(
					new UiSettingsWrapper(), getObject().getUiSettings());
		}
	}
	/**
	 * The map view is modeled as a camera looking down on a flat plane.
	 *See this <link>link|https://developers.google.com/maps/documentation/android/views#the_camera_position</link> for more information about the possible values.
	 */
	@ShortName("CameraPosition")
	public static class CameraPositionWrapper extends AbsObjectWrapper<CameraPosition> {
		/**
		 * Initializes the camera position with the given latitude, longitude and zoom.
		 */
		public void Initialize(double Lat, double Lng, float Zoom) {
			setObject(CameraPosition.fromLatLngZoom(new LatLng(Lat, Lng), Zoom));
		}
		/**
		 * Initializes the camera position with the given latitude, longitude, zoom, bearing and tile.
		 */
		public void Initialize2(double Lat, double Lng, float Zoom, float Bearing, float Tilt) {
			CameraPosition cp = new CameraPosition.Builder().target(new LatLng(Lat, Lng))
			.zoom(Zoom).bearing(Bearing).tilt(Tilt).build();
			setObject(cp);
		}
		/**
		 * Returns the location that the camera is pointing at.
		 */
		public LatLngWrapper getTarget() {
			return (LatLngWrapper)AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), getObject().target);
		}
		/**
		 * Returns the direction the camera is pointing at.
		 */
		public float getBearing() {
			return getObject().bearing;
		}
		/**
		 * Returns the zoom level.
		 */
		public float getZoom() {
			return getObject().zoom;
		}
		/**
		 * Returns the tilt value.
		 */
		public float getTilt() {
			return getObject().tilt;
		}
	}
	/**
	 * Holds latitude and longitude values.
	 */
	@ShortName("LatLng")
	public static class LatLngWrapper extends AbsObjectWrapper<LatLng> {
		/**
		 * Returns the latitude value.
		 */
		public double getLatitude() {
			return getObject().latitude;
		}
		/**
		 * Returns the longitude value.
		 */
		public double getLongitude() {
			return getObject().longitude;
		}
		/**
		 * Initializes a new object.
		 */
		public void Initialize(double Latitude, double Longitude) {
			setObject(new LatLng(Latitude, Longitude));
		}
	}
	/**
	 * An icon placed on the map. Call GoogleMap.AddMarker to create such a marker.
	 */
	@ShortName("Marker")
	public static class MarkerWrapper extends AbsObjectWrapper<Marker> {
		/**
		 * Gets or sets the marker position.
		 */
		public LatLngWrapper getPosition() {
			return (LatLngWrapper) AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), getObject().getPosition());
		}
		public void setPosition(LatLngWrapper value) {
			getObject().setPosition(value.getObject());
		}
		/**
		 * Gets or sets whether the marker can be dragged by the user.
		 */
		public void setDraggable(boolean v) {
			getObject().setDraggable(v);
		}
		public boolean getDraggable() {
			return getObject().isDraggable();
		}
		/**
		 * Gets or sets the snippet text that appears when the marker is clicked.
		 */
		public String getSnippet() {
			return getObject().getSnippet();
		}
		public void setSnippet(String v) {
			getObject().setSnippet(v);
		}
		/**
		 * Gets or sets whether the marker is visible.
		 */
		public boolean getVisible() {
			return getObject().isVisible();
		}
		public void setVisible(boolean v) {
			getObject().setVisible(v);
		}
		/**
		 * Removes the marker from the map.
		 */
		public void Remove() {
			getObject().remove();
		}
		/**
		 * Gets or sets the marker title.
		 */
		public String getTitle() {
			return getObject().getTitle();
		}
		public void setTitle(String v) {
			getObject().setTitle(v);
		}
		/**
		 * Gets or sets whether the info window is shown.
		 */
		public boolean getInfoWindowShown() {
			return getObject().isInfoWindowShown();
		}
		public void setInfoWindowShown(boolean v) {
			if (v)
				getObject().showInfoWindow();
			else
				getObject().hideInfoWindow();
		}
	}
	/**
	 * A series of lines added to the map.
	 *Call GoogleMap.AddPolyline to create this object.
	 */
	@ShortName("Polyline")
	public static class PolylineWrapper extends AbsObjectWrapper<Polyline> {
		/**
		 * Gets or sets the list of points that build the lines.
		 *The list should hold objects of type LatLng.
		 */
		public List getPoints() {
			java.util.List<LatLng> l = getObject().getPoints();
			if (l == null) {
				l = new ArrayList<LatLng>();
				getObject().setPoints(l);
			}
			return (List)AbsObjectWrapper.ConvertToWrapper(new List(), l);
		}
		public void setPoints(List points) {
			java.util.List<LatLng> l = new ArrayList<LatLng>();
			for (Object o : points.getObject()) {
				l.add((LatLng)o);
			}
			getObject().setPoints(l);
		}
		/**
		 * Gets or sets the line width.
		 */
		public void setWidth(float v) {
			getObject().setWidth(v);
		}
		public float getWidth() {
			return getObject().getWidth();
		}
		/**
		 * Gets or sets the line z-index.
		 */
		public void setZIndex(float v) {
			getObject().setZIndex(v);
		}
		public float getZIndex() {
			return getObject().getZIndex();
		}
		/**
		 * Gets or sets whether this line is visible.
		 */
		public boolean getVisibile() {
			return getObject().isVisible();
		}
		public void setVisible(boolean v) {
			getObject().setVisible(v);
		}
		/**
		 * Gets or sets whether the line segments are geodesic lines (shortest path on the earth surface) or straight lines.
		 *The default value is False.
		 */
		public boolean getGeodesic() {
			return getObject().isGeodesic();
		}
		public void setGeodesic(boolean v) {
			getObject().setGeodesic(v);
		}
		/**
		 * Gets or sets the line color.
		 */
		public int getColor() {
			return getObject().getColor();
		}
		public void setColor(int v) {
			getObject().setColor(v);
		}
		/**
		 * Removes the line from the map.
		 */
		public void Remove() {
			getObject().remove();
		}
	}
	/**
	 * Call GoogleMap.GetUiSettings to obtain this object.
	 */
	@ShortName("MapUiSettings")
	public static class UiSettingsWrapper extends AbsObjectWrapper<UiSettings> {
		/**
		 * Gets or sets whether the compass is enabled.
		 */
		public boolean getCompassEnabled() {
			return getObject().isCompassEnabled();
		}
		public void setCompassEnabled(boolean v) {
			getObject().setCompassEnabled(v);
		}
		/**
		 * Gets or sets whether the my-location button is enabled.
		 */
		public boolean getMyLocationButtonEnabled() {
			return getObject().isMyLocationButtonEnabled();
		}
		public void setMyLocationButtonEnabled(boolean v) {
			getObject().setMyLocationButtonEnabled(v);
		}
		/**
		 * Gets or sets whether rotate gestures are enabled.
		 */
		public boolean getRotateGesturesEnabled() {
			return getObject().isRotateGesturesEnabled();
		}
		public void setRotateGesturesEnabled(boolean v) {
			getObject().setRotateGesturesEnabled(v);
		}
		/**
		 * Gets or sets whether scroll gestures are enabled.
		 */
		public boolean getScrollGesturesEnabled() {
			return getObject().isScrollGesturesEnabled();
		}
		public void setScrollGesturesEnabled(boolean v) {
			getObject().setScrollGesturesEnabled(v);
		}
		/**
		 * Gets or sets whether tilt gestures are enabled.
		 */
		public boolean getTiltGesturesEnabled() {
			return getObject().isTiltGesturesEnabled();
		}
		public void setTiltGesturesEnabled(boolean v) {
			getObject().setTiltGesturesEnabled(v);
		}
		/**
		 * Gets or sets whether zoom controls are enabled.
		 */
		public boolean getZoomControlsEnabled() {
			return getObject().isZoomControlsEnabled();
		}
		public void setZoomControlsEnabled(boolean v) {
			getObject().setZoomControlsEnabled(v);
		}
		/**
		 * Gets or sets whether zoom gestures are enabled.
		 */
		public boolean getZoomGesturesEnabled() {
			return getObject().isZoomGesturesEnabled();
		}
		public void setZoomGesturesEnabled(boolean v) {
			getObject().setZoomGesturesEnabled(v);
		}
		/**
		 * Enables or disables all gestures.
		 */
		public void setAllGesturesEnabled(boolean v) {
			getObject().setAllGesturesEnabled(v);
		}
	}
	
}
