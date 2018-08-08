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

package gda.device.detector.uviewnew;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.detector.uviewnew.UViewController.ImageFile.ImageFormat;

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
	private ImageFormat format = ImageFormat.PNG;

	// file prefix to use (if any)
	private String filePrefix = "uviewImage_";

	private String newDataDir = null;

	private String currentFileName = null;

	private NumTracker runs = null;

	public UViewFile(String filePrefix, ImageFormat format) throws IOException {
		this.filePrefix = filePrefix;
		this.format = format;

		this.setupScanDir();
	}
	
	public void setFileExtenstion(ImageFormat format){
		this.format = format;
	}

	/**
	 * Setup a new image directory based on the tracker provided
	 * @throws IOException 
	 */
	public void newImageDir(String tracker, String imageDir) throws IOException{
		runs = new NumTracker(tracker);

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
	 * @throws IOException 
	 */
	public void setupScanDir() throws IOException {
		runs = new NumTracker("tmp");

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
		String fileExtension = "png";
		switch (this.format) {
		case PNG:
			fileExtension = "png";
			break;
		case BMP:
			fileExtension = "bmp";
			break;
		case DAT:
			fileExtension = "dat";
			break;
		case JPG:
			fileExtension = "jpg";
			break;
		case TIFF:
			fileExtension = "tif";
			break;
		case TIFF_UNCOMPRESSED:
			fileExtension = "tif";
			break;
		}

		currentFileName = newDataDir + File.separator + this.filePrefix + String.format("%08d", thisFileNumber) + "."
				+ fileExtension;
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