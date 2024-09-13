/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;

/**
 * Configure creation of nexus file before first scan point - use if NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START set to True
 */
public class NXDetectorSimpleExtraNameFileStructure extends NXDetector {

	private static final Logger logger = LoggerFactory.getLogger(NXDetectorSimpleExtraNameFileStructure.class);
	//ToDo - try and add to NXDetector, check if failing tests are correct
	private boolean ready = false;

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		ready = true;
	}

	@Override
	public void collectData() throws DeviceException {
		//Make sure we don't do data collection if via pos command as it's not supported.
		if(!ready) {
			throw new UnsupportedOperationException(NXDetector.UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
		}
		super.collectData();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		ready = false;
		super.atScanEnd();
	}

	@Override
	public NexusTreeProvider getFileStructure() throws DeviceException{
		logger.debug("Setting up file structure for detector: {}", getName());
		NexusTreeProvider nexustree = super.getFileStructure();

		//Get detector object so we can dynamically setup file structure with extra names provided
		if (nexustree == null) {
			nexustree = new NXDetectorData(this);
		}
		//ToDo - Combine with NXDetector so not different class
		for(String extraName : this.getExtraNames()) {
			NexusGroupData groupData = new NexusGroupData(0.0);
			if (nexustree instanceof NXDetectorData detectorData) {
				detectorData.addData(this.getName(), extraName, groupData);
			}
			//This is needed because addData will set it to true. This needs to be false to keep data format consistent between scans.
			//In a regular scan (file created at first point), the NXDetector.getPluginList() are not added as NXdata groups. If doing a scan where file
			//is created before first point, setting this to false will also replicate this behaviour and keep format consistent between scans
			groupData.isDetectorEntryData = false;
		}
		return nexustree;
	}
}
