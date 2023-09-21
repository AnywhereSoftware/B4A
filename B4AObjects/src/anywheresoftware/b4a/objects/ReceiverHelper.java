package anywheresoftware.b4a.objects;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import anywheresoftware.b4a.BA.Hide;

public class ReceiverHelper {
	@Hide
	public BroadcastReceiver receiver;
	@Hide
	public PendingResult pendingResult;
	public ReceiverHelper (BroadcastReceiver receiver) {
		this.receiver = receiver;
	}
	@Hide
	public void GoAsync() {
		pendingResult = receiver.goAsync();
	}
	@Hide
	public void FinishAsync() {
		pendingResult.finish();
	}
	
}
