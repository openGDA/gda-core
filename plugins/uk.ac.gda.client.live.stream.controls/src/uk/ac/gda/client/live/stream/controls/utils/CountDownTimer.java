package uk.ac.gda.client.live.stream.controls.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Schedule a count down until a given time in the future, with regular notifications
 * on the given time intervals along the way.
 *
 * Example of showing a 30 second count down in a text field:
 *
 * <pre class="prettyprint">
 * new CountDownTimer(30000, 1000) {
 *
 * 	public void onTick(long millisUntilFinished) {
 * 		mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
 * 	}
 *
 * 	public void onFinish() {
 * 		mTextField.setText("done!");
 * 	}
 * }.start();
 * </pre>
 *
 * The calls to {@link #onTick(long)} are synchronised to this object so that
 * one call to {@link #onTick(long)} won't ever occur before the previous
 * callback is complete. This is only relevant when the implementation of
 * {@link #onTick(long)} takes an amount of time to execute that is significant
 * compared to the count down interval.
 */
public abstract class CountDownTimer {
	/**
	 * Milliseconds since epoch when alarm should stop.
	 */
	private final long mMillisInFuture;
	/**
	 * The interval in milliseconds that the user receives callbacks
	 */
	private final long mCountdownInterval;
	private long mStopTimeInFuture;
	
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * boolean representing if the timer was cancelled
	 */
	private boolean mCancelled = false;

	/**
	 * @param millisInFuture
	 *            The number of millis in the future from the call to
	 *            {@link #start()} until the countdown is done and
	 *            {@link #onFinish()} is called.
	 * @param countDownInterval
	 *            The interval along the way to receive {@link #onTick(long)}
	 *            callbacks.
	 */
	public CountDownTimer(long millisInFuture, long countDownInterval) {
		mMillisInFuture = millisInFuture;
		mCountdownInterval = countDownInterval;
	}

	/**
	 * Cancel the countdown.
	 */
	public synchronized final void cancel() {
		mCancelled = true;
		onFinish();
	}

	/**
	 * Start the countdown.
	 */
	public synchronized final CountDownTimer start() {
		mCancelled = false;
		if (mMillisInFuture <= 0) {
			onFinish();
			return this;
		}
		mStopTimeInFuture = System.currentTimeMillis() + mMillisInFuture;
		executor.submit(mRunner);
		return this;
	}
	/**
	 * shutdown executor service
	 */
	public void dispose() {
		if (executor !=null) {
			executor.shutdown();
			try {
			    if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
			        executor.shutdownNow();
			    } 
			} catch (InterruptedException e) {
			    executor.shutdownNow();
			}
		}
	}

	/**
	 * Callback fired on regular interval.
	 * 
	 * @param millisUntilFinished
	 *            The amount of time until finished.
	 */
	public abstract void onTick(long millisUntilFinished);

	/**
	 * Callback fired when the time is up.
	 */
	public abstract void onFinish();

	//cannot use lambda here as final field 'mCountdownInterval' only can be initialised inside constructor.
	private Runnable mRunner = new Runnable() {

		@Override
		public void run() {
				while (!mCancelled && mStopTimeInFuture >= System.currentTimeMillis()) {
					final long millisLeft = mStopTimeInFuture - System.currentTimeMillis();
					if (millisLeft <= 0) {
						onFinish();
						break;
					} else {
						long lastTickStart = System.currentTimeMillis();
						onTick(millisLeft);
						// take into account user's onTick taking time to execute
						long lastTickDuration = System.currentTimeMillis() - lastTickStart;
						long delay;
						if (millisLeft < mCountdownInterval) {
							// just delay until done
							delay = millisLeft - lastTickDuration;
							// special case: user's onTick took more than interval to
							// complete, trigger onFinish without delay
							if (delay < 0)
								delay = 0;
						} else {
							delay = mCountdownInterval - lastTickDuration;
							// special case: user's onTick took more than interval to
							// complete, skip to next interval
							while (delay < 0)
								delay += mCountdownInterval;
						}
						try {
							TimeUnit.MICROSECONDS.sleep(delay);
						} catch (InterruptedException e) {
							// no-op
						}
					}
				}
			}
	};
}