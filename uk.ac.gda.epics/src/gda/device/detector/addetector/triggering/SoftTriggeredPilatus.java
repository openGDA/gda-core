/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADDriverPilatus;
import gda.device.detector.areadetector.v17.ADDriverPilatus.PilatusTriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.scan.ScanInformation;

public class SoftTriggeredPilatus extends SimpleAcquire {

	private ADDriverPilatus pilatusDriver;

	SoftTriggeredPilatus(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
		setReadoutTime(readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		enableOrDisableCallbacks();
		if (numImages != 1) {
			throw new IllegalArgumentException("This software triggered strategy only expects one image per point");
		}
		configureAcquireAndPeriodTimes(collectionTime);
		getAdBase().setNumImages(scanInfo.getNumberOfPoints());
		getAdBase().setImageMode(ImageMode.SINGLE.ordinal());
		getAdBase().setTriggerMode(PilatusTriggerMode.SOFTWARE_TRIGGER.ordinal());
	}

	@Override
	public void collectData() throws Exception {
		if (getAdBase().getAcquireState() == Detector.IDLE) {
			getAdBase().startAcquiring();
			Thread.sleep(10);
		}
		getPilatusDriver().sendSoftTrigger();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException {
		getPilatusDriver().waitForSoftTriggerCallback();
	}

	public ADDriverPilatus getPilatusDriver() {
		return pilatusDriver;
	}

	public void setPilatusDriver(ADDriverPilatus pilatusBase) {
		this.pilatusDriver = pilatusBase;
	}
}
