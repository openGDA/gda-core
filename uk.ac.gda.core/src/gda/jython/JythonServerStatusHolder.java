/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.jython;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.ObservableComponent;

/**
 * Maintains the status of the Jython server.
 */
public class JythonServerStatusHolder {

	private static final Logger logger = LoggerFactory.getLogger(JythonServerStatusHolder.class);

	private final JythonServer jythonServer;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	// These three fields determine the script status
	private boolean runningScript;
	private int numCommandsRunningSynchronously;
	private boolean paused;

	// This field determines the scan status
	private int lastScanStatus = Jython.IDLE;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private final ObservableComponent jythonServerStatusObservers = new ObservableComponent();

	public void addObserver(IJythonServerStatusObserver anObserver) {
		jythonServerStatusObservers.addIObserver(anObserver);
	}

	public void deleteObserver(IJythonServerStatusObserver anObserver) {
		jythonServerStatusObservers.deleteIObserver(anObserver);
	}

	public JythonServerStatusHolder(JythonServer jythonServer) {
		this.jythonServer = jythonServer;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean tryAcquireScriptLock() {

		boolean allowed = false;

		JythonServerStatus event = null;

		lock.writeLock().lock();
		try {

			final int statusBefore = getScriptStatus();

			if (!runningScript) {
				runningScript = true;
				allowed = true;
			}

			final int statusAfter = getScriptStatus();

			if (statusBefore != statusAfter) {
				event = new JythonServerStatus(statusAfter, lastScanStatus);
			}
		}

		finally {
			lock.writeLock().unlock();
		}

		if (event != null) {
			updateStatus(event);
		}

		return allowed;
	}

	public void releaseScriptLock() {

		JythonServerStatus event = null;

		lock.writeLock().lock();
		try {

			final int statusBefore = getScriptStatus();

			if (runningScript) {
				runningScript = false;
			}

			final int statusAfter = getScriptStatus();

			if (statusBefore != statusAfter) {
				event = new JythonServerStatus(statusAfter, lastScanStatus);
			}
		}

		finally {
			lock.writeLock().unlock();
		}

		if (event != null) {
			updateStatus(event);
		}
	}

	public void startRunningCommandSynchronously() {

		JythonServerStatus event = null;

		lock.writeLock().lock();
		try {

			final int statusBefore = getScriptStatus();

			numCommandsRunningSynchronously++;

			final int statusAfter = getScriptStatus();

			if (statusBefore != statusAfter) {
				event = new JythonServerStatus(statusAfter, lastScanStatus);
			}
		}

		finally {
			lock.writeLock().unlock();
		}

		if (event != null) {
			updateStatus(event);
		}
	}

	public void finishRunningCommandSynchronously() {

		JythonServerStatus event = null;

		lock.writeLock().lock();
		try {

			final int statusBefore = getScriptStatus();

			numCommandsRunningSynchronously--;

			final int statusAfter = getScriptStatus();

			if (statusBefore != statusAfter) {
				event = new JythonServerStatus(statusAfter, lastScanStatus);
			}
		}

		finally {
			lock.writeLock().unlock();
		}

		if (event != null) {
			updateStatus(event);
		}
	}

	public void setScriptStatus(int newStatus) {

		JythonServerStatus event = null;

		lock.writeLock().lock();
		try {

			final int statusBefore = getScriptStatus();

			if (newStatus == Jython.PAUSED) {
				paused = true;
			}

			else if (newStatus == Jython.RUNNING) {
				paused = false;
			}

			final int statusAfter = getScriptStatus();

			if (statusBefore != statusAfter) {
				event = new JythonServerStatus(statusAfter, lastScanStatus);
			}
		}

		finally {
			lock.writeLock().unlock();
		}

		if (event != null) {
			updateStatus(event);
		}
	}

	public int getScriptStatus() {

		lock.readLock().lock();
		try {

			if (paused) {
				return Jython.PAUSED;
			}

			else if (runningScript || (numCommandsRunningSynchronously > 0)) {
				return Jython.RUNNING;
			}

			else {
				return Jython.IDLE;
			}
		}

		finally {
			lock.readLock().unlock();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void updateScanStatus(int newStatus) {

		JythonServerStatus event = null;

		lock.writeLock().lock();
		try {
			if (newStatus != lastScanStatus) {
				lastScanStatus = newStatus;
				event = new JythonServerStatus(getScriptStatus(), lastScanStatus);
			}
		}

		finally {
			lock.writeLock().unlock();
		}

		if (event != null) {
			updateStatus(event);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void updateStatus(JythonServerStatus newStatus) {
		jythonServer.updateIObservers(newStatus);
		logger.info(newStatus.toString());
		jythonServerStatusObservers.notifyIObservers(null, newStatus);
	}

}
