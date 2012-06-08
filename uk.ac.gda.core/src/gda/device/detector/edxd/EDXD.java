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

package gda.device.detector.edxd;

import gda.device.DeviceException;
import gda.device.detector.DetectorBase;

/**
 * EDXD detectors front end class, this will need to communicate mainly with EPICS, but may also require some external
 * commands.
 */
public class EDXD extends DetectorBase {

	/**
	 * Needs to drive EPICS
	 */
	@Override
	public void collectData() throws DeviceException {
		// TODO Auto-generated method stub

	}

	/**
	 * Should get this from EPICS
	 */
	@Override
	public int getStatus() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * May need to be a filename sometimes, or other modes may return the values.
	 * 
	 * @return The filename of the output file, or the real data
	 * @throws DeviceException
	 */
	@Override
	public Object readout() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return true or false depending on the mode of operation
	 * @throws DeviceException
	 */
	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return "EDXD 23 Element Cambera Detector"
	 * @throws DeviceException
	 */
	@Override
	public String getDescription() throws DeviceException {
		return "EDXD 23 Element Cambera Detector";
	}

	/**
	 * @return part number from hardware or simulation
	 * @throws DeviceException
	 */
	@Override
	public String getDetectorID() throws DeviceException {
		// TODO Get this from the hardware
		return "ID";
	}

	/**
	 * @return "MCA"
	 * @throws DeviceException
	 */
	@Override
	public String getDetectorType() throws DeviceException {
		return "MCA";
	}

}
