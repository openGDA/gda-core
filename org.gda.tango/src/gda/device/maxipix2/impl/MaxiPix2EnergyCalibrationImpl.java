/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.maxipix2.impl;

import gda.device.maxipix2.MaxiPix2EnergyCalibration;

public class MaxiPix2EnergyCalibrationImpl implements MaxiPix2EnergyCalibration {

	double [] val = new double[2];
	
	@Override
	public double getThresholdSetPoint() {
		return val[THRESHOLD_SETPOINT_INDEX];
	}

	@Override
	public void setThresholdSetPoint(double thresholdSetPoint) {
		val[THRESHOLD_SETPOINT_INDEX] = thresholdSetPoint;
	}

	@Override
	public double getThresholdStepSize() {
		return val[THRESHOLD_STEPSIZE_INDEX];
	}

	@Override
	public void setThresholdStepSize(double thresholdStepSize) {
		val[THRESHOLD_STEPSIZE_INDEX] = thresholdStepSize;
	}

}
