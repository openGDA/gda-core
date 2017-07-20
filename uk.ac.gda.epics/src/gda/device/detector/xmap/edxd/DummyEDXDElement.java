/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.xmap.edxd;

import gda.device.DeviceException;

public class DummyEDXDElement implements IEDXDElement {

	double[] lowROIs;
	double[] highROIs;

	@Override
	public double[] getLowROIs() throws DeviceException {
		return lowROIs;
	}

	@Override
	public void setLowROIs(double[] roiLow) throws DeviceException {
		this.lowROIs = roiLow;
	}

	@Override
	public double[] getHighROIs() throws DeviceException {
		return highROIs;
	}

	@Override
	public void setHighROIs(double[] roiHigh) throws DeviceException {
		this.highROIs = roiHigh;
	}

}
