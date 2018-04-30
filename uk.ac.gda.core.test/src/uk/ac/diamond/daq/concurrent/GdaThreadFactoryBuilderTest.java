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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.diamond.daq.concurrent.GdaThreadFactoryBuilder.Threads;

public class GdaThreadFactoryBuilderTest {

	@Mock
	private Runnable task;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testThreadName() {
		ThreadFactory factory = Threads.named("helloWorld").factory();
		Thread thread = factory.newThread(task);
		assertEquals("helloWorld-0", thread.getName());
		thread = factory.newThread(task);
		assertEquals("helloWorld-1", thread.getName());
	}

	@Test
	public void testFormattedName() throws Exception {
		ThreadFactory factory = Threads.named("hello%s", "World").factory();
		Thread thread = factory.newThread(task);
		assertEquals("helloWorld-0", thread.getName());

		thread = factory.newThread(task);
		assertEquals("helloWorld-1", thread.getName());
	}

	@Test
	public void testThreadPriority() throws Exception {
		ThreadFactory factory = Threads.priority(3).factory();
		Thread t = factory.newThread(task);
		assertEquals("Set priority not used when creating threads", 3, t.getPriority());

		factory = Threads.priority(7).factory();
		t = factory.newThread(task);
		assertEquals("Set priority not used when creating threads", 7, t.getPriority());

		factory = Threads.factory();
		t = factory.newThread(task);
		assertEquals("Default priority not used when priority not set", Thread.NORM_PRIORITY, t.getPriority());
	}

	@Test
	public void testThreadDaemonStatus() throws Exception {
		ThreadFactory factory = Threads.daemon().factory();
		Thread t = factory.newThread(task);
		assertEquals("Daemon status not used when creating threads", true, t.isDaemon());

		factory = Threads.user().factory();
		t = factory.newThread(task);
		assertEquals("Daemon status not used when creating threads", false, t.isDaemon());
	}

	@Test
	public void testExceptionHandler() throws Exception {
		UncaughtExceptionHandler handler = mock(UncaughtExceptionHandler.class);
		ThreadFactory factory = Threads.uncaughtExceptionHandler(handler).factory();
		Thread t = factory.newThread(task);
		assertEquals("UncaughtExceptionHandler not used when creating threads",
				handler,
				t.getUncaughtExceptionHandler());

	}

	@Test
	public void testDefaultExceptionHandlerIsUsedByDefault() throws Exception {
		// If no exception handler is set, a default handler is used
		ThreadFactory factory = Threads.factory();
		Thread t = factory.newThread(task);
		assertEquals("Default UncaughtExceptionHandler not used when creating threads",
				GdaThreadFactoryBuilder.DEFAULT_EXCEPTION_HANDLER,
				t.getUncaughtExceptionHandler());
	}

	@Test
	public void testThreadGroup() throws Exception {
		ThreadFactory factory = Threads.group("newGroup").factory();
		Thread t = factory.newThread(task);
		assertEquals("Group name not used when creating threads",
				"newGroup",
				t.getThreadGroup().getName());

		factory = Threads.factory();
		Thread t1 = factory.newThread(task);
		Thread t2 = factory.newThread(task);
		assertEquals("New threads are not created in common group", t1.getThreadGroup(), t2.getThreadGroup());
	}

	@Test
	public void testClassLoader() throws Exception {
		ClassLoader loader = mock(ClassLoader.class);
		ThreadFactory factory = Threads.classLoader(loader).factory();
		Thread t = factory.newThread(task);
		assertEquals("ClassLoader is not used when creating threads",
				loader,
				t.getContextClassLoader());

		// If no class loader is set, the current thread's class loader is used
		factory = Threads.factory();
		t = factory.newThread(task);
		assertEquals("Current Thread's class loader is not used when creating threads",
				Thread.currentThread().getContextClassLoader(),
				t.getContextClassLoader());

	}

}
