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

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFileAppenderForSrs;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.detectorfilemonitor.HighestExistingFileMonitor;
import gda.device.detectorfilemonitor.HighestExitingFileMonitorSettings;
import gda.jython.IJythonNamespace;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write each image to a separate file.
 *
 * Note that if using an NDFile with no PluginBase (one integrated into the camserver) then this class will not attempt to
 * enable or disable callbacks and blocking callbacks.
 *
 * Constructors:
 *
 *   SingleImagePerFileWriter();
 *   SingleImagePerFileWriter("detectorName");
 */
public class SingleImagePerFileWriter extends FileWriterBase implements NXPlugin{

	protected static final String FILEPATH_EXTRANAME = "filepath";
	protected static final String DEFAULT_FILEWRITERNAME = "tifwriter";

	private static Logger logger = LoggerFactory.getLogger(SingleImagePerFileWriter.class);

	private String fileNameUsed = "";
	private String filePathUsed = "";
	private String fileTemplateUsed = "";
	private String fileWriterName = DEFAULT_FILEWRITERNAME;
	private boolean firstReadoutInScan=true;

	private long nextExpectedFileNumber = 0;
	boolean blocking = true;

	private boolean returnPathRelativeToDatadir = false; // TODO: should really be enabled by default RobW
	private boolean fullFileNameFromRBV = false;

	private int SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES = 10;

	private int MILLI_SECONDS_BETWEEN_POLLS = 500;
	/*
	 * Object that can be used observe the progress of the scan by looking for file - optional
	 */
	HighestExistingFileMonitor highestExistingFileMonitor = null;

	private boolean waitForFileArrival = true;

	private boolean waitForFileArrivalInCompleteCollection = false;

	private String keyNameForMetadataPathTemplate = "";

	private FileWriteMode fileWriteMode = FileWriteMode.SINGLE;

	private boolean filePathInaccessibleFromServer = false;

	private String lastExpectedFullFilepath = null;

	@Override
	public String getName() {
		return fileWriterName;
	}

	public void setName(String fileWriterName) {
		this.fileWriterName = fileWriterName;
	}

	public String getFileWriteMode() {
		return fileWriteMode.toString();
	}

	public void setFileWriteMode(String fileWriteMode) {
		this.fileWriteMode = FileWriteMode.valueOf(fileWriteMode);
	}

	private String fileTemplateForReadout = null;
	private boolean alreadyPrepared=false; //use to allow the same fileWriter to be used in the same multiscan
	private Double xPixelSize=null;

	private Double yPixelSize=null;

	private String xPixelSizeUnit=null;

	private String yPixelSizeUnit=null;

