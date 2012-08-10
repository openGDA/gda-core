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

package gda.device.detector.addetectorprovisional.filewriter;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.device.detector.addetectorprovisional.data.NXDetectorDataAppender;
import gda.device.detector.addetectorprovisional.data.NXDetectorDataFileAppenderForSrs;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * COPIED and modified version {@link gda.device.detector.addetector.filewriter.SingleImagePerFileWriter}.
 * A provisional version of SingleImagePerFileWriter with simpler and more versatile configuration settings.
 */
public class SingleImagePerFileWriter extends ProvisionalFileWriterBase {

	protected static final String FILEPATH_EXTRANAME = "filepath";
	
	private static Logger logger = LoggerFactory.getLogger(SingleImagePerFileWriter.class);

	private boolean returnExpectedFileName = false;

	private long nextExpectedFileNumber = 0;

	/**
	 * If true getFullFileName_RBV returns expected filename rather than value from ndfile plugin. Useful for example
	 * with continuous scans.
	 */
	public boolean isReturnExpectedFullFileName() {
		return returnExpectedFileName;
	}

	public void setReturnExpectedFullFileName(boolean returnExpectedFullFileName) {
		this.returnExpectedFileName = returnExpectedFullFileName;
	}

	/**
	 * Creates a SingleImageFileWriter which writes folders of files alongside the current file in the 'standard'
	 * location. e.g. <blockquote>
	 * 
	 * <pre>
	 * datadir
	 *    123.dat
	 *    123-pilatus100k-files
	 *       00001.cbf
	 * 
	 * @param ndFile
	 */
	public SingleImagePerFileWriter(NDFile ndFile, String detectorName) {
		// ndFile, fileTemplate, filePathTemplate, fileNameTemplate, fileNumberAtScanStart, setFileNameAndNumber
		super(ndFile, "%s%s%5.5d.jpg", "$datadir$/$scan$-" + detectorName + "-files", "", 1);
	}

	public SingleImagePerFileWriter(NDFile ndFile, String fileTemplate, String filePathTemplate,
			String fileNameTemplate, long fileNumberAtScanStart) {
		super(ndFile, fileTemplate, filePathTemplate, fileNameTemplate, fileNumberAtScanStart);
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
