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
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Normalize the data of a detector by a calibration channel 
 */
public class DetectorResponse extends ReductionDetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(DetectorResponse.class);

	private FloatDataset response;

	public FloatDataset getResponse() {
		return response;
	}

	public void setResponse(Dataset response) {
		this.response = (FloatDataset) response.cast(Dataset.FLOAT32);
	}

	public DetectorResponse(String name, String key) {
		super(name, key);
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		if (response == null) {
			return;
		}

		try {
			NexusGroupData parentngd = nxdata.getData(key, "data", NexusExtractor.SDSClassName);

			if (parentngd.dimensions.length != response.getShape().length + 1) {
				throw new DeviceException("response of wrong dimensionality");
			}

			uk.ac.diamond.scisoft.ncd.core.DetectorResponse dr = new uk.ac.diamond.scisoft.ncd.core.DetectorResponse();
			dr.setResponse(response);
			
			float[] mydata = dr.process(parentngd.getBuffer(), frames, parentngd.dimensions);
			NexusGroupData myngd = new NexusGroupData(parentngd.dimensions, NexusFile.NX_FLOAT32, mydata);
			nxdata.addData(getName(), myngd, "1", 1);
			addQAxis(nxdata, parentngd.dimensions.length);

			addMetadata(nxdata);
		} catch (Exception e) {
			logger.error("exception caugth reducing data", e);
		}
	}
}