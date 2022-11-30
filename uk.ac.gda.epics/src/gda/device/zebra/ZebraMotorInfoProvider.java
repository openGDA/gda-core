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

package gda.device.zebra;

import gda.device.IScannableMotor;

/**
 * Interface used to get information between to setup a Zebra
 * to perform a flyscan
 */
public interface ZebraMotorInfoProvider {

	double distanceToAccToVelocity(double requiredSpeed);

	/**
	 *
	 * @return index of Posn Trig PV of Zebra for this motor Enc1 = 0
	 */
	int getPcEnc();

	/**
	 *
	 * @return The actual ScannableMotor that will be moved during the scan
	 *
	 */
	IScannableMotor getActualScannableMotor();

	/**
	 * @return True if getExposureStep represents the size of the step of the motor to move during exposure.
	 */
	boolean isExposureStepDefined();

	/**
	 *
	 * @return The size of the step of the motor to move during exposure
	 */
	double getExposureStep();

}
