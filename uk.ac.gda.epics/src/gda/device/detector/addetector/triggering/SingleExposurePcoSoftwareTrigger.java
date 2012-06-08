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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.device.detector.areadetector.v17.ADDriverPco.PcoTriggerMode;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;

/*
 * Class to configure PCO Software trigger - puts trigger into Auto mode and does not arm
 * Although very slow it works.
 */
public class SingleExposurePcoSoftwareTrigger extends SingleExposureStandard {
	static final Logger logger = LoggerFactory.getLogger(SingleExposurePcoSoftwareTrigger.class);
	private final ADDriverPco adDriverPco;
	private double delay=10;

	public SingleExposurePcoSoftwareTrigger(ADBase adBase, ADDriverPco adDriverPco, double readoutTime) {
		super(adBase, readoutTime);
		this.adDriverPco = adDriverPco;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages) throws Exception {
		super.prepareForCollection(collectionTime, numImages);
		adDriverPco.getArmModePV().putCallback(false);
	}

	@Override
	public void endCollection() throws Exception {
		//wait for last busystate to clear from last acquire - as file is written and camera disarms
		logger.info("endCollection:Before waitWhileBusy");
		super.waitWhileBusy();
		super.endCollection();
		adDriverPco.getArmModePV().putCallback(false);
	}
	
	

	@Override
	protected void configureTriggerMode() throws Exception {
		// Reported Epics bug: changing mode while acquiring causes an IOC crash (28oct2011 RobW)
		getAdBase().stopAcquiring(); 
		getAdBase().setTriggerMode(PcoTriggerMode.AUTO.ordinal());
	}
	
	@Override
	public void collectData() throws Exception {
		//wait for last busystate to clear from last acquire - as file is written and camera disarms
		logger.info("collectData:Before waitWhileBusy");
		super.waitWhileBusy();
		logger.info("collectData:Before collectData");
		super.collectData();
		logger.info("collectData:Before sleep");
		Thread.sleep((long) (getAcquirePeriod()*1000+delay));
		logger.info("collectData:After sleep");
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		logger.info("waitWhileBusy");
		//return - we have waited in the collectData method
	}
}
