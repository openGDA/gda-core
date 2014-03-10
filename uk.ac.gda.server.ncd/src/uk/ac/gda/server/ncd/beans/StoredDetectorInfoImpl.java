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

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.data.metadata.MetadataBlaster;
import gda.observable.IObserver;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoredDetectorInfoImpl implements StoredDetectorInfo, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(StoredDetectorInfoImpl.class);
	private String name;
	private File saxsDetectorInfo;
	private MetadataBlaster currentDataDirectoryUpdater;
	private static final String DETECTOR_MASK_STORAGE = LocalProperties.getVarDir() + "detectorInfoFileLocation";
	
	public StoredDetectorInfoImpl() {
		if (new File(DETECTOR_MASK_STORAGE).exists()) {
			loadDetectorMaskFile();
		} else {
			saxsDetectorInfo = new File("");
		}
	}

	private void loadDetectorMaskFile() {
		logger.debug("Loading detector mask location from {}", DETECTOR_MASK_STORAGE);
		FileInputStream fileIn = null;
		ObjectInputStream objIn = null;
		try {
			fileIn = new FileInputStream(DETECTOR_MASK_STORAGE);
			objIn = new ObjectInputStream(fileIn);
			setSaxsDetectorInfoPath((String)objIn.readObject());
			logger.debug("Found detector mask location file. Detector mask location: {}", saxsDetectorInfo.getPath());
		} catch (FileNotFoundException e) {
			logger.info("No mask location stored, defaulting to base data directory");
		} catch (ClassNotFoundException e) {
			logger.error("Could not read stored mask location", e);
		} catch (IOException e) {
			logger.error("Could not read stored mask location", e);
		}
		finally {
			try {
				if (fileIn != null) fileIn.close();
				if (objIn != null) objIn.close();
			} catch (IOException e) {
				logger.error("Could not close loadDetectorMaskFile fileStreams", e);
			}
		}
	}

	private void storeDetectorMaskFile() {
		logger.info("Saving detector mask location to {}", DETECTOR_MASK_STORAGE);
		FileOutputStream fileOut = null;
		ObjectOutputStream objOut = null;
		try {
			fileOut = new FileOutputStream(DETECTOR_MASK_STORAGE);
			objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(saxsDetectorInfo.getPath());
		} catch (FileNotFoundException e) {
			logger.error("Could not save detector mask location to {}", DETECTOR_MASK_STORAGE, e);
		} catch (IOException e) {
			logger.error("Could not save detector mask location", e);
		} finally {
			try {
				if (objOut != null) objOut.close();
				if (fileOut != null) fileOut.close();
			} catch (IOException e) {
				logger.error("could not close storeDetectorMaskFile fileStreams", e);
			}
		}
	}

	@Override
	public void setSaxsDetectorInfoPath(String filePath) {
		saxsDetectorInfo = new File(filePath);
		storeDetectorMaskFile();
	}
	
	@Override
	public String getSaxsDetectorInfoPath() {
		return saxsDetectorInfo.getPath();
	}

	public void setCurrentDataDirectoryUpdater(MetadataBlaster currentDataDirectoryUpdater) {
		if (this.currentDataDirectoryUpdater != null) {
			this.currentDataDirectoryUpdater.deleteIObserver(this);
		}
		this.currentDataDirectoryUpdater = currentDataDirectoryUpdater;
		this.currentDataDirectoryUpdater.addIObserver(this);
	}

	@Override
	public void update(Object source, Object arg) {
		if (detectorInfoFileExists()) {
			String newFileName = PathConstructor.createFromDefaultProperty() + "/" + saxsDetectorInfo.getName();
			logger.info("Subdirectory changed, copying {} to {}", saxsDetectorInfo.getName(), arg);
			copyMaskToNewFile(newFileName);
			setSaxsDetectorInfoPath(newFileName);
		} else {
			logger.debug("Subdirectory changed but there is no mask to copy");
		}
	}
	
	private boolean detectorInfoFileExists() {
		return saxsDetectorInfo.exists();
	}
	
	private void copyMaskToNewFile(String newFileName) {
		File newFile = new File(newFileName);
		try {
			FileUtils.copyFile(saxsDetectorInfo, newFile);
		} catch (IOException e) {
			logger.error("Could not copy detectorMask to new folder", e);
		}
		
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
