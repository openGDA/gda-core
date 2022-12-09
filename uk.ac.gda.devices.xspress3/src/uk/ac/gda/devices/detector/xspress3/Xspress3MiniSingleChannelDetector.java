/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.factory.FactoryException;

/**
 * Class to be used with scan command, to override some of the complexities
 * present in the parent classes
 */
public class Xspress3MiniSingleChannelDetector extends Xspress3Detector {

	private static final Logger logger = LoggerFactory.getLogger(Xspress3MiniSingleChannelDetector.class);

	private boolean useParentClassMethods;


	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		setExtraNames(new String[] { getName() });
		super.configure();
	}

	@Override
	public void collectData() throws DeviceException {
		logger.info("collecting data from Xspress3Mini Fluorescence Detector");
		Xspress3MiniController miniController = (Xspress3MiniController)controller;
		miniController.setTriggerMode(TRIGGER_MODE.Burst);
		miniController.doErase();
		miniController.doStart();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while(controller.getStatus() == BUSY) {
			Thread.sleep(500);
		}
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NXDetectorData nexusData = new NXDetectorData(this);
		int[] deviceData = getSummedData()[0];
		int totalIntensity = sumData(deviceData);
		nexusData.setPlottableValue(getName(),(double)totalIntensity);
		nexusData.addData(getName(), "array", new NexusGroupData(deviceData));
		nexusData.addData(getName(), "total", new NexusGroupData(totalIntensity));
		return nexusData;
	}

	private int sumData(int[] data) {
		int sum = 0;
		for (int value : data) {
			sum += value;
		}
		return sum;
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		((Xspress3MiniController)controller).setAcquireTime(collectionTime);
	}

	public void setROIStartAndSize(int startX, int sizeX) throws DeviceException {
		logger.debug("Setting limits {} - {}", startX, startX+sizeX );
		((Xspress3MiniController)controller).setROIStartAndSize(startX, sizeX);
	}

	@Override
	public void atScanStart() throws DeviceException {
		if(useParentClassMethods) {
			super.atScanStart();
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		if(useParentClassMethods) {
			super.atScanLineStart();
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if(useParentClassMethods) {
			super.atScanEnd();
		}
	}

	@Override
	public void atPointEnd() throws DeviceException {
		if(useParentClassMethods) {
			super.atPointEnd();
		}
	}

	public void setUseParentClassMethods(boolean useParentClassMethods) {
		this.useParentClassMethods = useParentClassMethods;
	}

	@Override
	public int getStatus() throws DeviceException {
		return controller.getStatus();
	}

	@Override
	public String[] getExtraNames() {
		return extraNames;
	}
}
