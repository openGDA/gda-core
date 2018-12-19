/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This detector handles the special case of the times detector that does not need to be started/stopped and records its
 * data into other detector's sections.
 */
@ServiceInterface(INcdSubDetector.class)
public class NcdTimesDetector extends NcdWireDetector {

	private static final Logger logger = LoggerFactory.getLogger(NcdTimesDetector.class);

	private static Map<String, Integer> timeinfo = new HashMap<String, Integer>();

	static {
		timeinfo.put("wait_time", 0);
		timeinfo.put("count_time", 1);
	}

	@Override
	public void start() throws DeviceException {
		// no need for action
	}

	@Override
	public void stop() throws DeviceException {
		// no need for action
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		int[] dims = memory.getDimension();
		if (dims[0] != 8 || dims[1] != 1) {
			logger.warn("Dont know how to extract times data from given memory, not writing times information!");
			return;
		}

		for (String info : timeinfo.keySet()) {
			int where = timeinfo.get(info);
			NexusGroupData data_sds = new NexusGroupData(readFloat(0, where, 0, 1, 1, frames));
			NexusTreeNode data = new NexusTreeNode(info, NexusExtractor.SDSClassName, null, data_sds);
			data.setIsPointDependent(true);
			data.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, data,
					new NexusGroupData("s")));

			for (INexusTree branch : nxdata.getNexusTree()) {
				if (branch.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
					branch.addChildNode(data);
				}
			}
		}

		// record as stand alone detector as well for dead time correction and BSL back conversion
		// TODO check with William if we have to transpose the data ourselves
		
		float[] raw = readFloat(0, 0, 0, 1, 8, frames);
		float[] cooked = new float[frames*8];
		int i = 0;
		for (int chan = 0; chan < 8; chan++) {
			for (int frame = 0; frame < frames; frame++) {
				cooked[frame*8+chan] = raw[i];
				i++;
			}
		}
		
		NexusGroupData ngd = new NexusGroupData(new int[] { frames, 8 }, cooked);
		ngd.isDetectorEntryData = false;
		nxdata.addData(getName(), ngd, "s", 1);

		addMetadata(nxdata);
	}
}