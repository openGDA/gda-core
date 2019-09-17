/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import gda.jython.Jython;

public class SingleCommandRunnerTest {

	private static final long TIMEOUT = 200;
	/** Timeout for things that are expected to timeout to minimise delay */
	private static final long SHORT_TIMEOUT = 50;

	private static final String NO_ARG_COMMAND = "no arg template";
	private static final String ONE_ARG_COMMAND = "one arg template ( %s )";
	private static final String TWO_ARG_COMMAND = "two arg template ( %s, %s)";

	@Mock private Jython mockJython;
	private SingleCommandRunner runner;
	private MockExec exec = new MockExec();
	private MockExec execTwo = new MockExec();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		doAnswer(exec).doAnswer(execTwo).when(mockJython).exec(anyString());

		runner = new SingleCommandRunner();
		runner.setCommand(NO_ARG_COMMAND);
		runner.setRunner(mockJython);
	}

	@After
	public void clearup() {
		// ensure Aysnc tasks aren't left running
		exec.complete();
		execTwo.complete();
	}

	@Test
	public void commandShouldBeRun() throws InterruptedException {
		runner.runCommand();
		// Try and minimise wait time without making test unreliable - needed until Async can be made
		// more testable - see DAQ-2409
		boolean started = exec.hasStarted(TIMEOUT, MILLISECONDS);
		assertThat("Command should have been run", started);
		exec.complete();
	}

	@Test
	public void secondRunShouldThrow() throws InterruptedException {
		runner.runCommand();
		try {
			runner.runCommand();
			// Fail explicitly rather than adding expected exception to ensure first run doesn't throw
			fail("Second run command should have thrown");
		} catch (IllegalStateException ise) {
			// this is expected
			assertThat("Second command shouldn't have been run", execTwo.hasStarted(SHORT_TIMEOUT, MILLISECONDS), is(false));
		} finally {
			exec.complete();
		}
	}

	@Test
	public void sharedStateCantBeReplacedWhileRunning() {
		runner.runCommand();
		try {
			runner.setSharedState(new AtomicBoolean());
			// Fail explicitly rather than adding expected exception to ensure first run doesn't throw
			fail("Shared state shouldn't be replaced while command is running");
		} catch (IllegalStateException ise) {
			// required
		} finally {
			exec.complete();
		}
	}

	@Test
	public void sharedStateShouldPreventRunnersRunning() throws Exception {
		AtomicBoolean state = new AtomicBoolean();
		SingleCommandRunner runner2 = new SingleCommandRunner();
		runner2.setCommand(NO_ARG_COMMAND);
		runner2.setRunner(mockJython);

		runner.setSharedState(state);
		runner2.setSharedState(state);

		runner.runCommand();
		try {
			runner2.runCommand();
			fail("Second command shouldn't be run");
		} catch (IllegalStateException ise) {
			// expected
			assertThat("Second command shouldn't have been run", execTwo.hasStarted(SHORT_TIMEOUT, MILLISECONDS), is(false));
		} finally {
			exec.complete();
		}
	}

	@Test
	public void correctCommandIsRun() throws InterruptedException {
		runner.runCommand();
		// not really being tested but needed to ensure command has been set
		assertThat("Command wasn't started",  exec.hasStarted(TIMEOUT, MILLISECONDS));
		exec.complete();
		assertThat("Wrong command was run", exec.getCommand(), is(NO_ARG_COMMAND));
	}

	@Test(expected = NullPointerException.class)
	public void sharedStateCantBeNull() {
		runner.setSharedState(null);
	}

	@Test
	public void commandWithErrorDoesntPreventNextCommandRunning() throws InterruptedException {
		runner.runCommand();
		exec.setError(new Exception("command failed"));
		exec.complete();
		Thread.sleep(SHORT_TIMEOUT);
		runner.runCommand();
		assertThat("Second command should not be affected by failure", execTwo.hasStarted(TIMEOUT, MILLISECONDS));

	}

	/**
	 * Answer implementation to mock a jython command being run.
	 * <br>
	 * Allows caller to check when a command is started and to control when it is complete.
	 */
	private class MockExec implements Answer<Void> {
		private CountDownLatch complete = new CountDownLatch(1);
		private CountDownLatch started = new CountDownLatch(1);
		private Exception e;
		private String command = null;
		@Override
		public Void answer(InvocationOnMock invocation) throws Exception {
			command = invocation.getArgumentAt(0, String.class);
			started.countDown();
			complete.await();
			if (e != null) {
				throw e;
			}
			return null;
		}
		/** Check if this command has started. Returns false if it hasn't started by the end of the timeout */
		public boolean hasStarted(long timeout, TimeUnit units) throws InterruptedException {
			return started.await(timeout, units);
		}
		/** Complete this command execution now */
		public void complete() {
			complete.countDown();
		}
		/** Get the command that was run to create this execution */
		public String getCommand() {
			return command;
		}
		public void setError(Exception e) {
			this.e = e;
		}
	}
}
