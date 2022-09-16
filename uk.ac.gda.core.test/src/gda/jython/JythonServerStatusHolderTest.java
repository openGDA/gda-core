/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.jython;

import static gda.jython.JythonStatus.IDLE;
import static gda.jython.JythonStatus.RUNNING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JythonServerStatusHolderTest {

	private static final String SCRIPT_NAME = "test_script";

	static class JythonServerStatusEventCollector implements IJythonServerStatusObserver {

		private final List<JythonServerStatus> events = new ArrayList<>();

		public JythonServerStatusEventCollector(JythonServerStatusHolder holder) {
			holder.addObserver(this);
		}

		@Override
		public void update(Object source, Object arg) {
			if (arg instanceof JythonServerStatus) {
				JythonServerStatus status = (JythonServerStatus) arg;
				events.add(status);
			}
		}

		public List<JythonServerStatus> getEvents() {
			return events;
		}
	}

	private JythonServer server;
	private JythonServerStatusHolder holder;
	private JythonServerStatusEventCollector collector;

	@BeforeEach
	public void before() {
		server = new JythonServer();
		holder = new JythonServerStatusHolder(server);
		collector = new JythonServerStatusEventCollector(holder);
	}

	@Test
	public void testOneEventWhenScriptIsExecuted() {
		assertTrue(holder.tryAcquireScriptLock(SCRIPT_NAME));
		assertEquals(1, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, IDLE, SCRIPT_NAME), collector.events.get(0));
	}

	@Test
	public void testTwoEventsWhenScriptIsExecutedAndFinishes() {
		assertTrue(holder.tryAcquireScriptLock(SCRIPT_NAME));
		assertEquals(1, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, IDLE, SCRIPT_NAME), collector.events.get(0));
		holder.releaseScriptLock();
		assertEquals(2, collector.events.size());
		assertEquals(new JythonServerStatus(IDLE, IDLE), collector.events.get(1));
	}

	@Test
	public void testTwoEventsWhenScriptIsExecutedAndThenScanStarts() {
		assertTrue(holder.tryAcquireScriptLock(SCRIPT_NAME));
		assertEquals(1, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, IDLE, SCRIPT_NAME), collector.events.get(0));
		holder.updateScanStatus(RUNNING);
		assertEquals(2, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, RUNNING, SCRIPT_NAME), collector.events.get(1));
	}

	@Test
	public void testOneEventWhenScriptIsExecutedAndThenSynchronousCommandRuns() {
		assertTrue(holder.tryAcquireScriptLock(SCRIPT_NAME));
		assertEquals(1, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, IDLE, SCRIPT_NAME), collector.events.get(0));
		holder.startRunningCommandSynchronously();
		assertEquals(1, collector.events.size());
	}

	@Test
	public void testTwoEventsWhenScriptIsExecutedAndThenSynchronousCommandRunsAndThenBothFinish() {
		assertTrue(holder.tryAcquireScriptLock(SCRIPT_NAME));
		assertEquals(1, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, IDLE, SCRIPT_NAME), collector.events.get(0));
		holder.startRunningCommandSynchronously();
		assertEquals(1, collector.events.size());
		holder.releaseScriptLock();
		holder.finishRunningCommandSynchronously();
		assertEquals(2, collector.events.size());
		assertEquals(new JythonServerStatus(IDLE, IDLE), collector.events.get(1));
	}

	@Test
	public void testCannotStartTwoConcurrentScripts() {
		assertTrue(holder.tryAcquireScriptLock(SCRIPT_NAME));
		assertFalse(holder.tryAcquireScriptLock(SCRIPT_NAME));
		assertEquals(1, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, IDLE, SCRIPT_NAME), collector.events.get(0));
	}

	@Test
	public void testOneEventWhenMultipleSynchronousCommandsStart() {
		holder.startRunningCommandSynchronously();
		assertEquals(1, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, IDLE), collector.events.get(0));
		holder.startRunningCommandSynchronously();
		assertEquals(1, collector.events.size());
	}

	@Test
	public void testTwoEventsWhenSynchronousCommandStartsAndFinishes() {
		holder.startRunningCommandSynchronously();
		assertEquals(1, collector.events.size());
		assertEquals(new JythonServerStatus(RUNNING, IDLE), collector.events.get(0));
		holder.finishRunningCommandSynchronously();
		assertEquals(2, collector.events.size());
		assertEquals(new JythonServerStatus(IDLE, IDLE), collector.events.get(1));
	}

	@Test
	public void testScriptNameIsReturnedInStatus() {
		holder.tryAcquireScriptLock(SCRIPT_NAME);
		try {
			assertThat(holder.getServerStatus(), is(new JythonServerStatus(RUNNING, IDLE, SCRIPT_NAME)));
		} finally {
			holder.releaseScriptLock();
		}
	}
}
