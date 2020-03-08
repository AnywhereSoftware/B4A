
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

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;

@Hide
public class AdminReceiver2 extends DeviceAdminReceiver{
	 private boolean checkForService = true;
	    private Intent serviceIntent;
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (checkForService) {
	    		checkForService = false;
	    		try {
	    			Class<?> ser = Class.forName(context.getPackageName() + ".managerservice");
	    			serviceIntent = new Intent(context, ser); 
	    		} catch (ClassNotFoundException e) {
	    			System.out.println(BA.packageName + ".managerservice not found.");
	    			serviceIntent = null;
	    		}
	    	}

	    	super.onReceive(context, intent);
	    	if (serviceIntent != null) {
	    		serviceIntent.putExtra("admin_intent", intent);
	    		context.startService(serviceIntent);
	    	}
	    }
		@Override
		public void onEnabled(Context context, Intent intent) {
	    	if (serviceIntent != null) {
	    		serviceIntent.putExtra("admin", "Enabled");
	    	}
	    }
	    @Override
		public void onDisabled(Context context, Intent intent) {
	    	if (serviceIntent != null) {
	    		serviceIntent.putExtra("admin", "Disabled");
	    		
	    	}
	    }
     @Override
     public void onPasswordChanged(Context context, Intent intent) {
     	if (serviceIntent != null) {
	    		serviceIntent.putExtra("admin", "PasswordChanged");
     	}
     }
}
