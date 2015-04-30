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

package uk.ac.gda.server.ncd.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.data.metadata.MetadataBlaster;
import gda.observable.IObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.io.FileUtils;

public class StoredDetectorInfoImpl implements StoredDetectorInfo, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(StoredDetectorInfoImpl.class);
	private static final String DETECTOR_MASK_STORAGE = LocalProperties.getVarDir() + "detectorInfoFileLocation";
	private static final String REDUCTION_SETUP_STORAGE = LocalProperties.getVarDir() + "reductionSetupFileLocation";
	private String name;
	
	private MetadataBlaster currentDataDirectoryUpdater;

	private File saxsDetectorInfo;
	private File dataCalibrationReductionSetup;

	public StoredDetectorInfoImpl() {
		loadFileLocations();
	}
	
	public void setCurrentDataDirectoryUpdater(MetadataBlaster currentDataDirectoryUpdater) {
		if (this.currentDataDirectoryUpdater != null) {
			this.currentDataDirectoryUpdater.deleteIObserver(this);
		}
		this.currentDataDirectoryUpdater = currentDataDirectoryUpdater;
		this.currentDataDirectoryUpdater.addIObserver(this);
	}
	
	private void loadFileLocations() {
		saxsDetectorInfo = createFileFromLocation(DETECTOR_MASK_STORAGE);
		dataCalibrationReductionSetup = createFileFromLocation(REDUCTION_SETUP_STORAGE);
	}
	
	private File createFileFromLocation(String filePath) {
		logger.debug("Loading file location from {}", filePath);
		FileInputStream fileIn = null;
		ObjectInputStream objIn = null;
		String found = "";
		try {
			fileIn = new FileInputStream(filePath);
			objIn = new ObjectInputStream(fileIn);
			found = ((String)objIn.readObject());
			logger.debug("Found file location: {}", found);
		} catch (FileNotFoundException e) {
			logger.info("No file location stored, defaulting to base data directory");
		} catch (ClassNotFoundException e) {
			logger.error("Could not read stored file location from {}", filePath, e);
		} catch (IOException e) {
			logger.error("Could not read stored file location from {}", filePath, e);
		}
		finally {
			try {
				if (fileIn != null) fileIn.close();
				if (objIn != null) objIn.close();
			} catch (IOException e) {
				logger.error("Could not close loadDetectorMaskFile fileStreams", e);
			}
		}
		if (found == null || found.equals("")) {
			return null;
		}
		return new File(found);
	}

	@Override
	public void update(Object source, Object arg) {
		logger.debug("Subdirectory changed. New directory: {}", arg);
		updateFilePaths();
	}
	
	private void updateFilePaths() {
		updateSaxsDetectorInfo();
		updateDataCalibrationReductionSetup();
	}

	private void updateSaxsDetectorInfo() {
		if (saxsDetectorInfo.exists()) {
			logger.debug("Copying saxs Detector Info to new data directory");
			String newPath = getNewFilePath(saxsDetectorInfo);
			File newFile = new File(newPath);
			copyFile(saxsDetectorInfo, newFile);
			saxsDetectorInfo = newFile;
			storeFileLocation(saxsDetectorInfo, DETECTOR_MASK_STORAGE);
		} else {
			logger.debug("Detector info file does not exist");
			setSaxsDetectorInfoPath("");
		}
	}

	private void updateDataCalibrationReductionSetup() {
		if (dataCalibrationReductionSetup.exists()) {
			String newPath = getNewFilePath(dataCalibrationReductionSetup);
			File newFile = new File(newPath);
			copyFile(dataCalibrationReductionSetup, newFile);
			dataCalibrationReductionSetup = newFile;
			storeFileLocation(dataCalibrationReductionSetup, REDUCTION_SETUP_STORAGE);
		} else {
			logger.debug("Calibration and reduction info file does not exist");
			clearDataCalibrationReductionSetupPath();
		}
	}

	private String getNewFilePath(File currentFile) {
		return PathConstructor.createFromDefaultProperty() + "/" + currentFile.getName();
	}
	
	private void copyFile(File source, File target) {
		try {
			FileUtils.copy(source, target);
		} catch (IOException e) {
			logger.error("Could not copy {} to {}", source.getName(), target.getPath(), e);
		}
	}

	private void storeFileLocation(File file, String saveAsFile) {
		FileOutputStream fileOut = null;
		ObjectOutputStream objOut = null;
		String pathToSave = file == null ? "" : file.getPath();
		try {
			fileOut = new FileOutputStream(saveAsFile);
			objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(pathToSave);
		} catch (FileNotFoundException e) {
			logger.error("Could not save {} to {}", pathToSave, saveAsFile, e);
		} catch (IOException e) {
			logger.error("Could not save {} to {}", pathToSave, saveAsFile, e);
		} finally {
			try {
				if (objOut != null) objOut.close();
				if (fileOut != null) fileOut.close();
			} catch (IOException e) {
				logger.error("could not close fileStreams", e);
			}
		}
	}
	
	@Override
	public String getSaxsDetectorInfoPath() {
		return saxsDetectorInfo == null ? "" : saxsDetectorInfo.getPath();
	}

	@Override
	public void setSaxsDetectorInfoPath(String filePath) {
		File newFile = new File(filePath);
		if (newFile.exists()) {
			if (!(newFile.getParentFile().equals(new File(PathConstructor.createFromDefaultProperty())))) {
				File timeStamped = new File(PathConstructor.createFromDefaultProperty() + "/" + timeStamped("detectorMask") + ".h5");
				copyFile(newFile, timeStamped);
				this.saxsDetectorInfo = timeStamped;
			} else {
				this.saxsDetectorInfo = newFile;
			}
		} else {
			logger.error("new file does not exist");
			throw new IllegalArgumentException("New file does not exist");
		}
		logger.debug("saxsDetectorInfoPath is: {}", getSaxsDetectorInfoPath());
		storeFileLocation(saxsDetectorInfo, DETECTOR_MASK_STORAGE);
	}

	@Override
	public String getDataCalibrationReductionSetupPath() {
		return dataCalibrationReductionSetup == null ? "" : dataCalibrationReductionSetup.getPath();
	}

	@Override
	public void setDataCalibrationReductionSetupPath(String filePath) {
		File newFile = new File(filePath);
		if (newFile.exists()) {
			if (!(newFile.getParentFile().equals(new File(PathConstructor.createFromDefaultProperty())))) {
				File timeStamped = new File(PathConstructor.createFromDefaultProperty() + "/" + timeStamped("ReductionAndCalibration") + ".xml");
				copyFile(newFile, timeStamped);
				dataCalibrationReductionSetup = timeStamped;
			} else {
				dataCalibrationReductionSetup = newFile;
			}
		} else {
			logger.error("new file does not exist");
			throw new IllegalArgumentException("New file does not exist");
		}
		logger.debug("dataCalibrationReductionSetupPath is: {}", filePath);
		storeFileLocation(dataCalibrationReductionSetup, REDUCTION_SETUP_STORAGE);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	private String timeStamped(String toStamp) {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("-yyyyMMdd-HHmmss");
		return toStamp + sdf.format(now);
	}

	@Override
	public void clearSaxsDetectorInfoPath() {
		saxsDetectorInfo = null;
		storeFileLocation(null, DETECTOR_MASK_STORAGE);
	}

	@Override
	public void clearDataCalibrationReductionSetupPath() {
		dataCalibrationReductionSetup = null;
		storeFileLocation(null, REDUCTION_SETUP_STORAGE);
	}
}
