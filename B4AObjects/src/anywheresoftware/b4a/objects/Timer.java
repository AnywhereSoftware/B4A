package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.Msgbox;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
/**
 * A Timer object generates ticks events at specified intervals.
 *Using a timer is a good alternative to a long loop, as it allows the UI thread to handle other events and messages.
 *Note that the timer events will not fire while the UI thread is busy running other code.
 *The timer Enabled property is set to False by default. To make it start working you should change it to True.
 *Timer events will not fire when the activity is paused, or if a blocking dialog (like Msgbox) is visible.
 *<b>Timers should be declared in Sub Process_Globals</b>. Otherwise you may get multiple timers running when the activity is recreated.
 *It is also important to disable the timer when the activity is pausing and then enable it when it resumes. This will save CPU and battery.
 */
@ShortName("Timer")
@Events(values={"Tick"})
public class Timer implements CheckForReinitialize{
	private long interval;
	private boolean enabled = false;
	private int relevantTimer = 0;
	private BA ba;
	private String eventName;
	private ParentReference myRef = new ParentReference();
	/**
	 * Initializes the timer with the event sub prefix and the specified interval (measured in milliseconds).
	 *IMPORTANT: this object should be declared in Sub Process_Globals.
	 *Example:<code>
	 *Timer1.Initialize("Timer1", 1000)
	 *Timer1.Enabled = True
	 *
	 *Sub Timer1_Tick
	 * 'Handle tick events
	 *End Sub
	 *</code>
	 */
	public void Initialize(BA ba, String EventName, long Interval) {
		this.interval = Interval;
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul) + "_tick";
	}
	@Override
	public boolean IsInitialized() {
		return ba != null;
	}
	/**
	 * Gets or sets whether the timer is enabled (ticking).
	 */
	public boolean getEnabled() {
		return enabled;
	}
	/**
	 * Gets or sets the interval between tick events, measured in milliseconds.
	 */
	public void setInterval(long Interval) {
		if (this.interval == Interval)
			return;
		this.interval = Interval;
		if (this.enabled) {
			stopTicking();
			startTicking();
		}
	}
	public long getInterval() {
		return interval;
	}
	private void startTicking() {
		TickTack tt = new TickTack(relevantTimer, myRef, ba);
		BA.handler.postDelayed(tt, interval);
	}
	
	public void setEnabled(boolean Enabled) {
		if (Enabled == this.enabled)
			return;
		if (Enabled == true){ //to true
			myRef.timer = this;
			if (interval <= 0)
				throw new IllegalStateException("Interval must be larger than 0.");
			startTicking();
		}
		else {
			myRef.timer = null;
			stopTicking();
		}
		this.enabled = Enabled;
	}
	
	static class TickTack implements Runnable {
		private final ParentReference parent;
		private final int currentTimer;
		private final BA ba;
		public TickTack(int currentTimer, ParentReference parent, BA ba) {
			this.currentTimer = currentTimer;
			this.parent = parent;
			this.ba = ba;
		}
		@Override
		public void run() {
			Timer parentTimer = parent.timer;
			if (parentTimer == null || currentTimer != parentTimer.relevantTimer) //old messages in the queue
				return;
			BA.handler.postDelayed(this, parentTimer.interval);
			if (!ba.isActivityPaused() && !Msgbox.msgboxIsVisible())
				ba.raiseEvent2(parentTimer, false,parentTimer.eventName, true);

		}
	}
	static class ParentReference {
		public Timer timer;
	}
	private void stopTicking() {
		relevantTimer++;
	}

}
