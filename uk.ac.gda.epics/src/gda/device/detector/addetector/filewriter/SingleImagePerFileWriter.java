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

import static java.text.MessageFormat.format;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFileAppenderForSrs;
import gda.device.detectorfilemonitor.HighestExistingFileMonitor;
import gda.device.detectorfilemonitor.HighestExitingFileMonitorSettings;
import gda.jython.InterfaceProvider;
import gda.scan.ScanBase;

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

	private static Logger logger = LoggerFactory.getLogger(SingleImagePerFileWriter.class);

	private boolean returnExpectedFileName = true;
	private String fileNameUsed="";
	private String filePathUsed="";
	private String fileTemplateUsed="";
	private long nextExpectedFileNumber = 0;

	private int SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES = 10;

	private int MILLI_SECONDS_BETWEEN_POLLS = 500;
	/*
	 * Object that can be used observe the progress of the scan by looking for file - optional
	 */
	HighestExistingFileMonitor highestExistingFileMonitor = null;

	private boolean waitForFileArrival = true;

	/**
	 * Creates a SingleImageFileWriter with ndFile, fileTemplate, filePathTemplate, fileNameTemplate and
	 * fileNumberAtScanStart yet to be set.
	 */
	public SingleImagePerFileWriter() {

	}

	public void setWaitForFileArrival(boolean waitForFileArrival) {
		this.waitForFileArrival = waitForFileArrival;
	}

	public boolean isWaitForFileArrival() {
		return waitForFileArrival;
	}

	/**
	 * Creates a SingleImageFileWriter which writes folders of files alongside the current file in the 'standard'
	 * location (ndFile must still be configured). e.g. <blockquote>
	 * 
	 * <pre>
	 * datadir
	 *    123.dat
	 *    123-pilatus100k-files
	 * 		00001.tif
	 */
	public SingleImagePerFileWriter(String detectorName) {
		setFileTemplate("%s%s%05d.tif");
		setFilePathTemplate("$datadir$/$scan$-" + detectorName + "-files");
		setFileNameTemplate("");
		setFileNumberAtScanStart(1);
	}

	public void setReturnExpectedFullFileName(boolean returnExpectedFullFileName) {
		this.returnExpectedFileName = returnExpectedFullFileName;
	}

	public void setHighestExistingFileMonitor(HighestExistingFileMonitor highestExistingFileMonitor) {
		this.highestExistingFileMonitor = highestExistingFileMonitor;
	}

	/**
	 * If true getFullFileName_RBV returns expected filename rather than value from ndfile plugin. Useful for example
	 * with continuous scans.
	 */
	public boolean isReturnExpectedFullFileName() {
		return returnExpectedFileName;
	}

	public HighestExistingFileMonitor getHighestExistingFileMonitor() {
		return highestExistingFileMonitor;
	}

	@Override
	public boolean isLinkFilepath() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection) throws Exception {

		if (!isEnabled())
			return;

		// Create filePath directory if required
		File f = new File(getFilePath());
		if (!f.exists()) {
			if (!f.mkdirs())
				throw new Exception("Folder does not exist and cannot be made:" + getFilePath());
		}

		if (isSetFileNameAndNumber()) {
			configureNdFile();
		}

		setNDArrayPortAndAddress();
		getNdFile().getPluginBase().enableCallbacks();
		logger.warn("Detector will block the AreaDetectors acquisition thread while writing files");
		getNdFile().getPluginBase().setBlockingCallbacks((short) (returnExpectedFileName ? 1 : 1)); // always block

		getNdFile().setFileWriteMode(FileWriteMode.SINGLE);
	}

	protected void configureNdFile() throws Exception {

		fileTemplateUsed = getFileTemplate();
		getNdFile().setFileTemplate(fileTemplateUsed);

		filePathUsed = getFilePath();
		if(!filePathUsed.endsWith(File.separator))
			filePathUsed += File.separator;
		File f = new File(filePathUsed);
		if (!f.exists()){
			if(!f.mkdirs())
				throw new Exception("Folder does not exist and cannot be made:" + filePathUsed);
		}		
		getNdFile().setFilePath(filePathUsed);

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

		if( highestExistingFileMonitor != null){
			//remove the 2 %s from the fileTemplate to get to part after fileNameUsed
			String postFileName=fileTemplateUsed.replaceFirst("%s", "").replaceFirst("%s", "");
			HighestExitingFileMonitorSettings highestExitingFileMonitorSettings = 
					new HighestExitingFileMonitorSettings(filePathUsed,fileNameUsed+postFileName, (int) nextExpectedFileNumber);
			highestExistingFileMonitor.setHighestExitingFileMonitorSettings(highestExitingFileMonitorSettings);
			highestExistingFileMonitor.setRunning(true);
		}		
		
	}

	@Override
	public void completeCollection() throws Exception {
		if (!isEnabled())
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
	public String getFullFileName() throws Exception {
		if (returnExpectedFileName) {
			String fullFileName = String.format( fileTemplateUsed,filePathUsed,fileNameUsed, nextExpectedFileNumber);			
			nextExpectedFileNumber++;
			return fullFileName;
		}
		return super.getFullFileName();
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList(FILEPATH_EXTRANAME);
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%.2f");
	}

	/**
	 * Returns a single NXDetectorDataAppender for the current image with each call. If isWaitForFileArrival is true,
	 * then waits for the file to become visible before returning the appender.
	 */
	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {

		String filepath;
		try {
			filepath = getFullFileName();
		} catch (Exception e) {
			throw new DeviceException(e);
		}

		if (isWaitForFileArrival()) {
			long numChecksSinceLastMessage = 0;
			long totalNumChecks = 0;
			File f = new File(filepath);
			while (!f.exists()) {
				numChecksSinceLastMessage++;
				totalNumChecks++;
				Thread.sleep(MILLI_SECONDS_BETWEEN_POLLS);
				ScanBase.checkForInterrupts();
				int numPollsPerMessage = SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES / MILLI_SECONDS_BETWEEN_POLLS;
				if (numChecksSinceLastMessage >= numPollsPerMessage) {
					double totalSecondsPolling = totalNumChecks * MILLI_SECONDS_BETWEEN_POLLS / 1000.;
					InterfaceProvider.getTerminalPrinter().print(
							format("Waited {0}s for file '{0}' to be created.", filepath, totalSecondsPolling));
					numChecksSinceLastMessage = 0;
				}
			}
		}

		NXDetectorDataAppender dataAppender;
		dataAppender = new NXDetectorDataFileAppenderForSrs(filepath, FILEPATH_EXTRANAME);

		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(dataAppender);
		return appenders;
	}

}
