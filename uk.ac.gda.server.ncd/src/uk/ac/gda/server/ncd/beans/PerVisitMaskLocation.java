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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.BatonChanged;
import gda.observable.IObserver;
import uk.ac.gda.util.io.FileUtils;

public class PerVisitMaskLocation extends ConfigurableBase implements StoredDetectorInfo, IObserver {

	private static final String VISIT_DIRECTORY_PROPERTY = "gda.data.visitdirectory";

	private static final String FILE_LOCATION_STORE = "current-file-locations";
	private static final String DETECTOR_INFO_PREFIX = "detectorMask";
	private static final String DATA_REDUCTION_PREFIX = "dataReduction";

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private static final Logger logger = LoggerFactory.getLogger(PerVisitMaskLocation.class);

	private class FileHolder { //lets file locations be treated as mutable
		private File heldFile;
		public FileHolder() { heldFile = null; }
		public void setFile(File file) { heldFile = file; }
		public void setFile(String path) { heldFile = new File(path); }
		public File getFile() { return heldFile; }
		public String getPath() { return heldFile == null ? "" : heldFile.getAbsolutePath(); }
		public void clear() { heldFile = null; }
	}

	private String name;
	private File maskDirectory;
	private FileHolder detectorInfoFile = new FileHolder();
	private FileHolder dataReductionFile = new FileHolder();

	public PerVisitMaskLocation() {
	}

	@Override
	public void configure() throws FactoryException {
		try {
			maskDirectory = getMaskDirectory();
		} catch (IOException e) {
			throw new FactoryException("Could not find (or create) directory processing/masks in visit directory", e);
		}
		InterfaceProvider.getBatonStateProvider().addBatonChangedObserver(this);
		setConfigured(true);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	private void setNewFile(FileHolder fh, String path, String prefix) throws IOException {
		if (path == null || path.isEmpty()) {
			fh.setFile("");
		} else {
			File newFile = new File(path);
			if (newFile.exists()) {
				File target = new File(maskDirectory, getTimeStampedFilename(prefix, "h5"));
				try {
					FileUtils.copy(newFile, target);
					fh.setFile(target);
				} catch (IOException e) {
					logger.error("Could not copy {} to {}", newFile, target);
					throw e;
				}
			} else {
				throw new IllegalArgumentException("New file does not exist");
			}
		}
	}

	@Override
	public void setSaxsDetectorInfoPath(String filePath) throws IOException {
		logger.debug("Setting detectorInfo to {}", filePath);
		setNewFile(detectorInfoFile, filePath, DETECTOR_INFO_PREFIX);
		updateStoredFiles();
	}

	@Override
	public String getSaxsDetectorInfoPath() {
		return detectorInfoFile.getPath();
	}

	@Override
	public void setDataCalibrationReductionSetupPath(String filePath) throws IOException {
		logger.debug("Setting dataReduction to {}", filePath);
		setNewFile(dataReductionFile, filePath, DATA_REDUCTION_PREFIX);
		updateStoredFiles();
	}

	@Override
	public String getDataCalibrationReductionSetupPath() {
		return dataReductionFile.getPath();
	}

	@Override
	public void clearSaxsDetectorInfoPath() {
		detectorInfoFile.clear();
		updateStoredFiles();
		logger.info("detector info path cleared");
	}

	@Override
	public void clearDataCalibrationReductionSetupPath() {
		dataReductionFile.clear();
		updateStoredFiles();
		logger.info("data reduction path cleared");
	}

	private static File getMaskDirectory() throws IOException {
		File path = new File(String.format("%s/%s", PathConstructor.createFromProperty(VISIT_DIRECTORY_PROPERTY), "masks"));
		logger.debug("mask directory: {}", path);
		if (!path.exists()) {
			if (!path.mkdirs()) {
				throw new IOException("Could not create mask directory");
			}
		} else if (!path.canWrite()) {
			throw new IOException("No write permissions for mask directory: " + path.getAbsolutePath());
		}
		return path;
	}

	private void updateStoredFiles() {
		storeFileLocation(new File(maskDirectory, FILE_LOCATION_STORE), detectorInfoFile.getFile(), dataReductionFile.getFile());
	}

	private void storeFileLocation(File targetFilePath, File mask, File reduction) {
		FileOutputStream fileOut = null;
		ObjectOutputStream objOut = null;
		try {
			fileOut = new FileOutputStream(targetFilePath);
			objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(new File[] {mask, reduction});
		} catch (IOException e) {
			logger.error("Could not save files to {}", targetFilePath, e);
		} finally {
			try {
				if (objOut != null) objOut.close();
				if (fileOut != null) fileOut.close();
			} catch (IOException e) {
				logger.error("could not close fileStreams", e);
			}
		}
	}

	private void restoreFileLocations() {
		File fileLocation = new File(maskDirectory, FILE_LOCATION_STORE);
		if (fileLocation.exists()) {
			logger.debug("Loading file location from {}", fileLocation);
			FileInputStream fileIn = null;
			ObjectInputStream objIn = null;
			File[] found;
			try {
				fileIn = new FileInputStream(fileLocation);
				objIn = new ObjectInputStream(fileIn);
				found = (File[]) objIn.readObject();
				detectorInfoFile.setFile(found[0]);
				dataReductionFile.setFile(found[1]);
				logger.debug("Found file location record");
			} catch (FileNotFoundException e) {
				logger.info("No file location stored, defaulting to base data directory", e);
			} catch (IOException e) {
				logger.error("Could not read stored file location from {}", fileLocation, e);
			} catch (ClassNotFoundException e) {
				logger.error("Could not read stored file location from {}", fileLocation, e);
			} finally {
				try {
					if (fileIn != null) fileIn.close();
					if (objIn != null) objIn.close();
				} catch (IOException e) {
					logger.error("Could not close loadDetectorMaskFile fileStreams", e);
				}
			}
		} else {
			logger.debug("location file not found - resetting file locations");
			dataReductionFile.clear();
			detectorInfoFile.clear();
			clearDataCalibrationReductionSetupPath();
			clearSaxsDetectorInfoPath();
		}
	}

	private static String getTimeStampedFilename(String toStamp, String ext) {
		Date now = new Date();
		return String.format("%s-%s.%s", toStamp, DATE_FORMAT.format(now), ext);
	}

	@Override
	public void update(Object source, Object arg) {
		try {
			if ( arg instanceof BatonChanged) {
				try {
					maskDirectory = getMaskDirectory();
					restoreFileLocations();
				} catch (IOException e) {
					logger.error("Could not access mask directory, reset mask and data files to \"\"", e);
					detectorInfoFile.clear();
					dataReductionFile.clear();
				}
			}
		} catch (Exception e) {
			//catch everything to prevent JSF errors for new events
			logger.error("Failed to update mask locations on BatonChange", e);
		}
	}
}
