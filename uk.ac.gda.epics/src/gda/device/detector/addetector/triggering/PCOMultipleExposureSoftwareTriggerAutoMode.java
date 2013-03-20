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

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.device.detector.areadetector.v17.ADDriverPco.PcoTriggerMode;
import gda.scan.ScanInformation;

/*
 * Version of MultipleExposureSoftwareTriggerAutoMode that works with te PCO camera
 * The main  differences are:
 * In prepareCollection the PCo is armed and the triggerMode set to software
 */
public class PCOMultipleExposureSoftwareTriggerAutoMode extends MultipleExposureSoftwareTriggerAutoMode {
	private final ADDriverPco adDriverPco;
	public PCOMultipleExposureSoftwareTriggerAutoMode(ADBase adBase, double maxExposureTime, ADDriverPco adDriverPco) {
		super(adBase, maxExposureTime);
		this.adDriverPco = adDriverPco;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, numImagesIgnored, scanInfo);
		getAdBase().setAcquirePeriod(0.0); //this is needed for PCO to make sure delay=0
		getAdBase().setTriggerMode(PcoTriggerMode.SOFTWARE.ordinal());
		adDriverPco.getAdcModePV().put(1); //2 adcs
		//use 0 for timestamp on PCO Edge
		adDriverPco.getTimeStampModePV().put(1); // BCD - if set to None then the image is blank. BCD means a small digital timestamp on image
		adDriverPco.getArmModePV().putCallback(true);
		enableOrDisableCallbacks();
	}

	@Override
	public void completeCollection() throws Exception {
		adDriverPco.getArmModePV().putCallback(false);
		super.waitWhileBusy();
		super.completeCollection();
	}

}
