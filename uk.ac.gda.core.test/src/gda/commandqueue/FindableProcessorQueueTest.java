/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.commandqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import gda.observable.IObserver;

public class FindableProcessorQueueTest {

	private static final long MAX_TIMEOUT_MS = 100;

	private Processor processor;
	private Queue queue;
	private boolean completed;
	private boolean paused;

	class ProcessorObserver implements IObserver {

		Processor.STATE lastObservedState = Processor.STATE.WAITING_QUEUE;

		@Override
		public void update(Object source, Object arg) {
			if (arg instanceof Processor.STATE) {
				Processor.STATE state = (Processor.STATE) arg;
				if (state == lastObservedState) {
					return;
				} else {
					lastObservedState = state;
				}

				if (state == Processor.STATE.WAITING_QUEUE) {
					completed = true;
				}
				if (state == Processor.STATE.WAITING_START) {
					paused = true;
				}
			}
		}

	}

	@Before
	public void setUp() throws Exception {
		completed = false;
		paused = false;
		FindableProcessorQueue simpleProcessor = new FindableProcessorQueue();
		simpleProcessor.setStartImmediately(false);
		simpleProcessor.afterPropertiesSet();
		queue = new CommandQueue();
		simpleProcessor.setQueue(queue);
		processor = simpleProcessor;

		ProcessorObserver monitor = new ProcessorObserver();
		processor.addIObserver(monitor);
	}

	private void startProcessor() throws Exception {
		completed = false;
		paused = false;
		processor.start(MAX_TIMEOUT_MS);
	}

	private void waitForProcessor(long maxWait) throws Exception {
		long waited = 0;
		long sleepTime = 10;
		while (!completed && !paused && waited < maxWait) {
			Thread.sleep(sleepTime);
			waited += sleepTime;
		}
	}

	private void waitForProcessor() throws Exception {
		waitForProcessor(10000);
	}

	private void runProcessor() throws Exception {
		startProcessor();
		waitForProcessor();
	}

	@Test
	public void testPause() throws Exception {
		TestCommand pauseCommand = new TestCommand();
		pauseCommand.pause = true;
		queue.addToTail(pauseCommand);
		TestCommand normalCommand = new TestCommand(5);
		queue.addToTail(normalCommand);

		runProcessor();
		assertFalse(completed);
		assertTrue(paused);
		assertEquals(Command.STATE.PAUSED, pauseCommand.getState());
		assertEquals(Command.STATE.NOT_STARTED, normalCommand.getState());

		runProcessor();
		assertTrue(completed);
		assertFalse(paused);
		assertEquals(Command.STATE.COMPLETED, pauseCommand.getState());
		assertEquals(Command.STATE.COMPLETED, normalCommand.getState());
	}

	@Test
	public void testException() throws Exception {
		TestCommand throwExceptionCommand = new TestCommand();
		throwExceptionCommand.throwException=true;
		throwExceptionCommand.setDescription("testException");
		queue.addToTail(throwExceptionCommand);
		runProcessor();
		assertTrue(completed);
		assertFalse(paused);
	}


	@Test
	public void testSkip() throws Exception {
		TestCommand skipCommand = new TestCommand();
		skipCommand.skip=true;
		queue.addToTail(skipCommand);
		TestCommand normalCommand = new TestCommand();
		queue.addToTail(normalCommand);
		runProcessor();
		assertFalse(completed);
		assertTrue(paused);
		runProcessor();
		assertTrue(completed);
		assertFalse(paused);
		assertEquals(Command.STATE.ABORTED, skipCommand.getState());
		assertEquals(Command.STATE.COMPLETED, normalCommand.getState());
	}


	@Test
	public void testStartWithQueueEmpty() throws Exception {
		processor.start(MAX_TIMEOUT_MS);
		TestCommand command = new TestCommand(3);
		assertFalse(completed);

		queue.addToTail(command);
		waitForProcessor();

		assertTrue(completed);
		assertEquals(Command.STATE.COMPLETED, command.getState());
	}

	@Test
	public void testStartWithQueueNonEmpty() throws Exception {
		TestCommand command = new TestCommand(1);
		queue.addToTail(command);
		runProcessor();
		assertEquals(Command.STATE.COMPLETED, command.getState());
	}

	@Test
	public void testQueueMultipleItems() throws Exception {
		TestCommand c1 = new TestCommand(1);
		TestCommand c2 = new TestCommand(1);
		TestCommand c3 = new TestCommand(1);
		queue.addToTail(c1);
		queue.addToTail(c2);
		queue.addToTail(c3);
		runProcessor();
		assertEquals(Command.STATE.COMPLETED, c1.getState());
		assertEquals(Command.STATE.COMPLETED, c1.getState());
		assertEquals(Command.STATE.COMPLETED, c1.getState());
	}

	@Test
	public void testStop() throws Exception {
		startProcessor();
		processor.stop(MAX_TIMEOUT_MS);
		waitForProcessor(50);
		assertTrue(paused);
		assertFalse(completed);
	}

}
