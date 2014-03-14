/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.scan.ScanInformation;

/**
 * A collection strategy that sets the detector during the first collectData in a scan line
 * and then keeps going ignoring future calls to collectData.
 */
public class SoftwareSynchronisedMultipleExposure extends HardwareTriggeredStandard {

	private static final Logger logger = LoggerFactory.getLogger(SoftwareSynchronisedMultipleExposure.class);
	
	private boolean currentLineStarted;

	public SoftwareSynchronisedMultipleExposure(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored, ScanInformation scanInfo) throws Exception {
		getAdBase().stopAcquiring(); //to get out of armed state
 
		configureAcquireAndPeriodTimes(collectionTime);
		getAdBase().setImageModeWait(ImageMode.MULTIPLE);
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal()); // *does* wait

		int[] dimensions = scanInfo.getDimensions();
		int pointsInLine = dimensions[dimensions.length - 1];
		getAdBase().setNumImages(pointsInLine);
		
		enableOrDisableCallbacks();
		logger.info(getName() + " configured to take " + pointsInLine + " images for this scan line in MULTIPLE, INTERNAL triggered mode");
	}

	@Override
	public void collectData() throws Exception {
		if (!currentLineStarted) {
			logger.info(getName() + " starting collection for this line (after Scannables of lower level have been moved for this point)");
			super.collectData();
		}
		currentLineStarted = true;
	}
	
	@Override
	public void prepareForLine() throws Exception {
		currentLineStarted = false;
	}
	
	@Override
	public void completeCollection() throws Exception {
		logger.info(getName() + " waiting for line to complete");
		getAdBase().waitWhileStatusBusy();
		logger.info(getName() + " line completed");
		stop();
	}
	
	@Override
	public void stop() throws Exception {
		getAdBase().stopAcquiring();
		currentLineStarted = false;
	}

	@Override
	public void atCommandFailure() throws Exception {
		stop();
	}
	
	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		// Needs to let the scan thread keep moving. Instead waits in completeCollection()
	}

}
