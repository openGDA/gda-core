/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.impl.NDPluginBaseImpl;

/**
 * This is a kludge for https://trac.diamond.ac.uk/beam/ticket/8210
 * Basically the data on array plugin gets set to the wrong array size,
 * although the area detector has PVs with the right array size
 * This is an off by one rounding problem
 */
public class FakeNDPluginBaseForSwept extends NDPluginBaseImpl {
	static final Logger logger = LoggerFactory.getLogger(FakeNDPluginBaseForSwept.class);

	VGScientaAnalyser analyser;

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	@Override
	public int getArraySize0_RBV() throws Exception {
		int arraySize0 = super.getArraySize0_RBV();
		if (analyser != null) {
			if (!analyser.isFixedMode()) {
				int sweeptSteps = analyser.getNumberOfSweeptSteps();
				if (Math.abs(arraySize0-sweeptSteps) == 1) {
					// so it is off by one
					logger.info(String.format("this class has been a good class and fixed the off by one error in EPICS for you (array width was: %d corrected to: %d)", arraySize0, sweeptSteps));
					arraySize0 = sweeptSteps;
				}
			}
		}
		return arraySize0;
	}

	@Override
	public int getArraySize1_RBV() throws Exception {
		return super.getArraySize1_RBV();
	}

	@Override
	public int getArraySize2_RBV() throws Exception {
		return super.getArraySize2_RBV();
	}

}
