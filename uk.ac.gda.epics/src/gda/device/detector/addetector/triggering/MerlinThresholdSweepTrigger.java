/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADDriverMerlinThresholdSweep;
import gda.scan.ScanInformation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerlinThresholdSweepTrigger extends SimpleAcquire {

	private static final Logger logger = LoggerFactory.getLogger(MerlinThresholdSweepTrigger.class);

	private final ADDriverMerlinThresholdSweep sweepDriver;
	
	private Integer imagesPerSweep;

	public MerlinThresholdSweepTrigger(ADBase adBase, ADDriverMerlinThresholdSweep sweepDriver) {
		super(adBase, -1); // This will set the period to 0 in Epics, which will be corrcted by the Merlin software and
							// read back up in Epics (I think once the exposure time is set)
		this.sweepDriver = sweepDriver;
	}

	public ADDriverMerlinThresholdSweep getSweepDriver() {
		return sweepDriver;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		/* TODO: Is this belt and bracers check now needed? If so it should probably be configurable.
		String dataFormat = LocalProperties.get("gda.data.scan.datawriter.dataFormat");
		if (dataFormat != "NexusDataWriter") {
			throw new IllegalStateException("gda.data.scan.datawriter.dataFormat LocalProperty must be 'NexusDataWriter', not: '" + dataFormat + "'");
		}
		*/
		enableOrDisableCallbacks();
		logger.info("images per sweep = " + numImages);
		configureAcquireAndPeriodTimes(collectionTime);
		getAdBase().setNumImages(1);  // the threshold scan itself creates multiple images
		
		if (sweepDriver.isUseTriggerModeNotStartThresholdScanning()) {
			getAdBase().setImageMode(ADDriverMerlinThresholdSweep.MerlinThresholdSweepImageMode.THRESHOLD.ordinal());
		}
	}
	
	@Override
	public void collectData() throws Exception {
		if (sweepDriver.isUseTriggerModeNotStartThresholdScanning()) {
			// TODO: Test if this is sufficient:
			super.collectData();
			// TODO: Is setting Image mode back to SINGLE afterwards needed? Is SINGLE the correct mode to return it to?
			getAdBase().setImageMode(ADDriverMerlinThresholdSweep.MerlinThresholdSweepImageMode.SINGLE.ordinal());
			return;
			// In theory, with the new Merlin AD behaviour, we only need to set the trigger mode and do an ordinary collection.
			// Thus the rest of this function should not be needed.
		}
		String msg = "Sleeping for 2s before starting threshold scan to allow detector to steady itself after previous scan";
		logger.warn(msg);
		//InterfaceProvider.getTerminalPrinter().print(msg);
		Thread.sleep(2000);
		
		sweepDriver.getStartThresholdScanningPV().putNoWait(true);
		
		msg = "Sleeping for 1s after starting threshold scan to allow DetectotState_RBV to read busy";
		//InterfaceProvider.getTerminalPrinter().print(msg);
		logger.warn(msg);
		Thread.sleep(1000);
	}
	
	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws IOException {
		return sweepDriver.getNumberPointsPerSweep();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		try {
			getAdBase().waitForDetectorStateIDLE(Double.MAX_VALUE);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	public Integer getImagesPerSweep() {
		if (imagesPerSweep==null) {
			throw new IllegalStateException("imagesPerSweep has not been set");
		}
		return imagesPerSweep;
	}

	public void setImagesPerSweep(Integer imagesPerSweep) {
		this.imagesPerSweep = imagesPerSweep;
	}

}
