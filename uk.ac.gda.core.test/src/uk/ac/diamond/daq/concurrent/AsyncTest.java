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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
public class AsyncTest {
	@Mock
	private Runnable runnable;
	@Mock
	private Callable<String> callable;

	@Test
	public void testWrappingNullRunnable() {
		assertThrows(NullPointerException.class, () -> new Async.ThreadNamingRunnableWrapper("Thread Name", null));
	}

	@Test
	public void testWrappingNullCallable() {
		assertThrows(NullPointerException.class, () -> new Async.ThreadNamingCallableWrapper<>("Thread Name", null));
	}

	@Test
	public void testWrappingRunnableNullName() {
		assertThrows(NullPointerException.class, () -> new Async.ThreadNamingRunnableWrapper(null, runnable));
	}

	@Test
	public void testWrappingCallableNullName() {
		assertThrows(NullPointerException.class, () -> new Async.ThreadNamingCallableWrapper<>(null, callable));
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

	@Test
	public void testExecuteNullTask() throws Exception {
		assertThrows(NullPointerException.class, () -> Async.execute(null));
	}

	@Test
	public void testScheduleNullCallable() {
		assertThrows(NullPointerException.class, () -> Async.schedule((Runnable) null, 1, SECONDS));
	}

	@Test
	public void testScheduleNullRunnable() {
		assertThrows(NullPointerException.class, () -> Async.schedule((Callable<?>) null, 1, SECONDS));
	}

	@Test
	public void testScheduleNullAtFixedRate() {
		assertThrows(NullPointerException.class, () -> Async.scheduleAtFixedRate(null, 1, 1, SECONDS));
	}

	@Test
	public void testScheduleNullWithFixedDelay() {
		assertThrows(NullPointerException.class, () -> Async.scheduleWithFixedDelay(null, 1, 1, SECONDS));
	}

	@Test
	public void testSubmitNullCallable() {
		assertThrows(NullPointerException.class, () -> Async.submit((Callable<?>) null));
	}

	@Test
	public void testSubmitNullRunnable() {
		assertThrows(NullPointerException.class, () -> Async.submit((Runnable) null));
	}

}
