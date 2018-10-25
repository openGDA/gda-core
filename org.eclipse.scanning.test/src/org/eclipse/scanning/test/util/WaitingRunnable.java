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

package org.eclipse.scanning.test.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link Runnable} whose {@link #run()} method waits until released,
 * by another thread calling the {@link #release()} method. Additionally,
 * a thread can wait until the runnable is run in another thread by calling
 * {@link #waitUntilRun()}.
 * <p>
 * This class can be used like a breakpoint, to stop one thread to allow another thread
 * to take action knowing that the first thread is stopped. For example, if the runnable
 * is run as part of a queue, the whole queue can be paused if a {@link WaitingRunnable} is run
 * as part of it.
 */
public class WaitingRunnable implements Runnable {

	private final Lock lock = new ReentrantLock();

	// the predicate and condition to set and notify when run is called
	private final AtomicBoolean called = new AtomicBoolean(false);
	private final Condition calledCondition = lock.newCondition();

	// the predicate and condition for to set and notify when release is called
	private final AtomicBoolean released = new AtomicBoolean(false);
	private final Condition releasedCondition = lock.newCondition();

	/**
	 * A convenience method to set the predicate and notify the condition.
	 * @param condition
	 * @param predicate
	 */
	private void notifyCondition(Condition condition, AtomicBoolean predicate) {
		predicate.set(true);

		lock.lock();
		try {
			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	private void waitOnCondition(Condition condition, AtomicBoolean predicate) throws InterruptedException {
		lock.lock();

		try {
			while (predicate.get() == false) {
				condition.await();
			}
		} finally {
			lock.unlock();
		}
	}

	public void waitUntilRun() throws InterruptedException {
		waitOnCondition(calledCondition, called);
	}

	public void release() {
		notifyCondition(releasedCondition, released);
	}

	@Override
	public void run() {
		// Notify waiting thread
		notifyCondition(calledCondition, called);

		// Wait to be notified to continue
		try {
			waitOnCondition(releasedCondition, released);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Waiting thread interrupted");
		}
	}

}
