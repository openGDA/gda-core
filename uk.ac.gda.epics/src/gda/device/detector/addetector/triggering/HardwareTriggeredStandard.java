/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.scan.ScanInformation;

public class HardwareTriggeredStandard extends SimpleAcquire {


	public HardwareTriggeredStandard(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		configureAcquireAndPeriodTimes(collectionTime);
		configureTriggerMode();
		getAdBase().setImageModeWait(ImageMode.MULTIPLE);
		getAdBase().setNumImages(numImages);
		enableOrDisableCallbacks();
	}

	protected void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(StandardTriggerMode.EXTERNAL.ordinal());
	}

	@Override @Deprecated
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		if (getReadoutTime() < 0) {
			getAdBase().setAcquirePeriod(collectionTime);
			getAdBase().setAcquireTime(collectionTime);
		} else {
			getAdBase().setAcquirePeriod(collectionTime);
			getAdBase().setAcquireTime(collectionTime - getReadoutTime());
		}
	}
	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}
}
