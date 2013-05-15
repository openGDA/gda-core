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

import java.util.Arrays;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * average over first dimension
 */
public class Average extends ReductionDetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(Average.class);

	public Average(String name, String key) {
		super(name, key);
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		try {
			NexusGroupData parentngd = nxdata.getData(key, "data", NexusExtractor.SDSClassName);
			
			uk.ac.diamond.scisoft.ncd.Average average = new uk.ac.diamond.scisoft.ncd.Average();
			
			float[] mydata = average.process(parentngd.getBuffer(), parentngd.dimensions);
			
			int[] imagedim = Arrays.copyOfRange(parentngd.dimensions, 1, parentngd.dimensions.length);
			NexusGroupData myngd = new NexusGroupData(imagedim, NexusFile.NX_FLOAT32, mydata);
			nxdata.addData(getTreeName(), myngd, "1", 1);
			addQAxis(nxdata, parentngd.dimensions.length - 1);

			addMetadata(nxdata);
		} catch (Exception e) {
			logger.error("exception caugth reducing data", e);
		}
	}
	
}