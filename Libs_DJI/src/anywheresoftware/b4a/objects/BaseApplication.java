package anywheresoftware.b4a.objects;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class BaseApplication extends Application{
	 @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Log.i("B4A", "**************************** BaseApplication2 **************************");
        com.secneo.sdk.Helper.install(this);
    }
}
