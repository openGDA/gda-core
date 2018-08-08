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

import java.util.concurrent.Semaphore;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class WaitingAnswer<T> implements Answer<T> {

	private final T returnValue;

	private final Semaphore semaphore = new Semaphore(1, true); // true to make semaphore fair, see javadoc

	public WaitingAnswer(T returnValue) throws InterruptedException {
		this.returnValue = returnValue;
		semaphore.acquire();
	}

	@Override
	public T answer(InvocationOnMock invocation) throws Throwable {
		semaphore.release(); // Notify waiting thread
		semaphore.acquire(); // Wait to be notified to continue, note this only works because the semaphore is fair
		return returnValue;
	}

	public void waitUntilCalled() throws InterruptedException {
		// wait for answer to be called. This required the semaphore to be fair, see javadoc
		semaphore.acquire();
	}

	public void resume() {
		semaphore.release();
	}
}