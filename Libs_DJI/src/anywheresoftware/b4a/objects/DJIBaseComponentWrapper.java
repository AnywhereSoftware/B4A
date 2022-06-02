package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks.CompletionCallback;
import dji.common.util.CommonCallbacks.CompletionCallbackWith;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseComponent.ComponentListener;


@Hide
@Events(values={"Result (Success As Boolean, ErrorMessage As String)",
		"ResultWithValue (Success As Boolean, ErrorMessage As String, Value As Object)"})
public class DJIBaseComponentWrapper<T> extends AbsObjectWrapper<T>{
	@Hide
	public void initialize(final BA ba, final String EventName) {
		final String eventName = EventName.toLowerCase(BA.cul);
		AbsObjectWrapper.getExtraTags(getObject()).put("ba", ba);
		AbsObjectWrapper.getExtraTags(getObject()).put("eventName", eventName);
		final Object o = getObject();
		if (o instanceof BaseComponent) {
			final BaseComponent com = (BaseComponent) o;
			com.setComponentListener(new ComponentListener() {

				@Override
				public void onConnectivityChange(boolean paramBoolean) {
					ba.raiseEventFromUI(com, eventName + "_componentconnectivitychanged", paramBoolean);
					
				}

			});
		}
	}
	@Hide BA getBA() {
		return (BA) AbsObjectWrapper.getExtraTags(getObject()).get("ba");
	}
	@Hide
	public String getEventName() {
		return (String) AbsObjectWrapper.getExtraTags(getObject()).get("eventName");
	}
	public void setTag(Object o) {
		AbsObjectWrapper.getExtraTags(getObject()).put("tag", o);
	}
	public Object getTag() {
		return AbsObjectWrapper.getExtraTags(getObject()).get("tag");
	}
	public boolean getConnected() {
		return ((BaseComponent)getObject()).isConnected();
	}
	@SuppressWarnings("unchecked")
	@Hide
	public class B4ACompletionCallback implements CompletionCallback, CompletionCallbackWith{
		public B4ACompletionCallback() {
		}
		@Override
		public void onResult(DJIError err) {
			getBA().raiseEventFromUI(this, getEventName() + "_result", err == null, err == null ? "": err.getDescription());
		}
		@Override
		public void onFailure(DJIError err) {
			getBA().raiseEventFromUI(this, getEventName() + "_resultwithvalue", false, err == null ? "": err.getDescription(), null);
		}
		@Override
		public void onSuccess(Object paramT) {
			getBA().raiseEventFromUI(this, getEventName() + "_resultwithvalue", true, "", paramT);
		}
		
	}
}
