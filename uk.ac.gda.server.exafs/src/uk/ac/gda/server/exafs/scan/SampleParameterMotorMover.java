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

package uk.ac.gda.server.exafs.scan;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.gda.beans.exafs.ISampleParametersWithMotorPositions;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;

/**
 * Static methods to move motors in a List<{@link SampleParameterMotorPosition}> into position.
 * Move is only done if {@link SampleParameterMotorPosition#getDoMove()} returns true.
 * Refactored from B18SampleEnvironmentIterator for use with I20.
 * @since 9/10/2018
 *
 */
public class SampleParameterMotorMover {
	private static final Logger logger = LoggerFactory.getLogger(SampleParameterMotorMover.class);

	private SampleParameterMotorMover() {
	}

	public static void moveMotors(final ISampleParametersWithMotorPositions sampleParams) {
		moveMotors(sampleParams.getSampleParameterMotorPositions());
	}

	/**
	 * Move all the motors in SampleParameterMotorPosition list into demand positions.
	 * Motor moves are synchronous, and take place in the same order as the list.
	 */
	public static void moveMotors(final List<SampleParameterMotorPosition> sampleParameterMotors) {
		logger.info("Moving sample parameter motors into position");
		for(SampleParameterMotorPosition motorPos : sampleParameterMotors) {
			logger.info("scannable name = {}, description = {}, move? = {}", motorPos.getScannableName(), motorPos.getDescription(), motorPos.getDoMove());

			// Don't move motor
			if (!motorPos.getDoMove()) {
				continue;
			}

			Scannable scn = Finder.getInstance().find(motorPos.getScannableName());
			if (scn==null) {
				logger.warn("Unable to find scannable called {}", motorPos.getScannableName());
				continue;
			}

			// Catch the exception, so the other scannables in list can still be processed if one fails.
			try {
				logger.info("Moving scannable {} to position {}", motorPos.getScannableName(), motorPos.getDemandPosition());
				scn.moveTo(motorPos.getDemandPosition());
			} catch (DeviceException e) {
				logger.warn("Problem moving scannable", e);
			}
		}
	}
}
