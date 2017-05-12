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

package gda.device.detector.addetector.triggering;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.device.detector.areadetector.v17.ADDriverPco.PcoTriggerMode;
import gda.scan.ScanInformation;

public class PCOMultipleExposurePerCollection extends MultipleExposurePerCollectionStandard {
	private final ADDriverPco driver;
	private double acquirePeriod = 0;

	public PCOMultipleExposurePerCollection(ADBase adBase, ADDriverPco adDriverPco) {
		super(adBase, 0);
		this.driver = adDriverPco;
	}

	@Override
	public double getAcquirePeriod() {
		return acquirePeriod;
	}

	public void setAcquirePeriod(double acquirePeriod) {
		this.acquirePeriod = acquirePeriod;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, numImages, scanInfo);
		getAdBase().setAcquirePeriod(acquirePeriod);
		getAdBase().setTriggerMode(PcoTriggerMode.SOFTWARE.ordinal());
		driver.getAdcModePV().putNoWait(1);
		driver.getTimeStampModePV().putNoWait(1);
		driver.getArmModePV().putWait(true);
		enableOrDisableCallbacks();
	}

	@Override
	public void completeCollection() throws Exception {
		driver.getArmModePV().putWait(false);
		super.waitWhileBusy();
		super.completeCollection();
	}
}
