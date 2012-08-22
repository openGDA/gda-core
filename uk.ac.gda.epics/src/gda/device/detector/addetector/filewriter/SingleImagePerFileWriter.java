/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.filewriter;

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.addetectorprovisional.data.NXDetectorDataAppender;
import gda.device.detector.addetectorprovisional.data.NXDetectorDataFileAppenderForSrs;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detectorfilemonitor.HighestExistingFileMonitor;
import gda.device.detectorfilemonitor.HighestExitingFileMonitorSettings;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * SingleImagePerFileWriter(ndFileSimulator, "detectorName", "%%s%d%%s-%%d-detname.tif",true,true);
 */
public class SingleImagePerFileWriter extends FileWriterBase {
	
	protected static final String FILEPATH_EXTRANAME = "filepath";

	private static Logger logger = LoggerFactory.getLogger(ADDetector.class);

	private final String folderTemplate;

	boolean templatesRequireScanNumber = true;

	/*
	 * Object that can be used observe the progress of the scan by looking for file - optional
	 */
	HighestExistingFileMonitor highestExistingFileMonitor = null; // TODO: Not used (yet?)
	
	private boolean returnExpectedFileName = false;


	// non configuration state
	
	private String fileNameUsed = "";
	
	private String filePathUsed = "";
	
	private int nextFileNumber = 0;
	
	private String fileTemplateForScan = ""; // TODO, Why do we need this?



	/**
	 * @param ndFile
	 * @param fileName
	 * @param fileTemplate
	 *            - template combined with scanNumber e.g. "%%s%d%%s-%%d-detname.tif" -
	 * @param setFileNameAndNumber
	 */
	public SingleImagePerFileWriter(NDFile ndFile, String fileName, String fileTemplate, String folderTemplate,
			boolean setFileNumberToZero, boolean setFileNameAndNumber) {
		super(ndFile, fileName, fileTemplate, setFileNumberToZero, setFileNameAndNumber);
		this.folderTemplate = folderTemplate;
	}

	public boolean isTemplatesRequireScanNumber() {
		return templatesRequireScanNumber;
	}

	public void setTemplatesRequireScanNumber(boolean templatesRequireScanNumber) {
		this.templatesRequireScanNumber = templatesRequireScanNumber;
	}

	public HighestExistingFileMonitor getHighestExistingFileMonitor() {
		return highestExistingFileMonitor;
	}

	public void setHighestExistingFileMonitor(HighestExistingFileMonitor highestExistingFileMonitor) {
		this.highestExistingFileMonitor = highestExistingFileMonitor;
	}


	/**
	 * If true getFullFileName_RBV returns expected filename rather than value from ndfile plugin
	 */
	public boolean isReturnExpectedFullFileName() {
		return returnExpectedFileName;
	}

	public void setReturnExpectedFullFileName(boolean returnExpectedFullFileName) {
		this.returnExpectedFileName = returnExpectedFullFileName;
	}

	@Override
	public String getFullFileName_RBV() throws Exception {
		if (returnExpectedFileName) {
			String fullFileName = String.format(fileTemplateForScan, filePathUsed, fileNameUsed, nextFileNumber);
			// each call increments the number
			nextFileNumber++;
			return fullFileName;
		}
		return super.getFullFileName_RBV();
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection) throws Exception {
		if (!getEnable())
			return;
		if (isSetFileNameAndNumber()) {
			setupFilename();
		}
		setNDArrayPortAndAddress();
		getNdFile().getPluginBase().enableCallbacks();
		logger.warn("Detector will block the AreaDetectors acquisition thread while writing files");
		getNdFile().getPluginBase().setBlockingCallbacks((short) (returnExpectedFileName ? 1 : 1)); // always block
		getNdFile().setFileWriteMode(FileWriteMode.SINGLE);
	}

	private void setupFilename() throws Exception {
		String fileTemplate = getFileTemplate();
		if (isTemplatesRequireScanNumber()) {
			fileTemplate = String.format(fileTemplate, getScanNumber());
		}
		fileTemplateForScan = fileTemplate;
		getNdFile().setFileTemplate(fileTemplate);
		String fileName = getFileName();
		getNdFile().setFileName(fileName);
		fileNameUsed = fileName;

		if (isSetFileNumberToZero()) {
			getNdFile().setFileNumber(0);
			nextFileNumber = 0;
		} else {
			nextFileNumber = getNdFile().getFileNumber();
		}

		// Check to see if the data directory has been defined.
		String dataDir = PathConstructor.createFromDefaultProperty();
		if (folderTemplate != null) {
			if (isTemplatesRequireScanNumber()) {
				dataDir = String.format(folderTemplate, dataDir, getScanNumber());
			} else {
				dataDir = String.format(folderTemplate, dataDir);
			}
		}
		File f = new File(dataDir);
		if (!f.exists()) {
			if (!f.mkdirs())
				throw new Exception("Folder does not exist and cannot be made:" + dataDir);
		}
		getNdFile().setFilePath(dataDir);
		filePathUsed = dataDir;

		getNdFile().setAutoSave((short) 1);
		getNdFile().setAutoIncrement((short) 1);

		if (highestExistingFileMonitor != null) {
			fileTemplate = fileTemplate.replaceFirst("%s", "");
			fileTemplate = fileTemplate.replaceFirst("%s", "");
			HighestExitingFileMonitorSettings highestExitingFileMonitorSettings = new HighestExitingFileMonitorSettings(
					dataDir, fileName + fileTemplate, nextFileNumber);
			highestExistingFileMonitor.setHighestExitingFileMonitorSettings(highestExitingFileMonitorSettings);
			highestExistingFileMonitor.setRunning(true);
		}

	}

	@Override
	public void completeCollection() throws Exception {
		if (!getEnable())
			return;
		disableFileWriter();
	}

	@Override
	public void disableFileWriter() throws Exception {
		getNdFile().getPluginBase().disableCallbacks();
		getNdFile().getPluginBase().setBlockingCallbacks((short) 0);
		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
	}

	@Override
	public boolean isLinkFilepath() {
		return false;
	}

	@Override
	public List<String> getInputStreamExtraNames() {
		return Arrays.asList(FILEPATH_EXTRANAME);
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%.2f");
	}

	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		NXDetectorDataAppender dataAppender;
		try {
			dataAppender = new NXDetectorDataFileAppenderForSrs(getFullFileName_RBV(), FILEPATH_EXTRANAME);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(dataAppender);
		return appenders;
	}
}