	/**
	 * Creates a SingleImageFileWriter with ndFile, fileTemplate, filePathTemplate, fileNameTemplate and
	 * fileNumberAtScanStart yet to be set.
	 */
	public SingleImagePerFileWriter() {

	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public void setWaitForFileArrival(boolean waitForFileArrival) {
		this.waitForFileArrival = waitForFileArrival;
	}

	public boolean isWaitForFileArrival() {
		return waitForFileArrival;
	}

	public boolean isWaitForFileArrivalInCompleteCollection() {
		return waitForFileArrivalInCompleteCollection;
	}

	public void setWaitForFileArrivalInCompleteCollection(boolean waitForFileArrivalInCompleteCollection) {
		this.waitForFileArrivalInCompleteCollection = waitForFileArrivalInCompleteCollection;
	}

	/**
	 * Creates a SingleImageFileWriter which writes folders of files alongside the current file in the 'standard'
	 * location (ndFile must still be configured). e.g. <blockquote>
	 *
	 * <pre>
	 * datadir
	 *    123.dat
	 *    123-pilatus100k-files
	 * 00001.tif
	 */
	public SingleImagePerFileWriter(String detectorName) {
		setFileTemplate("%s%s%05d.tif");
		setFilePathTemplate("$datadir$/$scan$-" + detectorName + "-files");
		setFileNameTemplate("");
		setFileNumberAtScanStart(1);
	}

	public void setFileTemplateForReadout(String fileTemplateForReadout) {
		this.fileTemplateForReadout = fileTemplateForReadout;
	}


	public void setHighestExistingFileMonitor(HighestExistingFileMonitor highestExistingFileMonitor) {
		this.highestExistingFileMonitor = highestExistingFileMonitor;
	}

	public void setKeyNameForMetadataPathTemplate(String string) {
		this.keyNameForMetadataPathTemplate = string;
	}

	public String getFileTemplateForReadout() {
		return this.fileTemplateForReadout;
	}


	public HighestExistingFileMonitor getHighestExistingFileMonitor() {
		return highestExistingFileMonitor;
	}

	public String getkeyNameForMetadataPathTemplate() {
		return keyNameForMetadataPathTemplate;
	}

	@Override
	public boolean appendsFilepathStrings() {
		return isEnabled(); // will always append strings when enabled
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("prepareForCollection({}, {})", numberImagesPerCollection, scanInfo);

		if (!isEnabled())
			return;
		if( alreadyPrepared)
			return;
		// Create filePath directory if required

		if (!isFilePathInaccessibleFromServer()) {
			File f = new File(getFilePath());
			if (!f.exists()) {
				if (!f.mkdirs())
					throw new Exception("Folder does not exist and cannot be made:" + getFilePath());
			}
		}

		if (isSetFileNameAndNumber()) {
			configureNdFile();
		} else {
			if( !getNdFile().filePathExists())
				if (isPathErrorSuppressed())
					logger.warn("Ignoring Path does not exist on IOC");
				else
					throw new Exception("Path does not exist on IOC");
		}
		clearWriteStatusErr();

		setNDArrayPortAndAddress();
		NDPluginBase pluginBase = getNdFile().getPluginBase();
		if (pluginBase != null) {
			pluginBase.enableCallbacks();
			if (blocking) {
				logger.warn("Detector will block the AreaDetectors acquisition thread while writing files");
			}
			pluginBase.setBlockingCallbacks((short)(blocking? 1:0));
			// It should be possible to avoid blocking the acquisition thread
			// and use the pipeline by setting BlockingCallbacks according to
			// returnExpectedFileName, but when this was tried, at r48170, it
			// caused the files to be corrupted.
		} else {
			logger.warn("Cannot ensure callbacks and blocking callbacks are enabled as pluginBase is not set");
		}

		getNdFile().setFileWriteMode(fileWriteMode);
		if (fileWriteMode == FileWriteMode.CAPTURE || fileWriteMode == FileWriteMode.STREAM) {
			getNdFile().setNumCapture(1);
		}
		if (!getkeyNameForMetadataPathTemplate().isEmpty()) {
			addPathTemplateToMetadata();
		}
		firstReadoutInScan = true;
		alreadyPrepared=true;
		logger.trace("...prepareForCollection()");
	}

	private void addPathTemplateToMetadata() {
		IJythonNamespace jythonNamespace = InterfaceProvider.getJythonNamespace();
		String existingMetadataString = (String) jythonNamespace.getFromJythonNamespace("SRSWriteAtFileCreation");
		String newMetadataString;
		if (existingMetadataString == null) {
			newMetadataString = "";
		} else {
			newMetadataString = existingMetadataString;
		}

		String fileDirRelativeToDataDirIfPossible = getFileDirRelativeToDataDirIfPossible();
		String template = (getFileTemplateForReadout() == null) ? getFileTemplate() : getFileTemplateForReadout();
		String newValue = StringUtils.replaceOnce(template, "%s", fileDirRelativeToDataDirIfPossible + "/");
		newValue = StringUtils.replaceOnce(newValue, "%s", getFileName());
		String newKey = getkeyNameForMetadataPathTemplate();
		jythonNamespace.placeInJythonNamespace("SRSWriteAtFileCreation", newMetadataString + newKey + "='" + newValue
				+ "'\n");
		InterfaceProvider.getTerminalPrinter().print("Image location: " + newKey + "='" + newValue);
	}

	protected void configureNdFile() throws Exception {

		fileTemplateUsed = getFileTemplate();
		getNdFile().setFileTemplate(fileTemplateUsed);

		filePathUsed = getFilePath();
		if (!filePathUsed.endsWith(File.separator))
			filePathUsed += File.separator;
		File f = new File(filePathUsed);
		if (!filePathInaccessibleFromServer) {
			if (!f.exists()) {
				if (!f.mkdirs())
					throw new Exception("Folder does not exist and cannot be made:" + filePathUsed);
			}
		}
		getNdFile().setFilePath(filePathUsed);

		if (!getNdFile().filePathExists())
			if (isPathErrorSuppressed())
				logger.warn("Ignoring Path does not exist on IOC '" + filePathUsed + "'");
			else
				throw new Exception("Path does not exist on IOC '" + filePathUsed + "'");

		fileNameUsed = getFileName();
		getNdFile().setFileName(fileNameUsed);

		if (getFileNumberAtScanStart() >= 0) {
			getNdFile().setFileNumber((int) getFileNumberAtScanStart());
			nextExpectedFileNumber = getFileNumberAtScanStart();
		} else {
			nextExpectedFileNumber = getNdFile().getFileNumber();
		}

		getNdFile().setAutoIncrement((short) 1);

		getNdFile().setAutoSave((short) 1);

		if (highestExistingFileMonitor != null) {
			// remove the 2 %s from the fileTemplate to get to part after fileNameUsed
			String postFileName = fileTemplateUsed.replaceFirst("%s", "").replaceFirst("%s", "");
			HighestExitingFileMonitorSettings highestExitingFileMonitorSettings = new HighestExitingFileMonitorSettings(
					filePathUsed, fileNameUsed + postFileName, (int) nextExpectedFileNumber);
			highestExistingFileMonitor.setHighestExitingFileMonitorSettings(highestExitingFileMonitorSettings);
			highestExistingFileMonitor.setRunning(true);
		}

	}

	@Override
	public void completeCollection() throws Exception {
		logger.trace("completeCollection()");

		alreadyPrepared=false;
		if (!isEnabled())
			return;

		if (isWaitForFileArrivalInCompleteCollection() && (lastExpectedFullFilepath != null)) {
			checkErrorStatus();
			waitForFile(lastExpectedFullFilepath);
		}
		disableFileWriting();

		logger.trace("...completeCollection()");
	}

	@Override
	public void stop() throws Exception {
		alreadyPrepared=false;
		super.stop();
	}

	@Override
	public void atCommandFailure() throws Exception {
		alreadyPrepared=false;
		super.atCommandFailure();
	}

	@Override
	public void disableFileWriting() throws Exception {
		NDPluginBase filePluginBase = getNdFile().getPluginBase();
		if (filePluginBase != null) { // camserver filewriter has no base
			filePluginBase.disableCallbacks();
			filePluginBase.setBlockingCallbacks((short) 0);
		}
		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
	}

	@Override
	public String getFullFileName() throws Exception {
		String template = (getFileTemplateForReadout() != null) ? getFileTemplateForReadout() : fileTemplateUsed;
		String fullFileName = (isFullFileNameFromRBV() ? this.getNdFile().getFullFileName_RBV()
			: String.format(template, filePathUsed, fileNameUsed, nextExpectedFileNumber) );
		nextExpectedFileNumber++;
		return fullFileName;
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList((fileWriterName == DEFAULT_FILEWRITERNAME ? "" : fileWriterName+".") + FILEPATH_EXTRANAME);
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%.2f");
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		ArrayList<NXDetectorDataAppender> l = new ArrayList<NXDetectorDataAppender>();
		l.add(readNXDetectorDataAppender());
		return l;
	}

	/**
	 * Returns a single NXDetectorDataAppender for the current image with each call. If isWaitForFileArrival is true,
	 * then waits for the file to become visible before returning the appender.
	 */
	protected NXDetectorDataAppender readNXDetectorDataAppender()  throws NoSuchElementException, DeviceException{

		String filepath;
		boolean returnPathIsRelative = isReturnPathRelativeToDatadir();
		try {
			if (returnPathIsRelative) {
				if (!StringUtils.startsWith(getFilePathTemplate(), "$datadir$")) {
					throw new IllegalStateException(
							"If configured to return a path relative to the datadir, the configured filePathTemplate must begin wiht $datadir$. It is :'"
									+ getFilePathTemplate() + "'");
				}
				filepath = getRelativeFilePath();
			} else {
				filepath = getFullFileName();
			}
		} catch (Exception e) {
			throw new DeviceException(e);
		}
		lastExpectedFullFilepath = returnPathIsRelative ? getAbsoluteFilePath(filepath) : filepath;
		checkErrorStatus();
		if( isWaitForFileArrival()){
			// Now check that the file exists
			waitForFile(lastExpectedFullFilepath);
		}
		NXDetectorDataFileAppenderForSrs nxDetectorDataFileAppenderForSrs;
		if (firstReadoutInScan) {
			nxDetectorDataFileAppenderForSrs = new NXDetectorDataFileAppenderForSrs(filepath, getInputStreamNames().get(0), getxPixelSize(), getyPixelSize(), getxPixelSizeUnit(), getyPixelSizeUnit());
			firstReadoutInScan = false;
		} else {
			nxDetectorDataFileAppenderForSrs = new NXDetectorDataFileAppenderForSrs(filepath, getInputStreamNames().get(0));
		}


		// Multiple filewriters require different file writer names and extra names
		return nxDetectorDataFileAppenderForSrs;

	}

	private void waitForFile(String fullFilePath) throws DeviceException {
		try {
			File f = new File(fullFilePath);
			long numChecks = 0;
			//TODO must here have timeout in case the file system gone down?
			while (!f.exists()) {
				numChecks++;
				try {
					Thread.sleep(MILLI_SECONDS_BETWEEN_POLLS);
				} catch (InterruptedException e) {
					throw new InterruptedException("ScanBase is interrupted whilst waiting for '" + fullFilePath + "'");
				}
				checkErrorStatus();
				if ((numChecks * MILLI_SECONDS_BETWEEN_POLLS/1000) > SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES) {
					InterfaceProvider.getTerminalPrinter().print(
							"Waiting for file '" + fullFilePath + "' to be created");
					numChecks = 0;
				}
			}
		} catch (Exception e) {
			throw new DeviceException("Error checking for existence of file '" + fullFilePath + "'",e);
		}

	}

	public boolean isReturnPathRelativeToDatadir() {
		return returnPathRelativeToDatadir;
	}

	public void setReturnPathRelativeToDatadir(boolean returnPathRelativeToDatadir) {
		this.returnPathRelativeToDatadir = returnPathRelativeToDatadir;
	}

	public boolean isFullFileNameFromRBV() {
		return fullFileNameFromRBV;
	}

	/**
	 * Set this filewriter to get the full filename from the Epics readback. This is needed for detectors which add a different
	 * extension depending on their mode of operation, such as the Mar Area Detector, which adds .mar3450 or .mar2300 etc.
	 *
	 * Note, when this option is in use, only the name of the current image can be returned, the names of future images cannot
	 * be inferred, so this option is incompatible with the continuous scan mechanism.
	 *
	 * @param fullFileNameFromRBV defaults to false.
	 */
	public void setFullFileNameFromRBV(boolean fullFileNameFromRBV) {
		if (fullFileNameFromRBV)
			logger.warn("Getting full Filename from Epics RBV value: This will not work for continuous scanning.");
		this.fullFileNameFromRBV = fullFileNameFromRBV;
	}

	public boolean isFilePathInaccessibleFromServer() {
		return filePathInaccessibleFromServer;
	}

	public void setFilePathInaccessibleFromServer(boolean filePathNotVisibleFromServer) {
		this.filePathInaccessibleFromServer = filePathNotVisibleFromServer;
	}
	public Double getyPixelSize() {
		return yPixelSize;
	}

	public void setyPixelSize(Double yPixelSize) {
		this.yPixelSize = yPixelSize;
	}

	public Double getxPixelSize() {
		return xPixelSize;
	}

	public void setxPixelSize(Double xPixelSize) {
		this.xPixelSize = xPixelSize;
	}

	public String getxPixelSizeUnit() {
		return xPixelSizeUnit;
	}

	public void setxPixelSizeUnit(String xPixelSizeUnit) {
		this.xPixelSizeUnit = xPixelSizeUnit;
	}

	public void setyPixelSizeUnit(String yPixelSizeUnit) {
		this.yPixelSizeUnit=yPixelSizeUnit;

	}

	public String getyPixelSizeUnit() {
		return yPixelSizeUnit;
	}

}
