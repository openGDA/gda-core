/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;

class NXDetectorDataArrayAppender implements NXDetectorDataAppender {
	private boolean firstReadoutInScan = true;
	private NexusGroupData arrayData;

	NXDetectorDataArrayAppender(NexusGroupData arrayData2, boolean firstReadoutInScan) {
		arrayData = arrayData2;
		this.firstReadoutInScan = firstReadoutInScan;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		try {
			arrayData.isDetectorEntryData = true;
			data.addData(detectorName, "data", arrayData, null, 1);
			if (firstReadoutInScan) {
				// TODO add sensible axes
				firstReadoutInScan = false;
			}
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
}