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
	private int[] recordRois = {};


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
		int[] sumData = getSummedData()[0];
		int totalSumDataIntensity = sumArray(sumData);
		nexusData.setPlottableValue(getName(),(double)totalSumDataIntensity);
		nexusData.addData(getName(), "roiSummedArray", new NexusGroupData(sumData));
		nexusData.addData(getName(), "roiSummedTotal", new NexusGroupData(totalSumDataIntensity));

		if (recordRois.length != 0) {
			double[][] roisData = getRoiData(recordRois);
			int index = 0;
			for (double[] roi : roisData) {
				double totalRoiIntensity = sumArray(roi);
				nexusData.setPlottableValue(getName(), totalRoiIntensity);
				nexusData.addData(getName(), String.format("roi%1dArray", recordRois[index]), new NexusGroupData(roi));
				nexusData.addData(getName(), String.format("roi%1dTotal", recordRois[index]), new NexusGroupData(totalRoiIntensity));
				index++;
			}
		}


		return nexusData;
	}


	private int sumArray(int[] data) {
		int sum = 0;
		for (int value : data) {
			sum += value;
		}
		return sum;
	}

	private double sumArray(double[] data) {
		int sum = 0;
		for (double value : data) {
			sum += value;
		}
		return sum;
	}

	/**
	 * Get data array for specific ROI, Adding that method here since it's
	 * for a single channel multiple roi device
	 * @param recordRois
	 * @return double[][]
	 * @throws DeviceException
	 */
	public double[][] getRoiData(int[] recordRois) throws DeviceException {
		return ((Xspress3MiniController)controller).readoutRoiArrayData(recordRois);
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		((Xspress3MiniController)controller).setAcquireTime(collectionTime);
	}

	public void setRoiSumStartAndSize(int startX, int sizeX) throws DeviceException {
		/**
		 * Sets :ROISUM1:MinX and :ROISUM1:SizeX, these PV are for summing multiple channels
		 */
		logger.debug("Setting roi sum limits {} - {}", startX, startX+sizeX );
		((Xspress3MiniController)controller).setRoiSumStartAndSize(startX, sizeX);
	}

	public void setRoiStartAndSize(int roiNo, int startX, int sizeX) throws DeviceException {
		/**
		 * Sets ROI PVs start and size, roiNo can be 1 to 6
		 */
		logger.debug("Setting roi limits {} - {}", startX, startX+sizeX );
		((Xspress3MiniController)controller).setRoiStartAndSize(roiNo, startX, sizeX);
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

	public void setRecordRois(int[] recordRois) {
		this.recordRois = recordRois;
	}

}
