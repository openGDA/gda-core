/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.beamline.health;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import gda.factory.FactoryException;

public class ServerConditionTest {

	private static final String SERVER_NAME = "Test server";

	private ServerConditionForTest serverCondition;

	@Before
	public void setUp() throws FactoryException {
		serverCondition = new ServerConditionForTest();
		serverCondition.configure();
	}

	@Test
	public void testDefaultErrorMessage() {
		assertEquals(SERVER_NAME + " is not available", serverCondition.getErrorMessage());
	}

	@Test
	public void testGetCurrentState() {
		serverCondition.setRunning(true);
		assertEquals("Running", serverCondition.getCurrentState());

		serverCondition.setRunning(false);
		assertEquals("Not running", serverCondition.getCurrentState());
	}

	@Test
	public void testGetHealthState() {
		// Server running
		serverCondition.setRunning(true);
		assertEquals(BeamlineHealthState.OK, serverCondition.getHealthState());

		// Server not running, but not critical
		serverCondition.setRunning(false);
		serverCondition.setCritical(false);
		assertEquals(BeamlineHealthState.WARNING, serverCondition.getHealthState());

		// Server not running and is critical
		serverCondition.setCritical(true);
		assertEquals(BeamlineHealthState.ERROR, serverCondition.getHealthState());
	}

	private static class ServerConditionForTest extends ServerCondition {

		private boolean running;

		public ServerConditionForTest() {
			setDescription(SERVER_NAME);
		}

		public void setRunning(boolean running) {
			this.running = running;
		}

		@Override
		protected boolean isRunning() {
			return running;
		}
	}
}
