/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.mythen.epics;

import gda.device.DeviceException;
import gda.device.detector.NXDetector;
import gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy;
import gda.device.detector.addetector.triggering.SimpleAcquire;
import gda.device.detector.mythen.MythenDetectorImpl;
import gda.scan.ConcurrentScan;
import gda.scan.RepeatScan;
import gda.scan.ScanInformation;

public class MythenDetector extends MythenDetectorImpl {
	
	private MythenEpicsController controller;
	
	
	
	public void acquire(double collectionTime, int numImages) throws Exception {
		// TO-DO
		if (numImages <= 0) {
			throw new IllegalArgumentException("The input value of numImages must be a positive integer.");
		}
		
		ScanInformation scanInfo_IGNORED = null;
		int numberImagesPerCollection_IGNORED = -1;
		//prepareForCollection(collectionTime, numberImagesPerCollection_IGNORED, scanInfo_IGNORED);
		ConcurrentScan scan = RepeatScan.create_repscan(numImages, this);
		scan.runScan();
	}

	public void acquire(double collectionTime) throws Exception {
		// TO-DO
		if (collectionTime <= 0) {
			throw new IllegalArgumentException("The input value of collectionTime must be a positive number.");
		}
		
		int numImages = Integer.MAX_VALUE;
		numImages = 12; //for testing
		ScanInformation scanInfo_IGNORED = null;
		int numberImagesPerCollection_IGNORED = -1;
		//SimpleAcquire psa = (SimpleAcquire)this.getCollectionStrategy();
		//psa.prepareForCollection(collectionTime, numberImagesPerCollection_IGNORED, scanInfo_IGNORED);
		ConcurrentScan scan = RepeatScan.create_repscan(numImages, this);
		scan.runScan();
	}
	
	@Override
	public void stop() throws DeviceException {
		super.stop();
		try {
			stopAcquiring();
		} catch (Exception e) {
			throw new DeviceException("Failed to stop Pixium detector", e);
		}
	}
	
	public void stopAcquiring() throws Exception {
		//AbstractADTriggeringStrategy ats = (AbstractADTriggeringStrategy)this.getCollectionStrategy();
		//ats.getAdBase().stopAcquiring();
	}
}
