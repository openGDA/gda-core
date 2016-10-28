/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;

public class ScannableMotorWithDemandPosition extends ScannableMotor {

	private static final Logger logger = LoggerFactory.getLogger(ScannableMotorWithDemandPosition.class);

	private Double lastDemandedInternalPosition;

	@Override
	public void configure() throws FactoryException {
		// First configure the ScannableMotor super class
		super.configure();
		// Check no extra names have been set and only one input name
		if (extraNames.length != 0 || inputNames.length != 1) {
			throw new IllegalStateException("Extra or input names have been set. This is not supported");
		}
		// Add a extra name to return the set position
		setExtraNames(new String[] { getName() + "_set" });
		// Check that only one output format is set
		if (outputFormat.length == 1) {
			// Copy the output format to give the same format for both the actual and demand positions
			setOutputFormat(new String[] { getOutputFormat()[0], getOutputFormat()[0] });
		} else if (outputFormat.length != 2) { // This is useful if you want to specify different output formats for the actual and demand positions
			throw new IllegalStateException("Output format should only have 1 or 2 entries");
		}
	}

	@Override
	public Object rawGetDemandPosition() throws DeviceException {
		return lastDemandedInternalPosition;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return new Object[] { getActualPosition(), getDemandPosition() };
	}

	@Override
	public void rawAsynchronousMoveTo(Object internalPosition) throws DeviceException {
		super.rawAsynchronousMoveTo(internalPosition);
		// Save the demanded position assuming the super call didn't throw and the demand was therefore set.
		// This is saved here in addition as the field in scannable motor is private
		lastDemandedInternalPosition = PositionConvertorFunctions.toDouble(internalPosition);
	}

	@Override
	public void setReturnDemandPosition(boolean returnDemandPosition) {
		logger.error("This motor always returns the demand position in addition to the actual position. This flag is not supported and has been set to false");
		throw new UnsupportedOperationException(
				"This motor always returns the demand position in addition to the actual position. This flag is not supported and has been set to false");
	}

}
