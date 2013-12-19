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

package uk.ac.gda.devices;

import gda.device.DeviceException;
import gda.device.detector.addetector.triggering.SingleExposureStandard;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ExcaliburCollectionStrategy extends SingleExposureStandard implements InitializingBean{
	private static final Logger logger = LoggerFactory.getLogger(ExcaliburCollectionStrategy.class);

	private PV<String> operationModePV;
	private boolean burst=false;
	private boolean softwareTrigger=false;
	private String operationModePVName = "EXCALIBUR:CONFIG:ACQUIRE:OperationMode";
	private boolean started;
	
	

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		operationModePV = LazyPVFactory.newStringPV(operationModePVName);
	}

	public ExcaliburCollectionStrategy(ADBase adBase) {
		super(adBase, -1);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesPerCollection, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, 1, scanInfo);
		operationModePV.putWait(burst ? "Burst" : "Normal");
		getAdBase().setImageModeWait(ImageMode.MULTIPLE);
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());
		int numImagesToTrigger = 1;
		if(!softwareTrigger){
			ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
			if( scanInformation != null){
				int[] dimensions = scanInformation.getDimensions();
				numImagesToTrigger = dimensions[dimensions.length-1];
			}
		}
		getAdBase().setNumImages(numImagesToTrigger);

	}


	@Override
	public void prepareForLine() throws Exception {
		super.prepareForLine();
		started = false;
	}

	@Override
	public void collectData() throws Exception {
		if( softwareTrigger  || !started){
			super.collectData();
			started = true;
		}
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		if( softwareTrigger){
			super.waitWhileBusy();
		}
	}


	@Override
	public void completeLine() throws Exception {
		if( !softwareTrigger ){
			super.waitWhileBusy();
		}
		super.completeLine();
	}

	public boolean isBurst() {
		return burst;
	}

	public void setBurst(boolean burst) {
		this.burst = burst;
	}

	public String getOperationModePVName() {
		return operationModePVName;
	}

	public void setOperationModePVName(String operationModePVName) {
		this.operationModePVName = operationModePVName;
	}

	public boolean isSoftwareTrigger() {
		return softwareTrigger;
	}

	public void setSoftwareTrigger(boolean softwareTrigger) {
		this.softwareTrigger = softwareTrigger;
	}

	@Override
	public void stop() throws Exception {
		logger.error("Stop called for Excalibur but not possible as it will lead to a fault in the FEM comms");
	}

}
