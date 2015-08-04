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

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.scan.ScanInformation;

import java.util.ArrayList;

public class MultipleExposureHardwareTriggeredStrategy extends SimpleAcquire {

	private static final ArrayList<String> EMPTY_LIST = new ArrayList<String>();

	public MultipleExposureHardwareTriggeredStrategy(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		enableOrDisableCallbacks();
		configureAcquireAndPeriodTimes(collectionTime);
		configureTriggerMode();
		getAdBase().setImageModeWait(ImageMode.MULTIPLE);
		getAdBase().setNumImages(scanInfo.getDimensions()[0]);
		getAdBase().startAcquiring();
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		getAdBase().setAcquireTime(0.1);
		getAdBase().setAcquirePeriod(0);
	}

	@Override
	public void collectData() throws Exception {
		//System.out.println("test");
	//	getAdBase().startAcquiring();
	}

//	@Override
//	public List<String> getInputStreamNames() {
//		return EMPTY_LIST;
//	}
//
//	@Override
//	public List<String> getInputStreamFormats() {
//		return EMPTY_LIST;
//	}
	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		// getAdBase().waitWhileStatusBusy();
		return;
	}
	private void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(StandardTriggerMode.EXTERNAL.ordinal());
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}
}
