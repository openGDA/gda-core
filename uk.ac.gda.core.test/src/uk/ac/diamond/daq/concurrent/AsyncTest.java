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

package uk.ac.diamond.daq.concurrent;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("unused")
public class AsyncTest {
	@Mock
	private Runnable runnable;
	@Mock
	private Callable<String> callable;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected=NullPointerException.class)
	public void testWrappingNullRunnable() {
		new Async.ThreadNamingRunnableWrapper("Thread Name", null);
	}

	@Test(expected=NullPointerException.class)
	public void testWrappingNullCallable() {
		new Async.ThreadNamingCallableWrapper<>("Thread Name", null);
	}

	@Test(expected=NullPointerException.class)
	public void testWrappingRunnableNullName() {
		new Async.ThreadNamingRunnableWrapper(null, runnable);
	}

	@Test(expected=NullPointerException.class)
	public void testWrappingCallableNullName() {
		new Async.ThreadNamingCallableWrapper<>(null, callable);
	}

	@Test
	public void testWrappingRunnableNotRun() throws Exception {
		new Async.ThreadNamingRunnableWrapper("ThreadName", runnable);
		verify(runnable, never()).run();
	}

	@Test
	public void testWrappingCallableNotCalled() throws Exception {
		new Async.ThreadNamingCallableWrapper<>("ThreadName", callable);
		verify(callable, never()).call();
	}

	@Test
	public void testWrappedRunnableRun() throws Exception {
		new Async.ThreadNamingRunnableWrapper("ThreadName", runnable).run();
		verify(runnable).run();
	}

	@Test
	public void testWrappedCallableCalled() throws Exception {
		new Async.ThreadNamingCallableWrapper<>("ThreadName", callable).call();
		verify(callable).call();
	}

	@Test(expected=NullPointerException.class)
	public void testExecuteNullTask() throws Exception {
		Async.execute(null);
	}

	@Test(expected=NullPointerException.class)
	public void testScheduleNullCallable() {
		Async.schedule((Runnable)null, 1, SECONDS);
	}

	@Test(expected=NullPointerException.class)
	public void testScheduleNullRunnable() {
		Async.schedule((Callable<?>)null, 1, SECONDS);
	}

	@Test(expected=NullPointerException.class)
	public void testScheduleNullAtFixedRate() {
		Async.scheduleAtFixedRate(null, 1, 1, SECONDS);
	}

	@Test(expected=NullPointerException.class)
	public void testScheduleNullWithFixedDelay() {
		Async.scheduleWithFixedDelay(null, 1, 1, SECONDS);
	}

	@Test(expected=NullPointerException.class)
	public void testSubmitNullCallable() {
		Async.submit((Callable<?>)null);
	}

	@Test(expected=NullPointerException.class)
	public void testSubmitNullRunnable() {
		Async.submit((Runnable)null);
	}

}
