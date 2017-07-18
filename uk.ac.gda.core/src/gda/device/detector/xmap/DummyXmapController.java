/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.xmap;

import java.util.Random;

import gda.device.DeviceException;
import gda.factory.FactoryException;

/**
 * Dummy Xmap controller for use when GDA is writing data
 */
public class DummyXmapController extends DummyXmapControllerBase {

	@Override
	public void configure() throws FactoryException {
		// no specific configuration required
		configured = true;
	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		final int numberOfBins = getNumberOfBins();
		final int[] dummyData = new int[numberOfBins];
		final Random generator = new Random();

		for (int i = 0; i < numberOfBins; i++) {
			// say that for this simulation cannot count more than 10MHz
			dummyData[i] = generator.nextInt((int) (getRealTime()) * 10000);
		}
		return dummyData;
	}

	@Override
	public int[][] getData() throws DeviceException {
		final int numberOfElements = getNumberOfElements();
		final int numberOfBins = getNumberOfBins();
		final int[][] data = new int[numberOfElements][numberOfBins];

		for (int i = 0; i < numberOfElements; i++)
			data[i] = getData(i);
		return data;
	}
}
