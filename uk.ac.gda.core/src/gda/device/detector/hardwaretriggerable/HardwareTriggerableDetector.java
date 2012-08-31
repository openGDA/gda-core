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

package gda.device.detector.hardwaretriggerable;

import gda.device.DeviceException;

/**
 * A HardwareTriggeredDetector that can be configured to switched bewteen hardware triggering and non hardware triggering modes.
 */
public interface HardwareTriggerableDetector extends HardwareTriggeredDetector {

	/**
	 * Configure the Detector to trigger on hardware triggers, or not.
	 * 
	 * @param b
	 * @throws DeviceException
	 */
	public void setHardwareTriggering(boolean b) throws DeviceException;

	/**
	 * @return true if configured to triger on hardware triggers.
	 */
	public boolean isHardwareTriggering();

}
