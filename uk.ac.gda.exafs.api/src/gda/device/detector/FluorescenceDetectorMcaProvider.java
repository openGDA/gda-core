/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.device.detector;

import gda.device.DeviceException;
import gda.factory.Findable;
import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * Interface for simple class that returns MCA data from a {@link FluorescenceDetector}.
 * The {@link #getMCAData(String, double)} method returns an array of MCA count data, only if a scan/script is not running.
 * This is intended to provided MCA data for the client side {@link FluorescenceDetectorCompositeController} class.
 */
public interface FluorescenceDetectorMcaProvider extends Findable {

	/**
	 * Return MCA data on the named Fluorescence detector by calling {@link FluorescenceDetector#getMCAData(double)}.
	 * If a scan/script is running, an empty array will be returned and no calls will be made to the detector.
	 * @param detector
	 * @param time
	 * @return double[][] of MCA counts
	 * @throws DeviceException
	 */
	double[][] getMCAData(String detectorName, double time) throws DeviceException;

	/**
	 *
	 * @return True if MCA data can be retrieved from a detector (i.e. true if script/scan are not running)
	 */
	boolean canGetMcaData();

}
