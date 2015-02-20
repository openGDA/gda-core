/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.nxdata;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

import org.nexusformat.NexusFile;

public class NXDetectorDataDoubleArrayAppender implements NXDetectorDataAppender {

	private double[] array;
	private String units;

	public NXDetectorDataDoubleArrayAppender(double[] array, String units) {
		this.array = array;
		this.units = units;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		try {
			data.addData(detectorName, "data", new int[]{this.array.length}, NexusFile.NX_FLOAT64, this.array, units, 1);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

}
