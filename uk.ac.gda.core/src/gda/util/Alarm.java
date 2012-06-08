/***********************************************************************************************************************
 * Copyright (C)2000 - Just van den Broecke - See license below *
 **********************************************************************************************************************/

// package nl.justobjects.toolkit.sys;
package gda.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Alarm timer service.
 * <h3>Purpose and Responsibilities</h3>
 * Alarm can be used to schedule a callback to an {@link AlarmListener} object. This callback is performed once after a
 * specified amount of time (ms) has elapsed. After creation, a client may:
 * <ul>
 * <li>{@link #reschedule()} the alarm call for the same period of time (does not matter if the original period had
 * expired or not)</li>
 * <li>{@link #reschedule(long)} the alarm call for another period of time (does not matter if the original period had
 * expired or not)</li>
 * <li>{@link #cancel()} the alarm call</li>
 * </ul>
 * <h3>Usage and Examples</h3>
 * <p>
 * A client of Alarm has to instantiate an Alarm object with a specified amount of time and a callback
 * {@link AlarmListener} object. Optionally an argument to be passed on the callback can be specified.
 * </p>
 * 
 * <pre>
 * Alarm alarm = new Alarm(5000, new AlarmListener() {
 * 	public void alarm(Alarm anAlarm) {
 * 		// do something here
 * 	}
 * });
 * 
 * // reschedule the alarm on the previously specified time.
 * alarm.reschedule();
 * 
 * // reschedule the alarm on another time
 * alarm.reschedule(3000);
 * 
 * // cancel the callback before it is executed
 * alarm.cancel();
 * </pre>
 * 
 * <h3>Implementation</h3>
 * One thread loops infinitely executing expired alarms and sleeping until the next alarm expires and must be executed.
 * <h3>Concurrency</h3>
 * This class is thread-safe, because all read/write accesses on the scheduled alarms set are synchronized.
 * <p>
 * Special effort has been made to enable cancellations or rescheduling of alarm calls by clients within a alarm
 * callback. Author Just van den Broecke
 */
public class Alarm implements Comparable<Object> {
	/**
	 * Create an alarm call at a specified time from now.
	 * 
	 * @param aTimeMillis
	 *            time in milliseconds from now of the alarm call
	 * @param aListener
	 *            alarm call listener
	 */
	public Alarm(long aTimeMillis, AlarmListener aListener) {
		this(aTimeMillis, aListener, null);
	}

	/**
	 * Create an alarm call at a specified time from now.
	 * 
	 * @param aTimeMillis
	 *            time in milliseconds from now of the alarm call
	 * @param aListener
	 *            alarm call listener
	 * @param anArgument
	 *            argument that gets passed back to the listener during the alarm call (might be <code>null</code>)
	 */
	public Alarm(long aTimeMillis, AlarmListener aListener, Object anArgument) {

		listener = aListener;
		arg = anArgument;

		if (aTimeMillis < 0) {
			throw new IllegalArgumentException("timeMillis=" + aTimeMillis + " callback=" + aListener.getClass());
		}

		// ok, this is not a *re*schedule
		reschedule(aTimeMillis);
	}

	/**
	 * Return the number of currently pending Alarms.
	 * 
	 * @return number of alarms
	 */
	public static int getAlarmSize() {
		return alarms.size();
	}

	/**
	 * Return the string representing the time the execution of the last invoked Alarm started.
	 * 
	 * @return string time representation
	 */
	public static String getEndLastExecute() {
		return endLastExecute;
	}

	/**
	 * Return the string representing the time the execution of the last invoked Alarm ended.
	 * 
	 * @return string time representation
	 */
	public static String getStartLastExecute() {
		return startLastExecute;
	}

	/**
	 * Return the interval for the Alarm.
	 * 
	 * @return millisecond interval
	 */
	public long getIntervalMillis() {
		return deltaTimeMillis;

	}

	/**
	 * Cancel a scheduled alarm call.
	 */
	public void cancel() {

		synchronized (alarms) {
			alarms.remove(this);
		}
	}

	/**
	 * Compare the expiry time to that of another Alarm.
	 * 
	 * @param anObject
	 *            an alarm instance
	 * @return the result of the comparison of time
	 */
	@Override
	public int compareTo(Object anObject) {

		Alarm that = (Alarm) anObject;

		if (this.expireTimeMillis < that.expireTimeMillis) {
			return -1;
		} else if (this.expireTimeMillis == that.expireTimeMillis) {

			// If expiry times are equal the objects could still be
			// different.
			// return this.toString().compareTo(that.toString());

			// Shouldn't we use return this==that?0:1 ?
			// no because in that case if this.comaparesTo(that) equals 1
			// then
			// that.compareTo(this) will also be 1 and logically it should
			// be -1
			// maybe some classes that use compareTo() expect such a
			// behavior
			// therefore we use the following
			return (this == that) ? 0 : this.toString().compareTo(that.toString());
		} else {
			return 1;
		}
	}

	/**
	 * Get the optional call-back argument.
	 * 
	 * @return call-back argument
	 */
	public Object getArgument() {
		return arg;
	}

	/**
	 * Reschedule an alarm call for the same time period as the previous schedule.
	 */
	public void reschedule() {
		reschedule(deltaTimeMillis);
	}

	/**
	 * Reschedule an alarm call for a given time period.
	 * 
	 * @param aTimeMillis
	 *            time in milliseconds from now of the alarm call
	 */
	public void reschedule(long aTimeMillis) {

		// determine if the thread that does call backs needs to be waked up
		// once
		// this alarm is added
		boolean needsWakeUp = false;

		synchronized (alarms) {

			// We may have been scheduled already.
			// Cancel a pending alarm.
			alarms.remove(this);

			// Create new schedule.
			deltaTimeMillis = aTimeMillis;
			expireTimeMillis = System.currentTimeMillis() + aTimeMillis;

			// Schedule ourselves.
			alarms.add(this);

			Alarm firstAlarm = alarms.first();

			if (this.compareTo(firstAlarm) == 0) {
				needsWakeUp = true;
			}
		}

		// interupt the sleeping scheduler thread so it can adjust its current
		// sleep time, but only if this thread is not the alarm thread, because
		// then obviously it is not sleeping
		if ((Thread.currentThread() != alarmThread) && needsWakeUp) {
			alarmThread.interrupt();
		}
	}

	/**
	 * Execute the alarm callback.
	 */
	private void execute() {

		startLastExecute = listener.getClass() + " at " + System.currentTimeMillis();

		listener.alarm(this);

		endLastExecute = listener.getClass() + " at " + System.currentTimeMillis();
	}

	/**
	 * Get the remaining millis before expiration.
	 * 
	 * @return remaining time in milliseconds
	 */
	private long getRemainingMillis() {
		return expireTimeMillis - System.currentTimeMillis();
	}

	/**
	 * Is this alarm call expired?
	 * 
	 * @return true if alarm call is expired
	 */
	private boolean isExpired() {
		return getRemainingMillis() <= 0;
	}

	/**
	 * Alarm timer thread.
	 * <h3>Purpose and Responsibilities</h3>
	 * One (static) instance of the alarm thread loops infinitely executing expired alarms and sleeping until the next
	 * alarm expires and must be executed.
	 * <h3>Concurrency</h3>
	 * This class is thread-safe, because all read/write accesses on the scheduled alarms set are synchronized.
	 * <p>
	 * Special effort has been made to enable cancellations or rescheduling of alarm calls by clients within a alarm
	 * callback.
	 * 
	 * @author Just van den Broecke
	 * @version $Revision: 30585 $ $Date: 2010-10-14 16:48:24 +0100 (Thu, 14 Oct 2010) $
	 */
	private static class AlarmThread extends Thread {

		/**
		 * Maximum sleep time. If no alarms are scheduled or longer in the future than this constant, the schedule
		 * thread will still wake up to check for expired alarm calls.
		 */
		private static long MAX_SLEEP_MILLIS = 1000 * 20;

		/**
		 * Default constructor.
		 */
		public AlarmThread() {

			super("AlarmThread");

			setDaemon(true);
		}

		/**
		 * Handles expiration, execution and scheduling of alarm callbacks.
		 * <p>
		 * This method performs the main scheduler loop. Within this loop the following steps are performed:
		 * </p>
		 * <ol>
		 * <li>copy expired alarm objects from the alarms set to the expired alarms set</li>
		 * <li>for each expired alarm call its {@link Alarm#execute()} method (which performs the actual callback).</li>
		 * <li>if more alarms are scheduled sleep until the next one expires (if there are no more alarms scheduled
		 * sleep for {@link #MAX_SLEEP_MILLIS}</li>
		 * </ol>
		 */
		@Override
		public void run() {

			ArrayList<Alarm> expiredAlarms = new ArrayList<Alarm>();

			while (true) {

				expiredAlarms.clear();

				// loop over the sorted set of alarms, moving expired
				// alarms to the local expiredAlarms list.
				// If the first not-expired alarm is encountered, then the loop
				// is exited (or no not-expired alarms exist)
				synchronized (alarms) {

					Iterator<Alarm> iter = alarms.iterator();

					while (iter.hasNext()) {
						Alarm alarm = iter.next();

						if (alarm.isExpired()) {
							iter.remove();
							expiredAlarms.add(alarm);
						} else {

							// no more expired alarms at the beginning of
							// the
							// alarms list
							break;
						}
					}
				}

				// execute all expired alarms
				for (Alarm alarm : expiredAlarms) {
					// an unanticipated exception thrown during the callback
					// should not stop the scheduler, so catch anything
					try {
						alarm.execute();
					} catch (Throwable t) {
						// Message.alarm("Exception in Alarm callback: " + t);
					}
				}

				long sleepMillis;

				// Calculate the next sleep interval.
				if (alarms.size() > 0) {
					synchronized (alarms) {
						sleepMillis = alarms.first().getRemainingMillis();
					}
				} else {
					sleepMillis = MAX_SLEEP_MILLIS;
				}

				if (sleepMillis > 0) {

					try {

						// Message.debug("sleeping " + sleepMillis);
						sleep(sleepMillis);

						// Message.debug("wake up");
					} catch (InterruptedException ie) {

						// Message.debug("interrupted");
						// interrupted because an alarm is added
					}
				}
			}
		}
	}

	/** the schedule thread */
	private static AlarmThread alarmThread;

	static {
		alarmThread = new AlarmThread();

		alarmThread.start();
	}

	/** map of scheduled alarms */
	private static SortedSet<Alarm> alarms = new TreeSet<Alarm>();

	/**
	 * contains the system time of the begining of the execution of the last alarm
	 */
	private static String startLastExecute;

	/**
	 * contains the system time of the end of the execution of the last alarm
	 */
	private static String endLastExecute;

	/** optional call-back argument */
	private Object arg;

	/** delta between the moment of scheduling and expiration */
	private long deltaTimeMillis;

	/** absolute system time of expiration */
	private long expireTimeMillis;

	/** alarm call-back listener */
	private AlarmListener listener;
}

/*
 * Copyright (C)2000 Just A. van den Broecke <just@justobjects.nl> This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place -
 * Suite 330, Boston, MA 02111-1307, USA.
 */
