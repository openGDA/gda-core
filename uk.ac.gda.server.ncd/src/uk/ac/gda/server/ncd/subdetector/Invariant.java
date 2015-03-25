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

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import gda.data.nexus.NexusGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * calculates the total intensity in each frame
 */
public class Invariant extends ReductionDetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(Invariant.class);

	public Invariant(String name, String key) {
		super(name, key);
	}
	
	@Override
	public void setqAxis(IDataset qAxis) {
		// Ignore qAxis setting for Invariant subdetector
		this.qAxis = null;
	}

	@Override
	public Dataset getqAxis() {
		return null;
	}


	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		try {
			NexusGroupData parentngd = nxdata.getData(key, "data", NexusExtractor.SDSClassName);
			if (parentngd == null) return;
			uk.ac.diamond.scisoft.ncd.core.Invariant inv = new uk.ac.diamond.scisoft.ncd.core.Invariant();
			float[] mydata = inv.process(parentngd.getBuffer(), parentngd.dimensions);
			NexusGroupData myngd = new NexusGroupData(mydata);
			myngd.isDetectorEntryData = true;
			nxdata.addData(getName(), myngd, "1", 1);
			addMetadata(nxdata);
		} catch (Exception e) {
			logger.error("exception caugth reducing data", e);
		}
	}
	
}