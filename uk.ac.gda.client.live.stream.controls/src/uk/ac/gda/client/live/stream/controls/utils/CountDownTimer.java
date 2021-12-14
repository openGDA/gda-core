/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

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
	private long mCountdownInterval = 1;
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
	protected CountDownTimer(long millisInFuture, long countDownInterval) {
		mMillisInFuture = millisInFuture;
		mCountdownInterval = countDownInterval;
	}

	/**
	 * Cancel the countdown.
	 */
	public final synchronized void cancel() {
		mCancelled = true;
		onFinish();
	}

	/**
	 * Start the countdown.
	 */
	public final synchronized CountDownTimer start() {
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
			    Thread.currentThread().interrupt();
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

	private Runnable mRunner = ()->{

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
							Thread.currentThread().interrupt();
						}
					}
				}
	};
}