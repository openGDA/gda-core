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

package gda.device.detector.ccd;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;

public class NexusTangoCcd extends TangoCcd implements  NexusDetector {

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}
	
	@Override
	public String getDescription() throws DeviceException {
		return "nexus ccd";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "NexusTangoCCD";
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NXDetectorData nxdata = new NXDetectorData();
		NexusGroupData ngd;
		byte[] byteData = read();
		int width = (Integer) getAttribute("Width");
		int height = (Integer) getAttribute("Height");
		short depth = (Short) getAttribute("Depth");
		if (byteData != null) {
			ngd = new NexusGroupData(new int[] { width, height }, byteData);
			if (depth == 2) {
				ngd = ngd.asShort();
			} else if (depth != 1) {
				ngd = ngd.asInt();
			}
			ngd.isDetectorEntryData = true;
			nxdata.addData(getName(), ngd, "counts", 1);
		}
		return nxdata;
	}
}
