/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.temperature;

import gda.device.DeviceException;
import gda.device.temperature.DummyTemperature;
import junit.framework.TestCase;

/**
 * Test case for unit testing the temperature simulator.
 */
public class DummyTemperatureTest extends TestCase {

	DummyTemperature dummyTemperature;

	/*
	 * @see TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dummyTemperature = new DummyTemperature();
		dummyTemperature.configure();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Not implemented.
	 */
	public final void testAddRamp() {
	}

	/**
	 * Not implemented.
	 */
	public final void testClearRamps() {
	}

	/**
	 * Not implemented.
	 */
	public final void testGetLowerTemp() {

	}

	/**
	 * Not implemented.
	 */
	public final void testGetTargetTemperature() {
	}

	/**
	 * Not implemented.
	 */
	public final void testGetUpperTemp() {
	}

	/**
	 * Not implemented.
	 */
	public final void testHold() {
	}

	/*
	 * Class under test for void runRamp()
	 */
	/**
	 * Not implemented.
	 */
	public final void testRunRamp() {
	}

	/**
	 * setLowerTemp() test method.
	 * 
	 * @throws Exception
	 */
	public final void testSetLowerTemp() throws Exception {

		double lowerTemp = dummyTemperature.getLowerTemp();

		dummyTemperature.setLowerTemp(lowerTemp + 1);
		dummyTemperature.setLowerTemp(lowerTemp);
		try {
			dummyTemperature.setLowerTemp(lowerTemp - 1);
			fail("Expected exception, 1 degree below limit");
		} catch (DeviceException e) {
		}
	}

	/**
	 * Not implemented.
	 */
	public final void testSetProbe() {
	}

	/**
	 * setUpperTemp() test method.
	 * 
	 * @throws Exception
	 */
	public final void testSetUpperTemp() throws Exception {
		double UpperTemp = dummyTemperature.getUpperTemp();

		dummyTemperature.setUpperTemp(UpperTemp - 1);
		dummyTemperature.setUpperTemp(UpperTemp);
		try {
			dummyTemperature.setUpperTemp(UpperTemp + 1);
			fail("Expected exception, 1 degree below limit");
		} catch (DeviceException e) {
		}
	}

	/**
	 * Not implemented.
	 */
	public final void testSetTargetTemperature() {
	}

	/**
	 * Not implemented.
	 */
	public final void testDoStart() {
	}

	/**
	 * Not implemented.
	 */
	public final void testDoStop() {
	}

	/**
	 * Not implemented.
	 */
	public final void testSendRamp() {
	}

	/**
	 * Not implemented.
	 */
	public final void testStartNextRamp() {
	}

	/**
	 * Not implemented.
	 */
	public final void testStartTowardsTarget() {
	}

	/**
	 * Not implemented.
	 */
	public final void testGetCurrentTemperature() {
	}

	/**
	 * Not implemented.
	 */
	public final void testIsAtTargetTemperature() {
	}

	/**
	 * Not implemented.
	 */
	public final void testRun() {
	}

	/**
	 * Not implemented.
	 */
	public final void testSetAccuracy() {
	}

	/**
	 * Not implemented.
	 */
	public final void testPollDone() {
	}

	/**
	 * Not implemented.
	 */
	public final void testTemperatureBase() {
	}

	/**
	 * Not implemented.
	 */
	public final void testSetPolltime() {
	}

	/**
	 * Not implemented.
	 */
	public final void testGetPolltime() {
	}

	/**
	 * Not implemented.
	 */
	public final void testAlarm() {
	}

	/**
	 * Not implemented.
	 */
	public final void testCancelAlarm() {
	}

	/**
	 * Not implemented.
	 */
	public final void testStartPoller() {
	}

	/**
	 * Not implemented.
	 */
	public final void testSetProbeNames() {
	}

	/**
	 * Not implemented.
	 */
	public final void testSetProbeNames_db() {
	}

	/**
	 * Not implemented.
	 */
	public final void testGetProbeNames() {
	}

	/**
	 * Not implemented.
	 */
	public final void testGetProbeNames_db() {
	}

	/**
	 * Not implemented.
	 */
	public final void testSetRamps() {
	}

	/**
	 * Not implemented.
	 */
	public final void testStart() {
	}

	/**
	 * Not implemented.
	 */
	public final void testStartHoldTimer() {
	}

	/**
	 * Not implemented.
	 */
	public final void testStop() {
	}

	/**
	 * Not implemented.
	 */
	public final void testWaitForTemp() {
	}

	/**
	 * Not implemented.
	 */
	public final void testIsRunning() {
	}

}