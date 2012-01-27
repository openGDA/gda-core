/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector.uview;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is for writing uview image files.
 */
public class UViewFile {
	private static final Logger logger = LoggerFactory.getLogger(UViewFile.class);

	// the number of the file being written to
	private long thisFileNumber = 0;

	// file extension to use
	private String fileExtension = "png";

	// file prefix to use (if any)
	private String filePrefix = "uviewImage_";

	private String newDataDir = null;

	private String currentFileName = null;

	private NumTracker runs = null;

	/**
	 * Constructor for UViewFile image file
	 * 
	 * @param filePrefix
	 * @param fileExtension
	 */
	public UViewFile(String filePrefix, String fileExtension) {
		this.filePrefix = filePrefix;
		this.fileExtension = fileExtension;

		this.setupScanDir();
	}
	
	public void setFileExtenstion(String fileExtension){
		this.fileExtension = fileExtension;
	}

	/**
	 * Setup a new image directory based on the tracker provided
	 */
	public void newImageDir(String tracker, String imageDir){
		try {
			runs = new NumTracker(tracker);
		} catch (IOException e) {
			logger.error("ERROR: Could not instantiate NumTracker using: " + tracker);
		}

		long nextNum = 0L + runs.incrementNumber();
		newDataDir = PathConstructor.createFromDefaultProperty() + File.separator + imageDir + File.separator + nextNum + "_UViewImage";
//		newDataDir = PathConstructor.createFromDefaultProperty() + File.separator + nextNum + "_UViewImage";

		File fd = new File(newDataDir);
		if (!fd.exists()) {
			
			if (fd.mkdirs()){
				thisFileNumber = 0L;
				logger.debug("New directory for image created: " + newDataDir);
			}
			else{
				logger.debug("Can not create the image directory: " + newDataDir);
			}
			
		}
	}

	
	/**
	 * Find the directory in which the incremental run number (runTracker) file exists, this file name is the number of
	 * the current run and will be auto-incremented during scanning
	 */
	public void setupScanDir() {
		try {
			runs = new NumTracker("tmp");
		} catch (IOException e) {
			logger.error("ERROR: Could not instantiate NumTracker in IncrementalFile().");
		}

		long nextNum = 0L + runs.getCurrentFileNumber();
		newDataDir = PathConstructor.createFromDefaultProperty() + File.separator + nextNum + "_UViewImage";

		File fd = new File(newDataDir);
		if (!fd.exists()) {
			fd.mkdir();
			thisFileNumber = 0L;
			logger.debug("New directory for UView image created: " + newDataDir);
		}
	}

	/**
	 * Create a file name from the run number
	 * 
	 * @return String the file name
	 */
	public String getCurrentFullFileName() {
		++thisFileNumber;

		currentFileName = newDataDir + File.separator + this.filePrefix + String.format("%08d", thisFileNumber) + "."
				+ this.fileExtension;
		return currentFileName;
	}

	/**
	 * Create a Windows file name from the run number
	 * 
	 * @return String the file name
	 */
	public String getCurrentDetectorFileName() {
		String dataDirGDA = LocalProperties.get("gda.data.directoryMappingGDA");
		String dataDirDet = LocalProperties.get("gda.data.directoryMappingDetector");

		String currentFullFileNameDec = currentFileName.replace(dataDirGDA, dataDirDet);

		return currentFullFileNameDec;
	}
}