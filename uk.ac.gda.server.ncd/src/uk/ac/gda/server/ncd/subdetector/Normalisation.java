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

import gda.data.nexus.NexusGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

/**
 * Normalize the data of a detector by a calibration channel 
 */
public class Normalisation extends ReductionDetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(Normalisation.class);

	private double normvalue = 1;
	private String calibName;
	private int calibChannel = 1;

	public Normalisation(String name, String key) {
		super(name, key);
	}

	public double getNormvalue() {
		return normvalue;
	}

	public void setNormvalue(double normvalue) {
		this.normvalue = normvalue;
	}

	public String getCalibName() {
		return calibName;
	}

	public void setCalibName(String calibName) {
		this.calibName = calibName;
	}

	public int getCalibChannel() {
		return calibChannel;
	}

	public void setCalibChannel(int calibChannel) {
		this.calibChannel = calibChannel;
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		if (calibName == null) {
			logger.error(getName()+": no calibration source set up");
			return;
		}

		try {
			NexusGroupData parentngd = nxdata.getData(key, "data", NexusExtractor.SDSClassName);
			NexusGroupData calibngd = nxdata.getData(calibName, "data", NexusExtractor.SDSClassName);

			if (calibngd.dimensions.length != 2) {
				throw new DeviceException("calibration of wrong dimensionality");
			}

			uk.ac.diamond.scisoft.ncd.core.Normalisation nm = new uk.ac.diamond.scisoft.ncd.core.Normalisation();
			nm.setCalibChannel(calibChannel);
			nm.setNormvalue(normvalue);
			float[] mydata = nm.process(parentngd.getBuffer(), calibngd.getBuffer(), frames, parentngd.dimensions, calibngd.dimensions);
			NexusGroupData myngd = new NexusGroupData(parentngd.dimensions, NexusGlobals.NX_FLOAT32, mydata);
			myngd.isDetectorEntryData = true;
			nxdata.addData(getName(), myngd, "1", 1);
			addQAxis(nxdata, parentngd.dimensions.length);

			addMetadata(nxdata);
		} catch (Exception e) {
			logger.error("exception caught reducing data", e);
		}
	}
}