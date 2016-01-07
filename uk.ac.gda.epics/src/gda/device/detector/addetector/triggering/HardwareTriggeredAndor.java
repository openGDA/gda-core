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

import gda.device.detector.areadetector.v17.ADBase;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardwareTriggeredAndor extends HardwareTriggeredStandard {

	private static final Logger logger = LoggerFactory.getLogger(HardwareTriggeredAndor.class);

	private HardwareTriggeredAndorData data = new HardwareTriggeredAndorData(AndorTriggerMode.EXTERNAL);

	public enum AndorTriggerMode {
		INTERNAL, EXTERNAL, EXTERNAL_START, EXTERNAL_EXPOSURE, EXTERNAL_FVP, SOFTWARE
	}



	public HardwareTriggeredAndor(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, numImages, scanInfo);
	}

	/**
	 * Create a collection strategy for use with EXTERNAL_EXPOSURE mode where no readout time is specified.
	 *
	 * @param adBase
	 */
	public HardwareTriggeredAndor(ADBase adBase) {
		super(adBase, -1);
	}

	@Override
	protected void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(data.triggerMode.ordinal());
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		if (data.triggerMode == AndorTriggerMode.EXTERNAL_EXPOSURE) {
			logger.info("Not configuring acquire period and times as the triggerMode is EXTERNAL_EXPOSURE");
			return;
		}
		super.configureAcquireAndPeriodTimes(collectionTime);
	}

	public AndorTriggerMode getTriggerMode() {
		return data.triggerMode;
	}

	public void setTriggerMode(AndorTriggerMode triggerMode) {
		this.data.triggerMode = triggerMode;
	}

	@Override
	public void completeCollection() throws Exception {
		super.completeCollection();
		//by default the trigger mode used in scans is External_Exposure
		setTriggerMode(AndorTriggerMode.EXTERNAL_EXPOSURE);
		getAdBase().setTriggerMode(AndorTriggerMode.INTERNAL.ordinal());
	}
}
