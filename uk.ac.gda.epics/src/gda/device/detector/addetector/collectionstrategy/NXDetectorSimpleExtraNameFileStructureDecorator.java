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

package gda.device.detector.addetector.collectionstrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.detector.NXDetectorData;
import gda.factory.Finder;

/**
 * Configure creation of nexus file before first scan point - use if NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START set to True
 */
public class NXDetectorSimpleExtraNameFileStructureDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(NXDetectorSimpleExtraNameFileStructureDecorator.class);
	private String detectorName;

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	public String getDetectorName() {
		return detectorName;
	}

	@Override
	public NexusTreeProvider getFileStructure(){
		logger.debug("Setting up file structure for detector: {}", getName());

		//Get detector object so we can dynamically setup file structure with extra names provided
		Object detectorObject = Finder.find(getDetectorName());

		if (!(detectorObject instanceof Detector)) {
			throw new IllegalStateException(
				detectorObject == null ? "null" : detectorObject.toString() + " is not an instance of the \"Detector\" class"
			);
		}
		Detector detector = (Detector) detectorObject;

		String[] extraNames = detector.getExtraNames();
		NXDetectorData detectorData = new NXDetectorData(detector);

		for(String extraName : extraNames) {
			NexusGroupData groupData = new NexusGroupData(0.0);
			detectorData.addData(detector.getName(), extraName, groupData);

			//This is needed because addData will set it to true. This needs to be false to keep data format consistent between scans.
			//In a regular scan (file created at first point), the NXDetector.getPluginList() are not added as NXdata groups. If doing a scan where file
			//is created before first point, setting this to false will also replicate this behaviour and keep format consistent between scans
			groupData.isDetectorEntryData = false;
		}
		return detectorData;
	}

}
