
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

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DesignerProperties;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Property;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common.DesignerCustomView;
import anywheresoftware.b4a.objects.collections.Map;

import com.google.android.exoplayer2.ui.PlayerView;

/**
 * A player interface. Should be added as a custom view with the designer.
 *The Player property must be set.
 */
@DesignerProperties(values = {
		@Property(key="ResizeMode", displayName = "Resize Mode", fieldType="String", defaultValue="FIT", list="FIT|FIXED_HEIGHT|FIXED_WIDTH"),
		@Property(key="UseController", displayName = "Use Controller", fieldType="Boolean", defaultValue="True"),
		@Property(key="ControllerTimeout", displayName = "Controller Timeout (ms)", fieldType="Int", defaultValue="5000", description="Pass -1 to prevent the controller from hiding.")
		
		
	})
@ShortName("SimpleExoPlayerView")
public class SimpleExoPlayerViewWrapper extends ViewWrapper<PlayerView> implements DesignerCustomView {

	@Hide
	@Override
	public void _initialize(final BA ba, Object activityClass, String EventName) {
		final PlayerView view = new PlayerView(ba.context);
		final String eventName = EventName.toLowerCase(BA.cul);
		setObject(view);
		innerInitialize(ba, eventName, true);
	}

	@Override
	public void DesignerCreateView(PanelWrapper base, LabelWrapper lw, Map props) {
		CustomViewWrapper.replaceBaseWithView2(base, getObject());
		setResizeMode((String) props.Get("ResizeMode"));
		setUseController((Boolean)props.Get("UseController"));
		setControllerTimeout((Integer)props.Get("ControllerTimeout"));
		
	}
	/**
	 * Sets the resize mode. Possible values: FIT, FIXED_HEIGHT or FIXED_WIDTH
	 */
	public void setResizeMode(String s) {
		try {
			getObject().setResizeMode(com.google.android.exoplayer2.ui.AspectRatioFrameLayout.class.getField("RESIZE_MODE_" + s).getInt(null));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Enables or disables the controller.
	 */
	public void setUseController(boolean b) {
		getObject().setUseController(b);
	}
	public boolean getUseController() {
		return getObject().getUseController();
	}
	/**
	 * Gets or sets the controller timeout (measured in milliseconds). The control will disappear after the set period.
	 *Pass -1 to show it indefintely.
	 */
	public void setControllerTimeout(int i) {
		getObject().setControllerShowTimeoutMs(i);
	}
	public int getControllerTimeout() {
		return getObject().getControllerShowTimeoutMs();
	}
	/**
	 * Sets the player engine.
	 */
	public void setPlayer(SimpleExoPlayerWrapper Player) {
		getObject().setPlayer(Player.player);
	}
}
