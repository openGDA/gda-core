/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.analyser;

import gda.device.Analyser;
import gda.device.DeviceException;

/*
 * An interface to devices that support the EPICS MCA record
 */
public interface IEpicsMCA extends Analyser{
	EpicsMCARegionOfInterest getNthRegionOfInterest(int regionIndex)  throws DeviceException ;
	void setRegionsOfInterest(EpicsMCARegionOfInterest[] epicsMcaRois) throws DeviceException;
	void setCalibration(EpicsMCACalibration calibrate) throws DeviceException;
	public void eraseStartAcquisition() throws DeviceException;
	public double getRoiCount(int index) throws DeviceException;
	public double getRoiNetCount(int index) throws DeviceException;
}
