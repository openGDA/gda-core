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

package org.eclipse.scanning.sequencer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.LevelRole;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelRunnerTest {

	private static Logger logger = LoggerFactory.getLogger(LevelRunnerTest.class);

	private ScannableForTest level1a;
	private ScannableForTest level1b;
	private ScannableForTest level2a;
	private ScannableForTest level2b;
	private ScannableForTest level5a;
	private ScannableForTest level5b;

	private INameable scan;
	private IPosition position;
	private IPositionListener listener;

	private LevelRunnerForTest<ILevel> runner;

	@Before
	public void setUp() throws Exception {
		level1a = new ScannableForTest(1, "level1a", 11);
		level1b = new ScannableForTest(1, "level1b", 12);
		level2a = new ScannableForTest(2, "level2a", 21);
		level2b = new ScannableForTest(2, "level2b", 22);
		level5a = new ScannableForTest(5, "level5a", 51);
		level5b = new ScannableForTest(5, "level5b", 52);

		scan = mock(INameable.class);
		when(scan.getName()).thenReturn("Test solstice scan");

		listener = mock(IPositionListener.class);
		when(listener.positionWillPerform(any(PositionEvent.class))).thenReturn(true);

		position = mock(IPosition.class);

		runner = new LevelRunnerForTest<>(scan);
		runner.addPositionListener(listener);
	}

	@Test
	public void testRunOneLevel() throws Exception {
		runner.setDevices(asList(level5a, level5b));
		runner.run(position);

		// We expect the run() method to...

		// ...signal start of scan
		final ArgumentCaptor<PositionEvent> positionWillPerformCaptor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(listener).positionWillPerform(positionWillPerformCaptor.capture());

		final PositionEvent willPerform = positionWillPerformCaptor.getValue();
		assertEquals(0, willPerform.getLevel());
		assertNull(willPerform.getLevelObjects());
		assertEquals(position, willPerform.getPosition());
		assertEquals(scan, willPerform.getDevice());

		// ...create a task for each scannable
		final List<TestTask> tasksCreated = runner.getTasksCreated();
		assertEquals(level5a, tasksCreated.get(0).levelObject);
		assertEquals(position, tasksCreated.get(0).position);
		assertEquals(level5b, tasksCreated.get(1).levelObject);
		assertEquals(position, tasksCreated.get(1).position);

		// ...run each task
		final List<ILevel> objectsRun = runner.getTasksRun().stream().map(task -> task.levelObject).collect(toList());
		assertTrue(objectsRun.contains(level5a));
		assertTrue(objectsRun.contains(level5b));

		// ...signal level finished
		final ArgumentCaptor<PositionEvent> levelPerformedCaptor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(listener).levelPerformed(levelPerformedCaptor.capture());

		final PositionEvent levelPerformed = levelPerformedCaptor.getValue();
		assertEquals(5, levelPerformed.getLevel());
		assertEquals(position, levelPerformed.getPosition());
		assertEquals(scan, levelPerformed.getDevice());

		final List<? extends ILevel> levelObjects = levelPerformed.getLevelObjects();
		assertEquals(2, levelObjects.size());
		assertTrue(levelObjects.contains(level5a));
		assertTrue(levelObjects.contains(level5b));

		// ...signal position finished
		final ArgumentCaptor<PositionEvent> positionPerformedCaptor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(listener).positionPerformed(positionPerformedCaptor.capture());

		final PositionEvent positionPerformed = positionPerformedCaptor.getValue();
		assertEquals(0, positionPerformed.getLevel());
		assertNull(positionPerformed.getLevelObjects());
		assertEquals(position, positionPerformed.getPosition());
		assertEquals(scan, positionPerformed.getDevice());
	}

	@Test
	public void testRunMultipleLevels() throws Exception {
		runner.setDevices(asList(level5a, level2a, level5b, level1a, level1b, level2b));
		runner.run(position);

		// Check for start of scan
		final ArgumentCaptor<PositionEvent> positionWillPerformCaptor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(listener).positionWillPerform(positionWillPerformCaptor.capture());
		assertEquals(position, positionWillPerformCaptor.getValue().getPosition());

		// Tasks will be ordered by level and thereafter by the order in setDevices()
		final List<TestTask> tasksCreated = runner.getTasksCreated();
		assertEquals(level1a, tasksCreated.get(0).levelObject);
		assertEquals(level1b, tasksCreated.get(1).levelObject);
		assertEquals(level2a, tasksCreated.get(2).levelObject);
		assertEquals(level2b, tasksCreated.get(3).levelObject);
		assertEquals(level5a, tasksCreated.get(4).levelObject);
		assertEquals(level5b, tasksCreated.get(5).levelObject);

		// Tasks will be run by level
		final List<ILevel> objectsRun = runner.getTasksRun().stream().map(task -> task.levelObject).collect(toList());
		assertEquals(1, objectsRun.get(0).getLevel());
		assertEquals(1, objectsRun.get(1).getLevel());
		assertEquals(2, objectsRun.get(2).getLevel());
		assertEquals(2, objectsRun.get(3).getLevel());
		assertEquals(5, objectsRun.get(4).getLevel());
		assertEquals(5, objectsRun.get(5).getLevel());

		// "Level finished" events should be in order
		final ArgumentCaptor<PositionEvent> levelPerformedCaptor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(listener, times(3)).levelPerformed(levelPerformedCaptor.capture());
		final List<PositionEvent> levelPerformed = levelPerformedCaptor.getAllValues();
		assertEquals(1, levelPerformed.get(0).getLevel());
		assertEquals(2, levelPerformed.get(1).getLevel());
		assertEquals(5, levelPerformed.get(2).getLevel());

		// Position finished
		final ArgumentCaptor<PositionEvent> positionPerformedCaptor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(listener).positionPerformed(positionPerformedCaptor.capture());
		assertEquals(position, positionPerformedCaptor.getValue().getPosition());
	}

	@Test
	public void testRunNoBlocking() throws Exception {
		runner.setDevices(asList(level5a, level5b));
		runner.run(position, false);

		// We expect the same set-up behaviour as a blocking call
		verify(listener).positionWillPerform(any(PositionEvent.class));

		final List<TestTask> tasksCreated = runner.getTasksCreated();
		assertEquals(level5a, tasksCreated.get(0).levelObject);
		assertEquals(level5b, tasksCreated.get(1).levelObject);

		// But we have to wait for tasks to finish
		runner.await();

		final List<ILevel> objectsRun = runner.getTasksRun().stream().map(task -> task.levelObject).collect(toList());
		assertTrue(objectsRun.contains(level5a));
		assertTrue(objectsRun.contains(level5b));

		// and we don't get a "level performed" event
		verify(listener, never()).levelPerformed(any(PositionEvent.class));

		// but we do get "position performed"
		final ArgumentCaptor<PositionEvent> positionPerformedCaptor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(listener).positionPerformed(positionPerformedCaptor.capture());
		assertEquals(position, positionPerformedCaptor.getValue().getPosition());
	}

	@Test
	public void testRemovePositionListener() throws Exception {
		runner.setDevices(asList(level5a, level5b));
		runner.removePositionListener(listener);
		runner.run(position);
		verifyZeroInteractions(listener);
	}

	@Test(expected = ScanningException.class)
	public void testExceptionHandling() throws Exception {
		runner.setThrowsException(true);
		runner.setDevices(asList(level5a));
		runner.run(position);
	}

	/*
	 * Test behaviour when a timeout occurs.
	 *
	 * LevelRunner creates a Callable task for each device and submits them to the ForkJoinPool with a specified
	 * timeout, i.e. it is the ForkJoinPool that enforces the timeout.
	 *
	 * The following test behaviour for different combinations of delay and timeout.
	 */
	@Test(expected = ScanningException.class)
	public void testTimeout1Second() throws Exception {
		testDelayTimeout(3, 1);
	}

	@Test(expected = ScanningException.class)
	public void testTimeout2Seconds() throws Exception {
		testDelayTimeout(3, 2);
	}

	@Test
	public void testTimeout4Seconds() throws Exception {
		testDelayTimeout(3, 4);
	}

	private void testDelayTimeout(int delayInSeconds, int timeoutInSeconds) throws Exception {
		logger.debug("Running timeout test for delay: {}s, timeout: {}s", delayInSeconds, timeoutInSeconds);
		level5b.setDelay(delayInSeconds);
		runner.setDevices(asList(level5a, level5b));
		runner.setTimeout(timeoutInSeconds);
		runner.run(position);
	}

	@Test
	public void testTimeoutMessage() {
		// Exception message should contain only the device that has times out
		final String expectedMessage = "The timeout of 1s has been reached waiting for level 5 device(s): ScannableForTest [level=5, name=level5b, value=52.0]";
		level5b.setDelay(2);
		runner.setDevices(asList(level5a, level5b));
		runner.setTimeout(1);
		try {
			runner.run(position);
			fail("Expected to throw an exception due to timeout");
		} catch (ScanningException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	@Test(expected = ScanningException.class)
	public void testRunFailsAfterAbortException() throws Exception {
		runner.setDevices(asList(level5a, level5b));
		runner.abortWithError(scan, position, new ScanningException("Test scan failure"));
		runner.run(position);
	}

	@Test
	public void testResetClearsException() throws Exception {
		runner.setDevices(asList(level5a, level5b));
		runner.abortWithError(scan, position, new ScanningException("Test scan failure"));
		runner.reset();
		runner.run(position);
	}

	@Test
	public void testCreateEmptyRunner() throws Exception {
		final IPosition position = mock(IPosition.class);
		final ILevel levelObject = mock(ILevel.class);

		final LevelRunner<ILevel> emptyRunner = LevelRunner.createEmptyRunner();

		assertTrue(emptyRunner.run(position));
		assertEquals(position, emptyRunner.await());
		assertTrue(emptyRunner.getDevices().isEmpty());
		assertNull(emptyRunner.create(levelObject, position));
		assertNull(emptyRunner.getLevelRole());
	}

	/**
	 * Minimal subclass of {@link LevelRunner} implementing required methods and logging calls to
	 * {@link #create(ILevel, IPosition)} and calls to the tasks created.
	 */
	private static class LevelRunnerForTest<L extends ILevel> extends LevelRunner<L> {

		private Collection<L> devices = Collections.emptyList();

		protected List<TestTask> tasksCreated = new ArrayList<>();
		protected List<TestTask> tasksRun = new ArrayList<>();

		private boolean throwsException = false;

		protected LevelRunnerForTest(INameable device) {
			super(device);
		}

		@Override
		protected Collection<L> getDevices() throws ScanningException {
			return devices;
		}

		@Override
		protected Callable<IPosition> create(ILevel levelObject, IPosition position) throws ScanningException {
			final TestTask task = new TestTask(levelObject, position);

			// Register the create
			tasksCreated.add(task);

			return () -> {
				if (throwsException ) {
					throw new ScanningException("Exception running device");
				}
				// Register the call and delay if set, but we don't need to the Callable to do anything else
				tasksRun.add(task);
				final int delay = ((ScannableForTest) levelObject).getDelay();
				if (delay > 0) {
					Thread.sleep(delay * 1000);
				}
				return null;
			};
		}

		@Override
		protected LevelRole getLevelRole() {
			return LevelRole.MOVE;
		}

		public void setDevices(Collection<L> devices) {
			this.devices = devices;
		}

		public List<TestTask> getTasksCreated() {
			return tasksCreated;
		}

		public List<TestTask> getTasksRun() {
			return tasksRun;
		}

		public void setThrowsException(boolean throwsException) {
			this.throwsException = throwsException;
		}
	}

	/**
	 * Class to hold data corresponding to a task
	 */
	private static class TestTask {
		public final ILevel levelObject;
		public final IPosition position;

		public TestTask(ILevel levelObject, IPosition position) {
			this.levelObject = levelObject;
			this.position = position;
		}

		@Override
		public String toString() {
			return "CreateCall [levelObject=" + levelObject + ", position=" + position + "]";
		}
	}

	/**
	 * Simple implementation of IScannable, with the ability to specify delay time, for testing timeout
	 */
	private static class ScannableForTest implements IScannable<Double> {
		private int level;
		private String name;
		private double value;
		private int delay = 0; // delay time in seconds

		public ScannableForTest(int level, String name, double value) {
			this.level = level;
			this.name = name;
			this.value = value;
		}

		@Override
		public void setLevel(int level) {
			this.level = level;
		}

		@Override
		public int getLevel() {
			return level;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public Double getPosition() throws ScanningException {
			return value;
		}

		@Override
		public Double setPosition(Double value, IPosition position) throws ScanningException {
			this.value = value;
			return value;
		}

		@Override
		public String toString() {
			return "ScannableForTest [level=" + level + ", name=" + name + ", value=" + value + "]";
		}

		public int getDelay() {
			return delay;
		}

		public void setDelay(int delay) {
			this.delay = delay;
		}
	}
}
