/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.DummyBufferedDetector;

import org.junit.Test;

/**
 * test the communication between the DummyContinuousScannable and DummyHistogramDetector designed to work togther in
 * QScans
 */
public class DummyContinuousScannableTest {

	/**
	 * do a simple move and check the detector has been triggered the correct number of times.
	 */
	@Test
	public void testDataCollectionTriggering() {

		ContinuousParameters parameters = new ContinuousParameters();
		parameters.setStartPosition(50);
		parameters.setEndPosition(200);
		parameters.setTotalTime(.05);
		parameters.setNumberDataPoints(50);

		DummyBufferedDetector detector = new DummyBufferedDetector();
		detector.setContinuousMode(true);

		DummyContinuouslyScannable scannable = new DummyContinuouslyScannable();
		scannable.setName("scannable");
		scannable.addObserver(detector);
		scannable.setContinuousParameters(parameters);
		try {
			scannable.prepareForContinuousMove();
			scannable.performContinuousMove();
			scannable.waitWhileBusy();
		} catch (DeviceException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
		try {
			int[][] data = (int[][]) detector.readAllFrames();

			assertEquals(50, data.length);
		} catch (DeviceException e) {
			fail(e.getMessage());
		}

	}

}
