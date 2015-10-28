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

package gda.device.detector.addetector.triggering;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.scan.ScanInformation;

/**
 * For a CMOS detector which is hardware triggered even in step scans, so this strategy is required to setup the Area
 * Detector PVs, as in this situation there would not be any TriggerProvider.
 */
public class CMOSMultipleHardwareTriggered extends SimpleAcquire {

	public CMOSMultipleHardwareTriggered(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	@Override
	public void collectData() throws Exception {
		if (getAdBase().getAcquireState() == 0) {
			getAdBase().startAcquiring();
		}
	}

	@Override
	public void completeCollection() throws Exception {
//		getAdBase().stopAcquiring();  // this detector has problems if we do this - the underlying PSL software stops responding
	}


	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		getAdBase().setArrayCallbacks(0);
		configureAcquireAndPeriodTimes(collectionTime);
		configureTriggerMode();
		numImages = 1;
		for (int dim : scanInfo.getDimensions()) {
			numImages *= dim;
		}
		getAdBase().setNumImages(numImages);
		getAdBase().setArrayCounter(0);
		getAdBase().setArrayCallbacks(1);
		Thread.sleep(300);
		collectData();
	}

	protected void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(5);
		getAdBase().setImageMode(ImageMode.MULTIPLE);
	}

	@Override @Deprecated
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		if (getReadoutTime() < 0) {
			getAdBase().setAcquirePeriod(collectionTime);
			getAdBase().setAcquireTime(collectionTime);
		} else {
			getAdBase().setAcquirePeriod(collectionTime - getReadoutTime());
			getAdBase().setAcquireTime(collectionTime - getReadoutTime());
		}
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

	@Override
	public int getStatus() throws DeviceException {
		return Detector.IDLE;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		return;
	}
}
