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

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * An {@link Answer} for use with tests that use Mockito, that does not complete
 * until its {@link #resume()} method is called (from another thread). Additionally another
 * thread can wait until the run method is called.
 *
 * @param <T>
 */
public class WaitingAnswer<T> implements Answer<T> {

	private final WaitingRunnable waitResume = new WaitingRunnable();

	private final T returnValue;

	public WaitingAnswer(T returnValue) {
		this.returnValue = returnValue;
	}

	@Override
	public T answer(InvocationOnMock invocation) throws Throwable {
		waitResume.run();
		return returnValue;
	}

	public void waitUntilCalled() throws InterruptedException {
		waitResume.waitUntilRun();
	}

	public void resume() {
		waitResume.release();
	}

}