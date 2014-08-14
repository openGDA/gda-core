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

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;

public class BackgroundSubtraction extends ReductionDetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(BackgroundSubtraction.class);

	private FloatDataset background;

	public BackgroundSubtraction(String name, String key) {
		super(name, key);
	}

	public void setBackground(Dataset ds) {
		background = (FloatDataset) ds.cast(Dataset.FLOAT32);
	}

	public FloatDataset getBackground() {
		return background;
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		if (background == null) {
			return;
		}

		try {
			NexusGroupData parentngd = nxdata.getData(key, "data", NexusExtractor.SDSClassName);

			if (parentngd == null) {
				logger.error(getName() + ": no detector " + key + " found");
				return;
			}

			uk.ac.diamond.scisoft.ncd.core.BackgroundSubtraction bs = new uk.ac.diamond.scisoft.ncd.core.BackgroundSubtraction();
			bs.setBackground(background);
			float[] mydata = bs.process(parentngd.getBuffer(), parentngd.dimensions);
			NexusGroupData myngd = new NexusGroupData(parentngd.dimensions, NexusFile.NX_FLOAT32, mydata);
			myngd.isDetectorEntryData = true;
			nxdata.addData(getName(), myngd, "1", 1);
			addQAxis(nxdata, parentngd.dimensions.length);

			addMetadata(nxdata);
		} catch (Exception e) {
			logger.error("exception caugth reducing data", e);
		}
	}
	
}