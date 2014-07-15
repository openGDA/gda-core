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

package uk.ac.gda.server.ncd.data.scan;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.data.ProcessingRunner;
import gda.data.metadata.StoredScanMetadataEntry;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.scan.ScanInformation;

public class DataProcessingScanListener extends DataWriterExtenderBase {
	private static final Logger logger = LoggerFactory.getLogger(DataProcessingScanListener.class);
	private ScanInformation scanInformation;
	private Scannable detector;
	private ProcessingRunner runner;
	private String filepath;

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) {
		try {
			super.addData(parent, dataPoint);
			if (filepath == null) {
				if (parent instanceof DataWriter) {
					DataWriter writer = (DataWriter)parent;
					filepath = writer.getCurrentFileName();
				}
			}
		} catch (Exception e) {
			logger.error("Could not update scanInformation");
		}
	}

	public void setDetector(Scannable detector) {
		this.detector = detector;
	}

	public void setRunner(ProcessingRunner runner) {
		this.runner = runner;
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		super.completeCollection(parent);
		try {
			if (scanInformation == null) {
				scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
			}
		
			String[] detectors = scanInformation.getDetectorNames();
			if (Arrays.asList(detectors).contains(detector.getName())) {
				StoredScanMetadataEntry background = Finder.getInstance().find("backgroundDataFile");
				StoredScanMetadataEntry collection = Finder.getInstance().find("dataCollectionId");
				String backgroundPath = background.getMetadataValue();
				String collectionId = collection.getMetadataValue();
	
				logger.info("Processing running with '{}', background '{}' and id '{}'", filepath, backgroundPath, collectionId);
				try {
					runner.triggerProcessing(filepath, backgroundPath, collectionId);
				} catch (IOException e) {
					logger.error("Couldn't run data reduction/processing", e);
				}
			}
		} catch (NullPointerException npe) { //unfindable findables
			logger.error("Could not start processing: Could not get required metadata");
		} finally {
			scanInformation = null;
			filepath = null;
		}
	}
}
