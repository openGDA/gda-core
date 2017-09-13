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

package gda.device.trajectoryscan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import gda.device.trajectoryscancontroller.DummyTrajectoryScanController;
import gda.device.trajectoryscancontroller.TrajectoryScanController.ExecuteState;
import gda.device.trajectoryscancontroller.TrajectoryScanController.ExecuteStatus;

public class TrajectoryScanTest {

	private double tolerance = 1e-5;

	<T> void checkArrayEquals(T[] expected, List<T> actual, double tolerance) {
		for(int i=0; i<expected.length; i++) {
			if (expected[i] instanceof Double) {
				assertEquals((double)expected[i], (double)actual.get(i), tolerance);
			} else if (expected[i] instanceof Integer) {
				assertEquals((int)expected[i], (int)actual.get(i), tolerance);
			}
		}
	}

	@Test
	public void testTrajectoryScanBuildProfile() {
		Double[] positions = {1.0, 2.0, 3.0, 4.0, 5.0};
		Double[] times = {0.025, 0.05, 0.06, 0.08, 0.01};
		Integer[] velocityModes = {1, 1, 1, 1, 2};

		DummyTrajectoryScanController trajScan = new DummyTrajectoryScanController();
		trajScan.addPointsToTrajectory(positions, times, velocityModes);

		checkArrayEquals(positions, trajScan.getTrajectoryPositionsList(), tolerance);
		checkArrayEquals(times, trajScan.getTrajectoryTimesList(), tolerance);
		checkArrayEquals(velocityModes, trajScan.getTrajectoryVelocityModesList(), tolerance);
	}

	public DummyTrajectoryScanController getTrajScanForRun() {
		Double[] positions = {1.0, 2.0, 3.0, 4.0, 5.0};
		Double[] times = {0.025, 0.05, 0.03, 0.04, 0.01};
		Integer[] velocityModes = {1, 1, 1, 1, 3};

		DummyTrajectoryScanController trajScan = new DummyTrajectoryScanController();

		for(int i=0; i<10; i++) {
			trajScan.addPointsToTrajectory(positions, times, velocityModes);
		}
		return trajScan;
	}

	@Test
	public void testTrajectoryScanRuns() throws Exception {

		DummyTrajectoryScanController trajScan = getTrajScanForRun();
		trajScan.setBuildProfile();
		trajScan.sendProfileValues();

		if (trajScan.profileBuiltOk()) {
			trajScan.setExecuteProfile();
			while (trajScan.getExecuteState() == ExecuteState.EXECUTING) {
				System.out.println("Waiting for trjactory scan to finish. " + trajScan.getScanPercentComplete() + "% complete.");
				Thread.sleep(1000);
			}
		}
		assertEquals(100, trajScan.getScanPercentComplete());
	}

	@Test
	public void testTrajectoryScanFails() throws Exception {

		DummyTrajectoryScanController trajScan = getTrajScanForRun();
		trajScan.setBuildProfile();
		trajScan.sendProfileValues();

		if (trajScan.profileBuiltOk()) {
			trajScan.setExecuteProfile();
			while (trajScan.getExecuteState() == ExecuteState.EXECUTING) {
				System.out.println("Waiting for trjactory scan to finish. " + trajScan.getScanPercentComplete() + "% complete.");
				Thread.sleep(50);
				trajScan.setExecuteStatus(ExecuteStatus.FAILURE);
			}
		}
		assertTrue(trajScan.getScanPercentComplete()<100);
	}

	@Test
	public void testTrajectoryScanAborts() throws Exception {

		DummyTrajectoryScanController trajScan = getTrajScanForRun();
		trajScan.setBuildProfile();
		trajScan.sendProfileValues();

		if (trajScan.profileBuiltOk()) {
			trajScan.setExecuteProfile();
			while (trajScan.getExecuteState() == ExecuteState.EXECUTING) {
				System.out.println("Waiting for trjactory scan to finish. " + trajScan.getScanPercentComplete() + "% complete.");
				Thread.sleep(500);
				trajScan.setAbortProfile();
			}
		}
		assertTrue(trajScan.getScanPercentComplete()<100);
	}
}