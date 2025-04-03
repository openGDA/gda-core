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

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

public class NXDetectorDataDoubleArraysAppender implements NXDetectorDataAppender {
	private String name;
	private double[] array;
	private String units;
	private Integer signalVal;
	private boolean isPointDependent;

	public NXDetectorDataDoubleArraysAppender(String name, double[] array, String units, Integer signalVal, boolean isPointDependent) {
		this.name = name;
		this.array = array;
		this.units = units;
		this.signalVal = signalVal;
		this.isPointDependent = isPointDependent;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		try {
			data.addData(detectorName, name, new NexusGroupData(array), units, signalVal, null, isPointDependent);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

}
