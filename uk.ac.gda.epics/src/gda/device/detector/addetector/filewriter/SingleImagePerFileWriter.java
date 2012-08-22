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
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFileAppenderForSrs;
import gda.device.detectorfilemonitor.HighestExistingFileMonitor;
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

	private long nextExpectedFileNumber = 0;
	
	/*
	 * Object that can be used observe the progress of the scan by looking for file - optional
	 */
	HighestExistingFileMonitor highestExistingFileMonitor = null;

	/**
	 * Creates a SingleImageFileWriter with ndFile, fileTemplate, filePathTemplate, fileNameTemplate and
	 * fileNumberAtScanStart yet to be set.
	 */
	public SingleImagePerFileWriter() {
	
	}

	/**
	 * Creates a SingleImageFileWriter which writes folders of files alongside the current file in the 'standard'
	 * location (ndFile must still be configured). e.g. <blockquote>
	 * 
	 * <pre>
	 * datadir
	 *    123.dat
	 *    123-pilatus100k-files
	 *       00001.tif
	 *
	 */
	public SingleImagePerFileWriter(String detectorName) {
		setFileTemplate("%s%s%5.5d.tif");
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

		if (!getEnable())
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

		getNdFile().setFileTemplate(getFileTemplate());

		getNdFile().setFilePath(getFilePath());

		getNdFile().setFileName(getFileName());

		if (getFileNumberAtScanStart() >= 0) {
			getNdFile().setFileNumber((int) getFileNumberAtScanStart());
			nextExpectedFileNumber = getFileNumberAtScanStart();
		} else {
			nextExpectedFileNumber = getNdFile().getFileNumber();
		}

		getNdFile().setAutoIncrement((short) 1);

		getNdFile().setAutoSave((short) 1);

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
	public String getFullFileName_RBV() throws Exception {
		if (returnExpectedFileName) {
			String fullFileName = String
					.format(getFileTemplate(), getFilePath(), getFileName(), nextExpectedFileNumber);
			nextExpectedFileNumber++;
			return fullFileName;
		}
		return super.getFullFileName_RBV();
	}

	@Override
	public List<String> getInputStreamExtraNames() {
		return Arrays.asList(FILEPATH_EXTRANAME);
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%.2f");
	}

	/**
	 * Returns a single NXDetectorDataAppender for the current image with each call. 
	 */
	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		
//		if( detectorData instanceof NXDetectorDataWithFilepathForSrs){
//			String filepath = ((NXDetectorDataWithFilepathForSrs)detectorData).getFilepath();
//			if( data.checkFileExists && !filepath.equals(ADDetector.NullFileWriter.DUMMY_FILE_WRITER_GET_FULL_FILE_NAME_RBV)){
//				File f = new File(filepath);
//				long numChecks=0;
//				while( !f.exists() ){
//					numChecks++;
//					Thread.sleep(1000);
//					ScanBase.checkForInterrupts();
//					if( numChecks> 10){
//						//Inform user every 10 seconds
//						InterfaceProvider.getTerminalPrinter().print("Waiting for file " + filepath + " to be created");
//						numChecks=0;
//					}
//				}
//			}
//		}
		
		
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
