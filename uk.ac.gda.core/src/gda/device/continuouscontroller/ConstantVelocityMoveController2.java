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

package gda.device.continuouscontroller;

import gda.device.DeviceException;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.scannable.ContinuouslyScannableViaController;

import java.util.Collection;

/**
 * Extension of ConstantVelocityMoveController that requires knowledge of 
 * scannables and detectors
 * 
 * The start, and stop values are the values around which the exposures
 * are to take place irrespective of whether the detector is integration or not
 * 
 */

public interface ConstantVelocityMoveController2 extends ConstantVelocityMoveController{

	void setScannableToMove(Collection<ContinuouslyScannableViaController> scannablesToMove);

	void setDetectors(Collection<HardwareTriggeredDetector> detectors) throws DeviceException;

	int getPointBeingPrepared();
	
	void resetPointBeingPrepared();
}
