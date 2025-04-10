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

import java.util.Arrays;

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
	private String [] initialExtraNames = {};
	private String [] initialOutputFormats = {};
	private boolean isFirstPoint = true;


	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		setExtraNames(new String[] { getName() });
		super.configure();
		initialOutputFormats = getOutputFormat();
		initialExtraNames = getExtraNames();
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
		final NXDetectorData nexusData = new NXDetectorData(this);
		final int[] sumData = getSummedData()[0];
		final int totalSumDataIntensity = sumArray(sumData);
		nexusData.setPlottableValue(getName(),(double)totalSumDataIntensity);
		nexusData.addData(getName(), "SummedArray", new NexusGroupData(sumData));
		nexusData.addData(getName(), "SummedTotal", new NexusGroupData(totalSumDataIntensity));

		if (recordRois.length != 0) {
			final double[][] roisData = getRoiData(recordRois);
			int index = 0;
			for (double[] roi : roisData) {
				final double totalRoiIntensity = sumArray(roi);
				nexusData.setPlottableValue(getRoiName(recordRois[index]), totalRoiIntensity);
				nexusData.addData(getName(), getRoiName(recordRois[index]) + "Array", new NexusGroupData(roi));
				nexusData.addData(getName(), getRoiName(recordRois[index]) + "Total", new NexusGroupData(totalRoiIntensity));
				if (isFirstPoint) {
					nexusData.addData(getName(), getRoiName(recordRois[index]) + "StartAndSize", new NexusGroupData(getRoiStartAndSize(recordRois[index])));
					isFirstPoint = false;
				}
				index++;
			}
		}
		return nexusData;
	}

	private String getRoiName(int index) {
		return String.format("roi%1d", index);
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
		 * Sets AreaDetector plugin ROI PVs start and size, roiNo can be 1 to 6
		 */
		logger.debug("Setting roi limits {} - {}", startX, startX+sizeX );
		((Xspress3MiniController)controller).setRoiStartAndSize(roiNo, startX, sizeX);
	}

	public int[] getRoiStartAndSize(int roiNo) throws DeviceException {
		/**
		 * Get AreaDetector plugin ROI start and size, roiNo can be 1 to 6
		 */
		logger.debug("Getting roi limits for ROI {}", roiNo);
		return ((Xspress3MiniController)controller).getRoiStartAndSize(roiNo);
	}

	@Override
	public void atScanStart() throws DeviceException {
		isFirstPoint = true;
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
		updateExtraNamesAndOutputFormatWithRecordRois(recordRois);
	}

	private void updateExtraNamesAndOutputFormatWithRecordRois(int[] recordRois) {
		String[] newExtraNames = Arrays.copyOf(initialExtraNames, initialExtraNames.length+recordRois.length);
		String[] newOutputFormat = Arrays.copyOf(initialOutputFormats, initialOutputFormats.length+recordRois.length);
		for (int i = 0; i<this.recordRois.length;i++) {
			newExtraNames[initialExtraNames.length+i] = getRoiName(recordRois[i]);
			newOutputFormat[initialOutputFormats.length+i] = DEFAULT_OUTPUT_FORMAT;
		}
		setExtraNames(newExtraNames);
		setOutputFormat(newOutputFormat);
	}

}
