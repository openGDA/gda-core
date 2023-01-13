/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.mythen.data.MythenRawData;
import gda.device.detector.mythen.data.MythenRawDataset;

/**
 * Implementation of MythenClient that communicates with Mythen detector via Epics, using
 * area detector interface. Readback of detector data is through the array view PV rather than
 * the text file produced by Mythen detector.
 * This class is intended for use with Epics implementation that talks to Mythen using socket interface,
 * as used on B18.
 */
public class EpicsMythenClient implements MythenClient, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(EpicsMythenClient.class);

	private ADDetector mythenAreaDetector;
	private int numChannels = 0;
	private int[] rawArrayData;

	public void setAreaDetector(ADDetector detector) {
		mythenAreaDetector = detector;
	}

	public ADDetector getAreaDetector() {
		return mythenAreaDetector;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (mythenAreaDetector == null) {
			throw new IllegalStateException("The area detector has not been set for the Epics Mythen client");
		}
	}

	public void setNumChannels() throws Exception {
		NDPluginBase ndarrayPluginBase = mythenAreaDetector.getNdArray().getPluginBase();
		int dim0 = ndarrayPluginBase.getArraySize0_RBV();
		int dim1 = ndarrayPluginBase.getArraySize1_RBV();
		numChannels = Math.max(1, dim0) * Math.max(1, dim1);
		logger.debug("Setting number of channels to {}. (dim0 = {}, dim1 = {})", numChannels, dim0, dim1);
	}

	@Override
	public void acquire(AcquisitionParameters params) throws DeviceException {
		ADBase adBase = mythenAreaDetector.getAdBase();
		NDArray ndArray = mythenAreaDetector.getNdArray();
		try {
			// save current image and trigger mode settings, so they can be restored at the end
			int origImageMode = adBase.getImageMode();
			int origTrigMode = adBase.getTriggerMode();

			// enable callbacks so array plugin is updated with new data
			adBase.setArrayCallbacks(1); // in 'advanced' settings
			ndArray.getPluginBase().enableCallbacks(); // in array plugin

			// Set total number of channels
			setNumChannels();

			logger.debug("Setting up Mythen");
			adBase.setTriggerMode(0); // 0=internal, 1=external
			if (params.getTrigger() == Trigger.NONE) {
				adBase.setImageMode(ImageMode.SINGLE);
			} else if (params.getTrigger() == Trigger.CONTINUOUS) {
				adBase.setImageMode(ImageMode.CONTINUOUS);
			}

			adBase.setNumExposures(params.getFrames());
			adBase.setAcquireTime(params.getExposureTime().doubleValue());
			adBase.setAcquirePeriod(params.getExposureTime().doubleValue());
			logger.debug("Collecting data from Mythen...");
			mythenAreaDetector.collectData();
			mythenAreaDetector.waitWhileBusy();
			rawArrayData = ndArray.getIntArrayData(numChannels);
			logger.debug("Finished collecting data from Mythen.");

			logger.debug("Restoring trigger and image mode after collection");
			adBase.setImageMode(origImageMode);
			adBase.setTriggerMode(origTrigMode);

		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	public MythenRawDataset getRawData() {
		List<MythenRawData> mythenData = new ArrayList<MythenRawData>();
		for (int i = 0; i < numChannels; i++) {
			mythenData.add(new MythenRawData(i, rawArrayData[i]));
		}

		MythenRawDataset rawDataset = new MythenRawDataset();
		rawDataset.setLines(mythenData);
		return rawDataset;
	}

}
