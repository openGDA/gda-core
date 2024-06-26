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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.TimeUnit;
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

	private static final int DEFAULT_TIMEOUT_SECONDS = 60;

	private final int timeoutSeconds;
	
	private final Lock lock;

	// the predicate and condition to set and notify when run is called
	private final Condition calledCondition;

	// the predicate and condition for to set and notify when release is called
	private final Condition releasedCondition;
	
	// current status of the runnable
	private Status status;
	
	private enum Status {
		IDLE, RUNNING
	}

	public WaitingRunnable() {
		this(DEFAULT_TIMEOUT_SECONDS);
	}
	
	public WaitingRunnable(int timeoutSeconds) {
		super();
		this.timeoutSeconds = timeoutSeconds;
		this.lock = new ReentrantLock();
		this.calledCondition = lock.newCondition();
		this.releasedCondition = lock.newCondition();
		this.status = Status.IDLE;
	}
	
	public void waitUntilRun() throws InterruptedException {
		waitOnCondition(calledCondition, Status.RUNNING);
	}

	public void release() {
		notifyCondition(releasedCondition, Status.IDLE);
	}

	@Override
	public void run() {
		// Notify waiting thread
		notifyCondition(calledCondition, Status.RUNNING);

		// Wait to be notified to continue
		try {
			waitOnCondition(releasedCondition, Status.IDLE);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Waiting thread interrupted");
		}
	}

	/**
	 * A convenience method to set the predicate and notify the condition.
	 * @param condition
	 * @param predicate
	 */
	private void notifyCondition(Condition condition, Status status) {
		lock.lock();
		this.status = status;
		
		try {
			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}
	

	private void waitOnCondition(Condition condition, Status desiredStatus) throws InterruptedException {
		lock.lock();

		try {
			// Preferable to use a timeout than have a hanging test, especially during CI
			while (status != desiredStatus) {
				boolean timedOut = !condition.await(timeoutSeconds, TimeUnit.SECONDS);
				assertThat("Timed out waiting for condition", timedOut, is(false));
			}
		} finally {
			lock.unlock();
		}
	}
}
