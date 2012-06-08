/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import java.util.ArrayList;

/**
 * Class which loops forever calling methods at specified time interval. Classes which want to have methods called must
 * implement the PollerListener interface.
 */

public class Poller implements Runnable {
	private Thread thread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());

	private long pollTime = 10000;

	private ArrayList<PollerListener> listeners = new ArrayList<PollerListener>();
	private boolean isPollerRunning = false;

	/**
	 * Constructor
	 */
	public Poller() {
	}

	/**
	 * Constructor which specifies a PollerListener and uses the default polling time.
	 * 
	 * @param pollerListener
	 *            the PollerListener
	 */
	public Poller(PollerListener pollerListener) {
		listeners.add(pollerListener);
	}

	/**
	 * Constructor which specifies a PollerListener and a polling time
	 * 
	 * @param pollerListener
	 *            the PollerListener
	 * @param pollTime
	 *            the polling time (mS)
	 */
	public Poller(PollerListener pollerListener, long pollTime) {
		listeners.add(pollerListener);
		this.pollTime = pollTime;
	}

	/**
	 * Adds another PollerListener. Must be synchronized to prevent it being called while notifyListeners is accessing
	 * the listeners list.
	 * 
	 * @param pollerListener
	 *            the new PollerListener
	 */
	public synchronized void addListener(PollerListener pollerListener) {
		listeners.add(pollerListener);
	}

	/**
	 * Deletes a PollerListener. Must be synchronized to prevent it being called while notifyListeners is accessing the
	 * listeners list.
	 * 
	 * @param pollerListener
	 *            the PollerListener to be deleted
	 */
	public synchronized void deleteListener(PollerListener pollerListener) {
		listeners.remove(pollerListener);
	}

	/**
	 * Notifies each listener by calling its pollDone method.
	 */
	private void notifyListeners() {
		for (PollerListener pollerListener : listeners) {
			pollerListener.pollDone(new PollerEvent(this, pollTime));
		}
	}

	/**
	 * The thread run method.
	 */
	@Override
	public synchronized void run() {
		while (isPollerRunning()) {
			notifyListeners();

			try {
				wait(pollTime);
			} catch (InterruptedException ie) {
				// Deliberately does nothing.
				// If setPollTime calls interrupt while thread is waiting
				// an InterruptedException is generated so we do not want
				// a message displayed.
			}
		}
	}

	/**
	 * Sets the poll time interval.
	 * 
	 * @param newPollTime
	 *            the new polling time (mS)
	 */
	public void setPollTime(long newPollTime) {
		pollTime = newPollTime;

		// Interrupt the thread in case it is waiting for a long time -
		// really only need to do this if the newPollTime is less than the
		// old one but it causes no harm to do it always.
		// Do not send the interrupt if thread is the current thread. This
		// makes it safe to call setPollTime from within the pollDone of
		// a PollerListener.
		if (thread != Thread.currentThread()) {
			thread.interrupt();
		}
	}

	/**
	 * Starts the polling thread
	 */
	public void start() {
		setPollerRunning(true);
		thread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		thread.start();
	}

	/**
	 * check if poller is running
	 * @return boolean
	 */
	public boolean isPollerRunning() {
		return isPollerRunning;
	}

	/**
	 * set poller running state
	 * @param isPollerRunning
	 */
	public void setPollerRunning(boolean isPollerRunning) {
		this.isPollerRunning = isPollerRunning;
	}
	
	/**
	 * stops poller thread run
	 */
	public void stop() {
		setPollerRunning(false);
	}
}
